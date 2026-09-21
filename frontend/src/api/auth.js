import { del, get, post, put } from '@/utils/request'

// A. 用户与认证模块 —— 对齐文档接口清单
export function sendCaptcha(data) {
  return post('/auth/captcha', data)
}

export function register(data) {
  return post('/auth/register', data)
}

export function login(data) {
  return post('/auth/login', data)
}

export function logout() {
  return post('/auth/logout')
}

export function getMe() {
  return get('/user/me')
}

// 个人中心统计：我的订单 / 待付款 / 收藏 / 购物车 / 未读消息 条数
export function getMyStats() {
  return get('/user/me/stats')
}

export function updateMe(data) {
  return put('/user/me', data)
}

// 个人中心「增」：新增用户（POST /api/user）
export function createUser(data) {
  return post('/user', data)
}

// 个人中心「删」：删除当前登录用户（DELETE /api/user/me）
export function deleteMe() {
  return del('/user/me')
}

export function getMyProducts(params) {
  return get('/user/products', params)
}

export function getAddresses(params) {
  return get('/addresses', params)
}

export function createAddress(data) {
  return post('/addresses', data)
}

export function updateAddress(id, data) {
  return put(`/addresses/${id}`, data)
}

export function deleteAddress(id) {
  return del(`/addresses/${id}`)
}
