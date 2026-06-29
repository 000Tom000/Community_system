<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { createItem, updateItem, getItem } from '@/api/secondhand'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const categoryOptions = [
  { value: 'electronics', label: '数码电子' },
  { value: 'books', label: '书籍教材' },
  { value: 'clothing', label: '服饰鞋包' },
  { value: 'sports', label: '运动户外' },
  { value: 'daily', label: '生活日用' },
  { value: 'other', label: '其他' },
]

const conditionOptions = [
  { value: 'new', label: '全新' },
  { value: 'like-new', label: '几乎全新' },
  { value: 'slight', label: '轻微使用' },
  { value: 'normal', label: '正常使用' },
]

const isEdit = route.path.includes('/edit')
const editId = isEdit ? Number(route.params.id) : null
const loading = ref(false)
const submitting = ref(false)
const imageUploading = ref(false)

const form = reactive({
  category: 'other',
  title: '',
  description: '',
  price: '',
  originalPrice: '',
  condition: 'normal',
  isNegotiable: true,
  location: '',
  contact: '',
  imageUrl: '',
})

onMounted(async () => {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return }
  if (isEdit && editId) {
    loading.value = true
    try {
      const res = await getItem(editId)
      const item = res.data
      if (item.userId !== userStore.user?.id) {
        ElMessage.warning('只能编辑自己的商品')
        router.push(`/secondhand/${editId}`)
        return
      }
      form.category = item.category
      form.title = item.title
      form.description = item.description || ''
      form.price = String(item.price || '')
      form.originalPrice = item.originalPrice ? String(item.originalPrice) : ''
      form.condition = item.condition || 'normal'
      form.isNegotiable = item.isNegotiable
      form.location = item.location || ''
      form.contact = item.contact || ''
      form.imageUrl = item.imageUrl || ''
    } catch { router.push('/secondhand') }
    finally { loading.value = false }
  }
})

/** 图片上传 */
async function handleImageChange(file) {
  if (!file.raw.type.startsWith('image/')) { ElMessage.warning('请选择图片文件'); return }
  if (file.raw.size > 5 * 1024 * 1024) { ElMessage.warning('图片不能超过 5MB'); return }
  imageUploading.value = true
  try {
    const res = await uploadImage(file.raw)
    form.imageUrl = res.data.url
    ElMessage.success('图片上传成功')
  } catch { /* 拦截器已提示 */ }
  finally { imageUploading.value = false }
}

function imagePreviewUrl() {
  const url = form.imageUrl
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url
}

async function handleSubmit() {
  if (!form.title.trim()) { ElMessage.warning('请输入商品名称'); return }
  if (!form.price || Number(form.price) < 0) { ElMessage.warning('请输入有效价格'); return }
  if (!form.contact.trim()) { ElMessage.warning('请填写联系方式'); return }

  submitting.value = true
  try {
    const data = {
      ...form,
      price: Number(form.price),
      originalPrice: form.originalPrice ? Number(form.originalPrice) : null,
    }
    if (isEdit && editId) {
      await updateItem(editId, data)
      ElMessage.success('修改成功')
      router.push(`/secondhand/${editId}`)
    } else {
      const res = await createItem(data)
      ElMessage.success('发布成功')
      router.push(`/secondhand/${res.data.id}`)
    }
  } catch { /* 拦截器已提示 */ }
  finally { submitting.value = false }
}

function goBack() {
  if (isEdit && editId) router.push(`/secondhand/${editId}`)
  else router.push('/secondhand')
}
</script>

<template>
  <div class="create-page">
    <el-card v-loading="loading">
      <template #header><span>{{ isEdit ? '📝 编辑商品' : '🛒 发布二手商品' }}</span></template>

      <el-form :model="form" label-width="90px">
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="form.title" placeholder="如：9成新 iPad Air..." maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4"
                    placeholder="详细描述商品状况…" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="售价(¥)">
              <el-input v-model="form.price" placeholder="0.00" type="number" min="0" step="0.01" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价(¥)">
              <el-input v-model="form.originalPrice" placeholder="选填" type="number" min="0" step="0.01" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="成色">
          <el-select v-model="form.condition">
            <el-option v-for="c in conditionOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="支持议价">
          <el-switch v-model="form.isNegotiable" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" placeholder="交易/取货地点…" maxlength="200" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contact" placeholder="手机号/QQ/微信…" maxlength="100" />
        </el-form-item>
        <el-form-item label="商品图片">
          <div class="image-upload">
            <el-image v-if="form.imageUrl" :src="imagePreviewUrl()" fit="cover"
                      style="width:120px;height:120px;border-radius:6px;margin-right:12px" />
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageChange"
              accept="image/*"
            >
              <el-button :loading="imageUploading" size="small">
                {{ form.imageUrl ? '更换图片' : '点击上传' }}
              </el-button>
              <template #tip>
                <div class="el-upload__tip">支持 JPG/PNG/GIF，最大 5MB</div>
              </template>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '发布' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.create-page { max-width: 800px; margin: 0 auto; padding: 20px 0; }
.image-upload { display: flex; align-items: center; }
</style>
