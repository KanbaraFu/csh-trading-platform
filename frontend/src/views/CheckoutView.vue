<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AddressForm from '@/components/AddressForm.vue'
import EmptyState from '@/components/EmptyState.vue'
import PriceSummary from '@/components/PriceSummary.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { PAY_METHODS } from '@/constants/order'
import { useCartStore } from '@/store/cart'
import { useOrderStore } from '@/store/order'
import { useProductStore } from '@/store/product'
import { useUserStore } from '@/store/user'
import { toAmount } from '@/utils/format'
import { svgCover } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const productStore = useProductStore()
const orderStore = useOrderStore()

const loading = ref(true)
const submitting = ref(false)
const items = ref([])
const totalAmount = ref(0)
const payAmount = ref(0)
const remark = ref('')
const payMethod = ref('campus_card')
const selectedAddressId = ref(0)
const addressDialog = ref(false)
const savingAddress = ref(false)
const successVisible = ref(false)
const createdOrders = ref([])

const addresses = computed(() => userStore.addresses)
const selectedAddress = computed(() => addresses.value.find((item) => item.id === selectedAddressId.value) || null)
const sellerCount = computed(() => new Set(items.value.map((item) => item.product.seller_id)).size)

function imageOf(item) {
  return item.product?.cover || svgCover(item.product?.title, item.product_id)
}

function onImageError(event, item) {
  event.target.src = svgCover(item.product?.title, item.product_id)
}

function normalize(list) {
  return list
    .filter((item) => item.product)
    .map((item) => ({
      key: `${item.product_id}-${item.quantity}`,
      product_id: item.product_id,
      quantity: item.quantity,
      subtotal: Number(item.subtotal ?? item.product.price * item.quantity),
      product: item.product,
      cart_id: item.id || 0,
    }))
}

async function load() {
  loading.value = true
  try {
    await userStore.fetchAddresses()
    selectedAddressId.value = userStore.defaultAddressId

    const cartIds = String(route.query.cartIds || '').split(',').filter(Boolean)
    if (cartIds.length) {
      const data = await cartStore.preview(cartIds)
      items.value = normalize(data.items)
      totalAmount.value = data.total_amount
      payAmount.value = data.pay_amount
    } else if (route.query.productId) {
      const product = await productStore.fetchDetail(route.query.productId)
      const quantity = Math.max(1, Number(route.query.quantity) || 1)
      const subtotal = Number((product.price * quantity).toFixed(2))
      items.value = normalize([{ product_id: product.id, quantity, subtotal, product, id: 0 }])
      totalAmount.value = subtotal
      payAmount.value = subtotal
    }
  } finally {
    loading.value = false
  }
}

async function saveAddress(payload) {
  savingAddress.value = true
  try {
    const { id, ...rest } = payload
    await userStore.saveAddress(rest, id)
    ElMessage.success(id ? '地址已更新' : '地址已添加')
    addressDialog.value = false
  } finally {
    savingAddress.value = false
  }
}

async function submitOrder() {
  if (!items.value.length) {
    ElMessage.warning('订单中没有商品')
    return
  }
  if (!selectedAddressId.value) {
    ElMessage.warning('请先选择收货地址')
    return
  }

  // 按卖家分组，多卖家时自动拆分为多个订单
  const groups = new Map()
  items.value.forEach((item) => {
    const sellerId = item.product.seller_id
    if (!groups.has(sellerId)) groups.set(sellerId, [])
    groups.get(sellerId).push(item)
  })

  submitting.value = true
  try {
    const orders = []
    for (const group of groups.values()) {
      const order = await orderStore.create({
        addressId: selectedAddressId.value,
        items: group.map((item) => ({ productId: item.product_id, quantity: item.quantity })),
        remark: remark.value,
        cartIds: group.map((item) => item.cart_id).filter(Boolean),
      })
      orders.push(order)
    }
    createdOrders.value = orders
    successVisible.value = true
    await cartStore.fetchCart()
  } finally {
    submitting.value = false
  }
}

async function payNow() {
  const order = createdOrders.value[0]
  if (!order) return
  for (const item of createdOrders.value) {
    await orderStore.pay(item.id, payMethod.value)
  }
  ElMessage.success(`模拟支付成功，共支付 ¥${toAmount(order.pay_amount)}`)
  successVisible.value = false
  router.replace(createdOrders.value.length === 1
    ? { name: 'order-detail', params: { id: order.id } }
    : { name: 'orders' })
}

function payLater() {
  successVisible.value = false
  router.replace({ name: 'orders' })
}

onMounted(load)
</script>

