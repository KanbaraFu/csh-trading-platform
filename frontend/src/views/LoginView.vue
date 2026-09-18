<script setup>
import { computed, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendCaptcha } from '@/api/auth'
import { useUserStore } from '@/store/user'
import { useMessageStore } from '@/store/message'
import { useFavoriteStore } from '@/store/favorite'
import { useCartStore } from '@/store/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()
const favoriteStore = useFavoriteStore()
const cartStore = useCartStore()

const activeTab = ref('login')
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
const agreed = ref(true)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({ phone: '13800000001', password: '123456' })
const registerForm = reactive({ phone: '', code: '', password: '', confirm: '', nickname: '' })

let timer = null

const phoneRule = [
  { required: true, message: '请输入手机号 / 学号', trigger: 'blur' },
  { pattern: /^1\d{10}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' },
]

const loginRules = {
  phone: phoneRule,
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

const registerRules = {
  phone: phoneRule,
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请设置登录密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

const codeButtonText = computed(() => (countdown.value > 0 ? `${countdown.value}s 后重发` : '获取验证码'))

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

onUnmounted(() => clearInterval(timer))

async function hydrateStores() {
  await Promise.all([
    messageStore.fetchUnread().catch(() => 0),
    favoriteStore.fetchIds().catch(() => []),
    cartStore.fetchCart().catch(() => null),
  ])
}

async function afterLogin(message) {
  ElMessage.success(message)
  await hydrateStores()
  const redirect = route.query.redirect
  router.replace(typeof redirect === 'string' && redirect ? redirect : { name: 'home' })
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!agreed.value) {
    ElMessage.warning('请先阅读并同意《校园交易服务协议》')
    return
  }
  loading.value = true
  try {
    await userStore.loginForm({ phone: loginForm.phone, password: loginForm.password })
    await afterLogin(`欢迎回来，${userStore.nickname}`)
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!agreed.value) {
    ElMessage.warning('请先阅读并同意《校园交易服务协议》')
    return
  }
  loading.value = true
  try {
    await userStore.registerForm({
      phone: registerForm.phone,
      password: registerForm.password,
      code: registerForm.code,
      nickname: registerForm.nickname,
    })
    await afterLogin('注册成功，已自动为你登录')
  } finally {
    loading.value = false
  }
}

async function handleSendCode() {
  const valid = await registerFormRef.value?.validateField('phone').catch(() => false)
  if (valid === false) return
  if (countdown.value > 0) return
  sending.value = true
  try {
    const data = await sendCaptcha({ phone: registerForm.phone })
    registerForm.code = data.code
    ElMessage.success(`验证码已发送至 ${data.phone}，演示环境已自动填入：${data.code}`)
    startCountdown()
  } finally {
    sending.value = false
  }
}

function fillDemo() {
  loginForm.phone = '13800000001'
  loginForm.password = '123456'
  ElMessage.success('已填入演示账号 13800000001 / 123456')
}

function forgotPassword() {
  ElMessage.info('演示项目暂未开放找回密码，可直接使用演示账号登录体验')
}
</script>

<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <section class="brand-panel">
      <router-link class="back" :to="{ name: 'home' }">
        <el-icon :size="14"><ArrowLeft /></el-icon>
        返回首页
      </router-link>

      <div class="brand-content">
        <div class="brand-logo">
          <span class="mark"><el-icon :size="24"><Shop /></el-icon></span>
          <div>
            <h1>淘学二手</h1>
            <p>Campus Second-hand Trading Platform</p>
          </div>
        </div>

        <h2>让闲置在校园里<br />流转起来</h2>
        <p class="lead">
          教材、数码、生活用品、运动装备……同学们的真实闲置，校内当面交易，省心又划算。
        </p>

        <svg class="illustration" viewBox="0 0 420 260" aria-hidden="true">
          <defs>
            <linearGradient id="sky" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="#ffffff" stop-opacity="0.32" />
              <stop offset="100%" stop-color="#ffffff" stop-opacity="0.06" />
            </linearGradient>
          </defs>
          <rect x="18" y="24" width="384" height="212" rx="26" fill="url(#sky)" />
          <circle cx="336" cy="76" r="26" fill="#ffe6d4" opacity="0.9" />
          <path d="M60 200h300" stroke="#ffffff" stroke-width="3" opacity="0.5" stroke-linecap="round" />
          <rect x="86" y="126" width="104" height="74" rx="10" fill="#ffffff" opacity="0.92" />
          <rect x="98" y="140" width="58" height="8" rx="4" fill="#14b8a6" opacity="0.8" />
          <rect x="98" y="156" width="80" height="7" rx="3.5" fill="#cbd5e1" />
          <rect x="98" y="170" width="66" height="7" rx="3.5" fill="#cbd5e1" />
          <rect x="150" y="94" width="96" height="106" rx="10" fill="#ffffff" opacity="0.86" />
          <path d="M170 130h56M170 146h56M170 162h34" stroke="#0d9488" stroke-width="8" stroke-linecap="round" opacity="0.75" />
          <path d="M282 200v-46h44v46z" fill="#ff7a45" opacity="0.9" />
          <path d="M290 154v-12a19 19 0 0 1 38 0v12" fill="none" stroke="#ffffff" stroke-width="5" stroke-linecap="round" />
          <circle cx="356" cy="176" r="18" fill="#ffffff" opacity="0.85" />
          <path d="M349 176l6 6 12-13" stroke="#0d9488" stroke-width="5" fill="none" stroke-linecap="round" stroke-linejoin="round" />
          <circle cx="60" cy="182" r="9" fill="#5eead4" />
        </svg>

        <ul class="selling-points">
          <li><el-icon><CircleCheckFilled /></el-icon> 校内同学面对面交易，见面验货更放心</li>
          <li><el-icon><CircleCheckFilled /></el-icon> 教材 / 数码 / 生活用品，闲置一搜就有</li>
          <li><el-icon><CircleCheckFilled /></el-icon> 收藏、购物车、订单状态全程可视化</li>
        </ul>
      </div>
    </section>

    <!-- 右侧表单区 -->
    <section class="form-panel">
      <div class="form-card">
        <div class="tabs">
          <button class="tab" :class="{ active: activeTab === 'login' }" type="button" @click="activeTab = 'login'">
            登录
          </button>
          <button class="tab" :class="{ active: activeTab === 'register' }" type="button" @click="activeTab = 'register'">
            注册
          </button>
          <span class="tab-ink" :style="{ transform: activeTab === 'login' ? 'translateX(0)' : 'translateX(100%)' }"></span>
        </div>

        <p class="form-desc">
          {{ activeTab === 'login' ? '使用手机号登录，继续你的校园闲置交易之旅' : '注册校园账号，立刻发布你的第一件闲置' }}
        </p>

        <!-- 登录表单 -->
        <el-form
          v-if="activeTab === 'login'"
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          class="form"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="手机号 / 学号" prop="phone">
            <el-input v-model="loginForm.phone" size="large" placeholder="请输入手机号" :prefix-icon="'Iphone'" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              size="large"
              type="password"
              show-password
              placeholder="请输入登录密码"
              :prefix-icon="'Lock'"
            />
          </el-form-item>

          <div class="helper-row">
            <el-checkbox v-model="agreed">记住登录状态</el-checkbox>
            <button class="link-btn" type="button" @click="forgotPassword">忘记密码？</button>
          </div>

          <button class="primary-btn" type="button" :disabled="loading" @click="handleLogin">
            {{ loading ? '正在登录…' : '登录' }}
          </button>

<!--          <button class="demo-btn" type="button" @click="fillDemo">-->
<!--            <el-icon :size="14"><MagicStick /></el-icon>-->
<!--            一键填入演示账号-->
<!--          </button>-->
        </el-form>

        <!-- 注册表单 -->
        <el-form
          v-else
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-position="top"
          class="form"
        >
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="registerForm.phone" size="large" placeholder="请输入 11 位手机号" :prefix-icon="'Iphone'" />
          </el-form-item>
          <el-form-item label="验证码" prop="code">
            <div class="code-row">
              <el-input v-model="registerForm.code" size="large" placeholder="请输入 6 位验证码" :prefix-icon="'Key'" />
              <button class="code-btn" type="button" :disabled="countdown > 0 || sending" @click="handleSendCode">
                {{ sending ? '发送中…' : codeButtonText }}
              </button>
            </div>
          </el-form-item>
          <el-form-item label="设置密码" prop="password">
            <el-input v-model="registerForm.password" size="large" type="password" show-password placeholder="至少 6 位" :prefix-icon="'Lock'" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirm">
            <el-input v-model="registerForm.confirm" size="large" type="password" show-password placeholder="请再次输入密码" :prefix-icon="'Lock'" />
          </el-form-item>
          <el-form-item label="昵称（选填）" prop="nickname">
            <el-input v-model="registerForm.nickname" size="large" maxlength="12" placeholder="例如：计科院小满" :prefix-icon="'User'" />
          </el-form-item>

          <div class="helper-row">
            <el-checkbox v-model="agreed">我已阅读并同意《校园交易服务协议》</el-checkbox>
          </div>

          <button class="primary-btn" type="button" :disabled="loading" @click="handleRegister">
            {{ loading ? '正在注册…' : '注册并登录' }}
          </button>
        </el-form>

        <p class="switch-hint">
          {{ activeTab === 'login' ? '还没有账号？' : '已经有账号了？' }}
          <button class="link-btn" type="button" @click="activeTab = activeTab === 'login' ? 'register' : 'login'">
            {{ activeTab === 'login' ? '立即注册' : '去登录' }}
          </button>
        </p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  background: #f5f7fa;
}

.brand-panel {
  position: relative;
  padding: 42px 56px;
  color: #fff;
  background: linear-gradient(150deg, #0f766e 0%, #14b8a6 52%, #2dd4bf 100%);
  overflow: hidden;
}

.brand-panel::after {
  content: '';
  position: absolute;
  width: 420px;
  height: 420px;
  right: -140px;
  bottom: -160px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.28), transparent 68%);
}

