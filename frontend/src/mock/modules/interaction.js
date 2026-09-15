// 互动与消息模块 Mock：商品评论 / 回复 / 站内通知 / 未读数 / 已读
import { db, nextId } from '../index'
import { decorateComment, decorateProduct, fail, nowText, paginate, pushMessage, requireLogin } from '../helpers'
import { getProducts } from './product'

/**
 * 商品评论分页：只对顶层评论分页，total 为顶层评论数，
 * 当前页每条顶层评论的回复全部带上，避免回复被拆到其他页
 */
export function getComments({ route, query }) {
  const productId = Number(route.id)
  const all = db()
    .comments.filter((item) => item.product_id === productId)
    .sort((a, b) => (a.create_time < b.create_time ? -1 : 1))
    .map(decorateComment)

  const roots = all.filter((item) => !item.parent_id)
  const paged = paginate(roots, query.pageNum || 1, query.pageSize || 10)

  const records = []
  paged.records.forEach((root) => {
    records.push(root)
    all.filter((item) => Number(item.parent_id) === root.id).forEach((reply) => records.push(reply))
  })

  return { records, total: roots.length, pageNum: paged.pageNum, pageSize: paged.pageSize }
}

export function addComment({ body }) {
  const user = requireLogin()
  const productId = Number(body.productId)
  const content = String(body.content || '').trim()
  if (!content) fail('评论内容不能为空')
  if (content.length > 500) fail('评论内容不能超过 500 字')
  const product = db().products.find((item) => item.id === productId)
  if (!product) fail('商品不存在')

  const parentId = Number(body.parentId || 0)
  let replyUserId = 0
  if (parentId) {
    const parent = db().comments.find((item) => item.id === parentId)
    if (parent) replyUserId = parent.user_id
  }

  const id = nextId('comment')
  const comment = {
    id,
    product_id: productId,
    user_id: user.id,
    content,
    parent_id: parentId,
    reply_user_id: replyUserId || null,
    create_time: nowText(),
  }
  db().comments.push(comment)

  if (replyUserId && replyUserId !== user.id) {
    pushMessage(replyUserId, 'comment', `${user.nickname} 回复了你的评论`, content.slice(0, 60), productId)
  } else if (product.seller_id !== user.id) {
    pushMessage(product.seller_id, 'comment', `${user.nickname} 评论了你的商品`, `「${product.title}」：${content.slice(0, 60)}`, productId)
  }
  return decorateComment(comment)
}

export function deleteComment({ route }) {
  const user = requireLogin()
  const list = db().comments
  const index = list.findIndex((item) => item.id === Number(route.id) && item.user_id === user.id)
  if (index < 0) fail('只能删除自己发表的评论')
  list.splice(index, 1)
  return { id: Number(route.id) }
}

// ---------------------------------------------------------------- 消息
export function getMessages({ query }) {
  const user = requireLogin()
  const type = query.type && query.type !== 'all' ? query.type : null
  let list = db().messages.filter((item) => item.user_id === user.id)
  if (type) list = list.filter((item) => item.type === type)
  list = list.sort((a, b) => (a.create_time < b.create_time ? 1 : -1))

  const all = db().messages.filter((item) => item.user_id === user.id)
  const counts = {
    all: all.length,
    unread: all.filter((item) => !item.is_read).length,
    system: all.filter((item) => item.type === 'system').length,
    trade: all.filter((item) => item.type === 'trade').length,
    comment: all.filter((item) => item.type === 'comment').length,
  }
  const paged = paginate(list, query.pageNum || 1, query.pageSize || 20)
  return {
    records: paged.records.map((item) => {
      const record = { ...item }
      if (record.type === 'comment' && record.biz_id) {
        const product = db().products.find((row) => row.id === record.biz_id)
        record.product_title = product?.title ?? ''
        record.product_cover = product?.cover ?? ''
      }
      return record
    }),
    total: paged.total,
    pageNum: paged.pageNum,
    pageSize: paged.pageSize,
    counts,
  }
}

export function getUnreadCount() {
  const user = requireLogin()
  const messages = db().messages.filter((item) => item.user_id === user.id && !item.is_read)
  return { count: messages.length }
}

export function readMessage({ route }) {
  const user = requireLogin()
  const message = db().messages.find((item) => item.id === Number(route.id) && item.user_id === user.id)
  if (!message) fail('消息不存在')
  message.is_read = 1
  return message
}

export function readAllMessages({ body }) {
  const user = requireLogin()
  const type = body.type && body.type !== 'all' ? body.type : null
  db()
    .messages.filter((item) => item.user_id === user.id && (!type || item.type === type))
    .forEach((item) => { item.is_read = 1 })
  return getUnreadCount()
}

export function createMessage({ body }) {
  const target = Number(body.user_id || 0)
  if (!target) fail('缺少消息接收人')
  pushMessage(target, body.type || 'system', body.title || '系统通知', body.content || '', body.biz_id || 0)
  return { success: true }
}

// ---------------------------------------------------------------- 搜索与统计
export function search({ query }) {
  const keyword = String(query.keyword || '').trim()
  if (keyword) {
    const hot = db().hotWords.find((item) => item.word === keyword)
    if (hot) hot.score += 1
    else db().hotWords.push({ word: keyword, score: 1 })
  }
  const productResult = getProducts({ query: { ...query, keyword } })
  return { keyword, ...productResult }
}

export function getHotWords() {
  const list = db().hotWords.slice().sort((a, b) => b.score - a.score).slice(0, 12)
  const max = list[0]?.score || 1
  return list.map((item, index) => ({
    ...item,
    rank: index + 1,
    percent: Math.round((item.score / max) * 100),
    is_hot: index < 3,
  }))
}

export function recordView({ route }) {
  const product = db().products.find((item) => item.id === Number(route.productId))
  if (!product) fail('商品不存在')
  product.view_count += 1
  return { product_id: product.id, view_count: product.view_count }
}

export function getHotProducts({ query }) {
  const list = db()
    .products.filter((item) => item.status !== 0)
    .sort((a, b) => b.view_count - a.view_count)
    .slice(0, Number(query.limit || 8))
  const max = list[0]?.view_count || 1
  return list.map((item, index) => ({
    ...decorateProduct(item),
    rank: index + 1,
    hot_percent: Math.round((item.view_count / max) * 100),
  }))
}

export function getSales({ route }) {
  const product = db().products.find((item) => item.id === Number(route.productId))
  if (!product) fail('商品不存在')
  return { product_id: product.id, sales_count: product.sales_count, view_count: product.view_count }
}

export function getOverview() {
  const data = db()
  const today = new Date().toISOString().slice(0, 10)
  return {
    product_total: data.products.length,
    on_sale_total: data.products.filter((item) => item.status === 1).length,
    user_total: data.users.length,
    order_total: data.orders.length,
    favorite_total: data.favorites.length,
    today_new_product: data.products.filter((item) => String(item.create_time).startsWith(today)).length + 6,
    trade_amount: Number(data.orders.filter((item) => item.status !== 4).reduce((sum, item) => sum + item.pay_amount, 0).toFixed(2)),
    hot_keyword_total: data.hotWords.length,
  }
}
