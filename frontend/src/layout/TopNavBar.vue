<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { useMessageStore } from '@/store/message'
import { useFavoriteStore } from '@/store/favorite'
import { svgAvatar } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const messageStore = useMessageStore()
const favoriteStore = useFavoriteStore()

const keyword = ref(String(route.query.keyword || ''))
const scrolled = ref(false)

const navLinks = [
  { label: '首页', to: { name: 'home' }, key: 'home' },
  { label: '全部分类', to: { name: 'category' }, key: 'category' },
  { label: '全部商品', to: { name: 'products' }, key: 'products' },
  { label: '热门榜单', to: { name: 'rank' }, key: 'rank' },
]

const activeKey = computed(() => route.meta.nav || route.name)
const cartBadge = computed(() => cartStore.totalQuantity)
const unread = computed(() => messageStore.unread)
const isLogin = computed(() => userStore.isLogin)
const avatarSrc = computed(() => userStore.avatar || svgAvatar('游', 'guest'))
const displayName = computed(() => (isLogin.value ? userStore.nickname : '登录 / 注册'))

function onScroll() {
  scrolled.value = window.scrollY > 8
}

function doSearch() {
  const word = keyword.value.trim()
  router.push({ name: 'search', query: word ? { keyword: word } : {} })
}

function openGuarded(name) {
  if (!isLogin.value) {
    ElMessage.warning('请先登录后再继续操作')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  router.push({ name })
}

function onAvatarError(event) {
  event.target.src = svgAvatar(userStore.nickname || '游', 'guest')
}

async function handleCommand(command) {
  if (command === 'login') {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (command === 'logout') {
    await userStore.logout()
    messageStore.reset()
    favoriteStore.reset()
    cartStore.reset()
    ElMessage.success('已退出登录，期待下次相见')
    router.push({ name: 'home' })
    return
  }
  router.push({ name: command })
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
watch(() => route.query.keyword, (value) => { keyword.value = String(value || '') })
</script>

<template>
  <header class="top-nav" :class="{ 'is-scrolled': scrolled }">
    <div class="top-nav__inner">
      <router-link class="logo" :to="{ name: 'home' }">
        <span class="logo__mark">
          <el-icon :size="20"><Shop /></el-icon>
        </span>
        <span class="logo__text">
          淘学二手
          <small>校园二手交易平台</small>
        </span>
      </router-link>

      <nav class="nav-links">
        <router-link
          v-for="link in navLinks"
          :key="link.key"
          :to="link.to"
          class="nav-link"
          :class="{ active: activeKey === link.key }"
        >
          {{ link.label }}
        </router-link>
      </nav>

      <form class="search" @submit.prevent="doSearch">
        <el-icon class="search__icon"><Search /></el-icon>
        <input v-model="keyword" class="search__input" placeholder="搜索教材、数码、生活用品…" />
        <button class="search__btn" type="submit">搜索</button>
      </form>

      <div class="actions">
        <button class="publish-btn" type="button" @click="openGuarded('publish')">
          <el-icon><Plus /></el-icon>
          <span>发布闲置</span>
        </button>

        <button class="icon-btn" type="button" title="消息中心" @click="openGuarded('messages')">
          <el-icon :size="19"><Bell /></el-icon>
          <span v-if="unread" class="badge">{{ unread > 99 ? '99+' : unread }}</span>
        </button>

        <button class="icon-btn" type="button" title="购物车" @click="openGuarded('cart')">
          <el-icon :size="19"><ShoppingCart /></el-icon>
          <span v-if="cartBadge" class="badge">{{ cartBadge > 99 ? '99+' : cartBadge }}</span>
        </button>

        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-entry" :class="{ 'is-guest': !isLogin }">
            <img class="user-entry__avatar" :src="avatarSrc" alt="用户头像" @error="onAvatarError" />
            <span class="user-entry__name">{{ displayName }}</span>
            <el-icon :size="12"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <template v-if="isLogin">
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item command="publish" divided>发布闲置</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </template>
              <template v-else>
                <el-dropdown-item command="login">登录 / 注册</el-dropdown-item>
                <el-dropdown-item command="rank">热门榜单</el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </header>
</template>

<style scoped>
.top-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--nav-height);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid transparent;
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
}

.top-nav.is-scrolled {
  box-shadow: 0 6px 24px rgba(15, 23, 42, 0.07);
  border-bottom-color: var(--c-border);
}

.top-nav__inner {
  max-width: 1280px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 22px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.logo__mark {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(135deg, #2dd4bf, #0d9488);
  box-shadow: 0 6px 16px rgba(13, 148, 136, 0.32);
}

.logo__text {
  display: flex;
  flex-direction: column;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.4px;
  color: var(--c-text);
  line-height: 1.15;
}

.logo__text small {
  font-size: 11px;
  font-weight: 400;
  color: var(--c-text-muted);
  letter-spacing: 0;
}

.nav-links {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.nav-link {
  padding: 7px 12px;
  border-radius: 9px;
  font-size: 14px;
  color: var(--c-text-sub);
  transition: all 0.2s;
}

.nav-link:hover {
  color: var(--c-primary);
  background: var(--c-primary-mist);
}

.nav-link.active {
  color: var(--c-primary-dark);
  background: var(--c-primary-soft);
  font-weight: 600;
}

.search {
  position: relative;
  flex: 1;
  min-width: 160px;
  display: flex;
  align-items: center;
  height: 40px;
  padding-left: 38px;
  padding-right: 4px;
  border-radius: 999px;
  background: #f3f6f9;
  border: 1px solid transparent;
  transition: all 0.22s;
}

.search:focus-within {
  background: #fff;
  border-color: var(--c-primary);
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.12);
}

.search__icon {
  position: absolute;
  left: 14px;
  color: var(--c-text-muted);
}

.search__input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--c-text);
}

.search__input::placeholder {
  color: var(--c-text-muted);
}

.search__btn {
  height: 32px;
  padding: 0 18px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
}

.search__btn:hover {
  box-shadow: 0 6px 16px rgba(13, 148, 136, 0.35);
  transform: translateY(-1px);
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 38px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #ffa06b, var(--c-accent));
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(255, 122, 69, 0.32);
  transition: transform 0.18s, box-shadow 0.18s;
}

.publish-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(255, 122, 69, 0.4);
}