.back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.86);
  transition: color 0.2s;
}

.back:hover {
  color: #fff;
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 520px;
  margin-top: 40px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-logo .mark {
  width: 52px;
  height: 52px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
}

.brand-logo h1 {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 2px;
}

.brand-logo p {
  font-size: 12px;
  letter-spacing: 1.4px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.72);
}

.brand-content h2 {
  margin-top: 34px;
  font-size: 40px;
  line-height: 1.3;
  font-weight: 700;
  letter-spacing: 1px;
}

.lead {
  margin-top: 16px;
  font-size: 14px;
  line-height: 1.85;
  color: rgba(255, 255, 255, 0.88);
  max-width: 420px;
}

.illustration {
  width: 100%;
  max-width: 420px;
  margin: 22px 0 8px;
}

.selling-points {
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.92);
}

.selling-points li {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.form-card {
  width: 100%;
  max-width: 430px;
  padding: 34px 34px 28px;
  background: #fff;
  border-radius: 22px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.1);
}

.tabs {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  border-bottom: 1px solid var(--c-border);
}

.tab {
  padding: 12px 0;
  border: none;
  background: transparent;
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text-muted);
  cursor: pointer;
  transition: color 0.2s;
}

.tab.active {
  color: var(--c-primary-dark);
}

.tab-ink {
  position: absolute;
  left: 0;
  bottom: -1px;
  width: 50%;
  height: 3px;
  border-radius: 3px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  transition: transform 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}

