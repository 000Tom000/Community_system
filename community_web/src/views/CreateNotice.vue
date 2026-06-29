<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { createNotice, updateNotice, getNotice } from '@/api/notice'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const categoryOptions = [
  { value: 'academic', label: '学术讲座' },
  { value: 'club', label: '社团活动' },
  { value: 'admin', label: '学校通知' },
  { value: 'exam', label: '考试教务' },
  { value: 'general', label: '综合' },
]

const levelOptions = [
  { value: 'normal', label: '普通' },
  { value: 'important', label: '重要' },
  { value: 'urgent', label: '紧急' },
]

const isEdit = route.path.includes('/edit')
const editId = isEdit ? Number(route.params.id) : null
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  category: 'general',
  title: '',
  content: '',
  level: 'normal',
})

onMounted(async () => {
  if (!userStore.isLoggedIn || userStore.user?.role !== 'admin') {
    ElMessage.warning('仅管理员可发布公告')
    router.push('/notices')
    return
  }
  if (isEdit && editId) {
    loading.value = true
    try {
      const res = await getNotice(editId)
      const n = res.data
      form.category = n.category
      form.title = n.title
      form.content = n.content
      form.level = n.level || 'normal'
    } catch { router.push('/notices') }
    finally { loading.value = false }
  }
})

async function handleSubmit() {
  if (!form.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.content.trim()) { ElMessage.warning('请输入内容'); return }

  submitting.value = true
  try {
    if (isEdit && editId) {
      await updateNotice(editId, { ...form })
      ElMessage.success('修改成功')
      router.push(`/notices/${editId}`)
    } else {
      const res = await createNotice({ ...form })
      ElMessage.success('发布成功')
      router.push(`/notices/${res.data.id}`)
    }
  } catch { /* 拦截器已提示 */ }
  finally { submitting.value = false }
}

function goBack() {
  if (isEdit && editId) router.push(`/notices/${editId}`)
  else router.push('/notices')
}
</script>

<template>
  <div class="create-page">
    <el-card v-loading="loading">
      <template #header><span>{{ isEdit ? '📝 编辑公告' : '📢 发布公告' }}</span></template>

      <el-form :model="form" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级">
          <el-radio-group v-model="form.level">
            <el-radio-button v-for="l in levelOptions" :key="l.value" :value="l.value">{{ l.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="公告标题…" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="12"
                    placeholder="公告正文内容…" maxlength="5000" show-word-limit />
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
</style>
