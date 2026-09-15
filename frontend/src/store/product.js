import { ref } from 'vue'
import { defineStore } from 'pinia'
import * as productApi from '@/api/product'

export const useProductStore = defineStore('product', () => {
  const categories = ref([])
  const categoryLoaded = ref(false)
  const hotWords = ref([])

  async function fetchCategories(force = false) {
    if (categoryLoaded.value && !force) return categories.value
    categories.value = await productApi.getCategories()
    categoryLoaded.value = true
    return categories.value
  }

  function categoryName(id) {
    return categories.value.find((item) => item.id === Number(id))?.name ?? '未分类'
  }

  async function queryProducts(params) {
    return productApi.getProducts(params)
  }

  async function fetchDetail(id) {
    return productApi.getProductDetail(id)
  }

  async function publish(payload) {
    return productApi.createProduct(payload)
  }

  async function edit(id, payload) {
    return productApi.updateProduct(id, payload)
  }

  async function offline(id) {
    return productApi.offlineProduct(id)
  }

  async function relist(id) {
    return productApi.relistProduct(id)
  }

  return {
    categories,
    categoryLoaded,
    hotWords,
    fetchCategories,
    categoryName,
    queryProducts,
    fetchDetail,
    publish,
    edit,
    offline,
    relist,
  }
})
