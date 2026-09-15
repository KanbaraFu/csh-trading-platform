<script setup>
defineProps({
  title: { type: String, default: '这里还什么都没有' },
  description: { type: String, default: '换个条件试试，或者去首页逛逛吧' },
  actionText: { type: String, default: '' },
  icon: { type: String, default: 'box' },
})

defineEmits(['action'])
</script>

<template>
  <div class="empty-state">
    <div class="empty-state__art">
      <svg v-if="icon === 'search'" viewBox="0 0 120 120" class="art">
        <circle cx="60" cy="60" r="44" fill="#f0fdfa" />
        <circle cx="53" cy="53" r="20" fill="none" stroke="#5eead4" stroke-width="5" />
        <path d="M68 68l16 16" stroke="#0d9488" stroke-width="6" stroke-linecap="round" />
        <circle cx="90" cy="34" r="5" fill="#ff7a45" opacity="0.7" />
      </svg>
      <svg v-else-if="icon === 'cart'" viewBox="0 0 120 120" class="art">
        <circle cx="60" cy="60" r="44" fill="#f0fdfa" />
        <path d="M32 40h56l-6 40a8 8 0 0 1-8 7H46a8 8 0 0 1-8-7l-6-40z" fill="#5eead4" />
        <path d="M48 40v-6a12 12 0 0 1 24 0v6" fill="none" stroke="#0d9488" stroke-width="5" stroke-linecap="round" />
      </svg>
      <svg v-else-if="icon === 'order'" viewBox="0 0 120 120" class="art">
        <circle cx="60" cy="60" r="44" fill="#f0fdfa" />
        <rect x="34" y="30" width="52" height="62" rx="8" fill="#5eead4" />
        <path d="M46 52h28M46 64h28M46 76h16" stroke="#0d9488" stroke-width="5" stroke-linecap="round" />
      </svg>
      <svg v-else viewBox="0 0 120 120" class="art">
        <circle cx="60" cy="60" r="44" fill="#f0fdfa" />
        <rect x="30" y="46" width="60" height="42" rx="10" fill="#5eead4" />
        <path d="M60 46L42 30M60 46l18-16" stroke="#0d9488" stroke-width="5" stroke-linecap="round" />
        <circle cx="92" cy="32" r="6" fill="#ff7a45" opacity="0.75" />
      </svg>
    </div>
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
    <el-button v-if="actionText" type="primary" round @click="$emit('action')">{{ actionText }}</el-button>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 56px 20px;
  text-align: center;
}

.empty-state__art {
  margin-bottom: 6px;
}

.art {
  width: 132px;
  height: 132px;
}

.empty-state h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
}

.empty-state p {
  font-size: 13px;
  color: var(--c-text-muted);
  max-width: 320px;
}

.empty-state .el-button {
  margin-top: 8px;
}
</style>
