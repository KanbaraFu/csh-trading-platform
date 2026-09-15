// Mock 请求分发器：按接口清单匹配路由并调用对应模块，返回统一 Result 结构
import * as userApi from './modules/user'
import * as productApi from './modules/product'
import * as cartApi from './modules/cart'
import * as orderApi from './modules/order'
import * as interactionApi from './modules/interaction'
import { ok } from './helpers'

const routes = [
  // A. 用户与认证
  ['POST', '/auth/captcha', userApi.sendCaptcha],
  ['POST', '/auth/register', userApi.register],
  ['POST', '/auth/login', userApi.login],
  ['POST', '/auth/logout', userApi.logout],
  ['GET', '/user/me', userApi.getMe],
  ['PUT', '/user/me', userApi.updateMe],
  ['GET', '/user/products', userApi.getMyProducts],
  ['GET', '/addresses', userApi.getAddresses],
  ['POST', '/addresses', userApi.createAddress],
  ['PUT', '/addresses/:id', userApi.updateAddress],
  ['DELETE', '/addresses/:id', userApi.deleteAddress],

  // B. 商品与分类
  ['GET', '/categories', productApi.getCategories],
  ['GET', '/products', productApi.getProducts],
  ['POST', '/products', productApi.createProduct],
  ['GET', '/products/:id', productApi.getProductDetail],
  ['PUT', '/products/:id', productApi.updateProduct],
  ['DELETE', '/products/:id', productApi.offlineProduct],
  ['POST', '/products/:id/relist', productApi.relistProduct],

  // C. 收藏与购物车
  ['GET', '/favorites', productApi.getFavorites],
  ['GET', '/favorites/ids', productApi.getFavoriteIds],
  ['POST', '/favorites/:productId', productApi.addFavorite],
  ['DELETE', '/favorites/:productId', productApi.removeFavorite],
  ['GET', '/cart', cartApi.getCart],
  ['POST', '/cart', cartApi.addCart],
  ['GET', '/cart/preview', cartApi.cartPreview],
  ['PUT', '/cart/select-all', cartApi.selectAllCart],
  ['PUT', '/cart/:id', cartApi.updateCart],
  ['DELETE', '/cart/:id', cartApi.removeCart],

  // D. 订单与交易
  ['GET', '/orders', orderApi.getOrders],
  ['POST', '/orders', orderApi.createOrder],
  ['GET', '/orders/:id', orderApi.getOrderDetail],
  ['POST', '/orders/:id/pay', orderApi.payOrder],
  ['POST', '/orders/:id/ship', orderApi.shipOrder],
  ['POST', '/orders/:id/confirm', orderApi.confirmOrder],
  ['POST', '/orders/:id/cancel', orderApi.cancelOrder],

  // E. 互动与消息
  ['GET', '/products/:id/comments', interactionApi.getComments],
  ['POST', '/comments', interactionApi.addComment],
  ['DELETE', '/comments/:id', interactionApi.deleteComment],
  ['GET', '/messages', interactionApi.getMessages],
  ['POST', '/messages', interactionApi.createMessage],
  ['GET', '/messages/unread-count', interactionApi.getUnreadCount],
  ['PUT', '/messages/read-all', interactionApi.readAllMessages],
  ['PUT', '/messages/:id/read', interactionApi.readMessage],

  // F. 搜索与统计
  ['GET', '/search', interactionApi.search],
  ['GET', '/search/hot-words', interactionApi.getHotWords],
  ['POST', '/stat/view/:productId', interactionApi.recordView],
  ['GET', '/stat/hot-products', interactionApi.getHotProducts],
  ['GET', '/stat/sales/:productId', interactionApi.getSales],
  ['GET', '/stat/overview', interactionApi.getOverview],
]

function normalize(url) {
  let path = String(url || '').split('?')[0]
  if (path.startsWith('/api')) path = path.slice(4)
  if (!path.startsWith('/')) path = `/${path}`
  const trimmed = path.replace(/\/+$/, '')
  return trimmed || '/'
}

function matchPattern(pattern, path) {
  const patternParts = pattern.split('/').filter(Boolean)
  const pathParts = path.split('/').filter(Boolean)
  if (patternParts.length !== pathParts.length) return null
  const route = {}
  for (let i = 0; i < patternParts.length; i += 1) {
    if (patternParts[i].startsWith(':')) route[patternParts[i].slice(1)] = pathParts[i]
    else if (patternParts[i] !== pathParts[i]) return null
  }
  return route
}

function resolveRoute(method, path) {
  for (const [routeMethod, pattern, handler] of routes) {
    if (routeMethod !== method) continue
    const route = matchPattern(pattern, path)
    if (route) return { handler, route }
  }
  return null
}

/**
 * 模拟网络请求：返回 Promise<{ code, message, data }>
 */
export function mockDispatch(config) {
  const method = String(config.method || 'get').toUpperCase()
  const path = normalize(config.url)
  const query = { ...(config.params || {}) }
  const body = config.data && typeof config.data === 'object' ? config.data : {}
  const found = resolveRoute(method, path)
  const latency = 70 + Math.random() * 150

  return new Promise((resolve) => {
    setTimeout(() => {
      if (!found) {
        console.error(`[mock] 未匹配到接口: ${method} /api${path}`)
        resolve({ code: 404, message: `接口不存在：${method} /api${path}`, data: null })
        return
      }
      try {
        const data = found.handler({ route: found.route, query, body })
        resolve(ok(data))
      } catch (error) {
        if (!error.mockCode) console.error(`[mock] ${method} /api${path} 执行异常`, error)
        resolve({ code: error.mockCode || 500, message: error.message || '操作失败', data: null })
      }
    }, latency)
  })
}