.form-desc {
  margin: 16px 0 18px;
  font-size: 13px;
  color: var(--c-text-muted);
}

.form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.form :deep(.el-form-item__label) {
  font-size: 13px;
  color: var(--c-text-sub);
  padding-bottom: 4px;
}

.form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px var(--c-border) inset;
}

.form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--c-primary) inset;
}

.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.code-btn {
  flex-shrink: 0;
  width: 122px;
  border: 1px solid var(--c-primary);
  border-radius: 10px;
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.code-btn:hover:not(:disabled) {
  background: var(--c-primary-soft);
}

.code-btn:disabled {
  border-color: var(--c-border);
  background: #f5f7fa;
  color: var(--c-text-muted);
  cursor: not-allowed;
}

.helper-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: -4px 0 16px;
}

.primary-btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  box-shadow: 0 12px 26px rgba(13, 148, 136, 0.28);
  transition: transform 0.2s, box-shadow 0.2s;
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(13, 148, 136, 0.34);
}

.primary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/*.demo-btn {
  width: 100%;
  height: 42px;
  margin-top: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px dashed var(--c-primary-light);
  border-radius: 12px;
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}*/

.demo-btn:hover {
  background: #e3fbf5;
}

.link-btn {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 13px;
  color: var(--c-primary-dark);
  cursor: pointer;
}

.link-btn:hover {
  text-decoration: underline;
}

.switch-hint {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: var(--c-text-muted);
}

@media (max-width: 1023.98px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding: 30px 24px 26px;
  }

  .brand-content {
    margin-top: 22px;
  }

  .brand-content h2 {
    margin-top: 20px;
    font-size: 28px;
  }

  .illustration,
  .selling-points {
    display: none;
  }
}

@media (max-width: 767.98px) {
  .form-panel {
    padding: 20px 16px 40px;
  }

  .form-card {
    padding: 24px 20px 20px;
    box-shadow: 0 16px 36px rgba(15, 23, 42, 0.08);
  }
}
</style>
