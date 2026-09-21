<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TopNavBar from './TopNavBar.vue'
import BottomTabBar from './BottomTabBar.vue'
import { useUserStore } from '@/store/user'
import { useMessageStore } from '@/store/message'
import { useFavoriteStore } from '@/store/favorite'
import { useCartStore } from '@/store/cart'
import { setUnauthorizedHandler } from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()
const favoriteStore = useFavoriteStore()
const cartStore = useCartStore()

const isPlain = computed(() => Boolean(route.meta.plain))

setUnauthorizedHandler(() => {
  userStore.clearSession()
  messageStore.reset()
  favoriteStore.reset()
  cartStore.reset()
  if (route.name !== 'login') {
    router.replace({ name: 'login', query: { redirect: route.fullPath } })
  }
})

async function hydrate() {
  if (!userStore.token) return
  // 刷新页面时重新拉取一次资料，保证昵称 / 头像等与服务端一致
  await userStore.fetchMe().catch(() => null)
  if (!userStore.isLogin) return
  await Promise.all([
    messageStore.fetchUnread().catch(() => 0),
    favoriteStore.fetchIds().catch(() => []),
    cartStore.fetchCart().catch(() => null),
  ])
}

onMounted(hydrate)
</script>

<template>
  <div v-if="isPlain" class="plain-shell">
    <router-view />
  </div>

  <div v-else class="app-shell">
    <TopNavBar />

    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="app-footer">
      <div class="app-footer__inner">
        <div>
          <p class="brand">淘学二手 · 校园二手交易平台</p>
          <p class="slogan">让闲置在校园里流转起来，教材、数码、生活用品一键交易</p>
        </div>
        <div class="links">
          <router-link to="/products">全部商品</router-link>
          <router-link to="/rank">热门榜单</router-link>
          <router-link to="/category">分类导航</router-link>
          <router-link to="/publish">发布闲置</router-link>
        </div>
      </div>
      <p class="copyright">© 2026 淘学二手 · 校园二手交易平台</p>
    </footer>

    <BottomTabBar />
  </div>
</template>

<style scoped>
.plain-shell {
  min-height: 100%;
}

.app-shell {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
  padding-top: var(--nav-height);
}

.app-footer {
  background: #ffffff;
  border-top: 1px solid var(--c-border);
  padding: 28px 24px 84px;
  margin-top: 16px;
}

.app-footer__inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.brand {
  font-size: 15px;
  font-weight: 700;
  color: var(--c-primary-dark);
}

.slogan {
  margin-top: 4px;
  font-size: 13px;
  color: var(--c-text-muted);
}

.links {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: var(--c-text-sub);
}

.links a:hover {
  color: var(--c-primary);
}

.copyright {
  max-width: 1280px;
  margin: 20px auto 0;
  font-size: 12px;
  color: var(--c-text-muted);
}

@media (max-width: 767.98px) {
  .app-footer {
    padding: 24px 16px calc(var(--tabbar-height) + 32px + env(safe-area-inset-bottom));
  }

  .app-footer__inner {
    flex-direction: column;
    align-items: flex-start;
  }

  .links {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
