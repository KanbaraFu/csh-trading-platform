import { del, get, post, put } from '@/utils/request'

// C-2. 购物车模块 —— 对齐文档接口清单
export function addCart(data) {
  return post('/cart', data)
}

export function getCart() {
  return get('/cart')
}

export function updateCart(id, data) {
  return put(`/cart/${id}`, data)
}

export function removeCart(id) {
  return del(`/cart/${id}`)
}

export function selectAllCart(selected) {
  return put('/cart/select-all', { selected })
}

export function getCartPreview(params) {
  return get('/cart/preview', params)
}
