import { get, post } from '@/utils/request'

// D. 订单与交易模块
export function createOrder(data) {
  return post('/orders', data)
}

export function getOrders(params) {
  return get('/orders', params)
}

export function getOrderDetail(id) {
  return get(`/orders/${id}`)
}

export function payOrder(id, data) {
  return post(`/orders/${id}/pay`, data)
}

// 扩展接口：文档中「卖家发货」由后端自动流转，前端演示需要手动触发
export function shipOrder(id) {
  return post(`/orders/${id}/ship`)
}

export function confirmOrder(id) {
  return post(`/orders/${id}/confirm`)
}

export function cancelOrder(id) {
  return post(`/orders/${id}/cancel`)
}
