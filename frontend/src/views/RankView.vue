<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { useProductStore } from '@/store/product'
import { getHotProducts, getHotWords, getOverview } from '@/api/stat'
import { shortNumber, toAmount } from '@/utils/format'
import { svgCover } from '@/utils/image'

const router = useRouter()
const productStore = useProductStore()

const loading = ref(true)
const hotProducts = ref([])
const hotWords = ref([])
const overview = ref(null)
const activeMetric = ref('view')

const metrics = [
  { label: '浏览量榜', value: 'view' },
  { label: '销量榜', value: 'sales' },
  { label: '收藏热度', value: 'favorite' },
]

function sortedProducts() {
  const list = hotProducts.value.slice()
  if (activeMetric.value === 'sales') list.sort((a, b) => b.sales_count - a.sales_count)
  else if (activeMetric.value === 'favorite') list.sort((a, b) => b.view_count / (b.price || 1) - a.view_count / (a.price || 1))
  else list.sort((a, b) => b.view_count - a.view_count)
  return list
}

function metricValue(item) {
  if (activeMetric.value === 'sales') return `${item.sales_count} 件已售`
  if (activeMetric.value === 'favorite') return `热度指数 ${Math.round(item.view_count / (item.price || 1))}`
  return `${shortNumber(item.view_count)} 次浏览`
}

function percentOf(item) {
  const list = sortedProducts()
  const max = Math.max(...list.map((row) => row.view_count), 1)
  return Math.max(8, Math.round((item.view_count / max) * 100))
}

function onImageError(event, item) {
  event.target.src = svgCover(item.title, item.id)
}

