<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPosts } from '@/api/post'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 分类 tabs
const categoryTabs = [
  { key: '', label: '全部' },
  { key: 'study', label: '学习交流' },
  { key: 'life', label: '校园生活' },
  { key: 'kaoyan', label: '考研升学' },
  { key: 'teamup', label: '组队招募' },
  { key: 'tech', label: '技术讨论' },
  { key: 'other', label: '其他' },
]

const activeCategory = ref('')
const keyword = ref('')
const posts = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const categoryLabels = Object.fromEntries(categoryTabs.map(t => [t.key, t.label]))

async function fetchPosts() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (activeCategory.value) params.category = activeCategory.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getPosts(params)
    posts.value = res.data.list
    total.value = res.data.total
  } catch { /* 拦截器已提示 */ }
  finally { loading.value = false }
}

function search() {
  page.value = 1
  fetchPosts()
}

function goDetail(id) {
  router.push(`/forum/${id}`)
}

function goCreate() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/forum/create')
}

watch(activeCategory, () => {
  page.value = 1
  fetchPosts()
})

onMounted(fetchPosts)
</script>

<template>
  <div class="forum">
    <!-- 顶栏：分类 + 搜索 + 发帖 -->
    <div class="forum-toolbar">
      <div class="tabs">
        <el-radio-group v-model="activeCategory" size="small">
          <el-radio-button v-for="t in categoryTabs" :key="t.key" :value="t.key">
            {{ t.label }}
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="actions">
        <el-input v-model="keyword" placeholder="搜索标题…" size="small" style="width:200px"
                  clearable @keyup.enter="search" @clear="search" />
        <el-button type="primary" size="small" @click="goCreate">✍️ 发帖</el-button>
      </div>
    </div>

    <!-- 列表 -->
    <div v-loading="loading" class="post-list">
      <div v-if="posts.length === 0 && !loading" class="empty">
        <el-empty description="暂无帖子" />
      </div>

      <div v-for="post in posts" :key="post.id" class="post-card" @click="goDetail(post.id)">
        <div class="post-left">
          <el-avatar :size="40" :src="post.authorAvatar" />
        </div>
        <div class="post-body">
          <div class="post-title">
            <el-tag v-if="post.isPinned" size="small" type="danger" style="margin-right:6px">置顶</el-tag>
            <el-tag v-if="post.isSolved" size="small" type="success" style="margin-right:6px">已解决</el-tag>
            <span class="title-text">{{ post.title }}</span>
          </div>
          <div class="post-meta">
            <span class="author">{{ post.authorNickname }}</span>
            <span class="dot">·</span>
            <span>{{ post.createdAt }}</span>
            <span class="dot">·</span>
            <span>{{ categoryLabels[post.category] || post.category }}</span>
          </div>
        </div>
        <div class="post-stats">
          <span>👁 {{ post.viewCount }}</span>
          <span>👍 {{ post.likeCount }}</span>
          <span>💬 {{ post.commentCount }}</span>
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
        @current-change="fetchPosts"
        background
        small
      />
    </div>
  </div>
</template>

<style scoped>
.forum {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 0;
}

.forum-toolbar {
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

.post-card {
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
.post-card:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.post-body {
  flex: 1;
  min-width: 0;
}
.title-text {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
.post-meta {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}
.dot {
  margin: 0 6px;
}
.post-stats {
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
