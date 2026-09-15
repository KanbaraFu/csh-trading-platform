import { del, get, post, put } from '@/utils/request'

// E. 互动与消息模块 —— 对齐文档接口清单
export function createComment(data) {
  return post('/comments', data)
}

export function deleteComment(id) {
  return del(`/comments/${id}`)
}

export function getMessages(params) {
  return get('/messages', params)
}

export function sendMessage(data) {
  return post('/messages', data)
}

export function getUnreadCount() {
  return get('/messages/unread-count')
}

export function readMessage(id) {
  return put(`/messages/${id}/read`)
}

export function readAllMessages(type) {
  return put('/messages/read-all', { type })
}
