<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import PriceSummary from '@/components/PriceSummary.vue'
import { useCartStore } from '@/store/cart'
import { toAmount } from '@/utils/format'
import { svgCover } from '@/utils/image'

const router = useRouter()
const cartStore = useCartStore()

const previewVisible = ref(false)
const previewData = ref(null)
const loadingPreview = ref(false)

const items = computed(() => cartStore.items)
const selectedCount = computed(() => cartStore.selectedCount)
const selectedAmount = computed(() => cartStore.selectedAmount)
const allSelected = computed(() => cartStore.allSelected)

function onImageError(event, item) {
  event.target.src = svgCover(item.product?.title, item.product_id)
}

function imageOf(item) {
  return item.product?.cover || svgCover(item.product?.title, item.product_id)
}

async function changeQuantity(item, value) {
  await cartStore.changeQuantity(item.id, value)
}

async function toggleSelect(item, value) {
  await cartStore.toggleSelected(item.id, value)
}

async function toggleAll(value) {
  await cartStore.toggleAll(value)
}

async function removeItem(item) {
  await ElMessageBox.confirm(`确认从购物车移除「${item.product?.title}」吗？`, '移除商品', {
    confirmButtonText: '移除',
    cancelButtonText: '保留',
    type: 'warning',
  })
  await cartStore.remove(item.id)
  ElMessage.success('已从购物车移除')
}

async function openPreview() {
  if (!selectedCount.value) {
    ElMessage.warning('请先勾选要结算的商品')
    return
  }
  loadingPreview.value = true
  try {
    const ids = cartStore.selectedItems.map((item) => item.id)
    previewData.value = await cartStore.preview(ids)
    previewVisible.value = true
  } finally {
    loadingPreview.value = false
  }
}

function goCheckout() {
  const ids = cartStore.selectedItems.map((item) => item.id)
  previewVisible.value = false
  router.push({ name: 'checkout', query: { cartIds: ids.join(',') } })
}

onMounted(() => cartStore.fetchCart())
</script>

