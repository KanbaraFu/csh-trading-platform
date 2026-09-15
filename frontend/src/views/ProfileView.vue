<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AddressForm from '@/components/AddressForm.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { useCartStore } from '@/store/cart'
import { useFavoriteStore } from '@/store/favorite'
import { useMessageStore } from '@/store/message'
import { useOrderStore } from '@/store/order'
import { useProductStore } from '@/store/product'
import { useUserStore } from '@/store/user'
import { getMyProducts } from '@/api/auth'
import { formatDateTime, maskPhone, toAmount } from '@/utils/format'
import { svgCover } from '@/utils/image'

const router = useRouter()
const userStore = useUserStore()
const favoriteStore = useFavoriteStore()
const cartStore = useCartStore()
const messageStore = useMessageStore()
const orderStore = useOrderStore()
const productStore = useProductStore()

const activeTab = ref('profile')
const saving = ref(false)
const addressDialog = ref(false)
const editingAddress = ref(null)
const savingAddress = ref(false)
const myProducts = ref([])
const productsLoading = ref(true)
const myProductsTotal = ref(0)
const myProductsPage = ref(1)
const myProductsPageSize = 5
const avatarInput = ref(null)
const addressFormRef = ref(null)

const form = reactive({
  nickname: '',
  phone: '',
  gender: 0,
  college: '',
  student_no: '',
  bio: '',
})

const user = computed(() => userStore.user)
const addresses = computed(() => userStore.addresses)
const genderLabel = computed(() => ['未设置', '男生', '女生'][form.gender] || '未设置')

const quickLinks = computed(() => [
  { label: '我的订单', value: orderStore.counts.all || 0, icon: 'Tickets', route: 'orders' },
  { label: '待付款', value: orderStore.pendingPayCount, icon: 'Wallet', route: 'orders', query: { status: 0 } },
  { label: '我的收藏', value: favoriteStore.count, icon: 'Star', route: 'favorites' },
  { label: '购物车', value: cartStore.totalQuantity, icon: 'ShoppingCart', route: 'cart' },
  { label: '未读消息', value: messageStore.unread, icon: 'Bell', route: 'messages' },
])

function syncForm() {
  const source = user.value
  if (!source) return
  form.nickname = source.nickname || ''
  form.phone = source.phone || ''
  form.gender = source.gender || 0
  form.college = source.college || ''
  form.student_no = source.student_no || ''
  form.bio = source.bio || ''
}

async function loadMyProducts(page = myProductsPage.value) {
  productsLoading.value = true
  try {
    const data = await getMyProducts({ pageNum: page, pageSize: myProductsPageSize })
    myProducts.value = data.records
    myProductsTotal.value = data.total
    // 下架等操作后当前页可能为空，自动回退到最后一页
    const lastPage = Math.max(1, Math.ceil(data.total / myProductsPageSize))
    if (page > lastPage) {
      myProductsPage.value = lastPage
      const fallback = await getMyProducts({ pageNum: lastPage, pageSize: myProductsPageSize })
      myProducts.value = fallback.records
      myProductsTotal.value = fallback.total
    } else {
      myProductsPage.value = page
    }
  } finally {
    productsLoading.value = false
  }
}

function changeProductsPage(page) {
  loadMyProducts(page)
}

