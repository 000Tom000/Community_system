<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPosts } from '@/api/post'
import { getQuestions } from '@/api/qa'
import { getNotices } from '@/api/notice'

const router = useRouter()
const userStore = useUserStore()

const recentPosts = ref([])
const recentQuestions = ref([])
const recentNotices = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [postsRes, qaRes, noticesRes] = await Promise.all([
      getPosts({ page: 1, size: 5 }),
      getQuestions({ page: 1, size: 5 }),
      getNotices({ page: 1, size: 3 }),
    ])
    recentPosts.value = postsRes.data.list || []
    recentQuestions.value = qaRes.data.list || []
    recentNotices.value = noticesRes.data.list || []
  } catch { /* ignore */ }
  finally { loading.value = false }
})

function goForum() { router.push('/forum') }
function goQA() { router.push('/qa') }
function goNotices() { router.push('/notices') }
function goLostFound() { router.push('/lost-found') }
function goSecondHand() { router.push('/secondhand') }
function goResources() { router.push('/resources') }
function goPost(id) { router.push(`/forum/${id}`) }
function goQuestion(id) { router.push(`/qa/${id}`) }
function goNotice(id) { router.push(`/notices/${id}`) }
</script>

<template>
  <div class="home">
    <h2>👋 欢迎来到校园交流社区</h2>
    <p class="subtitle">信息发布 · 互动交流 · 资源共享</p>

    <!-- 功能入口 -->
    <div class="feature-grid">
      <div class="feature-card info" @click="goNotices">
        <span class="icon">📢</span>
        <span class="label">校园公告</span>
      </div>
      <div class="feature-card info" @click="goLostFound">
        <span class="icon">🔍</span>
        <span class="label">失物招领</span>
      </div>
      <div class="feature-card info" @click="goSecondHand">
        <span class="icon">🛒</span>
        <span class="label">二手市场</span>
      </div>
      <div class="feature-card community" @click="goForum">
        <span class="icon">💬</span>
        <span class="label">论坛广场</span>
      </div>
      <div class="feature-card community" @click="goQA">
        <span class="icon">❓</span>
        <span class="label">问答中心</span>
      </div>
      <div class="feature-card resource" @click="goResources">
        <span class="icon">📚</span>
        <span class="label">学习资料</span>
      </div>
    </div>

    <!-- 未登录提示 -->
    <el-alert
      v-if="!userStore.isLoggedIn"
      title="登录后即可发布内容、参与讨论"
      type="info" show-icon :closable="false"
      style="margin: 24px 0"
    />

    <!-- 内容区：三栏 -->
    <el-row :gutter="20" v-loading="loading" class="content-row">
      <!-- 最新公告 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>📢 最新公告</span>
              <el-button text type="primary" size="small" @click="goNotices">更多 →</el-button>
            </div>
          </template>
          <div v-if="recentNotices.length === 0" class="empty-tip">暂无公告</div>
          <div v-for="n in recentNotices" :key="n.id" class="feed-item" @click="goNotice(n.id)">
            <span class="feed-title">{{ n.title }}</span>
            <span class="feed-time">{{ n.createdAt?.slice(0, 10) }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 最新帖子 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>💬 论坛新帖</span>
              <el-button text type="primary" size="small" @click="goForum">更多 →</el-button>
            </div>
          </template>
          <div v-if="recentPosts.length === 0" class="empty-tip">暂无帖子</div>
          <div v-for="p in recentPosts" :key="p.id" class="feed-item" @click="goPost(p.id)">
            <span class="feed-title">{{ p.title }}</span>
            <span class="feed-meta">{{ p.authorNickname }} · {{ p.createdAt?.slice(0, 10) }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 最新问答 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>❓ 近期问答</span>
              <el-button text type="primary" size="small" @click="goQA">更多 →</el-button>
            </div>
          </template>
          <div v-if="recentQuestions.length === 0" class="empty-tip">暂无问题</div>
          <div v-for="q in recentQuestions" :key="q.id" class="feed-item" @click="goQuestion(q.id)">
            <span class="feed-title">
              <el-tag v-if="q.isSolved" size="small" type="success" style="margin-right:4px">已解</el-tag>
              {{ q.title }}
            </span>
            <span class="feed-meta">{{ q.authorNickname }} · 💬{{ q.answerCount }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.home {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 0;
}
h2 {
  text-align: center;
  font-size: 26px;
  color: #303133;
  margin-bottom: 6px;
}
.subtitle {
  text-align: center;
  color: #909399;
  margin-bottom: 28px;
  font-size: 15px;
}

/* 功能入口 6 宫格 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-bottom: 10px;
}
.feature-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 24px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform .15s, box-shadow .15s;
  font-size: 16px;
  font-weight: 500;
  color: #fff;
}
.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
}
.feature-card .icon {
  font-size: 28px;
}
.feature-card.info {
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.feature-card.community {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}
.feature-card.resource {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

/* 内容卡片 */
.content-row {
  margin-top: 10px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.feed-item {
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
  cursor: pointer;
}
.feed-item:last-child {
  border-bottom: none;
}
.feed-item:hover .feed-title {
  color: #409eff;
}
.feed-title {
  display: block;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.feed-meta, .feed-time {
  font-size: 12px;
  color: #909399;
  margin-top: 3px;
}
.empty-tip {
  text-align: center;
  color: #c0c4cc;
  padding: 20px 0;
  font-size: 14px;
}
</style>
