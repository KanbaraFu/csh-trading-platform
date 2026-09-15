<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import { MESSAGE_TABS, messageTypeColor, messageTypeIcon, messageTypeLabel } from '@/constants/message'
import { useMessageStore } from '@/store/message'
import { formatDateTime, fromNow } from '@/utils/format'
import { svgCover } from '@/utils/image'

const router = useRouter()
const messageStore = useMessageStore()

const activeTab = ref('all')
const activeId = ref(0)
const drawerVisible = ref(false)
const loading = ref(true)

const records = computed(() => messageStore.records)
const counts = computed(() => messageStore.counts)
const activeMessage = computed(() => records.value.find((item) => item.id === activeId.value) || null)

const tabs = computed(() =>
  MESSAGE_TABS.map((tab) => ({
    ...tab,
    count: tab.value === 'all' ? counts.value.all : counts.value[tab.value] || 0,
  })),
)

async function load(type = activeTab.value) {
  loading.value = true
  try {
    const data = await messageStore.fetchMessages({ type })
    if (!activeId.value || !data.records.some((item) => item.id === activeId.value)) {
      activeId.value = data.records[0]?.id || 0
    }
  } finally {
    loading.value = false
  }
}

async function selectTab(value) {
  activeTab.value = value
  activeId.value = 0
  await load(value)
}

async function openMessage(item) {
  activeId.value = item.id
  if (!item.is_read) await messageStore.markRead(item.id)
  drawerVisible.value = true
}

async function markAllRead() {
  await messageStore.markAllRead(activeTab.value)
  ElMessage.success('已将当前分类的消息标记为已读')
  await load()
}

function onImageError(event, item) {
  event.target.src = svgCover(item.product_title, item.product_id)
}

function gotoRelated(message) {
  if (message.type === 'trade') router.push({ name: 'orders' })
  else if (message.type === 'comment' && message.biz_id) {
    router.push({ name: 'product-detail', params: { id: message.biz_id } })
  } else {
    router.push({ name: 'products' })
  }
  drawerVisible.value = false
}

onMounted(() => load())
</script>

<template>
  <div class="page message-page">
    <header class="page-head fade-up">
      <div>
        <h1>消息中心</h1>
        <p>共 {{ counts.all }} 条消息，其中 {{ counts.unread }} 条未读</p>
      </div>
      <button class="ghost-btn" type="button" :disabled="!counts.unread" @click="markAllRead">
        <el-icon :size="14"><CircleCheck /></el-icon>
        全部标记已读
      </button>
    </header>

    <nav class="tabs fade-up">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="tab"
        :class="{ active: activeTab === tab.value }"
        type="button"
        @click="selectTab(tab.value)"
      >
        {{ tab.label }}
        <em v-if="tab.count">{{ tab.count }}</em>
      </button>
    </nav>

    <div class="layout fade-up">
      <section class="list-panel">
        <div v-if="loading" class="loading">
          <div v-for="index in 4" :key="index" class="skeleton-block row-skeleton"></div>
        </div>

        <EmptyState
          v-else-if="!records.length"
          title="当前分类暂无消息"
          description="完成一次交易或参与评论后，相关通知会出现在这里"
          action-text="去逛逛商品"
          @action="router.push({ name: 'products' })"
        />

        <ul v-else class="msg-list">
          <li
            v-for="item in records"
            :key="item.id"
            class="msg-item"
            :class="{ active: activeId === item.id, unread: !item.is_read }"
            @click="openMessage(item)"
          >
            <span class="type-icon" :style="{ background: `${messageTypeColor(item.type)}1a`, color: messageTypeColor(item.type) }">
              <el-icon :size="17"><component :is="messageTypeIcon(item.type)" /></el-icon>
            </span>
            <div class="msg-body">
              <div class="msg-top">
                <strong>{{ item.title }}</strong>
                <span class="time">{{ fromNow(item.create_time) }}</span>
              </div>
              <p class="msg-text">{{ item.content }}</p>
              <div class="msg-bottom">
                <span class="type-label">{{ messageTypeLabel(item.type) }}</span>
                <span v-if="!item.is_read" class="unread-dot">未读</span>
              </div>
            </div>
          </li>
        </ul>
      </section>

      <aside class="detail-panel">
        <template v-if="activeMessage">
          <div class="detail-head">
            <span class="type-icon" :style="{ background: `${messageTypeColor(activeMessage.type)}1a`, color: messageTypeColor(activeMessage.type) }">
              <el-icon :size="18"><component :is="messageTypeIcon(activeMessage.type)" /></el-icon>
            </span>
            <div>
              <h2>{{ activeMessage.title }}</h2>
              <p>{{ formatDateTime(activeMessage.create_time) }}</p>
            </div>
          </div>

          <div class="bubbles">
            <div class="bubble bubble-other">
              <span class="bubble-avatar">系</span>
              <div class="bubble-body">
                <p>{{ activeMessage.content }}</p>
              </div>
            </div>
            <div class="bubble bubble-me">
              <div class="bubble-body">
                <p>好的，我看到了，稍后处理～</p>
              </div>
              <span class="bubble-avatar me">我</span>
            </div>
          </div>

          <img
            v-if="activeMessage.product_cover"
            class="related-cover"
            :src="activeMessage.product_cover"
            :alt="activeMessage.product_title"
            @error="(event) => onImageError(event, activeMessage)"
          />
          <p v-if="activeMessage.product_title" class="related-title">{{ activeMessage.product_title }}</p>

          <el-button type="primary" round style="width: 100%" @click="gotoRelated(activeMessage)">
            {{ activeMessage.type === 'trade' ? '前往订单' : activeMessage.type === 'comment' ? '查看商品与评论' : '去逛逛商品' }}
          </el-button>
        </template>

        <EmptyState v-else title="选择一条消息" description="点击左侧消息即可查看完整内容" />
      </aside>
    </div>

    <el-drawer v-model="drawerVisible" title="消息详情" direction="btt" size="62%">
      <div v-if="activeMessage" class="drawer-body">
        <h3>{{ activeMessage.title }}</h3>
        <p class="drawer-time">{{ formatDateTime(activeMessage.create_time) }}</p>
        <p class="drawer-content">{{ activeMessage.content }}</p>
        <el-button type="primary" round style="width: 100%" @click="gotoRelated(activeMessage)">查看关联内容</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.message-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.page-head h1 {
  font-size: 24px;
  font-weight: 700;
}

