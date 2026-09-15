// Mock 公共辅助：统一返回、鉴权、分页、数据装饰
import { db, getCurrentUser } from './index'

export function ok(data) {
  return { code: 200, message: 'success', data }
}

export function fail(message, code = 500) {
  const error = new Error(message)
  error.mockCode = code
  throw error
}

export function requireLogin() {
  const user = getCurrentUser()
  if (!user) fail('登录状态已失效，请重新登录', 401)
  return user
}

export function stripPassword(user) {
  if (!user) return null
  const { password, ...rest } = user
  return rest
}

export function paginate(list, pageNum = 1, pageSize = 10) {
  const page = Math.max(1, Number(pageNum) || 1)
  const size = Math.max(1, Number(pageSize) || 10)
  const start = (page - 1) * size
  return {
    records: list.slice(start, start + size),
    total: list.length,
    pageNum: page,
    pageSize: size,
  }
}

/**
 * 商品数据装饰：补齐卖家与分类信息，前端无需再做关联查询
 */
export function decorateProduct(product) {
  if (!product) return null
  const data = db()
  const seller = data.users.find((user) => user.id === product.seller_id)
  const category = data.categories.find((item) => item.id === product.category_id)
  const images = data.productImages
    .filter((image) => image.product_id === product.id)
    .sort((a, b) => a.sort - b.sort)
    .map((image) => image.url)
  return {
    ...product,
    images: images.length ? images : [product.cover],
    seller_nickname: seller?.nickname ?? '已注销用户',
    seller_avatar: seller?.avatar ?? '',
    seller_college: seller?.college ?? '',
    category_name: category?.name ?? '未分类',
    is_available: product.status === 1 && product.stock > 0,
  }
}

export function decorateComment(comment) {
  const data = db()
  const user = data.users.find((item) => item.id === comment.user_id)
  const replyUser = comment.reply_user_id
    ? data.users.find((item) => item.id === comment.reply_user_id)
    : null
  return {
    ...comment,
    nickname: user?.nickname ?? '匿名同学',
    avatar: user?.avatar ?? '',
    reply_nickname: replyUser?.nickname ?? '',
  }
}

export function decorateOrder(order) {
  const data = db()
  const buyer = data.users.find((user) => user.id === order.buyer_id)
  const seller = data.users.find((user) => user.id === order.seller_id)
  const items = data.orderItems.filter((item) => item.order_id === order.id)
  return {
    ...order,
    buyer_nickname: buyer?.nickname ?? '',
    buyer_avatar: buyer?.avatar ?? '',
    seller_nickname: seller?.nickname ?? '',
    seller_avatar: seller?.avatar ?? '',
    items,
    item_count: items.reduce((sum, item) => sum + item.quantity, 0),
  }
}

export function nowText() {
  const date = new Date()
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/**
 * 站内通知（模拟 /api/messages 的内部调用）
 */
export function pushMessage(userId, type, title, content, bizId = 0) {
  const data = db()
  const id = data.nextIds.message
  data.nextIds.message = id + 1
  data.messages.unshift({
    id,
    user_id: userId,
    type,
    title,
    content,
    biz_id: bizId,
    is_read: 0,
    create_time: nowText(),
  })
}
