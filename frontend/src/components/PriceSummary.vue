<script setup>
import { toAmount } from '@/utils/format'

defineProps({
  totalQuantity: { type: Number, default: 0 },
  totalAmount: { type: [Number, String], default: 0 },
  discountAmount: { type: [Number, String], default: 0 },
  payAmount: { type: [Number, String], default: 0 },
  title: { type: String, default: '金额明细' },
  compact: { type: Boolean, default: false },
})
</script>

<template>
  <div class="price-summary" :class="{ compact }">
    <p v-if="!compact" class="title">{{ title }}</p>
    <div class="row">
      <span>商品件数</span>
      <span>{{ totalQuantity }} 件</span>
    </div>
    <div class="row">
      <span>商品总额</span>
      <span>¥{{ toAmount(totalAmount) }}</span>
    </div>
    <div class="row">
      <span>优惠金额</span>
      <span class="discount">-¥{{ toAmount(discountAmount) }}</span>
    </div>
    <div class="row">
      <span>运费</span>
      <span class="free">校内自提免运费</span>
    </div>
    <div class="row pay">
      <span>应付金额</span>
      <strong>¥{{ toAmount(payAmount) }}</strong>
    </div>
  </div>
</template>

<style scoped>
.price-summary {
  padding: 18px;
  background: linear-gradient(180deg, #f7fefd, #ffffff);
  border-radius: var(--radius-card);
  border: 1px solid var(--c-primary-soft);
}

.title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--c-text);
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--c-text-sub);
  padding: 6px 0;
}

.discount {
  color: var(--c-accent);
}

.free {
  color: var(--c-success);
}

.pay {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px dashed var(--c-primary-soft);
  font-size: 14px;
  color: var(--c-text);
}

.pay strong {
  font-size: 20px;
  color: var(--c-accent-strong);
  font-family: 'DIN Alternate', 'PingFang SC', sans-serif;
}

.compact {
  padding: 0;
  border: none;
  background: transparent;
}

.compact .row {
  padding: 4px 0;
}

.compact .pay {
  margin-top: 6px;
  padding-top: 8px;
}
</style>
