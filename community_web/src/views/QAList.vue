<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getQuestions } from '@/api/qa'
import { chatWithAI } from '@/api/ai'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// ---- AI 对话 ----
const AI_STORAGE_KEY = 'ai_chat_history'

function loadHistory() {
  try {
    const raw = localStorage.getItem(AI_STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch { return [] }
}

function saveHistory() {
  // 只保留最近 100 条，避免撑爆 localStorage
  const toSave = aiMessages.value.slice(-100)
  localStorage.setItem(AI_STORAGE_KEY, JSON.stringify(toSave))
}

const aiDrawer = ref(false)
const aiMessages = ref([])
const aiInput = ref('')
const aiLoading = ref(false)
const aiChatRef = ref(null)

function openAI() {
  if (aiMessages.value.length === 0) {
    const saved = loadHistory()
    if (saved.length > 0) {
      aiMessages.value = saved
    } else {
      aiMessages.value.push({
        role: 'ai',
        content: '你好！我是校园助手 🤖，有什么学习、选课、考研、实习方面的问题都可以问我～',
        time: new Date().toLocaleTimeString(),
      })
    }
  }
  aiDrawer.value = true
  nextTick(() => scrollToBottom())
}

function clearHistory() {
  aiMessages.value = [{
    role: 'ai',
    content: '你好！我是校园助手 🤖，有什么学习、选课、考研、实习方面的问题都可以问我～',
    time: new Date().toLocaleTimeString(),
  }]
  localStorage.removeItem(AI_STORAGE_KEY)
}

async function sendToAI() {
  const text = aiInput.value.trim()
  if (!text) return
  aiMessages.value.push({ role: 'user', content: text, time: new Date().toLocaleTimeString() })
  aiInput.value = ''
  saveHistory()
  aiLoading.value = true

  // 调用 DeepSeek API
  try {
    const apiMessages = aiMessages.value
      .filter(m => m.role === 'user' || m.role === 'ai')
      .map(m => ({
        role: m.role === 'ai' ? 'assistant' : 'user',
        content: m.content,
      }))
    const res = await chatWithAI(apiMessages)
    aiMessages.value.push({
      role: 'ai',
      content: res.data.reply,
      time: new Date().toLocaleTimeString(),
    })
  } catch {
    aiMessages.value.push({
      role: 'ai',
      content: '抱歉，AI 服务暂时不可用，请稍后再试 🙏',
      time: new Date().toLocaleTimeString(),
    })
  }
  saveHistory()
  aiLoading.value = false

  await nextTick()
  scrollToBottom()
}

function scrollToBottom() {
  const el = document.querySelector('.ai-chat-body')
  if (el) el.scrollTop = el.scrollHeight
}


const categoryTabs = [
  { key: '', label: '全部' },
  { key: 'course-selection', label: '选课指导' },
  { key: 'internship', label: '实习就业' },
  { key: 'kaoyan', label: '考研升学' },
  { key: 'competition', label: '竞赛科研' },
  { key: 'other', label: '其他' },
]

const sortOptions = [
  { value: 'new', label: '最新' },
  { value: 'hot', label: '热门' },
  { value: 'unsolved', label: '未解决' },
]

const activeCategory = ref('')
const activeSort = ref('new')
const keyword = ref('')
const questions = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const categoryLabels = Object.fromEntries(categoryTabs.map(t => [t.key, t.label]))

async function fetchQuestions() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, sort: activeSort.value }
    if (activeCategory.value) params.category = activeCategory.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getQuestions(params)
    questions.value = res.data.list
    total.value = res.data.total
  } catch { /* 拦截器已提示 */ }
  finally { loading.value = false }
}

function search() {
  page.value = 1
  fetchQuestions()
}

function goDetail(id) {
  router.push(`/qa/${id}`)
}

function goAsk() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/qa/ask')
}

watch(activeCategory, () => {
  page.value = 1
  fetchQuestions()
})

watch(activeSort, () => {
  page.value = 1
  fetchQuestions()
})

onMounted(fetchQuestions)
</script>

