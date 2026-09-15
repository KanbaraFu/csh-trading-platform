<script setup>
import { reactive, ref, watch } from 'vue'
import { REGION_OPTIONS } from '@/constants/address'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  address: { type: Object, default: null },
  saving: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref(null)
const form = reactive({
  receiver_name: '',
  phone: '',
  region: '',
  detail: '',
  is_default: false,
})

const rules = {
  receiver_name: [{ required: true, message: '请填写收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请填写联系电话', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' },
  ],
  region: [{ required: true, message: '请选择所在区域', trigger: 'change' }],
  detail: [{ required: true, message: '请填写详细地址（楼栋 / 房间号）', trigger: 'blur' }],
}

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) return
    const source = props.address
    form.receiver_name = source?.receiver_name || ''
    form.phone = source?.phone || ''
    form.region = source?.region || ''
    form.detail = source?.detail || ''
    form.is_default = Boolean(source?.is_default)
    formRef.value?.clearValidate()
  },
)

function close() {
  emit('update:modelValue', false)
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('saved', { ...form, id: props.address?.id })
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="address ? '编辑收货地址' : '新增收货地址'"
    width="480px"
    align-center
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="84px" label-position="left">
      <el-form-item label="收货人" prop="receiver_name">
        <el-input v-model="form.receiver_name" placeholder="请输入收货人姓名" maxlength="20" />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入 11 位手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="所在区域" prop="region">
        <el-select v-model="form.region" placeholder="请选择校区区域" style="width: 100%">
          <el-option v-for="item in REGION_OPTIONS" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="详细地址" prop="detail">
        <el-input
          v-model="form.detail"
          type="textarea"
          :rows="3"
          maxlength="120"
          show-word-limit
          placeholder="例如：6 栋 301 室 3 号床，楼下有快递暂存架"
        />
      </el-form-item>
      <el-form-item label="设为默认">
        <el-switch v-model="form.is_default" />
        <span class="hint">默认地址会在下单时自动选中</span>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存地址</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.hint {
  margin-left: 10px;
  font-size: 12px;
  color: var(--c-text-muted);
}
</style>
