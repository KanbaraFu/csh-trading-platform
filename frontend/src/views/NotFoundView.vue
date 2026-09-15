<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="page not-found">
    <svg viewBox="0 0 240 160" class="art">
      <circle cx="120" cy="80" r="62" fill="#f0fdfa" />
      <path d="M78 96h84" stroke="#5eead4" stroke-width="6" stroke-linecap="round" />
      <path d="M92 96V68a28 28 0 0 1 56 0v28" fill="none" stroke="#0d9488" stroke-width="6" stroke-linecap="round" />
      <circle cx="108" cy="78" r="4.5" fill="#ff7a45" />
      <circle cx="132" cy="78" r="4.5" fill="#ff7a45" />
      <path d="M176 40l10 10-10 10-10-10z" fill="#ffd6bd" />
    </svg>
    <h1>404</h1>
    <p>这个页面像是被同学淘走啦，去首页看看其他好物吧</p>
    <div class="actions">
      <el-button type="primary" round @click="router.push({ name: 'home' })">返回首页</el-button>
      <el-button round @click="router.push({ name: 'products' })">去逛商品</el-button>
      <el-button round @click="router.back()">返回上一页</el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  min-height: 60vh;
  gap: 8px;
}

.art {
  width: 220px;
  height: 150px;
}

.not-found h1 {
  font-size: 44px;
  font-weight: 700;
  color: var(--c-primary-dark);
  letter-spacing: 4px;
}

.not-found p {
  font-size: 14px;
  color: var(--c-text-muted);
  max-width: 380px;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
  flex-wrap: wrap;
  justify-content: center;
}
</style>
