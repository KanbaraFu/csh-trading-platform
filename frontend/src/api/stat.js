import { get, post } from '@/utils/request'

// F. 搜索与统计模块 —— 对齐文档接口清单
export function search(params) {
  return get('/search', params)
}

export function getHotWords() {
  return get('/search/hot-words')
}

export function recordView(productId) {
  return post(`/stat/view/${productId}`)
}

export function getHotProducts(params) {
  return get('/stat/hot-products', params)
}

export function getSales(productId) {
  return get(`/stat/sales/${productId}`)
}

// 扩展接口：排行榜页平台数据概览
export function getOverview() {
  return get('/stat/overview')
}
