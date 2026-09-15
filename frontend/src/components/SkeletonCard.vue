<script setup>
defineProps({
  count: { type: Number, default: 8 },
})
</script>

<template>
  <div class="product-grid">
    <div v-for="index in count" :key="index" class="skeleton-card">
      <div class="skeleton-block thumb"></div>
      <div class="body">
        <div class="skeleton-block line"></div>
        <div class="skeleton-block line short"></div>
        <div class="skeleton-block line price"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.skeleton-card {
  background: #fff;
  border-radius: var(--radius-card);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.thumb {
  aspect-ratio: 1 / 1;
  border-radius: 0;
}

.body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.line {
  height: 13px;
}

.line.short {
  width: 62%;
}

.line.price {
  height: 18px;
  width: 42%;
}
</style>
