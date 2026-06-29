<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { createQuestion, updateQuestion, getQuestion } from '@/api/qa'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const categories = [
  { value: 'course-selection', label: '选课指导' },
  { value: 'internship', label: '实习就业' },
  { value: 'kaoyan', label: '考研升学' },
  { value: 'competition', label: '竞赛科研' },
  { value: 'other', label: '其他' },
]

const isEdit = route.path.includes('/edit')
const editId = isEdit ? Number(route.params.id) : null
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  title: '',
  content: '',
  category: 'other',
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
      const res = await getQuestion(editId)
      const q = res.data
      if (q.userId !== userStore.user?.id) {
        ElMessage.warning('只能编辑自己的问题')
        router.push(`/qa/${editId}`)
        return
      }
      form.title = q.title
      form.content = q.content
      form.category = q.category
    } catch {
      router.push('/qa')
    } finally {
      loading.value = false
    }
  }
})

async function handleSubmit() {
  if (!form.title.trim()) { ElMessage.warning('请输入问题标题'); return }
  if (!form.content.trim()) { ElMessage.warning('请输入问题内容'); return }

  submitting.value = true
  try {
    if (isEdit && editId) {
      await updateQuestion(editId, {
        title: form.title,
        content: form.content,
        category: form.category,
      })
      ElMessage.success('编辑成功')
      router.push(`/qa/${editId}`)
    } else {
      const res = await createQuestion({
        title: form.title,
        content: form.content,
        category: form.category,
      })
      ElMessage.success('提问成功')
      router.push(`/qa/${res.data.id}`)
    }
  } catch { /* 拦截器已提示 */ }
  finally { submitting.value = false }
}

function goBack() {
  if (isEdit && editId) {
    router.push(`/qa/${editId}`)
  } else {
    router.push('/qa')
  }
}
</script>

<template>
  <div class="ask-page">
    <el-card v-loading="loading">
      <template #header>
        <span>{{ isEdit ? '📝 编辑问题' : '❓ 发布提问' }}</span>
      </template>

      <el-form :model="form" label-width="60px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="简明扼要地描述你的问题"
                    maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="10"
                    placeholder="详细描述你的问题…" maxlength="5000" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '发布提问' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.ask-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}
</style>