<template>
  <div class="page checkout-page">
    <header class="page-head fade-up">
      <div>
        <h1>确认订单</h1>
        <p>核对收货地址与商品清单，提交后即可进行模拟支付</p>
      </div>
      <button class="ghost-btn" type="button" @click="router.back()">返回上一页</button>
    </header>

    <div v-if="loading" class="loading-card">
      <div class="skeleton-block bar"></div>
      <div class="skeleton-block block"></div>
      <div class="skeleton-block block"></div>
    </div>

    <EmptyState
      v-else-if="!items.length"
      icon="cart"
      title="没有待结算的商品"
      description="先去购物车勾选商品，或从商品详情页点击「立即购买」"
      action-text="回到购物车"
      @action="router.push({ name: 'cart' })"
    />

    <template v-else>
      <!-- 收货地址 -->
      <section class="panel fade-up">
        <div class="panel-head">
          <h2>收货地址</h2>
          <button class="link-btn" type="button" @click="addressDialog = true">
            <el-icon :size="13"><Plus /></el-icon>
            新增地址
          </button>
        </div>
        <div class="address-list">
          <button
            v-for="item in addresses"
            :key="item.id"
            class="address-card"
            :class="{ active: selectedAddressId === item.id }"
            type="button"
            @click="userStore.setSelectedAddress(item.id)"
          >
            <div class="address-card__top">
              <strong>{{ item.receiver_name }}</strong>
              <span>{{ item.phone }}</span>
              <span v-if="item.is_default" class="default-tag">默认</span>
            </div>
            <p>{{ item.region }} {{ item.detail }}</p>
            <el-icon v-if="selectedAddressId === item.id" class="check" :size="18"><CircleCheckFilled /></el-icon>
          </button>
        </div>
      </section>

      <!-- 商品清单 -->
      <section class="panel fade-up">
        <div class="panel-head">
          <h2>商品清单</h2>
          <span class="hint">共 {{ items.length }} 种商品 · {{ sellerCount }} 个卖家</span>
        </div>
        <div class="items">
          <div v-for="item in items" :key="item.key" class="item">
            <img :src="imageOf(item)" :alt="item.product.title" @error="(event) => onImageError(event, item)" />
            <div class="item__body">
              <p class="item__title">{{ item.product.title }}</p>
              <p class="item__meta">
                <span>{{ item.product.condition }}</span>
                <span>交易地点：{{ item.product.location }}</span>
              </p>
              <div class="item__seller">
                <UserAvatar
                  :src="item.product.seller_avatar"
                  :name="item.product.seller_nickname"
                  :seed="item.product.seller_id"
                  :size="20"
                />
                <span>{{ item.product.seller_nickname }}</span>
              </div>
            </div>
            <div class="item__right">
              <span class="unit">¥{{ toAmount(item.product.price) }} × {{ item.quantity }}</span>
              <strong>¥{{ toAmount(item.subtotal) }}</strong>
            </div>
          </div>
        </div>
        <p v-if="sellerCount > 1" class="split-tip">
          <el-icon :size="14"><InfoFilled /></el-icon>
          当前商品来自 {{ sellerCount }} 个卖家，提交后将自动拆分为 {{ sellerCount }} 个订单。
        </p>
      </section>

      <!-- 订单信息 -->
      <section class="panel fade-up">
        <div class="panel-head"><h2>订单信息</h2></div>
        <div class="form">
          <label class="field">
            <span class="field__label">订单备注</span>
            <textarea
              v-model="remark"
              class="field__input"
              rows="3"
              maxlength="120"
              placeholder="例如：希望明天中午在校门口当面交易，谢谢！"
            ></textarea>
          </label>

          <div class="field">
            <span class="field__label">支付方式（演示环境为模拟支付）</span>
            <div class="pay-methods">
              <button
                v-for="method in PAY_METHODS"
                :key="method.value"
                class="pay-card"
                :class="{ active: payMethod === method.value }"
                type="button"
                @click="payMethod = method.value"
              >
                <el-icon :size="18"><component :is="method.icon" /></el-icon>
                <span>{{ method.label }}</span>
                <el-icon v-if="payMethod === method.value" class="check" :size="16"><CircleCheckFilled /></el-icon>
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- 金额与提交 -->
      <section class="submit-panel fade-up">
        <PriceSummary
          :total-quantity="items.reduce((sum, item) => sum + item.quantity, 0)"
          :total-amount="totalAmount"
          :pay-amount="payAmount"
          title="金额明细"
        />
        <div class="submit-bar">
          <div class="amount">
            应付金额：<strong>¥{{ toAmount(payAmount) }}</strong>
          </div>
          <button class="primary-btn" type="button" :disabled="submitting" @click="submitOrder">
            {{ submitting ? '提交中…' : '提交订单' }}
          </button>
        </div>
      </section>
    </template>

    <AddressForm v-model="addressDialog" :saving="savingAddress" @saved="saveAddress" />

    <el-dialog v-model="successVisible" title="订单创建成功" width="420px" align-center>
      <div class="success">
        <el-icon class="success__icon" :size="46"><CircleCheckFilled /></el-icon>
        <p class="success__title">已生成 {{ createdOrders.length }} 个订单</p>
        <p class="success__desc">
          收货人：{{ selectedAddress?.receiver_name }} · {{ selectedAddress?.region }}
        </p>
        <p class="success__amount">应付金额 <strong>¥{{ toAmount(payAmount) }}</strong></p>
        <p class="success__tip">请在 30 分钟内完成支付，逾期订单将自动取消。</p>
      </div>
      <template #footer>
        <el-button @click="payLater">稍后付款</el-button>
        <el-button type="primary" @click="payNow">立即模拟支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.checkout-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
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
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.ghost-btn:hover {
  color: var(--c-primary-dark);
  border-color: var(--c-primary-light);
  background: var(--c-primary-mist);
}

