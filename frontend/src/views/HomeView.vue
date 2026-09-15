<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import CategoryNav from '@/components/CategoryNav.vue'
import ProductCard from '@/components/ProductCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { useProductStore } from '@/store/product'
import { useFavoriteStore } from '@/store/favorite'
import { useUserStore } from '@/store/user'
import { getHotWords, getOverview } from '@/api/stat'

const router = useRouter()
const productStore = useProductStore()
const favoriteStore = useFavoriteStore()
const userStore = useUserStore()

const loading = ref(true)
const hotProducts = ref([])
const newProducts = ref([])
const hotWords = ref([])
const overview = ref(null)
const keyword = ref('')

const sortTabs = [
  { label: '最新发布', value: 'new' },
  { label: '人气最高', value: 'hot' },
  { label: '价格最低', value: 'price_asc' },
]
const activeSort = ref('new')
const sortLoading = ref(false)

async function loadSort(sort) {
  activeSort.value = sort
  sortLoading.value = true
  try {
    const data = await productStore.queryProducts({ pageNum: 1, pageSize: 8, sort })
    newProducts.value = data.records
  } finally {
    sortLoading.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await productStore.fetchCategories()
    if (userStore.isLogin && !favoriteStore.loaded) favoriteStore.fetchIds().catch(() => [])
    const [hot, fresh, words, stat] = await Promise.all([
      productStore.queryProducts({ pageNum: 1, pageSize: 8, sort: 'hot' }),
      productStore.queryProducts({ pageNum: 1, pageSize: 8, sort: 'new' }),
      getHotWords().catch(() => []),
      getOverview().catch(() => null),
    ])
    hotProducts.value = hot.records
    newProducts.value = fresh.records
    hotWords.value = words.slice(0, 10)
    overview.value = stat
  } finally {
    loading.value = false
  }
})

function goSearch(word) {
  const text = String(word || keyword.value || '').trim()
  router.push({ name: 'search', query: text ? { keyword: text } : {} })
}

function goCategory(item) {
  router.push({ name: 'products', query: { categoryId: item.id } })
}
</script>

<template>
  <div class="page home">
    <!-- Hero 区 -->
    <section class="hero fade-up">
      <div class="hero__text">
        <span class="hero__badge">
          <el-icon :size="13"><Sunny /></el-icon>
          毕业季清仓专场 · 闲置好物正在流转
        </span>
        <h1>校园里的闲置，<br />在这里找到新主人</h1>
        <p>教材、数码、生活用品、运动装备……同学们的真实闲置，校内当面交易，省心又划算。</p>

        <div class="hero__search">
          <el-icon class="icon"><Search /></el-icon>
          <input v-model="keyword" placeholder="想找点什么？试试「考研英语真题」" @keyup.enter="goSearch()" />
          <button type="button" @click="goSearch()">搜索商品</button>
        </div>

        <div v-if="hotWords.length" class="hero__words">
          <span class="label">热搜：</span>
          <button v-for="item in hotWords.slice(0, 6)" :key="item.word" type="button" @click="goSearch(item.word)">
            {{ item.word }}
          </button>
        </div>
      </div>

      <div class="hero__stats">
        <div class="stat-card">
          <strong>{{ overview?.on_sale_total ?? '-' }}</strong>
          <span>件在售闲置</span>
        </div>
        <div class="stat-card">
          <strong>{{ overview?.user_total ?? '-' }}</strong>
          <span>位校园用户</span>
        </div>
        <div class="stat-card">
          <strong>{{ overview?.order_total ?? '-' }}</strong>
          <span>笔累计交易</span>
        </div>
        <div class="stat-card highlight">
          <strong>{{ overview?.today_new_product ?? '-' }}</strong>
          <span>今日新增发布</span>
        </div>
      </div>
    </section>

    <!-- 分类导航 -->
    <section class="block fade-up">
      <div class="section-title">
        <h2>按分类淘一淘</h2>
        <span class="more" @click="router.push({ name: 'category' })">全部分类 →</span>
      </div>
      <CategoryNav :categories="productStore.categories" variant="grid" @select="goCategory" />
    </section>

    <!-- 热门推荐 -->
    <section class="block fade-up">
      <div class="section-title">
        <h2>
          <el-icon class="flame"><TrendCharts /></el-icon>
          大家都在看
        </h2>
        <span class="more" @click="router.push({ name: 'rank' })">查看榜单 →</span>
      </div>
      <SkeletonCard v-if="loading" :count="4" />
      <div v-else class="product-grid">
        <ProductCard v-for="item in hotProducts" :key="item.id" :product="item" />
      </div>
    </section>

    <!-- 最新发布 -->
    <section class="block fade-up">
      <div class="section-title">
        <h2>最新发布</h2>
        <div class="sort-tabs">
          <button
            v-for="tab in sortTabs"
            :key="tab.value"
            class="sort-tab"
            :class="{ active: activeSort === tab.value }"
            type="button"
            @click="loadSort(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>
      <SkeletonCard v-if="loading || sortLoading" :count="8" />
      <div v-else class="product-grid">
        <ProductCard v-for="item in newProducts" :key="item.id" :product="item" />
      </div>
      <div class="more-row">
        <button class="more-btn" type="button" @click="router.push({ name: 'products' })">
          浏览全部商品
          <el-icon :size="14"><Right /></el-icon>
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 34px;
}

