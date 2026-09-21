<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PriceSummary from '@/components/PriceSummary.vue'
import StatusTag from '@/components/StatusTag.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { ORDER_FLOW_STEPS, PAY_METHODS, orderFlowStep, orderStatusDesc, orderStatusLabel, orderStatusType } from '@/constants/order'
import { useOrderStore } from '@/store/order'
import { useUserStore } from '@/store/user'
import { formatDateTime, orderAddressText, toAmount } from '@/utils/format'
import { svgCover } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()

const loading = ref(true)
const payVisible = ref(false)
const payMethod = ref('campus_card')

const order = computed(() => orderStore.current)
const flowStep = computed(() => (order.value ? orderFlowStep(order.value.status) : 0))
const activeStep = computed(() => Math.max(0, flowStep.value - 1))
// 发货是卖家动作：后端 shipOrder 会校验 order.seller_id == 当前用户，前端按同一规则控制按钮
const isSeller = computed(() => Boolean(order.value) && Number(order.value.seller_id) === Number(userStore.user?.id))
// 确认收货是买家动作
const isBuyer = computed(() => Boolean(order.value) && Number(order.value.buyer_id) === Number(userStore.user?.id))

function payMethodLabel(value) {
  return PAY_METHODS.find((item) => item.value === value)?.label || '—'
}

function onImageError(event, item) {
  event.target.src = svgCover(item.product_title, item.product_id)
}

async function load() {
  loading.value = true
  try {
    await orderStore.fetchDetail(route.params.id)
    payMethod.value = order.value?.pay_method || 'campus_card'
  } finally {
    loading.value = false
  }
}

function openPay() {
  payVisible.value = true
}

async function confirmPay() {
  await orderStore.pay(order.value.id, payMethod.value)
  ElMessage.success('支付成功，等待卖家发货')
  payVisible.value = false
  await load()
}

async function cancelOrder() {
  await ElMessageBox.confirm('确认取消该订单吗？取消后商品库存会自动恢复。', '取消订单', {
    confirmButtonText: '确认取消',
    cancelButtonText: '再想想',
    type: 'warning',
  })
  await orderStore.cancel(order.value.id)
  ElMessage.success('订单已取消')
  await load()
}

async function confirmReceive() {
  await ElMessageBox.confirm('请确认已收到商品且验货无误，确认后交易完成。', '确认收货', {
    confirmButtonText: '确认收货',
    cancelButtonText: '还没收到',
    type: 'info',
  })
  await orderStore.confirm(order.value.id)
  ElMessage.success('交易完成，感谢你的信任')
  await load()
}

async function shipOrder() {
  await orderStore.ship(order.value.id)
  ElMessage.success('已发货，等待买家确认收货')
  await load()
}

function goComment(item) {
  router.push({ name: 'product-detail', params: { id: item.product_id } })
}

onMounted(load)
</script>

<template>
  <div class="page detail-page">
    <header class="page-head fade-up">
      <button class="back-btn" type="button" @click="router.push({ name: 'orders' })">
        <el-icon :size="14"><ArrowLeft /></el-icon>
        返回订单列表
      </button>
      <span v-if="order" class="order-no">订单号 {{ order.order_no }}</span>
    </header>

    <div v-if="loading" class="loading-card">
      <div class="skeleton-block bar"></div>
      <div class="skeleton-block block"></div>
    </div>

    <template v-else-if="order">
      <!-- 状态卡 -->
      <section class="status-panel fade-up">
        <div class="status-head">
          <div>
            <div class="status-title">
              <el-tag :type="orderStatusType(order.status)" effect="dark" round size="large">
                {{ orderStatusLabel(order.status) }}
              </el-tag>
              <h1>{{ orderStatusDesc(order.status) }}</h1>
            </div>
            <p class="status-meta">
              <span>下单时间：{{ formatDateTime(order.create_time) }}</span>
              <span v-if="order.pay_time">支付时间：{{ formatDateTime(order.pay_time) }}</span>
            </p>
          </div>

          <div class="status-actions">
            <button v-if="order.status === 0 && isBuyer" class="ghost-btn danger" type="button" @click="cancelOrder">取消订单</button>
            <button v-if="order.status === 1 && isBuyer" class="ghost-btn danger" type="button" @click="cancelOrder">取消订单</button>
            <button v-if="order.status === 1 && isSeller" class="primary-btn" type="button" @click="shipOrder">立即发货</button>
            <button v-if="order.status === 2 && isBuyer" class="primary-btn" type="button" @click="confirmReceive">确认收货</button>
            <button v-if="order.status === 0 && isBuyer" class="primary-btn" type="button" @click="openPay">立即支付</button>
          </div>
        </div>

        <div v-if="order.status === 4" class="cancel-tip">
          <el-icon :size="16"><CircleCloseFilled /></el-icon>
          订单已取消，商品库存已恢复。如有疑问可重新下单或联系卖家。
        </div>
        <el-steps v-else class="steps" :active="activeStep" align-center finish-status="success">
          <el-step v-for="step in ORDER_FLOW_STEPS" :key="step.title" :title="step.title" :description="step.desc" />
        </el-steps>
      </section>

      <!-- 商品清单 -->
      <section class="panel fade-up">
        <div class="panel-head">
          <h2>商品清单</h2>
          <StatusTag :status="order.status" kind="order" size="small" />
        </div>
        <div class="items">
          <div v-for="item in order.items" :key="item.id" class="item">
            <img :src="item.product_cover" :alt="item.product_title" @error="(event) => onImageError(event, item)" />
            <div class="item__body">
              <p class="item__title">{{ item.product_title }}</p>
              <p class="item__meta">单价 ¥{{ toAmount(item.price) }} × {{ item.quantity }}</p>
            </div>
            <div class="item__right">
              <strong>¥{{ toAmount(item.total_amount) }}</strong>
              <button v-if="order.status === 3" class="link-btn" type="button" @click="goComment(item)">去评价</button>
            </div>
          </div>
        </div>
      </section>

      <div class="two-col fade-up">
        <section class="panel">
          <div class="panel-head"><h2>收货信息</h2></div>
          <div class="address">
            <el-icon :size="16"><LocationFilled /></el-icon>
            <p>{{ orderAddressText(order) }}</p>
          </div>
          <ul class="meta-list">
            <li><span>订单备注</span><span>{{ order.remark || '无' }}</span></li>
            <li><span>支付方式</span><span>{{ order.pay_method ? payMethodLabel(order.pay_method) : '尚未支付' }}</span></li>
            <li>
              <span>交易对象</span>
              <span class="seller-line">
                <UserAvatar
                  :src="order.seller_avatar"
                  :name="order.seller_nickname"
                  :seed="order.seller_id"
                  :size="20"
                />
                {{ order.seller_nickname }}
              </span>
            </li>
          </ul>
        </section>

        <PriceSummary
          :total-quantity="order.item_count"
          :total-amount="order.total_amount"
          :discount-amount="Number((order.total_amount - order.pay_amount).toFixed(2))"
          :pay-amount="order.pay_amount"
          title="金额明细"
        />
      </div>
    </template>

    <el-dialog v-model="payVisible" title="确认支付" width="440px" align-center>
      <div v-if="order" class="pay-body">
        <p class="pay-amount">应付金额 <strong>¥{{ toAmount(order.pay_amount) }}</strong></p>
        <p class="pay-order">订单号：{{ order.order_no }}</p>
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
          </button>
        </div>
        <p class="pay-tip">不会产生任何真实扣款，请放心操作。</p>
      </div>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  color: var(--c-primary-dark);
  border-color: var(--c-primary-light);
  background: var(--c-primary-mist);
}