async function saveProfile() {
  if (!form.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  saving.value = true
  try {
    await userStore.updateProfile({
      nickname: form.nickname.trim(),
      phone: form.phone,
      gender: form.gender,
      college: form.college,
      student_no: form.student_no,
      bio: form.bio,
    })
    ElMessage.success('个人资料已保存')
  } finally {
    saving.value = false
  }
}

function pickAvatar() {
  avatarInput.value?.click()
}

function onAvatarChange(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (file.size > 3 * 1024 * 1024) {
    ElMessage.warning('头像文件不能超过 3MB')
    return
  }
  const reader = new FileReader()
  reader.onload = async () => {
    await userStore.updateProfile({ avatar: reader.result })
    ElMessage.success('头像已更新')
  }
  reader.onerror = () => ElMessage.error('头像读取失败，请重试')
  reader.readAsDataURL(file)
  event.target.value = ''
}

async function restoreAvatar() {
  await userStore.updateProfile({ avatar: '' })
  ElMessage.success('已恢复默认头像')
}

function openAddressDialog(address = null) {
  editingAddress.value = address
  addressDialog.value = true
}

async function saveAddress(payload) {
  savingAddress.value = true
  try {
    const { id, ...rest } = payload
    await userStore.saveAddress(rest, id)
    ElMessage.success(id ? '地址已更新' : '地址已添加')
    addressDialog.value = false
  } finally {
    savingAddress.value = false
  }
}

async function removeAddress(item) {
  await ElMessageBox.confirm(`确认删除「${item.region} ${item.detail}」这条地址吗？`, '删除地址', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await userStore.removeAddress(item.id)
  ElMessage.success('地址已删除')
}

async function setDefaultAddress(item) {
  await userStore.setDefaultAddress(item.id)
  ElMessage.success('已设为默认地址')
}

async function offlineProduct(item) {
  await ElMessageBox.confirm(`确认下架「${item.title}」吗？下架后买家将无法搜索到该商品。`, '下架商品', {
    confirmButtonText: '确认下架',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await productStore.offline(item.id)
  ElMessage.success('商品已下架')
  await loadMyProducts()
}

async function relistProduct(item) {
  await productStore.relist(item.id)
  ElMessage.success('商品已重新上架')
  await loadMyProducts()
}

async function logout() {
  await ElMessageBox.confirm('确认退出当前账号吗？', '退出登录', {
    confirmButtonText: '退出登录',
    cancelButtonText: '再逛逛',
    type: 'warning',
  })
  await userStore.logout()
  favoriteStore.reset()
  cartStore.reset()
  messageStore.reset()
  orderStore.reset()
  ElMessage.success('已退出登录')
  router.push({ name: 'home' })
}

onMounted(async () => {
  await userStore.fetchMe().catch(() => null)
  syncForm()
  await Promise.all([
    userStore.fetchAddresses().catch(() => null),
    favoriteStore.fetchList().catch(() => null),
    cartStore.fetchCart().catch(() => null),
    orderStore.fetchOrders({ pageSize: 1 }).catch(() => null),
    messageStore.fetchUnread().catch(() => 0),
    loadMyProducts(),
  ])
})
</script>

<template>
  <div class="page profile-page">
    <div class="layout fade-up">
      <!-- 左侧信息卡 -->
      <aside class="side-card">
        <div class="avatar-wrap">
          <UserAvatar :src="user?.avatar" :name="user?.nickname" :seed="user?.id" :size="86" />
          <button class="avatar-edit" type="button" title="更换头像" @click="pickAvatar">
            <el-icon :size="14"><Camera /></el-icon>
          </button>
          <input ref="avatarInput" class="hidden-input" type="file" accept="image/*" @change="onAvatarChange" />
        </div>

        <h2>{{ user?.nickname || '校园用户' }}</h2>
        <p class="college">{{ user?.college || '暂未填写学院信息' }}</p>

        <div class="tags">
          <el-tag v-if="user?.auth_status === 1" type="success" effect="light" round size="small">已认证学生</el-tag>
          <el-tag v-else type="warning" effect="light" round size="small">待认证</el-tag>
          <el-tag type="info" effect="plain" round size="small">{{ genderLabel }}</el-tag>
        </div>

        <p class="bio">{{ user?.bio || '这位同学还没有写个人简介～' }}</p>

        <ul class="info-list">
          <li><span>学号</span><span>{{ user?.student_no || '未填写' }}</span></li>
          <li><span>手机号</span><span>{{ maskPhone(user?.phone) || '未填写' }}</span></li>
          <li><span>注册时间</span><span>{{ formatDateTime(user?.create_time).slice(0, 10) }}</span></li>
        </ul>

        <button class="ghost-btn" type="button" @click="restoreAvatar">恢复默认头像</button>
      </aside>

      <!-- 右侧内容 -->
      <section class="main-card">
        <div class="quick-grid">
          <button
            v-for="link in quickLinks"
            :key="link.label"
            class="quick-item"
            type="button"
            @click="router.push({ name: link.route, query: link.query })"
          >
            <span class="quick-icon"><el-icon :size="18"><component :is="link.icon" /></el-icon></span>
            <span class="quick-value">{{ link.value }}</span>
            <span class="quick-label">{{ link.label }}</span>
          </button>
        </div>

        <nav class="tabs">
          <button class="tab" :class="{ active: activeTab === 'profile' }" type="button" @click="activeTab = 'profile'">
            资料编辑
          </button>
          <button class="tab" :class="{ active: activeTab === 'address' }" type="button" @click="activeTab = 'address'">
            收货地址
          </button>
          <button class="tab" :class="{ active: activeTab === 'products' }" type="button" @click="activeTab = 'products'">
            我的发布
          </button>
          <button class="tab logout" type="button" @click="logout">退出登录</button>
        </nav>

        <!-- 资料编辑 -->
        <div v-if="activeTab === 'profile'" class="panel">
          <el-form :model="form" label-position="top" class="form">
            <div class="grid-2">
              <el-form-item label="昵称">
                <el-input v-model="form.nickname" maxlength="12" show-word-limit placeholder="给自己取个好记的昵称" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="form.phone" maxlength="11" placeholder="用于接收交易通知" />
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="form.gender">
                  <el-radio :value="0">不公开</el-radio>
                  <el-radio :value="1">男生</el-radio>
                  <el-radio :value="2">女生</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="学院">
                <el-input v-model="form.college" maxlength="30" placeholder="例如：计算机科学与技术学院" />
              </el-form-item>
              <el-form-item label="学号">
                <el-input v-model="form.student_no" maxlength="20" placeholder="用于学生身份认证" />
              </el-form-item>
            </div>
            <el-form-item label="个人简介">
              <el-input
                v-model="form.bio"
                type="textarea"
                :rows="3"
                maxlength="80"
                show-word-limit
                placeholder="介绍一下你的交易习惯，例如：毕业清仓，支持校内当面验货"
              />
            </el-form-item>
            <div class="form-actions">
              <button class="primary-btn" type="button" :disabled="saving" @click="saveProfile">
                {{ saving ? '保存中…' : '保存资料' }}
              </button>
            </div>
          </el-form>
        </div>

        <!-- 收货地址 -->
        <div v-else-if="activeTab === 'address'" class="panel">
          <div class="panel-head">
            <h3>收货地址（{{ addresses.length }}）</h3>
            <button class="primary-btn small" type="button" @click="openAddressDialog(null)">新增地址</button>
          </div>

          <div v-if="addresses.length" class="address-list">
            <article v-for="item in addresses" :key="item.id" class="address-card">
              <div class="address-top">
                <strong>{{ item.receiver_name }}</strong>
                <span>{{ item.phone }}</span>
                <span v-if="item.is_default" class="default-tag">默认</span>
              </div>
              <p class="address-text">{{ item.region }} {{ item.detail }}</p>
              <div class="address-actions">
                <button v-if="!item.is_default" class="link-btn" type="button" @click="setDefaultAddress(item)">
                  设为默认
                </button>
                <button class="link-btn" type="button" @click="openAddressDialog(item)">编辑</button>
                <button class="link-btn danger" type="button" @click="removeAddress(item)">删除</button>
              </div>
            </article>
          </div>
          <p v-else class="empty-text">还没有收货地址，添加后下单时就能一键选择啦</p>
        </div>

        <!-- 我的发布 -->
        <div v-else class="panel">
          <div class="panel-head">
            <h3>我的发布（{{ myProductsTotal }}）</h3>
            <button class="primary-btn small" type="button" @click="router.push({ name: 'publish' })">发布新闲置</button>
          </div>

          <div v-if="productsLoading" class="skeleton-block products-skeleton"></div>

          <div v-else-if="myProducts.length" class="product-list">
            <article v-for="item in myProducts" :key="item.id" class="product-row">
              <img :src="item.cover" :alt="item.title" @error="(event) => (event.target.src = svgCover(item.title, item.id))" />
              <div class="product-body">
                <p class="product-title">{{ item.title }}</p>
                <p class="product-meta">
                  <span>¥{{ toAmount(item.price) }}</span>
                  <span>{{ item.category_name }}</span>
                  <span>{{ item.view_count }} 次浏览</span>
                  <span>{{ item.sales_count }} 件已售</span>
                </p>
              </div>
              <div class="product-status">
                <el-tag :type="item.status === 1 ? 'success' : item.status === 2 ? 'warning' : 'info'" round size="small">
                  {{ item.status === 1 ? '在售' : item.status === 2 ? '已售出' : '已下架' }}
                </el-tag>
              </div>
              <div class="product-actions">
                <button class="link-btn" type="button" @click="router.push({ name: 'product-detail', params: { id: item.id } })">
                  查看
                </button>
                <button class="link-btn" type="button" @click="router.push({ name: 'publish', query: { id: item.id } })">
                  编辑
                </button>
                <button v-if="item.status === 1" class="link-btn danger" type="button" @click="offlineProduct(item)">
                  下架
                </button>
                <button v-else-if="item.status === 0" class="link-btn" type="button" @click="relistProduct(item)">
                  重新上架
                </button>
              </div>
            </article>
          </div>

          <p v-else class="empty-text">还没有发布过闲置，把宿舍里的闲置拍两张照片就能上架啦</p>

          <div v-if="!productsLoading && myProductsTotal > myProductsPageSize" class="pager">
            <el-pagination
              background
              layout="prev, pager, next, total"
              :total="myProductsTotal"
              :page-size="myProductsPageSize"
              :current-page="myProductsPage"
              @current-change="changeProductsPage"
            />
          </div>
        </div>
      </section>
    </div>

    <AddressForm v-model="addressDialog" :address="editingAddress" :saving="savingAddress" @saved="saveAddress" />
  </div>
</template>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 18px;
  align-items: start;
}

.side-card,
.main-card {
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.side-card {
  position: sticky;
  top: calc(var(--nav-height) + 16px);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 26px 22px;
  text-align: center;
}

.avatar-wrap {
  position: relative;
}

.avatar-edit {
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border: 2px solid #fff;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s;
}

.avatar-edit:hover {
  transform: scale(1.08);
}

.hidden-input {
  display: none;
}

.side-card h2 {
  margin-top: 14px;
  font-size: 18px;
  font-weight: 700;
}

.college {
  margin-top: 4px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 12px;
}

.bio {
  margin-top: 14px;
  font-size: 12px;
  line-height: 1.8;
  color: var(--c-text-sub);
}

.info-list {
  width: 100%;
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13px;
  color: var(--c-text-sub);
  text-align: left;
}

.info-list li {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.info-list li span:first-child {
  color: var(--c-text-muted);
}

.ghost-btn {
  width: 100%;
  margin-top: 20px;
  padding: 9px 0;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.ghost-btn:hover {
  color: var(--c-primary-dark);
  border-color: var(--c-primary-light);
  background: var(--c-primary-mist);
}

.main-card {
  padding: 20px 22px 26px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px 8px;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  background: #fff;
  cursor: pointer;
  transition: all 0.22s;
}

.quick-item:hover {
  transform: translateY(-3px);
  border-color: var(--c-primary-light);
  box-shadow: var(--shadow-card);
}

.quick-icon {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 11px;
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
}

.quick-value {
  font-size: 17px;
  font-weight: 700;
  color: var(--c-text);
}

.quick-label {
  font-size: 11px;
  color: var(--c-text-muted);
}

.tabs {
  display: flex;
  gap: 6px;
  margin: 22px 0 18px;
  border-bottom: 1px solid var(--c-border);
  padding-bottom: 2px;
}

.tab {
  padding: 9px 16px;
  border: none;
  border-radius: 10px 10px 0 0;
  background: transparent;
  font-size: 14px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.tab:hover {
  color: var(--c-primary-dark);
  background: var(--c-primary-mist);
}

.tab.active {
  color: var(--c-primary-dark);
  font-weight: 600;
  box-shadow: inset 0 -2px 0 var(--c-primary);
}

.tab.logout {
  margin-left: auto;
  color: var(--c-text-muted);
}

.tab.logout:hover {
  color: var(--c-danger);
  background: #fef2f2;
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 20px;
}

.form :deep(.el-form-item__label) {
  font-size: 13px;
  color: var(--c-text-sub);
  padding-bottom: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.primary-btn {
  padding: 10px 28px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 22px rgba(13, 148, 136, 0.24);
  transition: transform 0.2s;
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.primary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.primary-btn.small {
  padding: 8px 18px;
  font-size: 13px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.panel-head h3 {
  font-size: 15px;
  font-weight: 700;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.address-card {
  padding: 16px;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  transition: all 0.2s;
}

.address-card:hover {
  border-color: var(--c-primary-light);
  box-shadow: var(--shadow-card);
}

.address-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.address-top strong {
  font-size: 14px;
}

.address-top span {
  font-size: 12px;
  color: var(--c-text-sub);
}

.default-tag {
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--c-primary-soft);
  color: var(--c-primary-dark) !important;
  font-size: 11px !important;
}

.address-text {
  margin-top: 8px;
  font-size: 12px;
  color: var(--c-text-muted);
  line-height: 1.7;
}

.address-actions {
  display: flex;
  gap: 14px;
  margin-top: 10px;
}

.link-btn {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 12px;
  color: var(--c-primary-dark);
  cursor: pointer;
}

.link-btn:hover {
  text-decoration: underline;
}

.link-btn.danger {
  color: var(--c-danger);
}

.empty-text {
  padding: 26px 0;
  text-align: center;
  font-size: 13px;
  color: var(--c-text-muted);
}

.products-skeleton {
  height: 200px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border-radius: 14px;
  background: var(--c-surface-alt);
  flex-wrap: wrap;
}

.product-row img {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  object-fit: cover;
  flex-shrink: 0;
  background: #eef1f5;
}

.product-body {
  flex: 1;
  min-width: 160px;
}

.product-title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 6px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.product-meta span:first-child {
  color: var(--c-accent-strong);
  font-weight: 600;
}

.product-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

@media (max-width: 1023.98px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .side-card {
    position: static;
  }

  .quick-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .address-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767.98px) {
  .main-card {
    padding: 16px;
  }

  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .grid-2 {
    grid-template-columns: 1fr;
  }

  .tabs {
    overflow-x: auto;
    scrollbar-width: none;
  }

  .product-row {
    align-items: flex-start;
  }
}
</style>
