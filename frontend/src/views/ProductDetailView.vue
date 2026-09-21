<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CommentList from '@/components/CommentList.vue'
import ProductCard from '@/components/ProductCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { useCartStore } from '@/store/cart'
import { useFavoriteStore } from '@/store/favorite'
import { useProductStore } from '@/store/product'
import { useUserStore } from '@/store/user'
import { createComment } from '@/api/message'
import { getComments } from '@/api/product'
import { discountText, formatDateTime, fromNow, productStatusLabel, sellerNickname, shortNumber, toAmount } from '@/utils/format'
import { svgCover } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const productStore = useProductStore()
const cartStore = useCartStore()
const favoriteStore = useFavoriteStore()
const userStore = useUserStore()

const loading = ref(true)
const product = ref(null)
const related = ref([])
const comments = ref([])
const commentLoading = ref(true)
const commentTotal = ref(0)
const commentPage = ref(1)
const commentPageSize = 5
const submitting = ref(false)
const quantity = ref(1)
const activeImage = ref(0)
const contactVisible = ref(false)

const images = computed(() => product.value?.images?.length ? product.value.images : [product.value?.cover])
const favorited = computed(() => (product.value ? favoriteStore.isFavorited(product.value.id) : false))
// 是否可购买：优先用后端返回的 is_available；该字段缺失时按状态/库存自行兜底，
// 避免接口未返回该字段时 Boolean(undefined) 恒为 false，导致所有商品都显示「已售出」。
const available = computed(() => {
  const detail = product.value
  if (!detail) return false
  if (detail.is_available !== null && detail.is_available !== undefined) {
    return Boolean(detail.is_available)
  }
  const status = Number(detail.status)
  if (status === 0 || status === 2) return false
  if (detail.stock === null || detail.stock === undefined) return status === 1
  return Number(detail.stock) > 0
})
const isOwner = computed(() => product.value && userStore.user?.id === product.value.seller_id)
// 卖家昵称：详情接口字段名是 seller_name，统一兼容后模板可直接使用
const sellerName = computed(() => sellerNickname(product.value))
// 展示状态：'已下架' / '已售出' / ''（在售）
const statusLabel = computed(() => productStatusLabel(product.value))
// 购买按钮文案：不可购买时按下架 / 已售出分别提示，避免下架商品也被写成「已售出」
const buyButtonText = computed(() => {
  if (isOwner.value) return '我发布的商品'
  if (available.value) return '立即购买'
  return statusLabel.value ? `该商品${statusLabel.value}` : '暂不可购买'
})
// 卖家查看自己商品时的提示，按状态给出对应后续操作
const ownerTip = computed(() => {
  if (statusLabel.value === '已下架') {
    return '这是你发布的商品，当前已下架，可在「个人中心 - 我的发布」中重新上架。'
  }
  if (statusLabel.value === '已售出') {
    return '这是你发布的商品，当前已售出，可在「个人中心」查看相关订单。'
  }
  return '这是你发布的商品，可在「个人中心 - 我的发布」中编辑或下架。'
})
const discount = computed(() => discountText(product.value?.price, product.value?.original_price))
const totalPrice = computed(() => Number((Number(product.value?.price || 0) * quantity.value).toFixed(2)))

function resolveImage(url) {
  return url || svgCover(product.value?.title, product.value?.id)
}

function onImageError(event) {
  event.target.src = svgCover(product.value?.title, product.value?.id)
}

async function loadComments(page = commentPage.value) {
  commentLoading.value = true
  try {
    let targetPage = page
    let data = await getComments(product.value.id, { pageNum: targetPage, pageSize: commentPageSize })
    const lastPage = Math.max(1, Math.ceil(data.total / commentPageSize))
    if (targetPage > lastPage) {
      targetPage = lastPage
      data = await getComments(product.value.id, { pageNum: targetPage, pageSize: commentPageSize })
    }
    comments.value = data.records
    commentTotal.value = data.total
    commentPage.value = targetPage
  } finally {
    commentLoading.value = false
  }
}

function changeCommentPage(page) {
  loadComments(page)
}

async function loadDetail() {
  loading.value = true
  try {
    product.value = await productStore.fetchDetail(route.params.id)
    activeImage.value = 0
    quantity.value = 1
    const relatedData = await productStore.queryProducts({
      pageNum: 1,
      pageSize: 5,
      categoryId: product.value.category_id,
      sort: 'hot',
    })
    related.value = relatedData.records.filter((item) => item.id !== product.value.id).slice(0, 4)
    await loadComments()
  } finally {
    loading.value = false
  }
}