<template>
  <div class="qa-list">
    <!-- 顶栏：分类 + 排序 + 搜索 + 提问 -->
    <div class="qa-toolbar">
      <div class="tabs">
        <el-radio-group v-model="activeCategory" size="small">
          <el-radio-button v-for="t in categoryTabs" :key="t.key" :value="t.key">
            {{ t.label }}
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="actions">
        <el-select v-model="activeSort" size="small" style="width:100px">
          <el-option v-for="s in sortOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索标题…" size="small" style="width:200px"
                  clearable @keyup.enter="search" @clear="search" />
        <el-button type="primary" size="small" @click="goAsk">✍️ 提问</el-button>
        <el-button type="success" size="small" @click="openAI">🤖 问 AI</el-button>
      </div>
    </div>

    <!-- 列表 -->
    <div v-loading="loading" class="question-list">
      <div v-if="questions.length === 0 && !loading" class="empty">
        <el-empty description="暂无问题" />
      </div>

      <div v-for="q in questions" :key="q.id" class="question-card" @click="goDetail(q.id)">
        <div class="q-left">
          <el-avatar :size="40" :src="q.authorAvatar" />
        </div>
        <div class="q-body">
          <div class="q-title">
            <el-tag v-if="q.isSolved" size="small" type="success" style="margin-right:6px">已解决</el-tag>
            <span class="title-text">{{ q.title }}</span>
          </div>
          <div class="q-meta">
            <span class="author">{{ q.authorNickname }}</span>
            <span class="dot">·</span>
            <span>{{ q.createdAt }}</span>
            <span class="dot">·</span>
            <span>{{ categoryLabels[q.category] || q.category }}</span>
          </div>
        </div>
        <div class="q-stats">
          <span>👁 {{ q.viewCount }}</span>
          <span>💬 {{ q.answerCount }}</span>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchQuestions"
        background
        small
      />
    </div>

    <!-- AI 对话抽屉 -->
    <el-drawer
      v-model="aiDrawer"
      direction="rtl"
      size="450px"
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="ai-drawer-header">
          <span>🤖 AI 校园助手</span>
          <el-button text size="small" type="danger" @click="clearHistory"
                     v-if="aiMessages.length > 1">🗑️ 清空聊天</el-button>
        </div>
      </template>
      <div class="ai-chat">
        <div class="ai-chat-body">
          <div v-for="(m, i) in aiMessages" :key="i" class="ai-msg" :class="m.role">
            <div class="ai-avatar">{{ m.role === 'ai' ? '🤖' : '👤' }}</div>
            <div class="ai-bubble">
              <div class="ai-text">{{ m.content }}</div>
              <div class="ai-time">{{ m.time }}</div>
            </div>
          </div>
          <div v-if="aiLoading" class="ai-msg ai">
            <div class="ai-avatar">🤖</div>
            <div class="ai-bubble typing">思考中<span class="dots">...</span></div>
          </div>
        </div>
        <div class="ai-chat-input">
          <el-input
            v-model="aiInput"
            placeholder="输入你的问题…"
            @keyup.enter="sendToAI"
            :disabled="aiLoading"
            clearable
          >
            <template #append>
              <el-button :loading="aiLoading" @click="sendToAI" type="primary">
                发送
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.qa-list {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 0;
}

.qa-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 10px;
}
.actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.question-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: box-shadow .2s;
}
.question-card:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.q-body {
  flex: 1;
  min-width: 0;
}
.title-text {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
.q-meta {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}
.dot {
  margin: 0 6px;
}
.q-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.empty {
  margin-top: 60px;
}

/* ---- AI 聊天 ---- */
.ai-drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.ai-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.ai-chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}
.ai-msg {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.ai-msg.user {
  flex-direction: row-reverse;
}
.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.ai-bubble {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}
.ai-msg.ai .ai-bubble {
  background: #f0f2f5;
  color: #303133;
}
.ai-msg.user .ai-bubble {
  background: #409eff;
  color: #fff;
}
.ai-bubble.typing {
  background: #f0f2f5;
  color: #909399;
}
.ai-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}
.ai-msg.user .ai-time {
  color: rgba(255,255,255,.7);
  text-align: right;
}
.ai-chat-input {
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}
.dots::after {
  content: '';
  animation: dot 1.4s infinite;
}
@keyframes dot {
  0%, 20% { content: '.'; }
  40% { content: '..'; }
  60%, 100% { content: '...'; }
}
</style>
