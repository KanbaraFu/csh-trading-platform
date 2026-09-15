import { del, get, post } from '@/utils/request'

// C-1. 收藏模块 —— 对齐文档接口清单
export function addFavorite(productId) {
  return post(`/favorites/${productId}`)
}

export function removeFavorite(productId) {
  return del(`/favorites/${productId}`)
}

export function getFavorites(params) {
  return get('/favorites', params)
}

export function getFavoriteIds() {
  return get('/favorites/ids')
}
