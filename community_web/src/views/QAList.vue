<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getQuestions } from '@/api/qa'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

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
</style>