onMounted(async () => {
  loading.value = true
  try {
    await productStore.fetchCategories()
    const [products, words, stat] = await Promise.all([
      getHotProducts({ limit: 10 }),
      getHotWords(),
      getOverview(),
    ])
    hotProducts.value = products
    hotWords.value = words
    overview.value = stat
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page rank-page">
    <header class="page-head fade-up">
      <div>
        <h1>热门榜单</h1>
        <p>基于浏览量、销量与搜索热度的实时排行，帮你快速发现校园里的抢手闲置</p>
      </div>
      <button class="ghost-btn" type="button" @click="router.push({ name: 'products', query: { sort: 'hot' } })">
        按热度浏览全部
      </button>
    </header>

    <!-- 平台数据概览 -->
    <section class="stat-grid fade-up">
      <div class="stat-card">
        <span class="stat-icon teal"><el-icon :size="20"><Goods /></el-icon></span>
        <div>
          <strong>{{ overview?.product_total ?? '-' }}</strong>
          <span>平台商品总数</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon orange"><el-icon :size="20"><PriceTag /></el-icon></span>
        <div>
          <strong>{{ overview?.on_sale_total ?? '-' }}</strong>
          <span>当前在售商品</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon blue"><el-icon :size="20"><User /></el-icon></span>
        <div>
          <strong>{{ overview?.user_total ?? '-' }}</strong>
          <span>注册校园用户</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon purple"><el-icon :size="20"><Tickets /></el-icon></span>
        <div>
          <strong>{{ overview?.order_total ?? '-' }}</strong>
          <span>累计订单数</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon green"><el-icon :size="20"><Wallet /></el-icon></span>
        <div>
          <strong>¥{{ toAmount(overview?.trade_amount ?? 0) }}</strong>
          <span>模拟交易总额</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon red"><el-icon :size="20"><TrendCharts /></el-icon></span>
        <div>
          <strong>{{ overview?.today_new_product ?? '-' }}</strong>
          <span>今日新增发布</span>
        </div>
      </div>
    </section>

    <div class="rank-layout fade-up">
      <!-- 热门商品榜 -->
      <section class="panel">
        <div class="panel-head">
          <h2>
            <el-icon class="flame"><TrendCharts /></el-icon>
            热门商品榜
          </h2>
          <div class="metric-tabs">
            <button
              v-for="item in metrics"
              :key="item.value"
              class="metric-tab"
              :class="{ active: activeMetric === item.value }"
              type="button"
              @click="activeMetric = item.value"
            >
              {{ item.label }}
            </button>
          </div>
        </div>

        <SkeletonCard v-if="loading" :count="4" />

        <ul v-else class="rank-list">
          <li
            v-for="(item, index) in sortedProducts()"
            :key="item.id"
            class="rank-item"
            @click="router.push({ name: 'product-detail', params: { id: item.id } })"
          >
            <span class="rank-no" :class="`top-${index + 1}`">{{ index + 1 }}</span>
            <img :src="item.cover" :alt="item.title" @error="(event) => onImageError(event, item)" />
            <div class="rank-body">
              <p class="rank-title">{{ item.title }}</p>
              <p class="rank-meta">
                <span>{{ item.category_name }}</span>
                <span>{{ item.seller_nickname }}</span>
                <span class="metric">{{ metricValue(item) }}</span>
              </p>
              <div class="bar">
                <span :style="{ width: `${percentOf(item)}%` }"></span>
              </div>
            </div>
            <div class="rank-price">
              <strong>¥{{ toAmount(item.price) }}</strong>
              <span>{{ item.condition }}</span>
            </div>
          </li>
        </ul>
      </section>

      <!-- 搜索热词榜 -->
      <section class="panel">
        <div class="panel-head">
          <h2>
            <el-icon class="flame hot"><Search /></el-icon>
            搜索热词榜
          </h2>
          <span class="hint">近 7 天累计</span>
        </div>

        <ul v-if="!loading" class="word-list">
          <li v-for="item in hotWords" :key="item.word" class="word-item">
            <span class="rank-no small" :class="`top-${item.rank}`">{{ item.rank }}</span>
            <div class="word-body">
              <div class="word-top">
                <strong>{{ item.word }}</strong>
                <span>{{ item.score }} 次搜索</span>
              </div>
              <div class="bar word-bar">
                <span :style="{ width: `${item.percent}%` }"></span>
              </div>
            </div>
            <button class="search-btn" type="button" @click="router.push({ name: 'search', query: { keyword: item.word } })">
              搜索
            </button>
          </li>
        </ul>

        <div v-else class="skeleton-block words-skeleton"></div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.rank-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
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

.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  transition: transform 0.22s, box-shadow 0.22s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.stat-icon {
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 12px;
}

.stat-icon.teal {
  background: #ccfbf1;
  color: #0d9488;
}

.stat-icon.orange {
  background: #ffedd5;
  color: #ea580c;
}

.stat-icon.blue {
  background: #dbeafe;
  color: #2563eb;
}

.stat-icon.purple {
  background: #ede9fe;
  color: #7c3aed;
}

.stat-icon.green {
  background: #dcfce7;
  color: #16a34a;
}

.stat-icon.red {
  background: #fee2e2;
  color: #dc2626;
}

.stat-card strong {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--c-text);
}

.stat-card span {
  font-size: 11px;
  color: var(--c-text-muted);
}

.rank-layout {
  display: grid;
  grid-template-columns: 1.35fr 0.65fr;
  gap: 18px;
  align-items: start;
}

.panel {
  padding: 20px 22px 24px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.panel-head h2 {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 700;
}

.flame {
  color: var(--c-accent);
  margin-right: 6px;
}

.flame.hot {
  color: #f59e0b;
}

.hint {
  font-size: 12px;
  color: var(--c-text-muted);
}

.metric-tabs {
  display: flex;
  gap: 6px;
}

.metric-tab {
  padding: 6px 14px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 12px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.metric-tab.active {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
}

.rank-list {
  display: flex;
  flex-direction: column;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.rank-item:hover {
  background: var(--c-surface-alt);
}

.rank-no {
  width: 26px;
  height: 26px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #f1f5f9;
  color: var(--c-text-muted);
  font-size: 12px;
  font-weight: 700;
}

.rank-no.small {
  width: 22px;
  height: 22px;
  font-size: 11px;
}

.rank-no.top-1 {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: #fff;
}

.rank-no.top-2 {
  background: linear-gradient(135deg, #cbd5e1, #94a3b8);
  color: #fff;
}

.rank-no.top-3 {
  background: linear-gradient(135deg, #fdba74, #ea580c);
  color: #fff;
}

.rank-item img {
  width: 62px;
  height: 62px;
  flex-shrink: 0;
  border-radius: 12px;
  object-fit: cover;
  background: #f3f6f9;
}

.rank-body {
  flex: 1;
  min-width: 0;
}

.rank-title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin: 5px 0 8px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.rank-meta .metric {
  color: var(--c-primary-dark);
}

.bar {
  height: 6px;
  border-radius: 999px;
  background: #f1f5f9;
  overflow: hidden;
}

.bar span {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #5eead4, #0d9488);
  transition: width 0.5s cubic-bezier(0.22, 1, 0.36, 1);
}

.rank-price {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}

.rank-price strong {
  font-size: 16px;
  color: var(--c-accent-strong);
}

.rank-price span {
  font-size: 11px;
  color: var(--c-text-muted);
}

.word-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.word-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.word-body {
  flex: 1;
  min-width: 0;
}

.word-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.word-top strong {
  font-size: 13px;
  font-weight: 600;
}

.word-top span {
  font-size: 11px;
  color: var(--c-text-muted);
}

.word-bar span {
  background: linear-gradient(90deg, #fdba74, #ea580c);
}

.search-btn {
  padding: 5px 12px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 12px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.search-btn:hover {
  color: var(--c-primary-dark);
  border-color: var(--c-primary-light);
  background: var(--c-primary-mist);
}

.words-skeleton {
  height: 320px;
}

@media (max-width: 1023.98px) {
  .stat-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .rank-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767.98px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .page-head h1 {
    font-size: 19px;
  }

  .rank-item img {
    width: 52px;
    height: 52px;
  }
}
</style>
