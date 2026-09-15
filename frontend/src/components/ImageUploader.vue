<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { svgCover } from '@/utils/image'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  max: { type: Number, default: 6 },
})

const emit = defineEmits(['update:modelValue'])

const fileInput = ref(null)
const dragIndex = ref(-1)
const orderTip = ref(false)

const images = computed(() => props.modelValue || [])
const canAdd = computed(() => images.value.length < props.max)

function update(list) {
  emit('update:modelValue', list)
}

function pickFiles() {
  fileInput.value?.click()
}

function onFilesChange(event) {
  const files = Array.from(event.target.files || [])
  if (!files.length) return
  const remain = props.max - images.value.length
  if (remain <= 0) {
    ElMessage.warning(`最多上传 ${props.max} 张图片`)
    return
  }
  files.slice(0, remain).forEach((file) => {
    if (file.size > 4 * 1024 * 1024) {
      ElMessage.warning(`${file.name} 超过 4MB，已跳过`)
      return
    }
    const reader = new FileReader()
    reader.onload = () => update([...images.value, reader.result])
    reader.onerror = () => ElMessage.error(`${file.name} 读取失败`)
    reader.readAsDataURL(file)
  })
  orderTip.value = files.length > 1
  event.target.value = ''
}

function addSample() {
  if (!canAdd.value) {
    ElMessage.warning(`最多上传 ${props.max} 张图片`)
    return
  }
  const seed = `${Date.now()}-${images.value.length}`
  update([...images.value, svgCover('实拍图', seed, 720)])
  orderTip.value = images.value.length >= 1
}

function removeAt(index) {
  const list = images.value.slice()
  list.splice(index, 1)
  update(list)
}

function onDragStart(index) {
  dragIndex.value = index
  orderTip.value = true
}

function onDrop(index) {
  if (dragIndex.value < 0 || dragIndex.value === index) return
  const list = images.value.slice()
  const [moved] = list.splice(dragIndex.value, 1)
  list.splice(index, 0, moved)
  update(list)
  dragIndex.value = -1
}
</script>

<template>
  <div class="uploader">
    <div class="grid">
      <div
        v-for="(url, index) in images"
        :key="`${index}-${url.slice(-12)}`"
        class="tile"
        :class="{ dragging: dragIndex === index }"
        draggable="true"
        @dragstart="onDragStart(index)"
        @dragover.prevent
        @drop="onDrop(index)"
        @dragend="dragIndex = -1"
      >
        <img :src="url" :alt="`商品图片 ${index + 1}`" />
        <span v-if="index === 0" class="cover-badge">封面</span>
        <button class="remove" type="button" title="删除图片" @click="removeAt(index)">
          <el-icon :size="13"><Close /></el-icon>
        </button>
      </div>

      <button v-if="canAdd" class="tile add" type="button" @click="pickFiles">
        <el-icon :size="22"><Plus /></el-icon>
        <span>上传图片</span>
        <small>{{ images.length }}/{{ max }}</small>
      </button>
    </div>

    <input
      ref="fileInput"
      class="hidden-input"
      type="file"
      accept="image/*"
      multiple
      @change="onFilesChange"
    />

    <div class="footer">
      <p class="tips">
        支持 JPG / PNG，单张不超过 4MB，最多 {{ max }} 张。第一张作为商品封面，可长按图片拖拽调整顺序。
      </p>
      <button class="sample-btn" type="button" @click="addSample">
        <el-icon :size="14"><Picture /></el-icon>
        没准备好实拍图？加一张占位图
      </button>
    </div>
  </div>
</template>

<style scoped>
.uploader {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(112px, 1fr));
  gap: 12px;
}

.tile {
  position: relative;
  aspect-ratio: 1 / 1;
  border-radius: 14px;
  overflow: hidden;
  background: #f3f6f9;
  cursor: grab;
  transition: transform 0.2s, box-shadow 0.2s;
}

.tile:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-card);
}

.tile.dragging {
  opacity: 0.5;
  transform: scale(0.96);
}

.tile img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-badge {
  position: absolute;
  left: 6px;
  bottom: 6px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(13, 148, 136, 0.9);
  color: #fff;
  font-size: 11px;
}

.remove {
  position: absolute;
  right: 6px;
  top: 6px;
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 50%;
  background: rgba(17, 24, 39, 0.6);
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
}

.remove:hover {
  background: var(--c-danger);
}

.tile.add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: 2px dashed var(--c-primary-light);
  background: var(--c-primary-mist);
  color: var(--c-primary-dark);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.tile.add:hover {
  border-color: var(--c-primary);
  background: #e6fbf6;
}

.tile.add small {
  font-size: 11px;
  color: var(--c-text-muted);
}

.hidden-input {
  display: none;
}

.footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}

.tips {
  font-size: 12px;
  color: var(--c-text-muted);
  flex: 1;
  min-width: 200px;
}

.sample-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: #fff;
  font-size: 12px;
  color: var(--c-text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.sample-btn:hover {
  color: var(--c-primary-dark);
  border-color: var(--c-primary-light);
  background: var(--c-primary-mist);
}
</style>
