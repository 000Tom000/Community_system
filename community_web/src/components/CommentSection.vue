<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getComments, addComment, deleteComment, toggleCommentLike } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  postId: { type: Number, required: true }
})

const router = useRouter()
const userStore = useUserStore()

const comments = ref([])
const loading = ref(false)
const inputVisible = ref(false)   // 主输入框

// 主评论输入
const commentText = ref('')
const submitting = ref(false)

// 回复状态：{ commentId: { show: true, text: '' } }
const replyStates = ref({})

onMounted(fetchComments)

async function fetchComments() {
  loading.value = true
  try {
    const res = await getComments(props.postId)
    comments.value = res.data || []
  } catch { /* ignore */ }
  finally { loading.value = false }
}

/** 发顶级评论 */
async function handleSubmit() {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  submitting.value = true
  try {
    await addComment({ postId: props.postId, content: commentText.value })
    commentText.value = ''
    inputVisible.value = false
    ElMessage.success('评论成功')
    await fetchComments()
  } catch { /* ignore */ }
  finally { submitting.value = false }
}

/** 显示回复框 */
function showReply(commentId) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  replyStates.value[commentId] = { show: true, text: '' }
}

/** 提交回复 */
async function submitReply(parentId) {
  const state = replyStates.value[parentId]
  if (!state || !state.text.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  try {
    await addComment({ postId: props.postId, parentId, content: state.text })
    replyStates.value[parentId] = { show: false, text: '' }
    ElMessage.success('回复成功')
    await fetchComments()
  } catch { /* ignore */ }
}

/** 点赞评论 */
async function handleLike(commentId) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await toggleCommentLike(commentId)
    await fetchComments()
  } catch { /* ignore */ }
}

/** 删除评论 */
async function handleDelete(comment) {
  try {
    await ElMessageBox.confirm('确定删除这条评论？', '提示', { type: 'warning' })
    await deleteComment(comment.id)
    ElMessage.success('已删除')
    await fetchComments()
  } catch { /* cancel */ }
}

function isOwner(comment) {
  return userStore.user?.id === comment.userId
}
function isAdmin() {
  return userStore.user?.role === 'admin'
}
</script>

<template>
  <div class="comment-section">
    <div class="comment-header">
      <h3>💬 评论 ({{ comments.length }})</h3>
      <el-button v-if="!inputVisible" size="small" type="primary" @click="inputVisible = true">
        写评论
      </el-button>
    </div>

    <!-- 主输入框 -->
    <div v-if="inputVisible" class="comment-input">
      <el-input v-model="commentText" type="textarea" :rows="3" placeholder="写下你的评论…" />
      <div class="input-actions">
        <el-button size="small" @click="inputVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="handleSubmit">发表</el-button>
      </div>
    </div>

    <el-divider />

    <!-- 评论列表 -->
    <div v-loading="loading">
      <div v-if="comments.length === 0 && !loading" style="text-align:center;color:#909399;padding:30px">
        暂无评论，来说点什么吧
      </div>

      <div v-for="c in comments" :key="c.id" class="comment-item">
        <el-avatar :size="36" :src="c.authorAvatar" />
        <div class="comment-body">
          <div class="comment-author">
            <span class="nickname">{{ c.authorNickname }}</span>
            <span class="time">{{ c.createdAt }}</span>
          </div>
          <div class="comment-content">{{ c.content }}</div>
          <div class="comment-actions">
            <el-button link size="small" @click="handleLike(c.id)">
              👍 {{ c.likeCount || 0 }}
            </el-button>
            <el-button link size="small" @click="showReply(c.id)">回复</el-button>
            <el-button v-if="isOwner(c) || isAdmin()" link size="small" type="danger"
                       @click="handleDelete(c)">删除</el-button>
          </div>

          <!-- 回复列表 -->
          <div v-if="c.replies && c.replies.length" class="replies">
            <div v-for="r in c.replies" :key="r.id" class="reply-item">
              <el-avatar :size="28" :src="r.authorAvatar" />
              <div class="reply-body">
                <div class="comment-author">
                  <span class="nickname">{{ r.authorNickname }}</span>
                  <span class="time">{{ r.createdAt }}</span>
                </div>
                <div class="comment-content">{{ r.content }}</div>
                <div class="comment-actions">
                  <el-button link size="small" @click="handleLike(r.id)">
                    👍 {{ r.likeCount || 0 }}
                  </el-button>
                  <el-button link size="small" @click="showReply(c.id)">回复</el-button>
                  <el-button v-if="isOwner(r) || isAdmin()" link size="small" type="danger"
                             @click="handleDelete(r)">删除</el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 回复输入框 -->
          <div v-if="replyStates[c.id]?.show" class="reply-input">
            <el-input v-model="replyStates[c.id].text" type="textarea" :rows="2"
                      placeholder="回复 {{ c.authorNickname }}…" />
            <div class="input-actions">
              <el-button size="small" @click="replyStates[c.id].show = false">取消</el-button>
              <el-button size="small" type="primary" @click="submitReply(c.id)">回复</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-section {
  margin-top: 20px;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.comment-header h3 {
  margin: 0;
  font-size: 16px;
}
.comment-input, .reply-input {
  margin-top: 12px;
}
.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-author .nickname {
  font-weight: 500;
  font-size: 13px;
  color: #303133;
}
.comment-author .time {
  font-size: 12px;
  color: #c0c4cc;
  margin-left: 8px;
}
.comment-content {
  margin: 6px 0;
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  word-break: break-word;
}
.comment-actions {
  display: flex;
  gap: 4px;
}
.replies {
  margin-top: 10px;
  padding-left: 12px;
  border-left: 2px solid #e8e8e8;
}
.reply-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #fafafa;
}
.reply-item:last-child {
  border-bottom: none;
}
.reply-body {
  flex: 1;
}
</style>
