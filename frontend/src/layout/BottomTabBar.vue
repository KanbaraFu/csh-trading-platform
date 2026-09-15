<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useMessageStore } from '@/store/message'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const messageStore = useMessageStore()
const userStore = useUserStore()

const tabs = [
  { key: 'home', label: '首页', icon: 'HomeFilled', to: { name: 'home' } },
  { key: 'category', label: '分类', icon: 'Menu', to: { name: 'category' } },
  { key: 'publish', label: '发布', icon: 'Plus', to: { name: 'publish' }, raised: true },
  { key: 'messages', label: '消息', icon: 'Bell', to: { name: 'messages' } },
  { key: 'profile', label: '我的', icon: 'User', to: { name: 'profile' } },
]

const activeKey = computed(() => {
  if (route.name === 'products' || route.name === 'product-detail') return 'category'
  if (route.name === 'orders' || route.name === 'order-detail' || route.name === 'cart') return 'profile'
  return route.meta.nav || route.name
})

const unread = computed(() => messageStore.unread)

function onTab(tab) {
  if (['publish', 'messages', 'profile'].includes(tab.key) && !userStore.isLogin) {
    ElMessage.warning('请先登录后再继续操作')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (route.name === tab.to.name) return
  router.push(tab.to)
}
</script>

<template>
  <nav class="tab-bar">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      class="tab-item"
      :class="{ active: activeKey === tab.key, raised: tab.raised }"
      type="button"
      @click="onTab(tab)"
    >
      <span class="tab-icon">
        <el-icon :size="tab.raised ? 24 : 21"><component :is="tab.icon" /></el-icon>
        <span v-if="tab.key === 'messages' && unread" class="dot">{{ unread > 99 ? '99+' : unread }}</span>
      </span>
      <span class="tab-label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

<style scoped>
.tab-bar {
  display: none;
}

@media (max-width: 767.98px) {
  .tab-bar {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 100;
    display: flex;
    align-items: flex-end;
    justify-content: space-around;
    height: calc(var(--tabbar-height) + env(safe-area-inset-bottom));
    padding-bottom: env(safe-area-inset-bottom);
    background: rgba(255, 255, 255, 0.96);
    backdrop-filter: blur(14px);
    border-top: 1px solid var(--c-border);
  }

  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2px;
    height: var(--tabbar-height);
    border: none;
    background: transparent;
    color: var(--c-text-muted);
    font-size: 11px;
    cursor: pointer;
    transition: color 0.2s;
  }

  .tab-item.active {
    color: var(--c-primary-dark);
    font-weight: 600;
  }

  .tab-icon {
    position: relative;
    display: grid;
    place-items: center;
  }

  .dot {
    position: absolute;
    top: -5px;
    right: -12px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    border-radius: 999px;
    background: var(--c-accent-strong);
    color: #fff;
    font-size: 10px;
    font-weight: 500;
    line-height: 16px;
    box-shadow: 0 0 0 2px #fff;
  }

  .tab-item.raised {
    color: #fff;
  }

  .tab-item.raised .tab-icon {
    width: 46px;
    height: 46px;
    margin-top: -22px;
    border-radius: 50%;
    background: linear-gradient(135deg, #2dd4bf, #0d9488);
    box-shadow: 0 10px 22px rgba(13, 148, 136, 0.42);
    transition: transform 0.2s;
  }

  .tab-item.raised:active .tab-icon {
    transform: scale(0.94);
  }

  .tab-item.raised .tab-label {
    margin-top: 2px;
    color: var(--c-primary-dark);
    font-weight: 600;
  }
}
</style>