<template>
  <div class="page cart-page">
    <header class="page-head fade-up">
      <div>
        <h1>购物车</h1>
        <p>共 {{ items.length }} 件商品，勾选后可批量结算（同一订单仅支持同一卖家的商品）</p>
      </div>
      <button class="ghost-btn" type="button" @click="router.push({ name: 'products' })">继续挑选</button>
    </header>

    <template v-if="items.length">
      <section class="list fade-up">
        <div class="list-head">
          <el-checkbox :model-value="allSelected" @change="toggleAll">全选</el-checkbox>
          <span class="col-title">商品信息</span>
          <span class="col-price">单价</span>
          <span class="col-qty">数量</span>
          <span class="col-sub">小计</span>
          <span class="col-op">操作</span>
        </div>

        <div v-for="item in items" :key="item.id" class="row">
          <el-checkbox :model-value="item.selected" @change="(value) => toggleSelect(item, value)" />

          <div class="product">
            <router-link :to="{ name: 'product-detail', params: { id: item.product_id } }" class="thumb">
              <img :src="imageOf(item)" :alt="item.product?.title" @error="(event) => onImageError(event, item)" />
            </router-link>
            <div class="product__body">
              <router-link class="product__title" :to="{ name: 'product-detail', params: { id: item.product_id } }">
                {{ item.product?.title }}
              </router-link>
              <p class="product__meta">
                <span>{{ item.product?.condition }}</span>
                <span>{{ item.product?.location }}</span>
                <span>卖家：{{ item.product?.seller_nickname }}</span>
              </p>
            </div>
          </div>

          <span class="price-cell">¥{{ toAmount(item.product?.price) }}</span>

          <div class="qty-cell">
            <el-input-number
              :model-value="item.quantity"
              :min="1"
              :max="Math.max(1, item.product?.stock || 1)"
              size="small"
              @change="(value) => changeQuantity(item, value)"
            />
          </div>

          <span class="sub-cell">¥{{ toAmount(item.subtotal) }}</span>

          <div class="op-cell">
            <button class="icon-btn" type="button" title="移除" @click="removeItem(item)">
              <el-icon :size="15"><Delete /></el-icon>
            </button>
          </div>
        </div>
      </section>

      <section class="checkout-bar fade-up">
        <div class="left">
          <el-checkbox :model-value="allSelected" @change="toggleAll">全选</el-checkbox>
          <span class="hint">已选 <em>{{ selectedCount }}</em> 件</span>
        </div>
        <div class="right">
          <div class="amount">
            合计：<strong>¥{{ toAmount(selectedAmount) }}</strong>
          </div>
          <button class="ghost-btn" type="button" :disabled="loadingPreview" @click="openPreview">结算预览</button>
          <button class="primary-btn" type="button" :disabled="!selectedCount" @click="goCheckout">去结算</button>
        </div>
      </section>
    </template>

    <EmptyState
      v-else
      icon="cart"
      title="购物车还是空的"
      description="把心仪的闲置加进来统一结算，价格和数量都能随时调整"
      action-text="去挑几件好物"
      @action="router.push({ name: 'products' })"
    />

    <el-dialog v-model="previewVisible" title="结算预览" width="520px" align-center>
      <div v-if="previewData" class="preview">
        <div class="preview-list">
          <div v-for="item in previewData.items" :key="item.id" class="preview-row">
            <img :src="imageOf(item)" :alt="item.product?.title" @error="(event) => onImageError(event, item)" />
            <div class="preview-row__body">
              <p class="preview-row__title">{{ item.product?.title }}</p>
              <p class="preview-row__meta">¥{{ toAmount(item.product?.price) }} × {{ item.quantity }}</p>
            </div>
            <span class="preview-row__sub">¥{{ toAmount(item.subtotal) }}</span>
          </div>
        </div>
        <PriceSummary
          :total-quantity="previewData.total_quantity"
          :total-amount="previewData.total_amount"
          :discount-amount="previewData.discount_amount"
          :pay-amount="previewData.pay_amount"
          title="金额明细"
        />
        <p v-if="!previewData.single_seller" class="warn">当前勾选商品来自多个卖家，提交后会自动拆分为多个订单。</p>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">继续挑选</el-button>
        <el-button type="primary" @click="goCheckout">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.cart-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.list {
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.list-head,
.row {
  display: grid;
  grid-template-columns: 46px 1fr 110px 150px 120px 70px;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
}

.list-head {
  background: var(--c-surface-alt);
  font-size: 12px;
  color: var(--c-text-muted);
}

.row {
  border-top: 1px solid var(--c-border);
  transition: background 0.2s;
}

.row:hover {
  background: #fbfdfd;
}

.product {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.thumb {
  width: 72px;
  height: 72px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #f3f6f9;
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product__body {
  min-width: 0;
}

.product__title {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 14px;
  color: var(--c-text);
  transition: color 0.2s;
}

.product__title:hover {
  color: var(--c-primary-dark);
}

.product__meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 6px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.price-cell,
.sub-cell {
  font-size: 14px;
  color: var(--c-text);
}

.sub-cell {
  font-weight: 700;
  color: var(--c-accent-strong);
}

.op-cell {
  display: flex;
  justify-content: center;
}

.icon-btn {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 9px;
  background: transparent;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: all 0.2s;
}

.icon-btn:hover {
  color: var(--c-danger);
  background: #fef2f2;
}

.checkout-bar {
  position: sticky;
  bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  border-radius: var(--radius-card);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.1);
  border: 1px solid var(--c-border);
  flex-wrap: wrap;
}

.checkout-bar .left,
.checkout-bar .right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.hint {
  font-size: 13px;
  color: var(--c-text-sub);
}

.hint em {
  font-style: normal;
  font-weight: 700;
  color: var(--c-accent-strong);
}

.amount {
  font-size: 13px;
  color: var(--c-text-sub);
}

.amount strong {
  font-size: 21px;
  color: var(--c-accent-strong);
  font-family: 'DIN Alternate', 'PingFang SC', sans-serif;
}

.ghost-btn {
  padding: 10px 20px;
  border: 1px solid var(--c-primary);
  border-radius: 999px;
  background: #fff;
  color: var(--c-primary-dark);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.ghost-btn:hover:not(:disabled) {
  background: var(--c-primary-mist);
}

.primary-btn {
  padding: 11px 34px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #ffa06b, var(--c-accent));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 22px rgba(255, 122, 69, 0.3);
  transition: transform 0.2s;
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
}

.primary-btn:disabled,
.ghost-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.preview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 260px;
  overflow-y: auto;
}

.preview-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview-row img {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  object-fit: cover;
  background: #f3f6f9;
}

.preview-row__body {
  flex: 1;
  min-width: 0;
}

.preview-row__title {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-row__meta {
  font-size: 12px;
  color: var(--c-text-muted);
}

.preview-row__sub {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-accent-strong);
}

.warn {
  font-size: 12px;
  color: var(--c-warning);
}

@media (max-width: 1023.98px) {
  .list-head {
    display: none;
  }

  .row {
    grid-template-columns: 30px 1fr;
    grid-template-areas:
      'check product'
      'qty qty'
      'price op';
    gap: 10px;
    padding: 16px;
  }

  .row > .el-checkbox {
    grid-area: check;
  }

  .product {
    grid-area: product;
  }

  .price-cell {
    grid-area: price;
  }

  .qty-cell {
    grid-area: qty;
  }

  .sub-cell {
    grid-area: price;
    justify-self: end;
  }

  .col-price,
  .col-qty,
  .col-sub,
  .col-op,
  .col-title {
    display: none;
  }

  .op-cell {
    grid-area: op;
    justify-content: flex-end;
  }
}

@media (max-width: 767.98px) {
  .checkout-bar {
    position: fixed;
    left: 0;
    right: 0;
    bottom: calc(var(--tabbar-height) + env(safe-area-inset-bottom));
    border-radius: 0;
    padding: 12px 16px;
    z-index: 90;
  }

  .amount strong {
    font-size: 18px;
  }

  .ghost-btn {
    display: none;
  }
}
</style>
