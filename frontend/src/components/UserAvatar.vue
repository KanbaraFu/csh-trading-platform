<script setup>
import { computed, ref, watch } from 'vue'
import { svgAvatar } from '@/utils/image'

const props = defineProps({
  src: { type: String, default: '' },
  name: { type: String, default: '同学' },
  size: { type: Number, default: 40 },
  seed: { type: [String, Number], default: '' },
})

const fallback = computed(() => svgAvatar(props.name || '同学', props.seed || props.name || 0))
const current = ref(props.src || fallback.value)

watch(
  () => props.src,
  (value) => {
    current.value = value || fallback.value
  },
)

function onError() {
  current.value = fallback.value
}
</script>

<template>
  <img
    class="user-avatar"
    :src="current"
    :alt="`${name} 的头像`"
    :style="{ width: `${size}px`, height: `${size}px` }"
    @error="onError"
  />
</template>

<style scoped>
.user-avatar {
  border-radius: 50%;
  object-fit: cover;
  background: #eef1f5;
  flex-shrink: 0;
}
</style>
