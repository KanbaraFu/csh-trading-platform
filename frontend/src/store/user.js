import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'
import { TOKEN_KEY, USER_ID_KEY, USER_KEY } from '@/constants/auth'

// 读取本地用户资料快照，脏数据直接丢弃，避免初始化时抛异常
function readCachedUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const user = ref(readCachedUser())
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const addresses = ref([])
  const defaultAddressId = ref(0)

  const isLogin = computed(() => Boolean(token.value && user.value))
  const defaultAddress = computed(() => addresses.value.find((item) => item.id === defaultAddressId.value) || null)
  const avatar = computed(() => user.value?.avatar || '')
  const nickname = computed(() => user.value?.nickname || '未登录')

  // 统一落盘：token 是请求头来源，userId 与资料快照用于刷新后立即恢复登录态
  function persistSession() {
    if (token.value) localStorage.setItem(TOKEN_KEY, token.value)
    if (user.value?.id) {
      localStorage.setItem(USER_ID_KEY, String(user.value.id))
      localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    }
  }

  function applyLogin(result) {
    token.value = result?.token || localStorage.getItem(TOKEN_KEY) || ''
    // 后端 LoginVO 返回字段为 userVO，本地 Mock 返回 user，这里统一兼容
    user.value =
      result?.user ||
      result?.userVO ||
      (result?.userId ? { id: result.userId, username: result.username } : null)
    persistSession()
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

  async function loadProfile() {
    const data = await authApi.getMe()
    user.value = data
    token.value = localStorage.getItem(TOKEN_KEY) || token.value
    persistSession()
    return data
  }

  async function fetchMe() {
    if (!token.value) return null
    return loadProfile()
  }

  let pendingProfile = null

  // 刷新或首次进入时补齐登录态：本地已有快照则直接复用，避免重复请求
  function ensureSession() {
    if (!token.value || user.value) return Promise.resolve(user.value)
    if (!pendingProfile) {
      pendingProfile = loadProfile()
        .catch(() => {
          clearSession()
          return null
        })
        .finally(() => {
          pendingProfile = null
        })
    }
    return pendingProfile
  }

  async function updateProfile(payload) {
    const data = await authApi.updateMe(payload)
    user.value = data
    persistSession()
    return data
  }

  async function logout() {
    await authApi.logout().catch(() => null)
    clearSession()
  }

  function clearSession() {
    user.value = null
    token.value = ''
    addresses.value = []
    defaultAddressId.value = 0
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_ID_KEY)
    localStorage.removeItem(USER_KEY)
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
    ensureSession,
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
