<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import ProductCard from '@/components/ProductCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { useFavoriteStore } from '@/store/favorite'

const router = useRouter()
const favoriteStore = useFavoriteStore()

const loading = ref(true)
const pageNum = ref(1)
const pageSize = 8

const records = computed(() => favoriteStore.records)
const total = computed(() => favoriteStore.total)
const totalAmount = computed(() => favoriteStore.totalAmount.toFixed(2))

async function load(page = pageNum.value) {
  loading.value = true
  try {
    const data = await favoriteStore.fetchList({ pageNum: page, pageSize })
    // 删除后当前页可能为空，自动回退到最后一页
    const lastPage = Math.max(1, Math.ceil(data.total / pageSize))
    if (page > lastPage) {
      pageNum.value = lastPage
      await favoriteStore.fetchList({ pageNum: lastPage, pageSize })
    } else {
      pageNum.value = page
    }
  } finally {
    loading.value = false
  }
}

function changePage(page) {
  load(page)
}

async function removeItem(item) {
  await ElMessageBox.confirm(`确认取消收藏「${item.product.title}」吗？`, '取消收藏', {
    confirmButtonText: '取消收藏',
    cancelButtonText: '再想想',
    type: 'warning',
  })
  await favoriteStore.remove(item.product_id)
  ElMessage.success('已取消收藏')
  await load()
}

onMounted(() => load(1))
</script>

<template>
  <div class="page favorite-page">
    <header class="page-head fade-up">
      <div>
        <h1>我的收藏</h1>
        <p>共收藏 {{ total }} 件商品，收藏总价约 ¥{{ totalAmount }}</p>
      </div>
      <button class="ghost-btn" type="button" @click="router.push({ name: 'products' })">去逛更多商品</button>
    </header>

    <SkeletonCard v-if="loading" :count="8" />

    <div v-else-if="records.length" class="product-grid fade-up">
      <div v-for="item in records" :key="item.id" class="wrap">
        <ProductCard :product="item.product" :show-favorite="false" />
        <button class="remove-btn" type="button" @click="removeItem(item)">
          <el-icon :size="13"><Delete /></el-icon>
          取消收藏
        </button>
      </div>
    </div>

    <EmptyState
      v-else
      title="还没有收藏任何商品"
      description="看到心仪的闲置先点个收藏，随时回来对比价格和成色"
      action-text="去首页逛逛"
      @action="router.push({ name: 'home' })"
    />

    <div v-if="!loading && total > pageSize" class="pager fade-up">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="changePage"
      />
    </div>
  </div>
</template>

<style scoped>
.favorite-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 4px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
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

.ghost-btn {
  padding: 9px 18px;
  border: 1px solid var(--c-primary);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: var(--c-primary-dark);
  cursor: pointer;
  transition: all 0.2s;
}

.ghost-btn:hover {
  background: var(--c-primary-mist);
}

.wrap {
  position: relative;
}

.remove-btn {
  width: 100%;
  margin-top: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 8px 0;
  border: 1px solid var(--c-border);
  border-radius: 10px;
  background: #fff;
  font-size: 12px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.remove-btn:hover {
  color: var(--c-danger);
  border-color: #fecaca;
  background: #fef2f2;
}

@media (max-width: 767.98px) {
  .page-head h1 {
    font-size: 19px;
  }
}
</style>
