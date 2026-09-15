<script setup>
import { categoryIcon } from '@/constants/message'

defineProps({
  categories: { type: Array, default: () => [] },
  activeId: { type: [Number, String], default: 0 },
  variant: { type: String, default: 'grid' }, // grid | chips
  showCount: { type: Boolean, default: false },
})

defineEmits(['select'])
</script>

<template>
  <!-- 圆形图标网格（首页 / 分类页） -->
  <div v-if="variant === 'grid'" class="cat-grid">
    <button
      v-for="item in categories"
      :key="item.id"
      class="cat-cell"
      :class="{ active: Number(activeId) === item.id }"
      type="button"
      @click="$emit('select', item)"
    >
      <span class="cat-icon">
        <el-icon :size="22"><component :is="categoryIcon(item.name)" /></el-icon>
      </span>
      <span class="cat-name">{{ item.name }}</span>
      <span v-if="showCount" class="cat-count">{{ item.product_count }} 件</span>
    </button>
  </div>

  <!-- 横向筛选条（列表页 / 搜索页） -->
  <div v-else class="cat-chips">
    <button
      class="chip"
      :class="{ active: !Number(activeId) }"
      type="button"
      @click="$emit('select', { id: 0, name: '全部' })"
    >
      全部
    </button>
    <button
      v-for="item in categories"
      :key="item.id"
      class="chip"
      :class="{ active: Number(activeId) === item.id }"
      type="button"
      @click="$emit('select', item)"
    >
      {{ item.name }}
    </button>
  </div>
</template>

<style scoped>
.cat-grid {
  display: grid;
  grid-template-columns: repeat(8, minmax(0, 1fr));
  gap: 14px;
}

.cat-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 6px;
  border: 1px solid transparent;
  border-radius: 14px;
  background: #fff;
  cursor: pointer;
  transition: all 0.22s cubic-bezier(0.22, 1, 0.36, 1);
  box-shadow: var(--shadow-card);
}

.cat-cell:hover {
  transform: translateY(-4px);
  border-color: var(--c-primary-soft);
  box-shadow: var(--shadow-hover);
}

.cat-cell.active {
  border-color: var(--c-primary);
  background: var(--c-primary-mist);
}

.cat-icon {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: var(--c-primary-dark);
  background: var(--c-primary-mist);
  transition: all 0.22s;
}

.cat-cell:hover .cat-icon,
.cat-cell.active .cat-icon {
  color: #fff;
  background: linear-gradient(135deg, #2dd4bf, #0d9488);
  box-shadow: 0 8px 18px rgba(13, 148, 136, 0.3);
}

.cat-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--c-text);
}

.cat-count {
  font-size: 11px;
  color: var(--c-text-muted);
}

.cat-chips {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
  scrollbar-width: none;
}

.cat-chips::-webkit-scrollbar {
  display: none;
}

.chip {
  flex-shrink: 0;
  padding: 7px 15px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.chip:hover {
  color: var(--c-primary-dark);
  border-color: var(--c-primary-light);
}

.chip.active {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  box-shadow: 0 6px 16px rgba(13, 148, 136, 0.28);
}

@media (max-width: 1023.98px) {
  .cat-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 767.98px) {
  .cat-grid {
    gap: 10px;
  }

  .cat-cell {
    padding: 12px 4px;
  }

  .cat-icon {
    width: 40px;
    height: 40px;
  }

  .cat-name {
    font-size: 12px;
  }
}
</style>
