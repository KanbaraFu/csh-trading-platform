<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import CategoryNav from '@/components/CategoryNav.vue'
import ProductCard from '@/components/ProductCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { categoryIcon } from '@/constants/message'
import { useProductStore } from '@/store/product'
import { getHotProducts } from '@/api/stat'

const router = useRouter()
const productStore = useProductStore()

const loading = ref(true)
const hotProducts = ref([])

onMounted(async () => {
  try {
    await productStore.fetchCategories()
    hotProducts.value = (await getHotProducts({ limit: 4 })).slice(0, 4)
  } finally {
    loading.value = false
  }
})

function goList(categoryId) {
  router.push({ name: 'products', query: categoryId ? { categoryId } : {} })
}
</script>

<template>
  <div class="page category-page">
    <header class="page-head fade-up">
      <div>
        <h1>全部分类</h1>
        <p>八大类目覆盖校园生活日常，点击分类直接进入对应商品列表</p>
      </div>
      <button class="all-btn" type="button" @click="goList(0)">
        浏览全部商品
        <el-icon :size="14"><Right /></el-icon>
      </button>
    </header>

    <section class="cat-panel fade-up">
      <div class="cat-grid">
        <button
          v-for="item in productStore.categories"
          :key="item.id"
          class="cat-card"
          type="button"
          @click="goList(item.id)"
        >
          <span class="cat-card__icon">
            <el-icon :size="26"><component :is="categoryIcon(item.name)" /></el-icon>
          </span>
          <div class="cat-card__body">
            <strong>{{ item.name }}</strong>
            <span>{{ item.product_count }} 件在售</span>
          </div>
          <el-icon class="cat-card__arrow" :size="16"><ArrowRight /></el-icon>
        </button>
      </div>
    </section>

    <section class="fade-up">
      <div class="section-title">
        <h2>分类热门速览</h2>
        <span class="more" @click="router.push({ name: 'rank' })">完整榜单 →</span>
      </div>
      <SkeletonCard v-if="loading" :count="4" />
      <div v-else class="product-grid">
        <ProductCard v-for="item in hotProducts" :key="item.id" :product="item" />
      </div>
    </section>
  </div>
</template>

<style scoped>
.category-page {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.page-head h1 {
  font-size: 24px;
  font-weight: 700;
}

.page-head p {
  margin-top: 6px;
  font-size: 13px;
  color: var(--c-text-muted);
}

.all-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  box-shadow: 0 10px 22px rgba(13, 148, 136, 0.24);
  transition: transform 0.2s;
}

.all-btn:hover {
  transform: translateY(-2px);
}

.cat-panel {
  padding: 22px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.cat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.cat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: all 0.22s cubic-bezier(0.22, 1, 0.36, 1);
}

.cat-card:hover {
  transform: translateY(-4px);
  border-color: var(--c-primary-light);
  box-shadow: var(--shadow-hover);
}

.cat-card__icon {
  width: 50px;
  height: 50px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 14px;
  color: var(--c-primary-dark);
  background: var(--c-primary-mist);
  transition: all 0.22s;
}

.cat-card:hover .cat-card__icon {
  color: #fff;
  background: linear-gradient(135deg, #2dd4bf, #0d9488);
}

.cat-card__body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.cat-card__body strong {
  font-size: 15px;
  color: var(--c-text);
}

.cat-card__body span {
  font-size: 12px;
  color: var(--c-text-muted);
}

.cat-card__arrow {
  margin-left: auto;
  color: var(--c-text-muted);
  transition: transform 0.22s, color 0.22s;
}

.cat-card:hover .cat-card__arrow {
  color: var(--c-primary);
  transform: translateX(3px);
}

@media (max-width: 1023.98px) {
  .cat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767.98px) {
  .cat-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .cat-card {
    padding: 14px 12px;
  }

  .cat-card__icon {
    width: 44px;
    height: 44px;
  }
}
</style>
