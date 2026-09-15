<script setup>
import { computed } from 'vue'
import { PRODUCT_STATUS } from '@/constants/product'
import { ORDER_STATUS } from '@/constants/order'

const props = defineProps({
  status: { type: [Number, String], required: true },
  kind: { type: String, default: 'product' }, // product | order
  size: { type: String, default: 'default' },
  effect: { type: String, default: 'light' },
})

const meta = computed(() => {
  const key = Number(props.status)
  const source = props.kind === 'order' ? ORDER_STATUS : PRODUCT_STATUS
  return source[key] ?? { label: '未知', type: 'info' }
})
</script>

<template>
  <el-tag :type="meta.type" :size="size" :effect="effect" round>{{ meta.label }}</el-tag>
</template>
