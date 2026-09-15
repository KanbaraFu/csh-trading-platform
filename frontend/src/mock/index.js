// Mock 内存数据仓库单例：所有写操作真实生效，刷新页面后重置为基准数据
import { createSeedData } from './data'
// 认证常量统一由 constants/auth 提供（单一来源），此处仅做转发，避免重复定义
import { TOKEN_KEY, USER_ID_KEY } from '@/constants/auth'

export { TOKEN_KEY, USER_ID_KEY }

const state = { db: createSeedData() }

export function db() {
  return state.db
}

export function resetMockData() {
  state.db = createSeedData()
}

export function nextId(key) {
  const value = state.db.nextIds[key] ?? 1
  state.db.nextIds[key] = value + 1
  return value
}

function uuid() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) return crypto.randomUUID()
  return `tk-${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

export function getCurrentUser() {
  const id = Number(localStorage.getItem(USER_ID_KEY) || 0)
  if (!id) return null
  return state.db.users.find((user) => user.id === id) || null
}

export function setLogin(userId) {
  localStorage.setItem(TOKEN_KEY, uuid())
  localStorage.setItem(USER_ID_KEY, String(userId))
}

export function clearLogin() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_ID_KEY)
}
