<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import StatusTag from '@/components/StatusTag.vue'
import { ORDER_TABS, PAY_METHODS } from '@/constants/order'
import { useCartStore } from '@/store/cart'
import { useOrderStore } from '@/store/order'
import { formatDateTime, toAmount } from '@/utils/format'
import { svgCover } from '@/utils/image'
// Mock 专属演示开关：仅本地 Mock 模式下展示「模拟卖家发货」入口
import { USE_MOCK } from '@/utils/request'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const cartStore = useCartStore()

const role = ref(route.query.role === 'seller' ? 'seller' : 'buyer')
const activeTab = ref(route.query.status !== undefined ? route.query.status : 'all')
const pageNum = ref(1)
const pageSize = 5
const payVisible = ref(false)
const payMethod = ref('campus_card')
const payingOrder = ref(null)

const records = computed(() => orderStore.records)
const counts = computed(() => orderStore.counts)
const loading = computed(() => orderStore.loading)

const tabs = computed(() =>
  ORDER_TABS.map((tab) => ({
    ...tab,
    count: tab.value === 'all' ? counts.value.all : counts.value[tab.value] || 0,
  })),
)

function onImageError(event, item) {
  event.target.src = svgCover(item.product_title, item.product_id)
}

async function load() {
  await orderStore.fetchOrders({
    pageNum: pageNum.value,
    pageSize,
    status: activeTab.value,
    role: role.value,
  })
}

function changeTab(value) {
  activeTab.value = value
  pageNum.value = 1
  load()
}

function changeRole(value) {
  role.value = value
  pageNum.value = 1
  load()
}

function changePage(page) {
  pageNum.value = page
  load()
}

function goDetail(order) {
  router.push({ name: 'order-detail', params: { id: order.id } })
}

function openPay(order) {
  payingOrder.value = order
  payMethod.value = 'campus_card'
  payVisible.value = true
}

async function confirmPay() {
  await orderStore.pay(payingOrder.value.id, payMethod.value)
  ElMessage.success('模拟支付成功，等待卖家发货')
  payVisible.value = false
  await load()
}

async function cancelOrder(order) {
  await ElMessageBox.confirm(`确认取消订单 ${order.order_no} 吗？取消后库存会自动恢复。`, '取消订单', {
    confirmButtonText: '确认取消',
    cancelButtonText: '再想想',
    type: 'warning',
  })
  await orderStore.cancel(order.id)
  ElMessage.success('订单已取消')
  await load()
}

async function confirmReceive(order) {
  await ElMessageBox.confirm('请确认已收到商品且验货无误，确认后交易完成。', '确认收货', {
    confirmButtonText: '确认收货',
    cancelButtonText: '还没收到',
    type: 'info',
  })
  await orderStore.confirm(order.id)
  ElMessage.success('交易完成，感谢你的信任')
  await load()
}

async function mockShip(order) {
  await orderStore.ship(order.id)
  ElMessage.success('已模拟卖家发货，订单进入待收货状态')
  await load()
}

async function buyAgain(order) {
  let added = 0
  for (const item of order.items) {
    const result = await cartStore.add(item.product_id, item.quantity).catch(() => null)
    if (result) added += 1
  }
  if (!added) {
    ElMessage.warning('商品已下架或售出，无法再次加入购物车')
    return
  }
  ElMessage.success(`已将 ${added} 件商品加入购物车`)
  router.push({ name: 'cart' })
}

onMounted(() => load())
</script>

