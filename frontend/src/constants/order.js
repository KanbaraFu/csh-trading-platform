// 订单领域常量：状态映射、筛选 Tab、状态流转步骤

// 订单状态：0 待支付 / 1 已支付 / 2 已发货 / 3 已完成 / 4 已取消
export const ORDER_STATUS = {
  0: { label: '待支付', type: 'warning', desc: '等待买家付款，超时订单将自动取消' },
  1: { label: '待发货', type: 'primary', desc: '买家已付款，等待卖家发货' },
  2: { label: '待收货', type: 'info', desc: '卖家已发货，等待买家确认收货' },
  3: { label: '已完成', type: 'success', desc: '交易已完成，欢迎评价本次交易' },
  4: { label: '已取消', type: 'info', desc: '订单已取消，交易关闭' },
}

export function orderStatusLabel(status) {
  return ORDER_STATUS[status]?.label ?? '未知'
}

export function orderStatusType(status) {
  return ORDER_STATUS[status]?.type ?? 'info'
}

export function orderStatusDesc(status) {
  return ORDER_STATUS[status]?.desc ?? ''
}

// 订单列表状态筛选 Tab
export const ORDER_TABS = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
]

// 订单进度条步骤
export const ORDER_FLOW_STEPS = [
  { title: '提交订单', desc: '订单创建成功' },
  { title: '买家付款', desc: '支付完成' },
  { title: '卖家发货', desc: '商品已交付' },
  { title: '确认收货', desc: '买家确认' },
  { title: '交易完成', desc: '订单归档' },
]

// 状态 -> 进度条当前步（-1 表示已取消，不展示正常流程）
export function orderFlowStep(status) {
  const map = { 0: 1, 1: 2, 2: 3, 3: 5, 4: -1 }
  return map[status] ?? 0
}

// 支付方式
export const PAY_METHODS = [
  { label: '校园一卡通', value: 'campus_card', icon: 'CreditCard' },
  { label: '微信支付', value: 'wechat', icon: 'ChatDotRound' },
  { label: '支付宝', value: 'alipay', icon: 'Wallet' },
]