/* Hero */
.hero {
  position: relative;
  display: grid;
  grid-template-columns: 1.35fr 0.65fr;
  gap: 28px;
  padding: 38px 40px;
  border-radius: 24px;
  color: #fff;
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 48%, #34d399 100%);
  box-shadow: 0 24px 54px rgba(13, 148, 136, 0.26);
  overflow: hidden;
}

.hero::before {
  content: '';
  position: absolute;
  width: 320px;
  height: 320px;
  top: -140px;
  right: -60px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3), transparent 70%);
}

.hero__text {
  position: relative;
  z-index: 1;
}

.hero__badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  font-size: 12px;
  backdrop-filter: blur(6px);
}

.hero h1 {
  margin-top: 16px;
  font-size: 34px;
  line-height: 1.35;
  font-weight: 700;
  letter-spacing: 1px;
}

.hero p {
  margin-top: 12px;
  font-size: 13px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.88);
  max-width: 460px;
}

.hero__search {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 22px;
  padding: 6px 6px 6px 42px;
  max-width: 520px;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 14px 32px rgba(6, 78, 59, 0.24);
}

.hero__search .icon {
  position: absolute;
  left: 16px;
  color: var(--c-text-muted);
}

.hero__search input {
  flex: 1;
  height: 40px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--c-text);
}

.hero__search input::placeholder {
  color: var(--c-text-muted);
}

.hero__search button {
  height: 40px;
  padding: 0 22px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #ffa06b, var(--c-accent));
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
}

.hero__search button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(255, 122, 69, 0.42);
}

.hero__words {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 14px;
  font-size: 12px;
}

.hero__words .label {
  color: rgba(255, 255, 255, 0.78);
}

.hero__words button {
  padding: 4px 11px;
  border: 1px solid rgba(255, 255, 255, 0.42);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.hero__words button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.hero__stats {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  align-content: center;
}

.stat-card {
  padding: 16px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.22);
}

.stat-card strong {
  display: block;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.stat-card span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.86);
}

.stat-card.highlight {
  background: rgba(255, 255, 255, 0.9);
  color: var(--c-primary-dark);
}

.stat-card.highlight span {
  color: var(--c-text-sub);
}

.block {
  display: flex;
  flex-direction: column;
}

.flame {
  color: var(--c-accent);
  margin-right: 6px;
}

.sort-tabs {
  display: flex;
  gap: 6px;
}

.sort-tab {
  padding: 6px 14px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.sort-tab:hover {
  color: var(--c-primary-dark);
  border-color: var(--c-primary-light);
}

.sort-tab.active {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
}

.more-row {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.more-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 11px 30px;
  border: 1px solid var(--c-primary);
  border-radius: 999px;
  background: #fff;
  color: var(--c-primary-dark);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.22s;
}

.more-btn:hover {
  background: var(--c-primary-mist);
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(13, 148, 136, 0.18);
}

@media (max-width: 1023.98px) {
  .hero {
    grid-template-columns: 1fr;
    padding: 28px 24px;
  }

  .hero h1 {
    font-size: 27px;
  }

  .hero__stats {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 767.98px) {
  .home {
    gap: 24px;
  }

  .hero {
    border-radius: 18px;
    padding: 22px 18px;
  }

  .hero h1 {
    font-size: 22px;
  }

  .hero__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hero__search {
    padding-left: 38px;
  }

  .stat-card strong {
    font-size: 20px;
  }

  .section-title h2 {
    font-size: 16px;
  }

  .sort-tabs {
    overflow-x: auto;
  }
}
</style>
