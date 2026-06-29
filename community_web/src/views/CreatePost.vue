<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { createPost, getPost, updatePost } from '@/api/post'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isEdit = ref(false)
const editId = ref(null)

const categoryOptions = [
  { value: 'study', label: '学习交流' },
  { value: 'life', label: '校园生活' },
  { value: 'kaoyan', label: '考研升学' },
  { value: 'teamup', label: '组队招募' },
  { value: 'tech', label: '技术讨论' },
  { value: 'other', label: '其他' },
]

const form = reactive({
  category: 'other',
  title: '',
  content: '',
})
const submitting = ref(false)

// 编辑模式加载帖子内容
const rawContent = ref('')  // 原始正文（编辑时比对用）

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  // 编辑模式
  const id = route.params.id
  if (route.path.includes('/edit') && id) {
    isEdit.value = true
    editId.value = Number(id)
    try {
      const res = await getPost(editId.value)
      const p = res.data
      if (p.userId !== userStore.user?.id) {
        ElMessage.warning('只能编辑自己的帖子')
        router.push(`/forum/${editId.value}`)
        return
      }
      form.category = p.category
      form.title = p.title
      form.content = p.content
      rawContent.value = p.content
    } catch {
      router.push('/forum')
    }
  }
})

/** 提交 */
async function handleSubmit() {
  if (!form.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.content.trim()) { ElMessage.warning('请输入内容'); return }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePost(editId.value, {
        title: form.title,
        content: form.content,
        category: form.category,
      })
      ElMessage.success('修改成功')
      router.push(`/forum/${editId.value}`)
    } else {
      const res = await createPost({
        title: form.title,
        content: form.content,
        category: form.category,
      })
      ElMessage.success('发帖成功')
      router.push(`/forum/${res.data.id}`)
    }
  } catch { /* ignore */ }
  finally { submitting.value = false }
}

/** 返回 */
function goBack() {
  if (isEdit.value) router.push(`/forum/${editId.value}`)
  else router.push('/forum')
}
</script>

<template>
  <div class="create-post">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '📝 编辑帖子' : '✍️ 发布新帖' }}</span>
      </template>

      <el-form :model="form" label-width="60px">
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option v-for="o in categoryOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="起个吸引人的标题…" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="12"
                    placeholder="想说些什么…（支持换行）" />
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
.create-post {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}
</style>
