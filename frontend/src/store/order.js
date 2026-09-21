import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as orderApi from '@/api/order'

export const useOrderStore = defineStore('order', () => {
  const records = ref([])
  const counts = ref({ all: 0, 0: 0, 1: 0, 2: 0, 3: 0, 4: 0 })
  const total = ref(0)
  const loading = ref(false)
  const current = ref(null)

  const pendingPayCount = computed(() => counts.value[0] || 0)

  async function fetchOrders(params) {
    loading.value = true
    try {
      const data = await orderApi.getOrders({ pageNum: 1, pageSize: 10, ...params })
      console.log(data)
      records.value = data.records
      counts.value = data.counts
      total.value = data.total
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchDetail(id) {
    current.value = await orderApi.getOrderDetail(id)
    return current.value
  }

  async function create(payload) {
    return orderApi.createOrder(payload)
  }

  async function pay(id, payMethod) {
    return orderApi.payOrder(id, { payMethod })
  }

  async function ship(id) {
    return orderApi.shipOrder(id)
  }

  async function confirm(id) {
    return orderApi.confirmOrder(id)
  }

  async function cancel(id) {
    return orderApi.cancelOrder(id)
  }

  function reset() {
    records.value = []
    current.value = null
  }

  return { records, counts, total, loading, current, pendingPayCount, fetchOrders, fetchDetail, create, pay, ship, confirm, cancel, reset }
})
