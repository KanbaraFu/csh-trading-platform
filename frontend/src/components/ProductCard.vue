<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import UserAvatar from './UserAvatar.vue'
import { useUserStore } from '@/store/user'
import { useFavoriteStore } from '@/store/favorite'
import { discountText, formatDateTime, shortNumber, toAmount } from '@/utils/format'
import { svgCover } from '@/utils/image'

const props = defineProps({
  product: { type: Object, required: true },
  showFavorite: { type: Boolean, default: true },
  showMeta: { type: Boolean, default: true },
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const favoriteStore = useFavoriteStore()

const cover = ref('')
watch(
  () => props.product,
  (product) => {
    cover.value = product?.cover || svgCover(product?.title, product?.id)
  },
  { immediate: true },
)

const favorited = computed(() => favoriteStore.isFavorited(props.product.id))
const discount = computed(() => discountText(props.product.price, props.product.original_price))
const soldOut = computed(() => Number(props.product.status) === 2 || Number(props.product.stock) === 0)

function onImageError() {
  cover.value = svgCover(props.product.title, props.product.id)
}

async function toggleFavorite() {
  if (!userStore.isLogin) {
    ElMessage.warning('登录后才能收藏商品')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  const added = await favoriteStore.toggle(props.product.id)
  ElMessage.success(added ? '已加入收藏，可在「我的收藏」查看' : '已取消收藏')
}
</script>

<template>
  <article class="product-card">
    <router-link class="thumb" :to="{ name: 'product-detail', params: { id: product.id } }">
      <img :src="cover" :alt="product.title" loading="lazy" @error="onImageError" />
      <span v-if="discount" class="discount">{{ discount }}</span>
      <span v-if="soldOut" class="sold-out">已售出</span>
    </router-link>

    <button
      v-if="showFavorite"
      class="fav-btn"
      :class="{ active: favorited }"
      type="button"
      :title="favorited ? '取消收藏' : '收藏商品'"
      @click.stop="toggleFavorite"
    >
      <el-icon :size="16"><StarFilled v-if="favorited" /><Star v-else /></el-icon>
    </button>

    <div class="info">
      <router-link class="title" :to="{ name: 'product-detail', params: { id: product.id } }">
        {{ product.title }}
      </router-link>

      <div class="price-row">
        <span class="price price-now">
          <small>¥</small>{{ toAmount(product.price) }}
        </span>
        <span v-if="Number(product.original_price) > Number(product.price)" class="price-origin">
          ¥{{ toAmount(product.original_price) }}
        </span>
      </div>

      <div v-if="showMeta" class="meta">
        <div class="seller">
          <UserAvatar :src="product.seller_avatar" :name="product.seller_nickname" :seed="product.seller_id" :size="22" />
          <span class="seller-name">{{ product.seller_nickname }}</span>
        </div>
        <span class="views">
          <el-icon :size="13"><View /></el-icon>
          {{ shortNumber(product.view_count) }}
        </span>
      </div>

      <div class="tags">
        <span class="tag tag-condition">{{ product.condition }}</span>
        <span class="tag tag-category">{{ product.category_name }}</span>
        <span class="time">{{ formatDateTime(product.create_time).slice(5, 16) }}</span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.product-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  transition: transform 0.24s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.24s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-hover);
}

.thumb {
  position: relative;
  display: block;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: #f3f6f9;
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.22, 1, 0.36, 1);
}

.product-card:hover .thumb img {
  transform: scale(1.06);
}

.discount {
  position: absolute;
  left: 10px;
  top: 10px;
  padding: 3px 8px;
  border-radius: 8px;
  background: linear-gradient(135deg, #ffa06b, var(--c-accent));
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(255, 122, 69, 0.35);
}

.sold-out {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(17, 24, 39, 0.52);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
}

.fav-btn {
  position: absolute;
  right: 10px;
  top: 10px;
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: var(--c-text-muted);
  cursor: pointer;
  backdrop-filter: blur(6px);
  transition: all 0.2s;
}

.fav-btn:hover {
  color: var(--c-accent);
  transform: scale(1.08);
}

.fav-btn.active {
  color: var(--c-accent-strong);
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding: 12px 13px 14px;
}

.title {
  font-size: 14px;
  line-height: 1.45;
  color: var(--c-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
  transition: color 0.2s;
}

.product-card:hover .title {
  color: var(--c-primary-dark);
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 7px;
}

.price-now {
  font-size: 19px;
  color: var(--c-accent-strong);
  font-weight: 700;
}

.price-now small {
  font-size: 12px;
  margin-right: 1px;
}

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.seller {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.seller-name {
  font-size: 12px;
  color: var(--c-text-sub);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.views {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: var(--c-text-muted);
  flex-shrink: 0;
}

.tags {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tag {
  padding: 2px 7px;
  border-radius: 6px;
  font-size: 11px;
}

.tag-condition {
  color: var(--c-primary-dark);
  background: var(--c-primary-mist);
}

.tag-category {
  color: var(--c-text-sub);
  background: #f3f6f9;
}

.time {
  margin-left: auto;
  font-size: 11px;
  color: var(--c-text-muted);
  white-space: nowrap;
}
</style>
