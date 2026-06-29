<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getQuestion, deleteQuestion, createAnswer, deleteAnswer, acceptAnswer, toggleAnswerLike } from '@/api/qa'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const question = ref(null)
const answers = ref([])
const loading = ref(true)
const answerText = ref('')
const submittingAnswer = ref(false)
const likeLoading = ref({})

const questionId = Number(route.params.id)

onMounted(async () => {
  try {
    const res = await getQuestion(questionId)
    question.value = res.data
    answers.value = res.data.answers || []
  } catch {
    router.push('/qa')
  } finally {
    loading.value = false
  }
})

async function reload() {
  try {
    const res = await getQuestion(questionId)
    question.value = res.data
    answers.value = res.data.answers || []
  } catch { /* ignore */ }
}

/* ----- 问题操作 ----- */

async function handleEdit() {
  router.push(`/qa/${questionId}/edit`)
}

async function handleDeleteQuestion() {
  try {
    await ElMessageBox.confirm('确定删除此问题？所有回答也将不可见。', '提示', { type: 'warning' })
    await deleteQuestion(questionId)
    ElMessage.success('已删除')
    router.push('/qa')
  } catch { /* 取消 */ }
}

/* ----- 回答操作 ----- */

async function handleSubmitAnswer() {
  if (!answerText.value.trim()) {
    ElMessage.warning('请输入回答内容')
    return
  }
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  submittingAnswer.value = true
  try {
    await createAnswer(questionId, { content: answerText.value })
    answerText.value = ''
    ElMessage.success('回答已发布')
    await reload()
  } catch { /* 拦截器已提示 */ }
  finally { submittingAnswer.value = false }
}

async function handleDeleteAnswer(answerId) {
  try {
    await ElMessageBox.confirm('确定删除此回答？', '提示', { type: 'warning' })
    await deleteAnswer(answerId)
    ElMessage.success('已删除')
    await reload()
  } catch { /* 取消 */ }
}

async function handleAccept(answerId) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await acceptAnswer(answerId, questionId)
    ElMessage.success('操作成功')
    await reload()
  } catch { /* 拦截器已提示 */ }
}

async function handleLike(answerId) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (likeLoading.value[answerId]) return
  likeLoading.value[answerId] = true
  try {
    const res = await toggleAnswerLike(answerId)
    const liked = res.data.liked
    // 更新本地状态
    const ans = answers.value.find(a => a.id === answerId)
    if (ans) {
      ans.liked = liked
      ans.likeCount += liked ? 1 : -1
    }
  } catch { /* 拦截器已提示 */ }
  finally { likeLoading.value[answerId] = false }
}

/* ----- 权限 ----- */

const isOwner = () => userStore.user?.id === question.value?.userId
const isAdmin = () => userStore.user?.role === 'admin'
const isAnswerOwner = (a) => userStore.user?.id === a.userId

const categoryLabels = {
  'course-selection': '选课指导',
  'internship': '实习就业',
  'kaoyan': '考研升学',
  'competition': '竞赛科研',
  'other': '其他',
}
</script>

