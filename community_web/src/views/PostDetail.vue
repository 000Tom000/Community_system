<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPost, deletePost, toggleLike, getLikeStatus, togglePin, toggleSolved } from '@/api/post'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const loading = ref(true)
const liked = ref(false)
const likeCount = ref(0)
const likeLoading = ref(false)

const postId = Number(route.params.id)

onMounted(async () => {
  try {
    const res = await getPost(postId)
    post.value = res.data
    likeCount.value = res.data.likeCount || 0
  } catch {
    router.push('/forum')
  } finally {
    loading.value = false
  }
  // 查询点赞状态
  if (userStore.isLoggedIn) {
    try {
      const r = await getLikeStatus(postId)
      liked.value = r.data.liked
    } catch { /* ignore */ }
  }
})

/** 点赞 */
async function handleLike() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (likeLoading.value) return
  likeLoading.value = true
  try {
    const res = await toggleLike(postId)
    liked.value = res.data.liked
    likeCount.value += res.data.liked ? 1 : -1
  } catch { /* ignore */ }
  finally { likeLoading.value = false }
}

/** 编辑 */
function handleEdit() {
  router.push(`/forum/${postId}/edit`)
}

/** 删除 */
async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除此帖子？', '提示', { type: 'warning' })
    await deletePost(postId)
    ElMessage.success('已删除')
    router.push('/forum')
  } catch { /* 取消或不处理 */ }
}

/** 置顶 */
async function handlePin() {
  try {
    await togglePin(postId)
    post.value.isPinned = !post.value.isPinned
    ElMessage.success(post.value.isPinned ? '已置顶' : '已取消置顶')
  } catch { /* ignore */ }
}

/** 标记解决 */
async function handleSolved() {
  try {
    await toggleSolved(postId)
    post.value.isSolved = !post.value.isSolved
    ElMessage.success(post.value.isSolved ? '已标记解决' : '已取消标记')
  } catch { /* ignore */ }
}

/** 判断是否本人 */
const isOwner = () => userStore.user?.id === post.value?.userId
const isAdmin = () => userStore.user?.role === 'admin'
</script>

<template>
  <div class="detail" v-loading="loading">
    <div v-if="post" class="detail-card">
      <!-- 标题区 -->
      <div class="header">
        <h1>
          <el-tag v-if="post.isPinned" size="small" type="danger">置顶</el-tag>
          <el-tag v-if="post.isSolved" size="small" type="success">已解决</el-tag>
          {{ post.title }}
        </h1>
        <div class="header-meta">
          <el-avatar :size="36" :src="post.authorAvatar" />
          <span class="author">{{ post.authorNickname }}</span>
          <span class="time">{{ post.createdAt }}</span>
          <span class="views">👁 {{ post.viewCount }} 阅读</span>
        </div>
      </div>

      <el-divider />

      <!-- 正文 -->
      <div class="content" v-html="post.content.replace(/\n/g, '<br>')"></div>

      <el-divider />

      <!-- 操作栏 -->
      <div class="actions">
        <el-button
          :type="liked ? 'danger' : 'default'"
          :loading="likeLoading"
          @click="handleLike"
          :icon="liked ? 'StarFilled' : ''"
        >
          👍 {{ likeCount }}
        </el-button>

        <template v-if="isOwner() || isAdmin()">
          <el-button v-if="isOwner()" @click="handleEdit">编辑</el-button>
          <el-button v-if="isOwner()" type="success" @click="handleSolved">
            {{ post.isSolved ? '取消解决' : '标记解决' }}
          </el-button>
          <el-button v-if="isAdmin()" type="warning" @click="handlePin">
            {{ post.isPinned ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button v-if="isOwner() || isAdmin()" type="danger" @click="handleDelete">删除</el-button>
        </template>

        <el-button @click="router.push('/forum')">返回列表</el-button>
      </div>

      <!-- 评论区占位 -->
      <el-divider />
      <el-empty description="🚧 评论功能即将上线" />
    </div>
  </div>
</template>

<style scoped>
.detail {
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
</style>
