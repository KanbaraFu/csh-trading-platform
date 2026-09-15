// 展示格式化工具：时间 / 金额 / 数量

function toDate(value) {
  if (!value) return null
  if (value instanceof Date) return value
  if (typeof value === 'number') return new Date(value)
  const normalized = String(value).replace(/-/g, '/')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

function pad(num) {
  return String(num).padStart(2, '0')
}

/**
 * 统一时间格式：yyyy-MM-dd HH:mm:ss
 */
export function formatDateTime(value) {
  const date = toDate(value)
  if (!date) return '-'
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/**
 * 日期格式：yyyy-MM-dd
 */
export function formatDate(value) {
  const date = toDate(value)
  if (!date) return '-'
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

/**
 * 相对时间：刚刚 / n 分钟前 / n 小时前 / n 天前 / yyyy-MM-dd
 */
export function fromNow(value) {
  const date = toDate(value)
  if (!date) return '-'
  const diff = Date.now() - date.getTime()
  if (diff < 60 * 1000) return '刚刚'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 30 * 24 * 60 * 60 * 1000) return `${Math.floor(diff / 86400000)} 天前`
  return formatDate(date)
}

/**
 * 金额：保留两位小数字符串
 */
export function toAmount(value) {
  const num = Number(value)
  if (Number.isNaN(num)) return '0.00'
  return num.toFixed(2)
}

/**
 * 金额展示：¥ 前缀
 */
export function formatPrice(value) {
  return `¥${toAmount(value)}`
}

/**
 * 大数字缩写：1200 -> 1.2k
 */
export function shortNumber(value) {
  const num = Number(value || 0)
  if (num < 1000) return String(num)
  if (num < 10000) return `${(num / 1000).toFixed(1)}k`
  return `${(num / 10000).toFixed(1)}w`
}

/**
 * 手机号脱敏：138****0001
 */
export function maskPhone(phone) {
  const str = String(phone || '')
  if (str.length < 7) return str
  return `${str.slice(0, 3)}****${str.slice(-4)}`
}

/**
 * 折扣：原价与现价计算，如 6.5 折
 */
export function discountText(price, originalPrice) {
  const p = Number(price)
  const o = Number(originalPrice)
  if (!o || !p || o <= p) return ''
  return `${((p / o) * 10).toFixed(1)} 折`
}
