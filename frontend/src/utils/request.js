// 网络层：默认走本地 Mock，通过 VITE_USE_MOCK 环境变量切换真实 /api 后端
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { TOKEN_KEY } from '@/constants/auth'

// Mock 开关：由环境变量驱动（.env / .env.production / .env.development.local）
// 防御式默认：未配置时仍走 Mock，避免配置缺失导致前端误连后端白屏
// 写成 `!== 'false'` 而非 `String(...) === 'true'`，是为了让构建工具能静态折叠此常量，
// 从而在生产构建（VITE_USE_MOCK=false）时彻底剔除 Mock 代码
export const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false'

const service = axios.create({
  baseURL: '/api',
  timeout: 12000,
})

let unauthorizedHandler = null

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

function handleUnauthorized(message) {
  if (unauthorizedHandler) unauthorizedHandler(message)
  else ElMessage.error(message || '请先登录后再操作')
}

function unwrap(body) {
  if (!body || typeof body.code === 'undefined') return body
  if (body.code === 200) return body.data
  const message = body.message || '请求失败，请稍后重试'
  if (body.code === 401) handleUnauthorized(message)
  else ElMessage.error(message)
  return Promise.reject(Object.assign(new Error(message), { code: body.code }))
}

service.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.token = token
  return config
})

service.interceptors.response.use(
  (response) => unwrap(response.data),
  (error) => Promise.reject(error),
)

export default async function request(config) {
  const finalConfig = {
    method: 'get',
    ...config,
    headers: { ...(config.headers || {}) },
  }
  if (!USE_MOCK) return service(finalConfig)
  // 动态引入：生产构建（USE_MOCK=false）时该分支不会执行，Mock 会被 tree-shake 丢弃
  const { mockDispatch } = await import('@/mock/handlers')
  return mockDispatch(finalConfig).then(unwrap)
}

export function get(url, params) {
  return request({ url, method: 'get', params })
}

export function post(url, data) {
  return request({ url, method: 'post', data })
}

export function put(url, data) {
  return request({ url, method: 'put', data })
}

export function del(url, params) {
  return request({ url, method: 'delete', params })
}
