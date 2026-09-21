// 订单与交易模块 Mock：创建订单 / 模拟支付 / 发货 / 确认收货 / 取消 / 列表 / 详情
import { db, nextId } from '../index'
import { decorateOrder, fail, nowText, paginate, pushMessage, requireLogin } from '../helpers'
// 模拟 Redis 键 order:lock:user:{userId}:product:{productId}，10 秒内防重复下单
const orderLock = new Map()

function buildOrderNo() {
  const date = new Date()
  const pad = (num, len = 2) => String(num).padStart(len, '0')
  return `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}${pad(date.getHours())}${pad(date.getMinutes())}${pad(Math.floor(Math.random() * 10000), 4)}`
}

function addressText(address) {
  return `${address.receiver_name} ${address.phone} ${address.region} ${address.detail}`
}

export function createOrder({ body }) {
  const user = requireLogin()
  const lines = Array.isArray(body.items) ? body.items : []
  if (!lines.length) fail('订单中没有商品，请先选择要购买的商品')

  const address = db().addresses.find((item) => item.id === Number(body.addressId) && item.user_id === user.id)
  if (!address) fail('请先选择有效的收货地址')

  const prepared = []
  lines.forEach((line) => {
    const product = db().products.find((item) => item.id === Number(line.productId))
    if (!product) fail('商品不存在或已被删除')
    if (product.status !== 1 || product.stock <= 0) fail(`「${product.title}」已下架或已售出`)
    if (product.seller_id === user.id) fail('不能购买自己发布的商品')
    const quantity = Math.max(1, Number(line.quantity) || 1)
    if (product.stock < quantity) fail(`「${product.title}」库存不足，仅剩 ${product.stock} 件`)
    const lockKey = `${user.id}:${product.id}`
    const lastTime = orderLock.get(lockKey) || 0
    if (Date.now() - lastTime < 10000) fail('请勿重复提交订单，请稍后再试')
    orderLock.set(lockKey, Date.now())
    prepared.push({ product, quantity })
  })

  // 按卖家分组：一次结算可包含多个卖家的商品，自动拆分为多个订单
  const groups = new Map()
  prepared.forEach((item) => {
    const sellerId = item.product.seller_id
    if (!groups.has(sellerId)) groups.set(sellerId, [])
    groups.get(sellerId).push(item)
  })

  // 扣减库存（对应 UPDATE product SET stock = stock - n WHERE stock >= n）
  prepared.forEach(({ product, quantity }) => {
    product.stock -= quantity
    product.sales_count += quantity
    if (product.stock <= 0) product.status = 2
    product.update_time = nowText()
  })

  const createdOrders = []
  groups.forEach((groupItems, sellerId) => {
    const totalAmount = Number(groupItems.reduce((sum, item) => sum + item.product.price * item.quantity, 0).toFixed(2))
    const orderId = nextId('order')
    const order = {
      id: orderId,
      order_no: buildOrderNo(),
      buyer_id: user.id,
      seller_id: sellerId,
      total_amount: totalAmount,
      pay_amount: totalAmount,
      status: 0,
      address_snapshot: addressText(address),
      remark: body.remark || '',
      pay_method: null,
      pay_time: null,
      create_time: nowText(),
      update_time: nowText(),
    }
    db().orders.unshift(order)

    groupItems.forEach(({ product, quantity }, index) => {
      db().orderItems.push({
        id: orderId * 10 + index + 1,
        order_id: orderId,
        product_id: product.id,
        product_title: product.title,
        product_cover: product.cover,
        price: product.price,
        quantity,
        total_amount: Number((product.price * quantity).toFixed(2)),
      })
    })

    pushMessage(user.id, 'trade', '订单创建成功', `订单 ${order.order_no} 已创建，请在 30 分钟内完成支付。`, orderId)
    pushMessage(sellerId, 'trade', '有新的订单待发货', `买家 ${user.nickname} 拍下了你的商品，等待付款。`, orderId)
    createdOrders.push(order)
  })

  // 下单成功后清理勾选的购物车项
  const cartIds = (body.cartIds || []).map((id) => Number(id))
  if (cartIds.length) {
    const carts = db().carts
    for (let i = carts.length - 1; i >= 0; i -= 1) {
      if (cartIds.includes(carts[i].id) && carts[i].user_id === user.id) carts.splice(i, 1)
    }
  }

  // 返回按卖家拆分后的订单数组
  return createdOrders.map(decorateOrder)
}

