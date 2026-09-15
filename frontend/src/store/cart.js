import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as cartApi from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)

  const totalQuantity = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const selectedItems = computed(() => items.value.filter((item) => item.selected))
  const selectedCount = computed(() => selectedItems.value.reduce((sum, item) => sum + item.quantity, 0))
  const selectedAmount = computed(() =>
    Number(selectedItems.value.reduce((sum, item) => sum + item.subtotal, 0).toFixed(2)),
  )
  const allSelected = computed(() => items.value.length > 0 && selectedItems.value.length === items.value.length)

  async function fetchCart() {
    loading.value = true
    try {
      const data = await cartApi.getCart()
      items.value = data.records
      return data
    } finally {
      loading.value = false
    }
  }

  async function add(productId, quantity = 1) {
    const data = await cartApi.addCart({ productId, quantity })
    await fetchCart()
    return data
  }

  async function changeQuantity(id, quantity) {
    const data = await cartApi.updateCart(id, { quantity })
    const target = items.value.find((item) => item.id === id)
    if (target) {
      target.quantity = data.quantity
      target.selected = data.selected
      target.subtotal = data.subtotal
    }
    return data
  }

  async function toggleSelected(id, selected) {
    const data = await cartApi.updateCart(id, { selected })
    const target = items.value.find((item) => item.id === id)
    if (target) {
      target.selected = data.selected
      target.subtotal = data.subtotal
    }
  }

  async function toggleAll(selected) {
    const data = await cartApi.selectAllCart(selected)
    items.value = data.records
  }

  async function remove(id) {
    const data = await cartApi.removeCart(id)
    items.value = data.records
  }

  async function preview(ids) {
    return cartApi.getCartPreview(ids?.length ? { ids: ids.join(',') } : {})
  }

  function reset() {
    items.value = []
  }

  return {
    items,
    loading,
    totalQuantity,
    selectedItems,
    selectedCount,
    selectedAmount,
    allSelected,
    fetchCart,
    add,
    changeQuantity,
    toggleSelected,
    toggleAll,
    remove,
    preview,
    reset,
  }
})
