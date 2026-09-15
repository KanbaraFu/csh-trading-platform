import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as messageApi from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const records = ref([])
  const counts = ref({ all: 0, unread: 0, system: 0, trade: 0, comment: 0 })
  const loading = ref(false)

  const unread = computed(() => counts.value.unread || 0)

  async function fetchMessages(params) {
    loading.value = true
    try {
      const data = await messageApi.getMessages({ pageNum: 1, pageSize: 30, ...params })
      records.value = data.records
      counts.value = data.counts
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchUnread() {
    const data = await messageApi.getUnreadCount()
    counts.value = { ...counts.value, unread: data.count }
    return data.count
  }

  async function markRead(id) {
    const data = await messageApi.readMessage(id)
    const target = records.value.find((item) => item.id === id)
    if (target && !target.is_read) {
      target.is_read = 1
      counts.value = { ...counts.value, unread: Math.max(0, counts.value.unread - 1) }
    }
    return data
  }

  async function markAllRead(type) {
    await messageApi.readAllMessages(type)
    records.value = records.value.map((item) =>
      (!type || type === 'all' || item.type === type ? { ...item, is_read: 1 } : item),
    )
    await fetchUnread()
  }

  function reset() {
    records.value = []
    counts.value = { all: 0, unread: 0, system: 0, trade: 0, comment: 0 }
  }

  return { records, counts, loading, unread, fetchMessages, fetchUnread, markRead, markAllRead, reset }
})