.order-no {
  font-size: 12px;
  color: var(--c-text-muted);
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
  height: 140px;
}

.status-panel,
.panel {
  padding: 22px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.status-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.status-title {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.status-title h1 {
  font-size: 17px;
  font-weight: 600;
}

.status-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 10px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.status-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.cancel-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
  padding: 12px 16px;
  border-radius: 12px;
  background: #f8fafc;
  color: var(--c-text-sub);
  font-size: 13px;
}

.steps {
  margin-top: 26px;
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

.items {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border-radius: 14px;
  background: var(--c-surface-alt);
}

.item img {
  width: 76px;
  height: 76px;
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item__meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.item__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.item__right strong {
  font-size: 16px;
  color: var(--c-accent-strong);
}

.link-btn {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 12px;
  color: var(--c-primary-dark);
  cursor: pointer;
}

.link-btn:hover {
  text-decoration: underline;
}

.two-col {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 18px;
}

.address {
  display: flex;
  gap: 10px;
  padding: 14px;
  border-radius: 12px;
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
  font-size: 13px;
  line-height: 1.7;
}

/* 地址快照是一整串文本，保证长地址能正常换行且图标不被压缩 */
.address .el-icon {
  flex-shrink: 0;
  margin-top: 3px;
}

.address p {
  flex: 1;
  min-width: 0;
  word-break: break-word;
}

.meta-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13px;
  color: var(--c-text-sub);
}

.meta-list li {
  display: flex;
  gap: 12px;
}

.meta-list li > span:first-child {
  width: 74px;
  flex-shrink: 0;
  color: var(--c-text-muted);
}

.seller-line {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.ghost-btn {
  padding: 9px 20px;
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

.ghost-btn.danger:hover {
  color: var(--c-danger);
  border-color: #fecaca;
  background: #fef2f2;
}

.primary-btn {
  padding: 9px 24px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #ffa06b, var(--c-accent));
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(255, 122, 69, 0.28);
  transition: transform 0.2s;
}

.primary-btn:hover {
  transform: translateY(-1px);
}

.pay-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pay-amount {
  font-size: 14px;
  color: var(--c-text-sub);
}

.pay-amount strong {
  font-size: 24px;
  color: var(--c-accent-strong);
}

.pay-order {
  font-size: 12px;
  color: var(--c-text-muted);
}

.pay-methods {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 6px;
}

.pay-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border: 1px solid var(--c-border);
  border-radius: 12px;
  background: #fff;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.pay-card.active {
  border-color: var(--c-primary);
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
  font-weight: 500;
}

.pay-tip {
  margin-top: 4px;
  font-size: 12px;
  color: var(--c-text-muted);
}

@media (max-width: 1023.98px) {
  .two-col {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767.98px) {
  .status-panel,
  .panel {
    padding: 16px;
  }

  .status-title h1 {
    font-size: 15px;
  }

  .steps :deep(.el-step__description) {
    display: none;
  }

  .item img {
    width: 62px;
    height: 62px;
  }
}
</style>