.loading-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 22px;
  background: #fff;
  border-radius: var(--radius-card);
}

.bar {
  height: 24px;
  width: 40%;
}

.block {
  height: 120px;
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
}

.panel-head h2 {
  font-size: 16px;
  font-weight: 700;
}

.hint {
  font-size: 12px;
  color: var(--c-text-muted);
}

.link-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--c-primary-dark);
  cursor: pointer;
}

.link-btn:hover {
  text-decoration: underline;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.address-card {
  position: relative;
  padding: 14px 16px;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s;
}

.address-card:hover {
  border-color: var(--c-primary-light);
}

.address-card.active {
  border-color: var(--c-primary);
  background: var(--c-primary-mist);
  box-shadow: 0 8px 20px rgba(13, 148, 136, 0.14);
}

.address-card__top {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.address-card__top strong {
  font-weight: 600;
}

.address-card__top span {
  font-size: 12px;
  color: var(--c-text-sub);
}

.default-tag {
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--c-primary-soft);
  color: var(--c-primary-dark) !important;
  font-size: 11px !important;
}

.address-card p {
  margin-top: 6px;
  font-size: 12px;
  color: var(--c-text-muted);
  line-height: 1.6;
}

.address-card .check {
  position: absolute;
  right: 12px;
  top: 12px;
  color: var(--c-primary);
}

.items {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.item {
  display: flex;
  gap: 14px;
  padding: 14px;
  border-radius: 14px;
  background: var(--c-surface-alt);
}

.item img {
  width: 84px;
  height: 84px;
  border-radius: 12px;
  object-fit: cover;
  flex-shrink: 0;
  background: #eef1f5;
}

.item__body {
  flex: 1;
  min-width: 0;
}

.item__title {
  font-size: 14px;
  color: var(--c-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item__meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 6px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.item__seller {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--c-text-sub);
}

.item__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 6px;
  flex-shrink: 0;
}

.unit {
  font-size: 12px;
  color: var(--c-text-muted);
}

.item__right strong {
  font-size: 16px;
  color: var(--c-accent-strong);
}

.split-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 14px;
  padding: 10px 14px;
  border-radius: 10px;
  background: #fffbeb;
  color: #b45309;
  font-size: 12px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field__label {
  font-size: 13px;
  color: var(--c-text-sub);
}

.field__input {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid var(--c-border);
  border-radius: 10px;
  font-family: inherit;
  font-size: 13px;
  color: var(--c-text);
  resize: vertical;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.field__input:focus {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.pay-methods {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.pay-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  border: 1px solid var(--c-border);
  border-radius: 12px;
  background: #fff;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.pay-card:hover {
  border-color: var(--c-primary-light);
}

.pay-card.active {
  border-color: var(--c-primary);
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
  font-weight: 500;
}

.pay-card .check {
  margin-left: auto;
  color: var(--c-primary);
}

.submit-panel {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  align-items: stretch;
}

.submit-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.amount {
  font-size: 14px;
  color: var(--c-text-sub);
}

.amount strong {
  font-size: 24px;
  color: var(--c-accent-strong);
  font-family: 'DIN Alternate', 'PingFang SC', sans-serif;
}

.primary-btn {
  padding: 13px 40px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #ffa06b, var(--c-accent));
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 12px 26px rgba(255, 122, 69, 0.3);
  transition: transform 0.2s;
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.success {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}

.success__icon {
  color: var(--c-success);
}

.success__title {
  font-size: 17px;
  font-weight: 700;
}

.success__desc {
  font-size: 12px;
  color: var(--c-text-muted);
}

.success__amount {
  margin-top: 6px;
  font-size: 13px;
  color: var(--c-text-sub);
}

.success__amount strong {
  font-size: 20px;
  color: var(--c-accent-strong);
}

.success__tip {
  font-size: 12px;
  color: var(--c-warning);
}

@media (max-width: 1023.98px) {
  .address-list,
  .pay-methods {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .submit-panel {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767.98px) {
  .panel {
    padding: 16px;
  }

  .address-list,
  .pay-methods {
    grid-template-columns: 1fr;
  }

  .item img {
    width: 68px;
    height: 68px;
  }

  .submit-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .primary-btn {
    width: 100%;
  }
}
</style>
