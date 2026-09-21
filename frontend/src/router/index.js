import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录 / 注册', plain: true },
  },
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '首页', nav: 'home' },
  },
  {
    path: '/category',
    name: 'category',
    component: () => import('@/views/CategoryView.vue'),
    meta: { title: '全部分类', nav: 'category' },
  },
  {
    path: '/products',
    name: 'products',
    component: () => import('@/views/ProductListView.vue'),
    meta: { title: '商品列表' },
  },
  {
    path: '/products/:id',
    name: 'product-detail',
    component: () => import('@/views/ProductDetailView.vue'),
    meta: { title: '商品详情' },
  },
  {
    path: '/publish',
    name: 'publish',
    component: () => import('@/views/PublishView.vue'),
    meta: { title: '发布闲置', requiresAuth: true, nav: 'publish' },
  },
  {
    path: '/favorites',
    name: 'favorites',
    component: () => import('@/views/FavoriteView.vue'),
    meta: { title: '我的收藏', requiresAuth: true },
  },
  {
    path: '/cart',
    name: 'cart',
    component: () => import('@/views/CartView.vue'),
    meta: { title: '购物车', requiresAuth: true },
  },
  {
    path: '/checkout',
    name: 'checkout',
    component: () => import('@/views/CheckoutView.vue'),
    meta: { title: '确认订单', requiresAuth: true },
  },
  {
    path: '/orders',
    name: 'orders',
    component: () => import('@/views/OrderListView.vue'),
    meta: { title: '我的订单', requiresAuth: true },
  },
  {
    path: '/orders/:id',
    name: 'order-detail',
    component: () => import('@/views/OrderDetailView.vue'),
    meta: { title: '订单详情', requiresAuth: true },
  },
  {
    path: '/messages',
    name: 'messages',
    component: () => import('@/views/MessageView.vue'),
    meta: { title: '消息中心', requiresAuth: true, nav: 'messages' },
  },
  {
    path: '/search',
    name: 'search',
    component: () => import('@/views/SearchView.vue'),
    meta: { title: '搜索商品' },
  },
  {
    path: '/rank',
    name: 'rank',
    component: () => import('@/views/RankView.vue'),
    meta: { title: '热门榜单' },
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { title: '个人中心', requiresAuth: true, nav: 'profile' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0, behavior: 'smooth' }),
})

router.beforeEach(async (to) => {
  document.title = to.meta.title ? `${to.meta.title} · 淘学二手` : '淘学二手 · 校园二手交易平台'
  // 登录页本身不需要校验，直接放行，避免与已失效会话的重定向互相打断
  if (to.name === 'login') return true
  const userStore = useUserStore()
  // 本地有 token 但内存中还没有用户资料（如刚刷新）时补齐一次，避免误判未登录
  await userStore.ensureSession()
  if (to.meta.requiresAuth && !userStore.isLogin) {
    ElMessage.warning('请先登录后再继续操作')
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
