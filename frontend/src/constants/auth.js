// 认证领域常量：本地登录态存储键
// 注意：这两个 key 属于前端存储约定，与后端无关，不应放在 mock 目录下

// 登录令牌存储键（同时作为请求头 token 的来源）
export const TOKEN_KEY = 'campus_token'

// 当前登录用户 ID 存储键（与后端 token 配套，便于按用户维度取数）
export const USER_ID_KEY = 'campus_user_id'

// 当前登录用户资料快照存储键：刷新后立即恢复登录态，避免出现「有 token 却提示未登录」
export const USER_KEY = 'campus_user'
