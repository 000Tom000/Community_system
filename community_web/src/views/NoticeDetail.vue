<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getNotice, deleteNotice, togglePin } from '@/api/notice'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const notice = ref(null)
const loading = ref(true)

const noticeId = Number(route.params.id)

onMounted(async () => {
  try {
    const res = await getNotice(noticeId)
    notice.value = res.data
  } catch { router.push('/notices') }
  finally { loading.value = false }
})

const isAdmin = () => userStore.user?.role === 'admin'

function handleEdit() { router.push(`/notices/${noticeId}/edit`) }

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除此公告？', '提示', { type: 'warning' })
    await deleteNotice(noticeId)
    ElMessage.success('已删除')
    router.push('/notices')
  } catch { /* 取消 */ }
}

async function handlePin() {
  try {
    await togglePin(noticeId)
    notice.value.isPinned = !notice.value.isPinned
    ElMessage.success(notice.value.isPinned ? '已置顶' : '已取消置顶')
  } catch { /* 拦截器已提示 */ }
}

const categoryLabels = { academic:'学术讲座', club:'社团活动', admin:'学校通知', exam:'考试教务', general:'综合' }
</script>

<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="notice" class="detail-card">
      <div class="header">
        <h1>
          <el-tag v-if="notice.isPinned" size="small" type="danger" style="margin-right:8px">置顶</el-tag>
          <el-tag v-if="notice.level==='urgent'" size="small" type="danger" style="margin-right:8px">紧急</el-tag>
          <el-tag v-else-if="notice.level==='important'" size="small" type="warning" style="margin-right:8px">重要</el-tag>
          <el-tag size="small" type="info" style="margin-right:8px">{{ categoryLabels[notice.category] || notice.category }}</el-tag>
          {{ notice.title }}
        </h1>
        <div class="header-meta">
          <el-avatar :size="36" :src="notice.authorAvatar" />
          <span class="author">{{ notice.authorNickname }}</span>
          <span class="time">{{ notice.createdAt }}</span>
          <span class="views">👁 {{ notice.viewCount }} 阅读</span>
        </div>
      </div>

      <el-divider />

      <div class="content">{{ notice.content }}</div>

      <el-divider />

      <div class="actions">
        <template v-if="isAdmin()">
          <el-button @click="handleEdit">编辑</el-button>
          <el-button type="warning" @click="handlePin">
            {{ notice.isPinned ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button type="danger" @click="handleDelete">删除</el-button>
        </template>
        <el-button @click="router.push('/notices')">返回列表</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page { max-width: 800px; margin: 0 auto; padding: 20px 0; }
.detail-card { background: #fff; padding: 30px 40px; border-radius: 8px; box-shadow: 0 1px 6px rgba(0,0,0,0.06); }
.header h1 { font-size: 24px; margin-bottom: 12px; color: #303133; }
.header-meta { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #909399; flex-wrap: wrap; }
.author { font-weight: 500; color: #606266; }
.content { font-size: 16px; line-height: 1.8; color: #303133; min-height: 100px; white-space: pre-wrap; word-break: break-word; }
.actions { display: flex; flex-wrap: wrap; gap: 10px; }
</style>
