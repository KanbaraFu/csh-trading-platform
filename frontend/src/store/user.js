import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'
import { TOKEN_KEY, USER_ID_KEY } from '@/constants/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const addresses = ref([])
  const defaultAddressId = ref(0)

  const isLogin = computed(() => Boolean(token.value && user.value))
  const defaultAddress = computed(() => addresses.value.find((item) => item.id === defaultAddressId.value) || null)
  const avatar = computed(() => user.value?.avatar || '')
  const nickname = computed(() => user.value?.nickname || '未登录')

  function applyLogin(result) {
    token.value = result.token || localStorage.getItem(TOKEN_KEY) || ''
    user.value = result.user
  }

  async function loginForm(payload) {
    const result = await authApi.login(payload)
    applyLogin(result)
    return result
  }

  async function registerForm(payload) {
    const result = await authApi.register(payload)
    applyLogin(result)
    return result
  }

  async function fetchMe() {
    if (!localStorage.getItem(USER_ID_KEY)) return null
    const data = await authApi.getMe()
    user.value = data
    token.value = localStorage.getItem(TOKEN_KEY) || token.value
    return data
  }

  async function updateProfile(payload) {
    const data = await authApi.updateMe(payload)
    user.value = data
    return data
  }

  async function logout() {
    await authApi.logout().catch(() => null)
    user.value = null
    token.value = ''
    addresses.value = []
    defaultAddressId.value = 0
  }

  function clearSession() {
    user.value = null
    token.value = ''
    addresses.value = []
    defaultAddressId.value = 0
  }

  async function fetchAddresses(params) {
    const data = await authApi.getAddresses(params)
    addresses.value = data.records
    defaultAddressId.value = data.selected_id
    return data
  }

  async function saveAddress(payload, id) {
    const data = id ? await authApi.updateAddress(id, payload) : await authApi.createAddress(payload)
    await fetchAddresses()
    return data
  }

  async function removeAddress(id) {
    await authApi.deleteAddress(id)
    await fetchAddresses()
  }

  async function setDefaultAddress(id) {
    await authApi.updateAddress(id, { is_default: true })
    await fetchAddresses()
  }

  function setSelectedAddress(id) {
    defaultAddressId.value = id
  }

  return {
    user,
    token,
    addresses,
    defaultAddressId,
    isLogin,
    defaultAddress,
    avatar,
    nickname,
    loginForm,
    registerForm,
    fetchMe,
    updateProfile,
    logout,
    clearSession,
    fetchAddresses,
    saveAddress,
    removeAddress,
    setDefaultAddress,
    setSelectedAddress,
  }
})
