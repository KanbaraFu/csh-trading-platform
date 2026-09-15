import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as favoriteApi from '@/api/favorite'

export const useFavoriteStore = defineStore('favorite', () => {
  const ids = ref([])
  const records = ref([])
  const loaded = ref(false)
  const total = ref(0)
  const totalAmount = ref(0)

  const idSet = computed(() => new Set(ids.value))
  const count = computed(() => ids.value.length)

  function isFavorited(productId) {
    return idSet.value.has(Number(productId))
  }

  async function fetchIds() {
    ids.value = await favoriteApi.getFavoriteIds()
    loaded.value = true
    return ids.value
  }

  async function fetchList(params) {
    const data = await favoriteApi.getFavorites({ pageNum: 1, pageSize: 8, ...params })
    records.value = data.records
    total.value = data.total
    totalAmount.value = Number(data.total_amount || 0)
    // ids 始终取全量，避免分页导致非当前页商品的心形收藏态错乱
    await fetchIds()
    loaded.value = true
    return data
  }

  async function toggle(productId) {
    const id = Number(productId)
    if (isFavorited(id)) {
      await favoriteApi.removeFavorite(id)
      ids.value = ids.value.filter((item) => item !== id)
      const target = records.value.find((item) => item.product_id === id)
      if (target) {
        totalAmount.value = Number(
          Math.max(0, totalAmount.value - Number(target.product?.price || 0)).toFixed(2),
        )
      }
      records.value = records.value.filter((item) => item.product_id !== id)
      total.value = Math.max(0, total.value - 1)
      return false
    }
    await favoriteApi.addFavorite(id)
    ids.value = [...ids.value, id]
    return true
  }

  async function remove(productId) {
    const id = Number(productId)
    await favoriteApi.removeFavorite(id)
    ids.value = ids.value.filter((item) => item !== id)
    const target = records.value.find((item) => item.product_id === id)
    if (target) {
      totalAmount.value = Number(
        Math.max(0, totalAmount.value - Number(target.product?.price || 0)).toFixed(2),
      )
    }
    records.value = records.value.filter((item) => item.product_id !== id)
    total.value = Math.max(0, total.value - 1)
  }

  function reset() {
    ids.value = []
    records.value = []
    total.value = 0
    totalAmount.value = 0
    loaded.value = false
  }

  return {
    ids, records, loaded, total, totalAmount, idSet, count, isFavorited, fetchIds, fetchList, toggle, remove, reset,
  }
})
