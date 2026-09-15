<script setup>
import { computed } from 'vue'
import { PRICE_RANGES, SORT_OPTIONS } from '@/constants/product'

const props = defineProps({
  categories: { type: Array, default: () => [] },
  categoryId: { type: [Number, String], default: 0 },
  sort: { type: String, default: 'new' },
  priceIndex: { type: Number, default: 0 },
  total: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
})

const emit = defineEmits(['update:categoryId', 'update:sort', 'update:priceIndex', 'change'])

const categoryValue = computed({
  get: () => Number(props.categoryId) || 0,
  set: (value) => {
    emit('update:categoryId', value)
    emit('change')
  },
})

const sortValue = computed({
  get: () => props.sort,
  set: (value) => {
    emit('update:sort', value)
    emit('change')
  },
})

const priceValue = computed({
  get: () => props.priceIndex,
  set: (value) => {
    emit('update:priceIndex', value)
    emit('change')
  },
})
</script>

<template>
  <div class="filter-bar">
    <div class="filter-bar__row">
      <span class="label">分类</span>
      <el-select v-model="categoryValue" class="control" placeholder="全部分类" size="default">
        <el-option label="全部分类" :value="0" />
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>

      <span class="label">价格</span>
      <el-select v-model="priceValue" class="control" placeholder="全部价格">
        <el-option
          v-for="(range, index) in PRICE_RANGES"
          :key="range.label"
          :label="range.label"
          :value="index"
        />
      </el-select>

      <span class="total">
        共 <em>{{ total }}</em> 件商品
      </span>
    </div>

    <div class="filter-bar__row sort-row">
      <span class="label">排序</span>
      <div class="sort-group">
        <button
          v-for="option in SORT_OPTIONS"
          :key="option.value"
          class="sort-btn"
          :class="{ active: sortValue === option.value }"
          type="button"
          @click="sortValue = option.value"
        >
          {{ option.label }}
        </button>
      </div>
      <span v-if="loading" class="loading-hint">
        <el-icon class="is-loading"><Loading /></el-icon>
        正在加载
      </span>
    </div>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 18px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.filter-bar__row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.label {
  font-size: 13px;
  color: var(--c-text-muted);
  flex-shrink: 0;
}

.control {
  width: 150px;
}

.total {
  margin-left: auto;
  font-size: 13px;
  color: var(--c-text-sub);
}

.total em {
  font-style: normal;
  font-weight: 700;
  color: var(--c-accent-strong);
}

.sort-group {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.sort-btn {
  padding: 6px 14px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.sort-btn:hover {
  color: var(--c-primary-dark);
  border-color: var(--c-primary-light);
}

.sort-btn.active {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  box-shadow: 0 6px 14px rgba(13, 148, 136, 0.26);
}

.loading-hint {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-left: auto;
  font-size: 12px;
  color: var(--c-text-muted);
}

@media (max-width: 767.98px) {
  .filter-bar {
    padding: 14px;
  }

  .control {
    width: calc(50% - 34px);
    min-width: 108px;
  }

  .total {
    width: 100%;
    margin-left: 0;
  }

  .sort-group {
    flex: 1;
  }
}
</style>