<template>
  <div class="page order-page">
    <header class="page-head fade-up">
      <div>
        <h1>我的订单</h1>
        <p>跟踪每一笔交易的付款、发货与收货状态，及时处理待办事项</p>
      </div>
      <div class="role-switch">
        <button class="role-btn" :class="{ active: role === 'buyer' }" type="button" @click="changeRole('buyer')">
          我买到的
        </button>
        <button class="role-btn" :class="{ active: role === 'seller' }" type="button" @click="changeRole('seller')">
          我卖出的
        </button>
      </div>
    </header>

    <nav class="tabs fade-up">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="tab"
        :class="{ active: activeTab === tab.value }"
        type="button"
        @click="changeTab(tab.value)"
      >
        {{ tab.label }}
        <em v-if="tab.count">{{ tab.count }}</em>
      </button>
    </nav>

    <div v-if="loading" class="loading-list">
      <div v-for="index in 2" :key="index" class="skeleton-block big-card"></div>
    </div>

    <EmptyState
      v-else-if="!records.length"
      icon="order"
      title="暂无相关订单"
      description="下单后可以在这里查看订单状态，并完成付款、确认收货等操作"
      action-text="去逛逛商品"
      @action="router.push({ name: 'products' })"
    />

    <div v-else class="order-list">
      <article v-for="order in records" :key="order.id" class="order-card fade-up">
        <header class="order-card__head">
          <div class="head-left">
            <span class="order-no">订单号：{{ order.order_no }}</span>
            <span class="time">{{ formatDateTime(order.create_time) }}</span>
          </div>
          <StatusTag :status="order.status" kind="order" />
        </header>

        <div class="order-card__body" @click="goDetail(order)">
          <div class="thumbs">
            <img
              v-for="item in order.items.slice(0, 4)"
              :key="item.id"
              :src="item.product_cover"
              :alt="item.product_title"
              @error="(event) => onImageError(event, item)"
            />
            <span v-if="order.items.length > 4" class="more-count">+{{ order.items.length - 4 }}</span>
          </div>

          <div class="summary">
            <p class="title">{{ order.items[0]?.product_title }}</p>
            <p class="meta">
              <span>共 {{ order.item_count }} 件商品</span>
              <span>{{ role === 'seller' ? `买家：${order.buyer_nickname}` : `卖家：${order.seller_nickname}` }}</span>
            </p>
            <p v-if="order.remark" class="remark">备注：{{ order.remark }}</p>
          </div>

          <div class="amount">
            <span class="label">实付金额</span>
            <strong>¥{{ toAmount(order.pay_amount) }}</strong>
          </div>
        </div>

        <footer class="order-card__foot">
          <button class="ghost-btn" type="button" @click="goDetail(order)">查看详情</button>

          <template v-if="role === 'buyer'">
            <button
              v-if="order.status === 3 || order.status === 4"
              class="ghost-btn"
              type="button"
              @click="buyAgain(order)"
            >
              再次购买
            </button>
            <button
              v-if="order.status === 0 || order.status === 1"
              class="ghost-btn danger"
              type="button"
              @click="cancelOrder(order)"
            >
              取消订单
            </button>
            <button v-if="order.status === 1 && USE_MOCK" class="ghost-btn" type="button" @click="mockShip(order)">
              模拟卖家发货
            </button>
            <button v-if="order.status === 2" class="primary-btn" type="button" @click="confirmReceive(order)">
              确认收货
            </button>
            <button v-if="order.status === 0" class="primary-btn" type="button" @click="openPay(order)">
              立即付款
            </button>
          </template>
          <template v-else>
            <button v-if="order.status === 1 && USE_MOCK" class="primary-btn" type="button" @click="mockShip(order)">
              模拟发货
            </button>
          </template>
        </footer>
      </article>

      <div v-if="orderStore.total > pageSize" class="pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="orderStore.total"
          :page-size="pageSize"
          :current-page="pageNum"
          @current-change="changePage"
        />
      </div>
    </div>

    <el-dialog v-model="payVisible" title="模拟支付" width="440px" align-center>
      <div v-if="payingOrder" class="pay-body">
        <p class="pay-amount">应付金额 <strong>¥{{ toAmount(payingOrder.pay_amount) }}</strong></p>
        <p class="pay-order">订单号：{{ payingOrder.order_no }}</p>
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
        <p class="pay-tip">演示环境为模拟支付，不会产生任何真实扣款。</p>
      </div>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-page {
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

.role-switch {
  display: flex;
  padding: 4px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid var(--c-border);
}

.role-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 999px;
  background: transparent;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.role-btn.active {
  color: #fff;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
}

.tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 6px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  scrollbar-width: none;
}

.tabs::-webkit-scrollbar {
  display: none;
}

.tab {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  border: none;
  border-radius: 999px;
  background: transparent;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.tab:hover {
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
}

.tab.active {
  color: #fff;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  box-shadow: 0 8px 18px rgba(13, 148, 136, 0.24);
}

.tab em {
  font-style: normal;
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.28);
  font-size: 11px;
}

.tab:not(.active) em {
  background: var(--c-accent-soft);
  color: var(--c-accent-strong);
}

.loading-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.big-card {
  height: 180px;
  border-radius: var(--radius-card);
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  transition: box-shadow 0.24s;
}

.order-card:hover {
  box-shadow: var(--shadow-hover);
}

.order-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 20px;
  background: var(--c-surface-alt);
  flex-wrap: wrap;
}

.head-left {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.order-no {
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text);
}

.time {
  font-size: 12px;
  color: var(--c-text-muted);
}

.order-card__body {
  display: flex;
  gap: 18px;
  padding: 18px 20px;
  cursor: pointer;
  flex-wrap: wrap;
}

.thumbs {
  position: relative;
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.thumbs img {
  width: 68px;
  height: 68px;
  border-radius: 12px;
  object-fit: cover;
  background: #f3f6f9;
}

.more-count {
  position: absolute;
  right: -6px;
  bottom: -6px;
  padding: 1px 7px;
  border-radius: 999px;
  background: rgba(31, 41, 55, 0.82);
  color: #fff;
  font-size: 11px;
}

.summary {
  flex: 1;
  min-width: 180px;
}

.summary .title {
  font-size: 14px;
  color: var(--c-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.summary .meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 8px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.summary .remark {
  margin-top: 8px;
  font-size: 12px;
  color: var(--c-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 4px;
  flex-shrink: 0;
}

.amount .label {
  font-size: 12px;
  color: var(--c-text-muted);
}

.amount strong {
  font-size: 20px;
  color: var(--c-accent-strong);
  font-family: 'DIN Alternate', 'PingFang SC', sans-serif;
}

.order-card__foot {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid var(--c-border);
  flex-wrap: wrap;
}

.ghost-btn {
  padding: 8px 18px;
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
  padding: 8px 22px;
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

.pager {
  display: flex;
  justify-content: center;
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

.pay-card:hover {
  border-color: var(--c-primary-light);
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

@media (max-width: 767.98px) {
  .page-head h1 {
    font-size: 19px;
  }

  .order-card__body {
    padding: 14px;
    gap: 12px;
  }

  .thumbs img {
    width: 56px;
    height: 56px;
  }

  .order-card__foot {
    justify-content: stretch;
  }

  .ghost-btn,
  .primary-btn {
    flex: 1;
    min-width: 96px;
  }
}
</style>