.page-head p {
  margin-top: 6px;
  font-size: 13px;
  color: var(--c-text-muted);
}

.ghost-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 9px 18px;
  border: 1px solid var(--c-primary);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: var(--c-primary-dark);
  cursor: pointer;
  transition: all 0.2s;
}

.ghost-btn:hover:not(:disabled) {
  background: var(--c-primary-mist);
}

.ghost-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 6px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  scrollbar-width: none;
}

.tabs::-webkit-scrollbar {
  display: none;
}

.tab {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  border: none;
  border-radius: 999px;
  background: transparent;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.tab:hover {
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
}

.tab.active {
  color: #fff;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  box-shadow: 0 8px 18px rgba(13, 148, 136, 0.24);
}

.tab em {
  font-style: normal;
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.28);
  font-size: 11px;
}

.tab:not(.active) em {
  background: var(--c-accent-soft);
  color: var(--c-accent-strong);
}

.layout {
  display: grid;
  grid-template-columns: 1.25fr 0.75fr;
  gap: 18px;
  align-items: start;
}

.list-panel,
.detail-panel {
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  padding: 8px;
}

.detail-panel {
  padding: 20px;
  position: sticky;
  top: calc(var(--nav-height) + 16px);
}

.loading {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 8px;
}

.row-skeleton {
  height: 76px;
  border-radius: 12px;
}

.msg-list {
  display: flex;
  flex-direction: column;
}

.msg-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.msg-item:hover {
  background: var(--c-surface-alt);
}

.msg-item.active {
  background: var(--c-primary-mist);
}

.type-icon {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 12px;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.msg-top strong {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-top .time {
  font-size: 11px;
  color: var(--c-text-muted);
  flex-shrink: 0;
}

.msg-text {
  margin-top: 4px;
  font-size: 12px;
  color: var(--c-text-sub);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.msg-bottom {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.type-label {
  font-size: 11px;
  color: var(--c-text-muted);
}

.unread-dot {
  padding: 0 7px;
  border-radius: 999px;
  background: var(--c-accent-soft);
  color: var(--c-accent-strong);
  font-size: 11px;
}

.detail-head {
  display: flex;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--c-border);
}

.detail-head h2 {
  font-size: 15px;
  font-weight: 600;
}

.detail-head p {
  margin-top: 4px;
  font-size: 11px;
  color: var(--c-text-muted);
}

.bubbles {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 0;
}

.bubble {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.bubble-avatar {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 9px;
  background: var(--c-primary-soft);
  color: var(--c-primary-dark);
  font-size: 12px;
  font-weight: 600;
}

.bubble-avatar.me {
  background: var(--c-accent-soft);
  color: var(--c-accent-strong);
}

.bubble-body {
  max-width: 82%;
  padding: 10px 13px;
  border-radius: 12px;
  background: var(--c-surface-alt);
  font-size: 13px;
  line-height: 1.7;
  color: var(--c-text-sub);
}

.bubble-me {
  justify-content: flex-end;
}

.bubble-me .bubble-body {
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
}

.related-cover {
  width: 100%;
  height: 130px;
  object-fit: cover;
  border-radius: 12px;
  background: #f3f6f9;
}

.related-title {
  margin: 8px 0 14px;
  font-size: 13px;
  color: var(--c-text-sub);
}

.drawer-body h3 {
  font-size: 16px;
  font-weight: 700;
}

.drawer-time {
  margin-top: 4px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.drawer-content {
  margin: 14px 0 20px;
  font-size: 14px;
  line-height: 1.85;
  color: var(--c-text-sub);
}

@media (max-width: 1023.98px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .detail-panel {
    display: none;
  }
}

@media (max-width: 767.98px) {
  .page-head h1 {
    font-size: 19px;
  }
}
</style>
