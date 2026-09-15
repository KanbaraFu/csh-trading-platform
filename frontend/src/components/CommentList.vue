<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import UserAvatar from './UserAvatar.vue'
import { useUserStore } from '@/store/user'
import { fromNow } from '@/utils/format'

const props = defineProps({
  comments: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false },
  total: { type: Number, default: 0 },
  pageNum: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
})

const emit = defineEmits(['submit', 'page-change'])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const content = ref('')
const replyTarget = ref(null)
const replyContent = ref('')

const roots = computed(() => props.comments.filter((item) => !item.parent_id))

function repliesOf(id) {
  return props.comments.filter((item) => Number(item.parent_id) === Number(id))
}

function ensureLogin() {
  if (userStore.isLogin) return true
  ElMessage.warning('登录后才能参与评论互动')
  router.push({ name: 'login', query: { redirect: route.fullPath } })
  return false
}

function submitRoot() {
  if (!ensureLogin()) return
  const text = content.value.trim()
  if (!text) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  emit('submit', { content: text, parentId: 0 })
  content.value = ''
}

function openReply(comment) {
  if (!ensureLogin()) return
  replyTarget.value = replyTarget.value?.id === comment.id ? null : comment
  replyContent.value = ''
}

function submitReply(comment) {
  if (!ensureLogin()) return
  const text = replyContent.value.trim()
  if (!text) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  emit('submit', { content: text, parentId: comment.id, replyUserId: comment.user_id })
  replyTarget.value = null
  replyContent.value = ''
}
</script>

<template>
  <section class="comment-block">
    <div class="head">
      <h3>商品评论</h3>
      <span class="count">共 {{ total || comments.length }} 条评论</span>
    </div>

    <div class="editor">
      <UserAvatar :src="userStore.avatar" :name="userStore.nickname" :seed="userStore.user?.id || 'guest'" :size="40" />
      <div class="editor__body">
        <textarea
          v-model="content"
          class="editor__input"
          rows="3"
          maxlength="500"
          placeholder="想问问卖家成色、交易时间？或者分享一下使用感受吧～"
        ></textarea>
        <div class="editor__actions">
          <span class="hint">友善交流，请勿发布广告或联系方式的敏感信息</span>
          <button class="submit-btn" type="button" :disabled="submitting" @click="submitRoot">
            {{ submitting ? '发送中…' : '发表评论' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="list">
      <div v-for="index in 3" :key="index" class="skeleton-row">
        <div class="skeleton-block circle"></div>
        <div class="skeleton-block bar"></div>
      </div>
    </div>

    <ul v-else-if="roots.length" class="list">
      <li v-for="comment in roots" :key="comment.id" class="item">
        <UserAvatar :src="comment.avatar" :name="comment.nickname" :seed="comment.user_id" :size="40" />
        <div class="item__body">
          <div class="item__top">
            <span class="nickname">{{ comment.nickname }}</span>
            <span class="time">{{ fromNow(comment.create_time) }}</span>
          </div>
          <p class="content">{{ comment.content }}</p>
          <button class="reply-link" type="button" @click="openReply(comment)">
            <el-icon :size="13"><ChatLineSquare /></el-icon>
            回复
          </button>

          <ul v-if="repliesOf(comment.id).length" class="replies">
            <li v-for="reply in repliesOf(comment.id)" :key="reply.id" class="reply">
              <UserAvatar :src="reply.avatar" :name="reply.nickname" :seed="reply.user_id" :size="26" />
              <div class="reply__body">
                <p class="reply__meta">
                  <span class="nickname">{{ reply.nickname }}</span>
                  <span v-if="reply.reply_nickname" class="reply-to">回复 @{{ reply.reply_nickname }}</span>
                  <span class="time">{{ fromNow(reply.create_time) }}</span>
                </p>
                <p class="content">{{ reply.content }}</p>
              </div>
            </li>
          </ul>

          <div v-if="replyTarget?.id === comment.id" class="reply-editor">
            <textarea
              v-model="replyContent"
              class="editor__input small"
              rows="2"
              maxlength="500"
              :placeholder="`回复 ${comment.nickname}：一起聊聊这件商品的细节吧`"
            ></textarea>
            <div class="editor__actions">
              <button class="ghost-btn" type="button" @click="replyTarget = null">取消</button>
              <button class="submit-btn small" type="button" :disabled="submitting" @click="submitReply(comment)">
                发送回复
              </button>
            </div>
          </div>
        </div>
      </li>
    </ul>

    <p v-else class="empty">还没有人留言，来提第一个问题吧～</p>

    <div v-if="!loading && total > pageSize" class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="(page) => emit('page-change', page)"
      />
    </div>
  </section>
</template>

<style scoped>
.comment-block {
  padding: 20px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.head h3 {
  font-size: 16px;
  font-weight: 700;
}

.count {
  font-size: 12px;
  color: var(--c-text-muted);
}

.editor {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: var(--c-surface-alt);
}

.editor__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.editor__input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--c-border);
  border-radius: 10px;
  background: #fff;
  font-family: inherit;
  font-size: 13px;
  color: var(--c-text);
  resize: vertical;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.editor__input:focus {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.editor__input.small {
  font-size: 12px;
}

.editor__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.hint {
  font-size: 11px;
  color: var(--c-text-muted);
}

.submit-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(13, 148, 136, 0.32);
}

.submit-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.submit-btn.small {
  padding: 6px 16px;
  font-size: 12px;
}

.ghost-btn {
  padding: 6px 14px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 12px;
  color: var(--c-text-sub);
  cursor: pointer;
}

.list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.item {
  display: flex;
  gap: 12px;
}

.item__body {
  flex: 1;
  min-width: 0;
}

.item__top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nickname {
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text);
}

.time {
  font-size: 11px;
  color: var(--c-text-muted);
}

.content {
  margin-top: 6px;
  font-size: 13px;
  color: var(--c-text-sub);
  line-height: 1.7;
  word-break: break-word;
}

.reply-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  padding: 0;
  border: none;
  background: transparent;
  font-size: 12px;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: color 0.2s;
}

.reply-link:hover {
  color: var(--c-primary);
}

.replies {
  margin-top: 12px;
  padding: 12px;
  border-radius: 12px;
  background: var(--c-surface-alt);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply {
  display: flex;
  gap: 10px;
}

.reply__body {
  flex: 1;
  min-width: 0;
}

.reply__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.reply-to {
  font-size: 11px;
  color: var(--c-primary-dark);
}

.reply-editor {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.bar {
  flex: 1;
  height: 16px;
}

.empty {
  margin-top: 24px;
  text-align: center;
  font-size: 13px;
  color: var(--c-text-muted);
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid var(--c-border);
}
</style>