function findOwnOrder(id) {
  const user = requireLogin()
  const order = db().orders.find((item) => item.id === Number(id))
  if (!order) fail('订单不存在')
  if (order.buyer_id !== user.id && order.seller_id !== user.id) fail('无权查看该订单')
  return { user, order }
}

export function payOrder({ route, body }) {
  const { user, order } = findOwnOrder(route.id)
  if (order.status !== 0) fail('当前订单状态不支持支付')
  if (order.buyer_id !== user.id) fail('只有买家可以支付该订单')
  order.status = 1
  order.pay_method = body.payMethod || 'campus_card'
  order.pay_time = nowText()
  order.update_time = nowText()
  pushMessage(user.id, 'trade', '支付成功', `订单 ${order.order_no} 支付成功，等待卖家发货。`, order.id)
  pushMessage(order.seller_id, 'trade', '买家已付款', `订单 ${order.order_no} 买家已付款，请尽快安排交付。`, order.id)
  return decorateOrder(order)
}

export function shipOrder({ route }) {
  const { order } = findOwnOrder(route.id)
  if (order.status !== 1) fail('只有已付款的订单可以发货')
  order.status = 2
  order.update_time = nowText()
  pushMessage(order.buyer_id, 'trade', '卖家已发货', `订单 ${order.order_no} 卖家已交付商品，请及时确认收货。`, order.id)
  return decorateOrder(order)
}

export function confirmOrder({ route }) {
  const { user, order } = findOwnOrder(route.id)
  if (order.status !== 2) fail('只有待收货的订单可以确认收货')
  if (order.buyer_id !== user.id) fail('只有买家可以确认收货')
  order.status = 3
  order.update_time = nowText()
  pushMessage(user.id, 'trade', '交易完成', `订单 ${order.order_no} 已完成交易，欢迎评价本次购物体验。`, order.id)
  pushMessage(order.seller_id, 'trade', '交易已完成', `订单 ${order.order_no} 买家已确认收货，货款将结算到你的账户。`, order.id)
  return decorateOrder(order)
}

export function cancelOrder({ route }) {
  const { user, order } = findOwnOrder(route.id)
  if (![0, 1].includes(order.status)) fail('订单已进入发货流程，无法取消')
  if (order.buyer_id !== user.id) fail('只有买家可以取消该订单')
  order.status = 4
  order.update_time = nowText()
  // 回滚库存
  db().orderItems.filter((item) => item.order_id === order.id).forEach((item) => {
    const product = db().products.find((row) => row.id === item.product_id)
    if (!product) return
    product.stock += item.quantity
    product.sales_count = Math.max(0, product.sales_count - item.quantity)
    if (product.status === 2 && product.stock > 0) product.status = 1
  })
  pushMessage(user.id, 'trade', '订单已取消', `订单 ${order.order_no} 已取消，商品库存已恢复。`, order.id)
  return decorateOrder(order)
}

export function getOrders({ query }) {
  const user = requireLogin()
  const role = query.role === 'seller' ? 'seller' : 'buyer'
  const status = query.status
  let list = db().orders.filter((item) => (role === 'seller' ? item.seller_id === user.id : item.buyer_id === user.id))
  if (status !== undefined && status !== '' && status !== 'all') {
    list = list.filter((item) => item.status === Number(status))
  }
  list = list.sort((a, b) => (a.create_time < b.create_time ? 1 : -1))

  const all = db().orders.filter((item) => (role === 'seller' ? item.seller_id === user.id : item.buyer_id === user.id))
  const counts = { all: all.length, 0: 0, 1: 0, 2: 0, 3: 0, 4: 0 }
  all.forEach((item) => { counts[item.status] = (counts[item.status] || 0) + 1 })

  const paged = paginate(list, query.pageNum || 1, query.pageSize || 5)
  return { ...paged, records: paged.records.map(decorateOrder), counts }
}

export function getOrderDetail({ route }) {
  const { order } = findOwnOrder(route.id)
  const data = db()
  const address = order.address_snapshot
  return { ...decorateOrder(order), address_text: address }
}