function requireLogin() {
  if (userStore.isLogin) return true
  ElMessage.warning('请先登录后再继续操作')
  router.push({ name: 'login', query: { redirect: route.fullPath } })
  return false
}

async function toggleFavorite() {
  if (!requireLogin()) return
  const added = await favoriteStore.toggle(product.value.id)
  ElMessage.success(added ? '已加入收藏' : '已取消收藏')
}

async function addToCart() {
  if (!requireLogin()) return
  if (isOwner.value) {
    ElMessage.warning('这是你自己发布的商品，不能加入购物车')
    return
  }
  await cartStore.add(product.value.id, quantity.value)
  ElMessage.success(`已加入购物车，共 ${quantity.value} 件`)
}

function buyNow() {
  if (!requireLogin()) return
  if (isOwner.value) {
    ElMessage.warning('不能购买自己发布的商品')
    return
  }
  router.push({ name: 'checkout', query: { productId: product.value.id, quantity: quantity.value } })
}

async function submitComment(payload) {
  if (!requireLogin()) return
  submitting.value = true
  try {
    await createComment({ productId: product.value.id, ...payload })
    ElMessage.success('评论发布成功')
    // 新评论追加在最后一页，回复则停留在当前页
    const targetPage = payload.parentId
        ? commentPage.value
        : Math.max(1, Math.ceil((commentTotal.value + 1) / commentPageSize))
    await loadComments(targetPage)
  } finally {
    submitting.value = false
  }
}

watch(() => route.params.id, (id) => {
  if (id) loadDetail()
})

onMounted(loadDetail)
</script>

