<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getResource, deleteResource, rateResource, getDownloadUrl } from '@/api/resource'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const id = Number(route.params.id)

const resource = ref(null)
const myRating = ref(0)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getResource(id)
    resource.value = res.data.resource
    myRating.value = res.data.myRating || 0
  } catch { router.push('/resources') }
  finally { loading.value = false }
})

const isOwner = () => userStore.user?.id === resource.value?.userId
const isAdmin = () => userStore.user?.role === 'admin'

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除此资源？', '提示', { type: 'warning' })
    await deleteResource(id)
    ElMessage.success('已删除')
    router.push('/resources')
  } catch { /* cancel */ }
}

const ratingHover = ref(0)
async function handleRate(star) {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return }
  try {
    await rateResource(id, star)
    myRating.value = star
    ElMessage.success(`已评 ${star} 分`)
    // 刷新以获取新平均分
    const res = await getResource(id)
    resource.value = res.data.resource
  } catch { /* ignore */ }
}

function download() {
  // 下载链接触发后，刷新页面更新计数
  window.open(getDownloadUrl(id), '_blank')
  setTimeout(async () => {
    const res = await getResource(id)
    resource.value = res.data.resource
  }, 1000)
}
</script>

<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="resource" class="detail-card">
      <!-- 头部 -->
      <div class="header">
        <div class="file-icon-large">📁</div>
        <div class="header-info">
          <h1>{{ resource.title }}</h1>
          <p class="author">上传者：{{ resource.authorNickname }} · {{ resource.createdAt }}</p>
          <p class="desc" v-if="resource.description">{{ resource.description }}</p>
        </div>
      </div>

      <el-divider />

      <!-- 文件信息 -->
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="文件名">{{ resource.fileName }}</el-descriptions-item>
        <el-descriptions-item label="大小">{{ resource.fileSizeStr }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ resource.fileType?.toUpperCase() }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ resource.category }}</el-descriptions-item>
        <el-descriptions-item label="下载">{{ resource.downloadCount }} 次</el-descriptions-item>
        <el-descriptions-item label="评分">
          ⭐ {{ resource.ratingAvg ?? '暂无' }}
          <span v-if="resource.ratingCount">({{ resource.ratingCount }}人)</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 操作 -->
      <div class="actions">
        <el-button type="primary" size="large" @click="download">⬇️ 下载</el-button>

        <!-- 评分 -->
        <div class="rating-stars">
          <span v-for="i in 5" :key="i"
                :class="['star', { active: i <= (ratingHover || myRating) }]"
                @click="handleRate(i)"
                @mouseenter="ratingHover = i"
                @mouseleave="ratingHover = 0">
            {{ i <= (ratingHover || myRating) ? '⭐' : '☆' }}
          </span>
          <span v-if="myRating > 0" class="my-rate">(我的评分: {{ myRating }})</span>
        </div>

        <div class="admin-actions">
          <el-button v-if="isOwner() || isAdmin()" type="danger" @click="handleDelete">删除</el-button>
          <el-button @click="router.push('/resources')">返回列表</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page { max-width: 800px; margin: 0 auto; padding: 20px 0; }
.detail-card { background: #fff; padding: 30px 40px; border-radius: 8px; box-shadow: 0 1px 6px rgba(0,0,0,0.06); }
.header { display: flex; gap: 20px; align-items: flex-start; }
.file-icon-large { font-size: 60px; }
.header-info h1 { font-size: 22px; margin-bottom: 8px; }
.author { font-size: 14px; color: #909399; }
.desc { margin-top: 8px; font-size: 15px; color: #606266; white-space: pre-wrap; }
.actions { margin-top: 20px; display: flex; flex-wrap: wrap; gap: 16px; align-items: center; }
.rating-stars { display: flex; align-items: center; gap: 2px; }
.star { font-size: 24px; cursor: pointer; transition: transform .1s; }
.star:hover { transform: scale(1.2); }
.my-rate { font-size: 13px; color: #909399; margin-left: 6px; }
.admin-actions { margin-left: auto; display: flex; gap: 10px; }
</style>