.icon-btn {
  position: relative;
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
}

.badge {
  position: absolute;
  top: 2px;
  right: 1px;
  min-width: 17px;
  height: 17px;
  padding: 0 4px;
  border-radius: 999px;
  background: var(--c-accent-strong);
  color: #fff;
  font-size: 10px;
  line-height: 17px;
  text-align: center;
  box-shadow: 0 0 0 2px #fff;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 7px;
  height: 38px;
  padding: 0 12px 0 5px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s;
  outline: none;
}

.user-entry:hover {
  background: var(--c-primary-mist);
}

.user-entry__avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px var(--c-border);
}

.user-entry__name {
  font-size: 13px;
  color: var(--c-text-sub);
  max-width: 84px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-entry.is-guest .user-entry__avatar {
  filter: grayscale(0.3);
}

@media (max-width: 1023.98px) {
  .nav-links {
    display: none;
  }
}

@media (max-width: 767.98px) {
  .top-nav__inner {
    padding: 0 12px;
    gap: 10px;
  }

  .logo__text {
    font-size: 15px;
  }

  .logo__text small,
  .publish-btn,
  .user-entry__name,
  .search__btn {
    display: none;
  }

  .logo__mark {
    width: 34px;
    height: 34px;
  }

  .search {
    height: 36px;
  }

  .publish-btn {
    width: 38px;
    height: 38px;
    padding: 0;
    justify-content: center;
  }

  .icon-btn {
    width: 34px;
    height: 34px;
  }

  .user-entry {
    padding: 0;
  }
}
</style>