<template>
  <div class="qa-detail" v-loading="loading">
    <div v-if="question" class="detail-card">
      <!-- 问题 header -->
      <div class="header">
        <h1>
          <el-tag v-if="question.isSolved" size="small" type="success" style="margin-right:8px">已解决</el-tag>
          {{ question.title }}
        </h1>
        <div class="header-meta">
          <el-avatar :size="36" :src="question.authorAvatar" />
          <span class="author">{{ question.authorNickname }}</span>
          <span class="time">{{ question.createdAt }}</span>
          <el-tag size="small" type="info">{{ categoryLabels[question.category] || question.category }}</el-tag>
          <span class="views">👁 {{ question.viewCount }} 阅读</span>
        </div>
      </div>

      <el-divider />

      <!-- 问题内容 -->
      <div class="content">{{ question.content }}</div>

      <el-divider />

      <!-- 问题操作栏 -->
      <div class="actions">
        <template v-if="isOwner() || isAdmin()">
          <el-button v-if="isOwner()" @click="handleEdit">编辑</el-button>
          <el-button v-if="isOwner() || isAdmin()" type="danger" @click="handleDeleteQuestion">删除</el-button>
        </template>
        <el-button @click="router.push('/qa')">返回列表</el-button>
      </div>

      <!-- ====== 回答区域 ====== -->
      <div class="answers-section">
        <div class="answers-header">
          <h3>💬 回答 ({{ answers.length }})</h3>
        </div>

        <!-- 写回答 -->
        <div v-if="userStore.isLoggedIn" class="answer-form">
          <el-input
            v-model="answerText"
            type="textarea"
            :rows="4"
            placeholder="写下你的回答…"
            maxlength="5000"
            show-word-limit
          />
          <div class="answer-form-btn">
            <el-button type="primary" :loading="submittingAnswer" @click="handleSubmitAnswer">
              发布回答
            </el-button>
          </div>
        </div>
        <div v-else class="answer-login-hint">
          <el-button type="primary" @click="router.push('/login')">登录后回答</el-button>
        </div>

        <el-divider />

        <!-- 回答列表 -->
        <div v-if="answers.length === 0" class="empty-answers">
          暂无回答，来写第一个回答吧
        </div>

        <div v-for="a in answers" :key="a.id" class="answer-card" :class="{ accepted: a.isAccepted }">
          <div class="answer-header">
            <el-avatar :size="32" :src="a.authorAvatar" />
            <span class="author">{{ a.authorNickname }}</span>
            <span class="time">{{ a.createdAt }}</span>
            <el-tag v-if="a.isAccepted" type="success" size="small">已采纳</el-tag>
          </div>
          <div class="answer-content">{{ a.content }}</div>
          <div class="answer-actions">
            <el-button
              size="small"
              :type="a.liked ? 'primary' : 'default'"
              :loading="likeLoading[a.id]"
              @click="handleLike(a.id)"
            >
              👍 {{ a.likeCount || 0 }}
            </el-button>

            <!-- 采纳按钮：仅问题作者可见 -->
            <el-button
              v-if="isOwner()"
              size="small"
              :type="a.isAccepted ? 'success' : 'default'"
              @click="handleAccept(a.id)"
            >
              {{ a.isAccepted ? '✓ 已采纳' : '采纳' }}
            </el-button>

            <!-- 删除回答 -->
            <el-button
              v-if="isAnswerOwner(a) || isAdmin()"
              size="small"
              type="danger"
              @click="handleDeleteAnswer(a.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.qa-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}
.detail-card {
  background: #fff;
  padding: 30px 40px;
  border-radius: 8px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}
.header h1 {
  font-size: 24px;
  margin-bottom: 12px;
  color: #303133;
}
.header-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #909399;
  flex-wrap: wrap;
}
.author {
  font-weight: 500;
  color: #606266;
}
.content {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  min-height: 100px;
  white-space: pre-wrap;
  word-break: break-word;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

/* ===== 回答区域 ===== */
.answers-section {
  margin-top: 30px;
}
.answers-header h3 {
  margin-bottom: 16px;
  font-size: 17px;
  color: #303133;
}
.answer-form {
  margin-bottom: 10px;
}
.answer-form-btn {
  margin-top: 10px;
  text-align: right;
}
.answer-login-hint {
  text-align: center;
  padding: 16px 0;
}
.empty-answers {
  text-align: center;
  color: #909399;
  padding: 40px 0;
  font-size: 15px;
}

.answer-card {
  background: #fafafa;
  border-radius: 6px;
  padding: 16px 20px;
  margin-bottom: 12px;
  border: 1px solid #ebeef5;
}
.answer-card.accepted {
  border-color: #67c23a;
  background: #f0f9eb;
}
.answer-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}
.answer-header .author {
  font-weight: 500;
  color: #606266;
}
.answer-content {
  font-size: 15px;
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: 10px;
}
.answer-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
