<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CategoryNav from '@/components/CategoryNav.vue'
import EmptyState from '@/components/EmptyState.vue'
import FilterBar from '@/components/FilterBar.vue'
import ProductCard from '@/components/ProductCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { PRICE_RANGES } from '@/constants/product'
import { useProductStore } from '@/store/product'
import { useFavoriteStore } from '@/store/favorite'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const productStore = useProductStore()
const favoriteStore = useFavoriteStore()
const userStore = useUserStore()

const loading = ref(true)
const records = ref([])
const total = ref(0)
const pageSize = 12

const filters = reactive({
  categoryId: 0,
  keyword: '',
  sort: 'new',
  priceIndex: 0,
  pageNum: 1,
})

function syncFromQuery() {
  const query = route.query
  filters.categoryId = Number(query.categoryId || 0)
  filters.keyword = String(query.keyword || '')
  filters.sort = String(query.sort || 'new')
  filters.priceIndex = Number(query.range || 0)
  filters.pageNum = Number(query.page || 1)
}

async function load() {
  loading.value = true
  try {
    const range = PRICE_RANGES[filters.priceIndex] || PRICE_RANGES[0]
    const data = await productStore.queryProducts({
      pageNum: filters.pageNum,
      pageSize,
      categoryId: filters.categoryId || undefined,
      keyword: filters.keyword || undefined,
      minPrice: range.min ?? undefined,
      maxPrice: range.max ?? undefined,
      sort: filters.sort,
    })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function applyFilters(patch) {
  const next = {
    categoryId: filters.categoryId || undefined,
    keyword: filters.keyword || undefined,
    sort: filters.sort,
    range: filters.priceIndex || undefined,
    page: 1,
    ...patch,
  }
  Object.keys(next).forEach((key) => {
    if (next[key] === undefined || next[key] === '' || next[key] === 0) delete next[key]
  })
  router.push({ name: 'products', query: next })
}

function changePage(page) {
  const query = { ...route.query, page }
  router.push({ name: 'products', query })
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
  await load()
})
</script>

<template>
  <div class="page list-page">
    <header class="page-head fade-up">
      <div>
        <h1>{{ filters.keyword ? `“${filters.keyword}” 的搜索结果` : '全部商品' }}</h1>
        <p>筛选条件可自由组合，支持分类、价格区间与多种排序方式</p>
      </div>
    </header>

    <section class="fade-up">
      <CategoryNav
        :categories="productStore.categories"
        :active-id="filters.categoryId"
        variant="chips"
        @select="(item) => applyFilters({ categoryId: item.id })"
      />
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

      <div v-else-if="records.length" class="product-grid">
        <ProductCard v-for="item in records" :key="item.id" :product="item" />
      </div>

      <EmptyState
        v-else
        icon="search"
        title="没有找到匹配的商品"
        description="试试更换分类或放宽价格区间，也可以去首页看看其他同学的闲置"
        action-text="重置筛选条件"
        @action="router.push({ name: 'products' })"
      />
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
.list-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.pager {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

@media (max-width: 767.98px) {
  .page-head h1 {
    font-size: 19px;
  }
}
</style>
