<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { createItem, updateItem, getItem } from '@/api/lostfound'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const typeOptions = [
  { value: 'lost', label: '🔍 寻物（我丢了东西）' },
  { value: 'found', label: '✅ 招领（我捡到了东西）' },
]

const categoryOptions = [
  { value: 'electronics', label: '电子产品' },
  { value: 'documents', label: '证件卡片' },
  { value: 'clothing', label: '衣物饰品' },
  { value: 'books', label: '书籍文具' },
  { value: 'keys', label: '钥匙' },
  { value: 'other', label: '其他' },
]

const isEdit = route.path.includes('/edit')
const editId = isEdit ? Number(route.params.id) : null
const loading = ref(false)
const submitting = ref(false)
const imageUploading = ref(false)

const form = reactive({
  type: 'lost',
  category: 'other',
  title: '',
  description: '',
  location: '',
  contact: '',
  imageUrl: '',
})

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (isEdit && editId) {
    loading.value = true
    try {
      const res = await getItem(editId)
      const item = res.data
      if (item.userId !== userStore.user?.id) {
        ElMessage.warning('只能编辑自己的信息')
        router.push(`/lost-found/${editId}`)
        return
      }
      form.type = item.type
      form.category = item.category
      form.title = item.title
      form.description = item.description || ''
      form.location = item.location || ''
      form.contact = item.contact || ''
      form.imageUrl = item.imageUrl || ''
    } catch {
      router.push('/lost-found')
    } finally {
      loading.value = false
    }
  }
})

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
  if (!form.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.contact.trim()) { ElMessage.warning('请留下联系方式'); return }

  submitting.value = true
  try {
    if (isEdit && editId) {
      await updateItem(editId, { ...form })
      ElMessage.success('修改成功')
      router.push(`/lost-found/${editId}`)
    } else {
      const res = await createItem({ ...form })
      ElMessage.success('发布成功')
      router.push(`/lost-found/${res.data.id}`)
    }
  } catch { /* 拦截器已提示 */ }
  finally { submitting.value = false }
}

function goBack() {
  if (isEdit && editId) {
    router.push(`/lost-found/${editId}`)
  } else {
    router.push('/lost-found')
  }
}
</script>

<template>
  <div class="create-page">
    <el-card v-loading="loading">
      <template #header>
        <span>{{ isEdit ? '📝 编辑信息' : '📢 发布失物招领' }}</span>
      </template>

      <el-form :model="form" label-width="80px">
        <el-form-item label="类型">
          <el-radio-group v-model="form.type" :disabled="isEdit">
            <el-radio-button v-for="t in typeOptions" :key="t.value" :value="t.value">
              {{ t.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="简要描述物品…" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="form.description" type="textarea" :rows="4"
                    placeholder="物品特征、颜色、品牌等详细信息…" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" placeholder="如：图书馆二楼、食堂门口…" maxlength="200" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contact" placeholder="手机号/QQ/微信…" maxlength="100" />
        </el-form-item>
        <el-form-item label="物品图片">
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
