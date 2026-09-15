<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ImageUploader from '@/components/ImageUploader.vue'
import ProductCard from '@/components/ProductCard.vue'
import { CONDITION_OPTIONS, TRADE_PLACES } from '@/constants/product'
import { useProductStore } from '@/store/product'
import { toAmount } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const productStore = useProductStore()

const formRef = ref(null)
const publishing = ref(false)
const previewVisible = ref(false)
const editId = ref(Number(route.query.id || 0))

const form = reactive({
  images: [],
  title: '',
  description: '',
  category_id: null,
  price: null,
  original_price: null,
  stock: 1,
  condition: '九成新',
  location: '图书馆一楼大厅',
})

const rules = {
  title: [
    { required: true, message: '请填写商品标题', trigger: 'blur' },
    { min: 4, max: 60, message: '标题建议 4 - 60 个字', trigger: 'blur' },
  ],
  category_id: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  description: [
    { required: true, message: '请填写商品描述', trigger: 'blur' },
    { min: 10, message: '描述至少 10 个字，方便买家了解商品', trigger: 'blur' },
  ],
  price: [{ required: true, message: '请填写售价', trigger: 'blur' }],
  images: [{ type: 'array', required: true, min: 1, message: '至少上传一张商品图片', trigger: 'change' }],
}

const previewProduct = computed(() => ({
  id: editId.value || 9999,
  title: form.title || '商品标题预览',
  cover: form.images[0] || '',
  price: Number(form.price || 0),
  original_price: Number(form.original_price || form.price || 0),
  condition: form.condition,
  category_name: productStore.categoryName(form.category_id),
  seller_nickname: '我',
  seller_avatar: '',
  seller_id: 0,
  view_count: 0,
  create_time: new Date().toISOString(),
  status: 1,
  stock: form.stock,
}))

onMounted(async () => {
  await productStore.fetchCategories()
  if (!editId.value) return
  const data = await productStore.fetchDetail(editId.value)
  Object.assign(form, {
    images: data.images || [],
    title: data.title,
    description: data.description,
    category_id: data.category_id,
    price: data.price,
    original_price: data.original_price,
    stock: data.stock,
    condition: data.condition,
    location: data.location,
  })
})

function validate(isPreview = false) {
  return formRef.value
    ?.validate()
    .then(() => true)
    .catch(() => {
      if (!isPreview) ElMessage.warning('还有必填项未完成，请检查表单提示')
      return false
    })
}

async function openPreview() {
  const valid = await validate(true)
  if (!valid) {
    ElMessage.warning('请先补全商品信息再预览')
    return
  }
  previewVisible.value = true
}

async function submit() {
  const valid = await validate()
  if (!valid) return
  publishing.value = true
  try {
    const payload = {
      ...form,
      price: Number(form.price),
      original_price: Number(form.original_price || form.price),
      stock: Number(form.stock || 1),
    }
    const data = editId.value
      ? await productStore.edit(editId.value, payload)
      : await productStore.publish(payload)
    ElMessage.success(editId.value ? '商品信息已更新' : '发布成功，商品已上架')
    router.push({ name: 'product-detail', params: { id: data.id } })
  } finally {
    publishing.value = false
  }
}

function resetForm() {
  form.images = []
  form.title = ''
  form.description = ''
  form.category_id = null
  form.price = null
  form.original_price = null
  form.stock = 1
  form.condition = '九成新'
  form.location = '图书馆一楼大厅'
  formRef.value?.clearValidate()
}
</script>

