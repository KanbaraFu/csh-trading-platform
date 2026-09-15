<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import EmptyState from '@/components/EmptyState.vue'
import FilterBar from '@/components/FilterBar.vue'
import ProductCard from '@/components/ProductCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { categoryIcon } from '@/constants/message'
import { PRICE_RANGES } from '@/constants/product'
import { useProductStore } from '@/store/product'
import { useFavoriteStore } from '@/store/favorite'
import { useUserStore } from '@/store/user'
import { getHotWords, search } from '@/api/stat'

const route = useRoute()
const router = useRouter()
const productStore = useProductStore()
const favoriteStore = useFavoriteStore()
const userStore = useUserStore()

const keyword = ref(String(route.query.keyword || ''))
const hotWords = ref([])
const records = ref([])
const total = ref(0)
const loading = ref(true)
const searched = ref(false)
const pageSize = 12

const filters = reactive({ categoryId: 0, sort: 'new', priceIndex: 0, pageNum: 1 })

function syncFromQuery() {
  const query = route.query
  keyword.value = String(query.keyword || '')
  filters.categoryId = Number(query.categoryId || 0)
  filters.sort = String(query.sort || 'new')
  filters.priceIndex = Number(query.range || 0)
  filters.pageNum = Number(query.page || 1)
}

async function load() {
  loading.value = true
  searched.value = true
  try {
    const range = PRICE_RANGES[filters.priceIndex] || PRICE_RANGES[0]
    const data = await search({
      keyword: keyword.value || undefined,
      pageNum: filters.pageNum,
      pageSize,
      categoryId: filters.categoryId || undefined,
      minPrice: range.min ?? undefined,
      maxPrice: range.max ?? undefined,
      sort: filters.sort,
    })
    records.value = data.records
    total.value = data.total
    if (keyword.value) hotWords.value = await getHotWords()
  } finally {
    loading.value = false
  }
}

function applyFilters(patch) {
  const next = {
    keyword: keyword.value || undefined,
    categoryId: filters.categoryId || undefined,
    sort: filters.sort,
    range: filters.priceIndex || undefined,
    page: 1,
    ...patch,
  }
  Object.keys(next).forEach((key) => {
    if (next[key] === undefined || next[key] === '' || next[key] === 0) delete next[key]
  })
  router.push({ name: 'search', query: next })
}

function doSearch(word) {
  if (typeof word === 'string') keyword.value = word
  applyFilters({ page: 1 })
}

function changePage(page) {
  router.push({ name: 'search', query: { ...route.query, page } })
}

watch(
  () => route.query,
  () => {
    syncFromQuery()
    load()
  },
)

onMounted(async () => {
  syncFromQuery()
  await productStore.fetchCategories()
  if (userStore.isLogin && !favoriteStore.loaded) favoriteStore.fetchIds().catch(() => [])
  hotWords.value = await getHotWords().catch(() => [])
  await load()
})
</script>

<template>
  <div class="page search-page">
    <section class="search-hero fade-up">
      <h1>搜索校园闲置</h1>
      <div class="search-box">
        <el-icon class="icon"><Search /></el-icon>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="输入关键词，例如：考研英语真题、iPad、台灯"
          @keyup.enter="doSearch()"
        />
        <button class="search-btn" type="button" @click="doSearch()">搜索</button>
      </div>

      <div class="hot-words">
        <span class="label">
          <el-icon :size="13"><Histogram /></el-icon>
          搜索热词
        </span>
        <button
          v-for="item in hotWords.slice(0, 10)"
          :key="item.word"
          class="word"
          :class="{ hot: item.is_hot }"
          type="button"
          @click="doSearch(item.word)"
        >
          {{ item.word }}
        </button>
      </div>
    </section>

    <section class="fade-up">
      <FilterBar
        :categories="productStore.categories"
        :category-id="filters.categoryId"
        :sort="filters.sort"
        :price-index="filters.priceIndex"
        :total="total"
        :loading="loading"
        @update:category-id="(value) => applyFilters({ categoryId: value })"
        @update:sort="(value) => applyFilters({ sort: value })"
        @update:price-index="(value) => applyFilters({ range: value })"
      />
    </section>

    <section class="fade-up">
      <SkeletonCard v-if="loading" :count="8" />

      <div v-else-if="records.length">
        <div class="section-title">
          <h2>{{ keyword ? `“${keyword}” 找到 ${total} 件商品` : `共 ${total} 件在售商品` }}</h2>
        </div>
        <div class="product-grid">
          <ProductCard v-for="item in records" :key="item.id" :product="item" />
        </div>
      </div>

      <div v-else-if="searched" class="empty-wrap">
        <EmptyState
          icon="search"
          title="没有找到相关商品"
          :description="keyword ? `换个关键词试试，或者看看下面的推荐分类` : '当前筛选条件下暂无商品'"
          action-text="清空筛选条件"
          @action="router.push({ name: 'search' })"
        />
        <div class="recommend">
          <p class="recommend-title">试试这些分类</p>
          <div class="recommend-list">
            <button
              v-for="item in productStore.categories"
              :key="item.id"
              class="recommend-item"
              type="button"
              @click="applyFilters({ categoryId: item.id, page: 1 })"
            >
              <el-icon :size="15"><component :is="categoryIcon(item.name)" /></el-icon>
              {{ item.name }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <div v-if="!loading && total > pageSize" class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="filters.pageNum"
        @current-change="changePage"
      />
    </div>
  </div>
</template>

<style scoped>
.search-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-hero {
  padding: 30px 32px;
  border-radius: 22px;
  background: linear-gradient(135deg, #f0fdfa 0%, #ffffff 60%, #fff7f1 100%);
  border: 1px solid var(--c-primary-soft);
}

.search-hero h1 {
  font-size: 24px;
  font-weight: 700;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
  padding: 6px 6px 6px 44px;
  max-width: 640px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid var(--c-border);
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.07);
  transition: box-shadow 0.2s, border-color 0.2s;
}

.search-box:focus-within {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.12);
}

.search-box .icon {
  position: absolute;
  left: 17px;
  color: var(--c-text-muted);
}

.search-input {
  flex: 1;
  height: 42px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--c-text);
}

.search-btn {
  height: 42px;
  padding: 0 26px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
}

.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(13, 148, 136, 0.32);
}

.hot-words {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 18px;
}

.hot-words .label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--c-accent);
  font-weight: 600;
}

.word {
  padding: 5px 13px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 12px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.word:hover {
  color: var(--c-primary-dark);
  border-color: var(--c-primary-light);
  background: var(--c-primary-mist);
}

.word.hot {
  color: var(--c-accent-strong);
  border-color: #ffd3bd;
  background: var(--c-accent-soft);
}

.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  padding-bottom: 28px;
}

.recommend {
  text-align: center;
}

.recommend-title {
  font-size: 13px;
  color: var(--c-text-muted);
  margin-bottom: 12px;
}

.recommend-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.recommend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.recommend-item:hover {
  color: var(--c-primary-dark);
  border-color: var(--c-primary-light);
  background: var(--c-primary-mist);
}

.pager {
  display: flex;
  justify-content: center;
}

@media (max-width: 767.98px) {
  .search-hero {
    padding: 20px 16px;
    border-radius: 16px;
  }

  .search-hero h1 {
    font-size: 19px;
  }

  .search-box {
    padding-left: 38px;
  }

  .search-btn {
    padding: 0 18px;
    font-size: 13px;
  }
}
</style>
