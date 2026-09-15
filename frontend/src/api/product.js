import { del, get, post, put } from '@/utils/request'

// B. 商品与分类模块 —— 对齐文档接口清单
export function getCategories() {
  return get('/categories')
}

export function getProducts(params) {
  return get('/products', params)
}

export function getProductDetail(id) {
  return get(`/products/${id}`)
}

export function createProduct(data) {
  return post('/products', data)
}

export function updateProduct(id, data) {
  return put(`/products/${id}`, data)
}

export function offlineProduct(id) {
  return del(`/products/${id}`)
}

export function relistProduct(id) {
  return post(`/products/${id}/relist`)
}

export function getComments(productId, params) {
  return get(`/products/${productId}/comments`, params)
}
