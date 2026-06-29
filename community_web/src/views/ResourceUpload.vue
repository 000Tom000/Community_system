<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { uploadResource } from '@/api/resource'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const categories = [
  { value: 'courseware', label: '课件' },
  { value: 'notes', label: '笔记' },
  { value: 'booklist', label: '书单' },
  { value: 'exam', label: '试卷' },
  { value: 'other', label: '其他' },
]

const form = reactive({
  title: '',
  description: '',
  category: 'other',
})
const file = ref(null)
const uploading = ref(false)

function onFileChange(f) {
  file.value = f.raw
}

async function handleSubmit() {
  if (!form.title.trim()) { ElMessage.warning('请输入资源名称'); return }
  if (!file.value) { ElMessage.warning('请选择文件'); return }
  if (file.value.size > 50 * 1024 * 1024) { ElMessage.warning('文件不能超过 50MB'); return }

  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('title', form.title)
    fd.append('description', form.description)
    fd.append('category', form.category)
    fd.append('file', file.value)

    const res = await uploadResource(fd)
    ElMessage.success('上传成功')
    router.push(`/resources/${res.data.id}`)
  } catch (e) {
    // 错误提示已由请求拦截器统一处理，此处仅记录
    console.error('上传失败:', e)
  } finally { uploading.value = false }
}
</script>

<template>
  <div class="upload-page">
    <el-card>
      <template #header><span>📤 上传学习资源</span></template>

      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.title" placeholder="资源名称" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4"
                    placeholder="简要描述资源内容…" maxlength="2000" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件">
          <el-upload
            :auto-upload="false"
            :limit="1"
            :on-change="onFileChange"
            :on-exceed="() => ElMessage.warning('只能上传一个文件')"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 PDF/PPT/DOC/ZIP 等，最大 50MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="uploading" @click="handleSubmit">上传</el-button>
          <el-button @click="router.push('/resources')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.upload-page { max-width: 650px; margin: 0 auto; padding: 20px 0; }
</style>