<template>
  <div class="page detail-page">
    <div v-if="loading" class="detail-skeleton">
      <div class="skeleton-block gallery"></div>
      <div class="side">
        <div class="skeleton-block bar-lg"></div>
        <div class="skeleton-block bar"></div>
        <div class="skeleton-block bar short"></div>
        <div class="skeleton-block block"></div>
      </div>
    </div>

    <template v-else-if="product">
      <nav class="crumbs">
        <router-link :to="{ name: 'home' }">首页</router-link>
        <el-icon :size="12"><ArrowRight /></el-icon>
        <router-link :to="{ name: 'products', query: { categoryId: product.category_id } }">
          {{ product.category_name }}
        </router-link>
        <el-icon :size="12"><ArrowRight /></el-icon>
        <span class="current">{{ product.title }}</span>
      </nav>

      <section class="detail-main fade-up">
        <div class="gallery">
          <div class="stage">
            <el-carousel
              :autoplay="false"
              height="420px"
              indicator-position="none"
              arrow="hover"
              @change="activeImage = $event"
            >
              <el-carousel-item v-for="(url, index) in images" :key="index">
                <img class="gallery__img" :src="resolveImage(url)" :alt="product.title" @error="onImageError" />
              </el-carousel-item>
            </el-carousel>
            <!-- 与列表卡片一致：已下架用灰蓝、已售出用深色，一眼可区分 -->
            <span v-if="statusLabel" class="stage__mask" :class="{ 'is-offline': statusLabel === '已下架' }">
              {{ statusLabel }}
            </span>
          </div>

          <div class="thumbs">
            <button
              v-for="(url, index) in images"
              :key="index"
              class="thumb"
              :class="{ active: activeImage === index }"
              type="button"
              @click="activeImage = index"
            >
              <img :src="resolveImage(url)" :alt="`商品图 ${index + 1}`" @error="onImageError" />
            </button>
          </div>
        </div>

        <div class="info">
          <div class="info__tags">
            <StatusTag :status="product.status" kind="product" />
            <span class="condition">{{ product.condition }}</span>
            <span v-if="discount" class="discount">{{ discount }}</span>
          </div>

          <h1 class="title">{{ product.title }}</h1>

          <div class="price-box">
            <div class="price-now"><small>¥</small>{{ toAmount(product.price) }}</div>
            <div v-if="Number(product.original_price) > Number(product.price)" class="origin">
              原价 <span>¥{{ toAmount(product.original_price) }}</span>
            </div>
          </div>

          <ul class="meta-list">
            <li>
              <span class="label">库存</span>
              <span>{{ product.stock }} 件</span>
            </li>
            <li>
              <span class="label">浏览量</span>
              <span>{{ shortNumber(product.view_count) }} 次</span>
            </li>
            <li>
              <span class="label">已售</span>
              <span>{{ product.sales_count }} 件</span>
            </li>
            <li>
              <span class="label">交易地点</span>
              <span>{{ product.location }}</span>
            </li>
            <li>
              <span class="label">发布时间</span>
              <span>{{ formatDateTime(product.create_time) }}</span>
            </li>
          </ul>

          <div class="seller-card">
            <UserAvatar :src="product.seller_avatar" :name="sellerName" :seed="product.seller_id" :size="46" />
            <div class="seller-card__body">
              <strong>{{ sellerName }}</strong>
              <span>{{ product.seller_college || '校园认证用户' }}</span>
            </div>
            <button class="contact-btn" type="button" @click="contactVisible = true">联系卖家</button>
          </div>

          <div class="buy-row">
            <div class="qty">
              <span class="label">数量</span>
              <el-input-number v-model="quantity" :min="1" :max="Math.max(1, product.stock)" size="default" />
            </div>
            <div class="total">
              合计 <strong>¥{{ toAmount(totalPrice) }}</strong>
            </div>
          </div>

          <div class="action-row">
            <button class="ghost-btn" :class="{ active: favorited }" type="button" @click="toggleFavorite">
              <el-icon :size="16"><StarFilled v-if="favorited" /><Star v-else /></el-icon>
              {{ favorited ? '已收藏' : '收藏' }}
            </button>
            <button class="cart-btn" type="button" :disabled="!available || isOwner" @click="addToCart">
              <el-icon :size="16"><ShoppingCart /></el-icon>
              加入购物车
            </button>
            <button class="buy-btn" type="button" :disabled="!available || isOwner" @click="buyNow">
              {{ buyButtonText }}
            </button>
          </div>

          <p v-if="isOwner" class="owner-tip">{{ ownerTip }}</p>
        </div>
      </section>

      <section class="fade-up">
        <div class="section-title"><h2>商品描述</h2></div>
        <div class="description">
          <p>{{ product.description }}</p>
          <div class="desc-tags">
            <span>分类：{{ product.category_name }}</span>
            <span>成色：{{ product.condition }}</span>
            <span>交易方式：{{ product.location }}</span>
          </div>
        </div>
      </section>

      <section class="fade-up">
        <CommentList
          :comments="comments"
          :loading="commentLoading"
          :submitting="submitting"
          :total="commentTotal"
          :page-num="commentPage"
          :page-size="commentPageSize"
          @submit="submitComment"
          @page-change="changeCommentPage"
        />
      </section>

      <section v-if="related.length" class="fade-up">
        <div class="section-title">
          <h2>同类推荐</h2>
          <span class="more" @click="router.push({ name: 'products', query: { categoryId: product.category_id } })">
            查看更多 →
          </span>
        </div>
        <div class="product-grid">
          <ProductCard v-for="item in related" :key="item.id" :product="item" />
        </div>
      </section>
    </template>

    <el-dialog v-model="contactVisible" title="联系卖家" width="380px" align-center>
      <div class="contact">
        <UserAvatar :src="product?.seller_avatar" :name="sellerName" :seed="product?.seller_id" :size="58" />
        <p class="contact__name">{{ sellerName }}</p>
        <p class="contact__college">{{ product?.seller_college || '校园认证用户' }}</p>
        <p class="contact__tip">暂不提供实时聊天能力，建议在商品评论区留言约定交易时间与地点。</p>
        <el-button type="primary" round style="width: 100%" @click="contactVisible = false">我知道了</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-skeleton {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.gallery {
  height: 420px;
  border-radius: var(--radius-card);
}

/* 主图区：状态遮罩的定位容器 */
.stage {
  position: relative;
  border-radius: var(--radius-card);
  overflow: hidden;
}

.stage__mask {
  position: absolute;
  inset: 0;
  z-index: 3;
  display: grid;
  place-items: center;
  background: rgba(17, 24, 39, 0.45);
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 4px;
  pointer-events: none;
}

/* 已下架：灰蓝底色，与「已售出」的深色区分 */
.stage__mask.is-offline {
  background: rgba(100, 116, 139, 0.5);
}

.side {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bar-lg {
  height: 26px;
  width: 70%;
}

.bar {
  height: 16px;
}

.bar.short {
  width: 40%;
}

.block {
  height: 220px;
}

.crumbs {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.crumbs a:hover {
  color: var(--c-primary);
}

.crumbs .current {
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 26px;
  padding: 24px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.gallery__img {
  width: 100%;
  height: 420px;
  object-fit: cover;
  border-radius: 14px;
}

.thumbs {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.thumb {
  width: 68px;
  height: 68px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  background: #f3f6f9;
  transition: border-color 0.2s, transform 0.2s;
}

.thumb:hover {
  transform: translateY(-2px);
}

.thumb.active {
  border-color: var(--c-primary);
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info__tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.condition,
.discount {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.condition {
  color: var(--c-primary-dark);
  background: var(--c-primary-mist);
}

.discount {
  color: #fff;
  background: linear-gradient(135deg, #ffa06b, var(--c-accent));
}

.title {
  font-size: 22px;
  line-height: 1.45;
  font-weight: 700;
}

.price-box {
  display: flex;
  align-items: baseline;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 14px;
  background: linear-gradient(120deg, #fff7f1, #fffdfb);
  border: 1px solid #ffe6d5;
}

.price-now {
  font-size: 32px;
  font-weight: 700;
  color: var(--c-accent-strong);
  font-family: 'DIN Alternate', 'PingFang SC', sans-serif;
}

.price-now small {
  font-size: 16px;
  margin-right: 2px;
}

.origin {
  font-size: 12px;
  color: var(--c-text-muted);
}

.origin span {
  text-decoration: line-through;
}

.meta-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
  font-size: 13px;
  color: var(--c-text-sub);
}

.meta-list li {
  display: flex;
  gap: 8px;
}

.meta-list .label {
  color: var(--c-text-muted);
  flex-shrink: 0;
}

.seller-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: var(--c-surface-alt);
}

.seller-card__body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.seller-card__body strong {
  font-size: 14px;
}

.seller-card__body span {
  font-size: 12px;
  color: var(--c-text-muted);
}

.contact-btn {
  margin-left: auto;
  padding: 7px 16px;
  border: 1px solid var(--c-primary);
  border-radius: 999px;
  background: #fff;
  color: var(--c-primary-dark);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.contact-btn:hover {
  background: var(--c-primary-mist);
}

.buy-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}

.qty {
  display: flex;
  align-items: center;
  gap: 10px;
}

.qty .label {
  font-size: 13px;
  color: var(--c-text-muted);
}

.total {
  font-size: 13px;
  color: var(--c-text-sub);
}

.total strong {
  font-size: 20px;
  color: var(--c-accent-strong);
}

.action-row {
  display: flex;
  gap: 10px;
}

.ghost-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 12px 18px;
  border: 1px solid var(--c-border);
  border-radius: 12px;
  background: #fff;
  color: var(--c-text-sub);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.ghost-btn:hover,
.ghost-btn.active {
  color: var(--c-accent-strong);
  border-color: #ffd3bd;
  background: #fff7f1;
}

.cart-btn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 18px;
  border: 1px solid var(--c-primary);
  border-radius: 12px;
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.cart-btn:hover:not(:disabled) {
  background: var(--c-primary-soft);
}

.buy-btn {
  flex: 1;
  padding: 12px 18px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffa06b, var(--c-accent));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 22px rgba(255, 122, 69, 0.3);
  transition: transform 0.2s, box-shadow 0.2s;
}

.buy-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(255, 122, 69, 0.38);
}

.cart-btn:disabled,
.buy-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}

.owner-tip {
  font-size: 12px;
  color: var(--c-text-muted);
}

.description {
  padding: 22px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  font-size: 14px;
  line-height: 1.95;
  color: var(--c-text-sub);
}

.desc-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 18px;
}

.desc-tags span {
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--c-surface-alt);
  font-size: 12px;
  color: var(--c-text-sub);
}

.contact {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  text-align: center;
}

.contact__name {
  font-size: 15px;
  font-weight: 600;
}

.contact__college {
  font-size: 12px;
  color: var(--c-text-muted);
}

.contact__tip {
  margin: 10px 0 16px;
  font-size: 12px;
  color: var(--c-text-sub);
  line-height: 1.7;
}

@media (max-width: 1023.98px) {
  .detail-main,
  .detail-skeleton {
    grid-template-columns: 1fr;
  }

  .gallery__img {
    height: 320px;
  }
}

@media (max-width: 767.98px) {
  .detail-main {
    padding: 16px;
    gap: 18px;
  }

  .title {
    font-size: 18px;
  }

  .price-now {
    font-size: 26px;
  }

  .action-row {
    flex-wrap: wrap;
  }

  .ghost-btn {
    padding: 12px 14px;
  }

  .cart-btn,
  .buy-btn {
    min-width: 140px;
  }
}
</style>
