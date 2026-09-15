// 商品与分类模块 Mock：发布 / 编辑 / 下架 / 列表 / 详情 / 分类 / 收藏
import { db, nextId } from '../index'
import { decorateProduct, fail, nowText, paginate, requireLogin } from '../helpers'
import { productCover } from '@/utils/image'

export function getCategories() {
  const list = db()
    .categories.filter((item) => item.status === 1)
    .sort((a, b) => a.sort - b.sort)
    .map((item) => ({
      ...item,
      product_count: db().products.filter((product) => product.category_id === item.id && product.status === 1).length,
    }))
  return list
}

/**
 * 商品分页列表，支持分类 / 关键词 / 价格区间 / 排序 / 卖家筛选
 */
export function getProducts({ query = {} }) {
  const data = db()
  const {
    pageNum = 1, pageSize = 12, categoryId, keyword, minPrice, maxPrice, sort = 'new', sellerId, status,
  } = query
  let list = data.products.slice()

  if (status !== undefined && status !== '') list = list.filter((item) => item.status === Number(status))
  else list = list.filter((item) => item.status !== 0)
  if (categoryId) list = list.filter((item) => item.category_id === Number(categoryId))
  if (sellerId) list = list.filter((item) => item.seller_id === Number(sellerId))

  const word = String(keyword || '').trim()
  if (word) {
    list = list.filter((item) => item.title.includes(word) || String(item.description).includes(word))
  }
  if (minPrice !== undefined && minPrice !== '' && minPrice !== null) {
    list = list.filter((item) => Number(item.price) >= Number(minPrice))
  }
  if (maxPrice !== undefined && maxPrice !== '' && maxPrice !== null) {
    list = list.filter((item) => Number(item.price) <= Number(maxPrice))
  }

  const sorters = {
    new: (a, b) => (a.create_time < b.create_time ? 1 : -1),
    price_asc: (a, b) => a.price - b.price,
    price_desc: (a, b) => b.price - a.price,
    hot: (a, b) => b.view_count + b.sales_count * 10 - (a.view_count + a.sales_count * 10),
  }
  list.sort(sorters[sort] || sorters.new)

  const paged = paginate(list, pageNum, pageSize)
  return { ...paged, records: paged.records.map(decorateProduct) }
}

export function getProductDetail({ route, query }) {
  const id = Number(route.id)
  const product = db().products.find((item) => item.id === id)
  if (!product) fail('商品不存在或已被删除')
  if (query.withView !== 'false') {
    product.view_count += 1
  }
  return decorateProduct(product)
}

export function createProduct({ body }) {
  const user = requireLogin()
  if (!body.title || String(body.title).trim().length < 4) fail('商品标题至少 4 个字')
  if (!body.category_id) fail('请选择商品所属分类')
  if (!body.price || Number(body.price) <= 0) fail('请填写正确的商品价格')
  if (!body.description || String(body.description).trim().length < 10) fail('商品描述至少 10 个字，方便买家了解详情')

  const id = nextId('product')
  const images = Array.isArray(body.images) && body.images.length ? body.images : [productCover(id)]
  const product = {
    id,
    seller_id: user.id,
    category_id: Number(body.category_id),
    title: String(body.title).trim(),
    description: String(body.description).trim(),
    price: Number(body.price),
    original_price: Number(body.original_price || body.price),
    stock: Number(body.stock || 1),
    cover: images[0],
    status: 1,
    view_count: 0,
    sales_count: 0,
    condition: body.condition || '九成新',
    location: body.location || '校内可送达',
    create_time: nowText(),
    update_time: nowText(),
  }
  db().products.unshift(product)
  images.forEach((url, index) => {
    db().productImages.push({ id: id * 10 + index + 1, product_id: id, url, sort: index + 1 })
  })
  return decorateProduct(product)
}

export function updateProduct({ route, body }) {
  const user = requireLogin()
  const product = db().products.find((item) => item.id === Number(route.id))
  if (!product) fail('商品不存在')
  if (product.seller_id !== user.id) fail('只能编辑自己发布的商品')
  const fields = ['title', 'description', 'category_id', 'price', 'original_price', 'stock', 'condition', 'location']
  fields.forEach((field) => {
    if (body[field] !== undefined) {
      product[field] = ['category_id', 'price', 'original_price', 'stock'].includes(field)
        ? Number(body[field])
        : body[field]
    }
  })
  if (Array.isArray(body.images) && body.images.length) {
    product.cover = body.images[0]
    const images = db().productImages
    for (let i = images.length - 1; i >= 0; i -= 1) {
      if (images[i].product_id === product.id) images.splice(i, 1)
    }
    body.images.forEach((url, index) => {
      images.push({ id: product.id * 10 + index + 1, product_id: product.id, url, sort: index + 1 })
    })
  }
  product.update_time = nowText()
  return decorateProduct(product)
}

export function offlineProduct({ route }) {
  const user = requireLogin()
  const product = db().products.find((item) => item.id === Number(route.id))
  if (!product) fail('商品不存在')
  if (product.seller_id !== user.id) fail('只能下架自己发布的商品')
  product.status = 0
  product.update_time = nowText()
  return decorateProduct(product)
}

export function relistProduct({ route }) {
  const user = requireLogin()
  const product = db().products.find((item) => item.id === Number(route.id))
  if (!product) fail('商品不存在')
  if (product.seller_id !== user.id) fail('只能上架自己发布的商品')
  product.status = 1
  product.update_time = nowText()
  return decorateProduct(product)
}

// ---------------------------------------------------------------- 收藏
export function addFavorite({ route }) {
  const user = requireLogin()
  const productId = Number(route.productId)
  if (!db().products.some((item) => item.id === productId)) fail('商品不存在')
  const exists = db().favorites.find((item) => item.user_id === user.id && item.product_id === productId)
  if (exists) return { product_id: productId, favorited: true }
  db().favorites.push({
    id: nextId('favorite'),
    user_id: user.id,
    product_id: productId,
    create_time: nowText(),
  })
  return { product_id: productId, favorited: true }
}

export function removeFavorite({ route }) {
  const user = requireLogin()
  const productId = Number(route.productId)
  const list = db().favorites
  const index = list.findIndex((item) => item.user_id === user.id && item.product_id === productId)
  if (index >= 0) list.splice(index, 1)
  return { product_id: productId, favorited: false }
}

export function getFavorites({ query = {} } = {}) {
  const user = requireLogin()
  const all = db()
    .favorites.filter((item) => item.user_id === user.id)
    .sort((a, b) => (a.create_time < b.create_time ? 1 : -1))
    .map((item) => {
      const product = db().products.find((row) => row.id === item.product_id)
      if (!product) return null
      return { ...item, product: decorateProduct(product) }
    })
    .filter(Boolean)

  const paged = paginate(all, query.pageNum || 1, query.pageSize || 8)
  // 全量收藏总价：在分页前统计，保证页头数字与页码无关
  const totalAmount = Number(all.reduce((sum, item) => sum + Number(item.product?.price || 0), 0).toFixed(2))
  return { ...paged, total_amount: totalAmount }
}

export function getFavoriteIds() {
  const user = requireLogin()
  return db().favorites.filter((item) => item.user_id === user.id).map((item) => item.product_id)
}
