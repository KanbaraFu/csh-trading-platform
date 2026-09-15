// 收藏与购物车模块 Mock：加购 / 改数量 / 删除 / 结算预览
import { db, nextId } from '../index'
import { decorateProduct, fail, nowText, requireLogin } from '../helpers'

function buildCartItem(row) {
  const product = db().products.find((item) => item.id === row.product_id)
  return {
    ...row,
    selected: row.selected === 1,
    product: decorateProduct(product),
    subtotal: Number(((product?.price ?? 0) * row.quantity).toFixed(2)),
  }
}

export function getCart() {
  const user = requireLogin()
  const records = db()
    .carts.filter((item) => item.user_id === user.id)
    .sort((a, b) => (a.create_time < b.create_time ? 1 : -1))
    .map(buildCartItem)
  const selected = records.filter((item) => item.selected)
  return {
    records,
    total_quantity: records.reduce((sum, item) => sum + item.quantity, 0),
    selected_quantity: selected.reduce((sum, item) => sum + item.quantity, 0),
    selected_amount: Number(selected.reduce((sum, item) => sum + item.subtotal, 0).toFixed(2)),
    all_selected: records.length > 0 && selected.length === records.length,
  }
}

export function addCart({ body }) {
  const user = requireLogin()
  const productId = Number(body.productId)
  const quantity = Math.max(1, Number(body.quantity) || 1)
  const product = db().products.find((item) => item.id === productId)
  if (!product) fail('商品不存在')
  if (product.seller_id === user.id) fail('不能购买自己发布的商品')
  if (product.status !== 1 || product.stock <= 0) fail('该商品已下架或已售出')

  const exists = db().carts.find((item) => item.user_id === user.id && item.product_id === productId)
  if (exists) {
    const next = Math.min(exists.quantity + quantity, product.stock)
    exists.quantity = next
    exists.selected = 1
    exists.update_time = nowText()
    return buildCartItem(exists)
  }
  const row = {
    id: nextId('cart'),
    user_id: user.id,
    product_id: productId,
    quantity: Math.min(quantity, product.stock),
    selected: 1,
    create_time: nowText(),
    update_time: nowText(),
  }
  db().carts.push(row)
  return buildCartItem(row)
}

export function updateCart({ route, body }) {
  const user = requireLogin()
  const row = db().carts.find((item) => item.id === Number(route.id) && item.user_id === user.id)
  if (!row) fail('购物车项不存在')
  const product = db().products.find((item) => item.id === row.product_id)
  if (body.quantity !== undefined) {
    const quantity = Number(body.quantity)
    if (quantity < 1) fail('数量不能小于 1')
    if (product && quantity > product.stock) fail(`库存仅剩 ${product.stock} 件`)
    row.quantity = quantity
  }
  if (body.selected !== undefined) row.selected = body.selected ? 1 : 0
  row.update_time = nowText()
  return buildCartItem(row)
}

export function selectAllCart({ body }) {
  const user = requireLogin()
  const selected = body.selected ? 1 : 0
  db().carts.filter((item) => item.user_id === user.id).forEach((item) => { item.selected = selected })
  return getCart()
}

export function removeCart({ route }) {
  const user = requireLogin()
  const list = db().carts
  const index = list.findIndex((item) => item.id === Number(route.id) && item.user_id === user.id)
  if (index < 0) fail('购物车项不存在')
  list.splice(index, 1)
  return getCart()
}

/**
 * 结算预览：不传 ids 时默认取全部已勾选项
 */
export function cartPreview({ query }) {
  const user = requireLogin()
  const ids = String(query.ids || '')
    .split(',')
    .map((id) => Number(id))
    .filter((id) => !Number.isNaN(id) && id > 0)
  const list = db().carts.filter((item) => item.user_id === user.id)
  const picked = (ids.length ? list.filter((item) => ids.includes(item.id)) : list.filter((item) => item.selected === 1))
    .map(buildCartItem)
  if (!picked.length) fail('请先选择要结算的商品')
  const unavailable = picked.filter((item) => !item.product?.is_available)
  if (unavailable.length) fail(`「${unavailable[0].product?.title}」已下架或库存不足，请先移除`)

  const sellerIds = [...new Set(picked.map((item) => item.product.seller_id))]
  const totalAmount = Number(picked.reduce((sum, item) => sum + item.subtotal, 0).toFixed(2))
  return {
    items: picked,
    total_quantity: picked.reduce((sum, item) => sum + item.quantity, 0),
    total_amount: totalAmount,
    discount_amount: 0,
    pay_amount: totalAmount,
    seller_count: sellerIds.length,
    single_seller: sellerIds.length === 1,
    cart_ids: picked.map((item) => item.id),
  }
}