<template>
  <div class="page publish-page">
    <header class="page-head fade-up">
      <div>
        <h1>{{ editId ? '编辑商品' : '发布闲置' }}</h1>
        <p>把闲置信息填完整，成交速度会更快哦～发布前可以先预览一下展示效果</p>
      </div>
      <div class="head-actions">
        <button class="ghost-btn" type="button" @click="resetForm">重置表单</button>
        <button class="ghost-btn" type="button" @click="openPreview">预览效果</button>
      </div>
    </header>

    <section class="publish-panel fade-up">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="publish-form">
        <el-form-item label="商品图片" prop="images">
          <ImageUploader v-model="form.images" :max="6" />
        </el-form-item>

        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" maxlength="60" show-word-limit placeholder="例如：考研英语一 历年真题（含手写笔记）" />
        </el-form-item>

        <div class="grid-2">
          <el-form-item label="商品分类" prop="category_id">
            <el-select v-model="form.category_id" placeholder="请选择分类" style="width: 100%">
              <el-option v-for="item in productStore.categories" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="商品成色" prop="condition">
            <el-select v-model="form.condition" style="width: 100%">
              <el-option v-for="item in CONDITION_OPTIONS" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            maxlength="600"
            show-word-limit
            placeholder="介绍一下购买时间、使用频率、有无瑕疵、附赠配件等，越详细越容易成交"
          />
        </el-form-item>

        <div class="grid-3">
          <el-form-item label="售价（元）" prop="price">
            <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="原价（选填）">
            <el-input-number v-model="form.original_price" :min="0" :precision="2" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="可售数量">
            <el-input-number v-model="form.stock" :min="1" :max="99" style="width: 100%" />
          </el-form-item>
        </div>

        <el-form-item label="交易地点" prop="location">
          <el-select v-model="form.location" style="width: 100%">
            <el-option v-for="item in TRADE_PLACES" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <div class="tips-card">
          <el-icon :size="16"><InfoFilled /></el-icon>
          <div>
            <strong>发布小贴士</strong>
            <p>建议在描述中写明交易时间与地点，商品图片尽量使用实拍图；请勿发布违禁物品或虚假信息。</p>
          </div>
        </div>

        <div class="submit-row">
          <button class="primary-btn" type="button" :disabled="publishing" @click="submit">
            {{ publishing ? '提交中…' : editId ? '保存修改' : '立即发布' }}
          </button>
        </div>
      </el-form>
    </section>

    <el-dialog v-model="previewVisible" title="商品展示效果预览" width="460px" align-center>
      <div class="preview-body">
        <ProductCard :product="previewProduct" :show-favorite="false" :show-meta="false" />
        <div class="preview-detail">
          <p class="preview-price">售价 ¥{{ toAmount(form.price) }}</p>
          <p class="preview-desc">{{ form.description }}</p>
          <p class="preview-meta">成色：{{ form.condition }} · 交易地点：{{ form.location }} · 库存：{{ form.stock }} 件</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">继续修改</el-button>
        <el-button type="primary" :loading="publishing" @click="submit">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.publish-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.page-head h1 {
  font-size: 24px;
  font-weight: 700;
}

.page-head p {
  margin-top: 6px;
  font-size: 13px;
  color: var(--c-text-muted);
}

.head-actions {
  display: flex;
  gap: 10px;
}

.ghost-btn {
  padding: 9px 18px;
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

.publish-panel {
  padding: 26px 26px 30px;
  background: #fff;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.publish-form :deep(.el-form-item__label) {
  font-size: 13px;
  color: var(--c-text-sub);
  font-weight: 500;
  padding-bottom: 4px;
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 20px;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 20px;
}

.tips-card {
  display: flex;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 12px;
  background: var(--c-primary-mist);
  border: 1px dashed var(--c-primary-light);
  color: var(--c-primary-dark);
  font-size: 12px;
}

.tips-card strong {
  display: block;
  font-size: 13px;
  margin-bottom: 2px;
}

.tips-card p {
  color: var(--c-text-sub);
  line-height: 1.7;
}

.submit-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 22px;
}

.primary-btn {
  min-width: 180px;
  height: 46px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  box-shadow: 0 12px 26px rgba(13, 148, 136, 0.26);
  transition: transform 0.2s, box-shadow 0.2s;
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(13, 148, 136, 0.32);
}

.primary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.preview-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-detail {
  padding: 0 4px;
  font-size: 13px;
  color: var(--c-text-sub);
  line-height: 1.8;
}

.preview-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--c-accent-strong);
}

.preview-meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--c-text-muted);
}

@media (max-width: 767.98px) {
  .publish-panel {
    padding: 18px 16px 22px;
  }

  .grid-2,
  .grid-3 {
    grid-template-columns: 1fr;
  }

  .submit-row {
    justify-content: stretch;
  }

  .primary-btn {
    width: 100%;
  }
}
</style>
