<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getItem, deleteItem, closeItem } from '@/api/lostfound'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const item = ref(null)
const loading = ref(true)
const closing = ref(false)

const itemId = Number(route.params.id)

onMounted(async () => {
  try {
    const res = await getItem(itemId)
    item.value = res.data
  } catch {
    router.push('/lost-found')
  } finally {
    loading.value = false
  }
})

/* ----- 操作 ----- */

function handleEdit() {
  router.push(`/lost-found/${itemId}/edit`)
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除此信息？', '提示', { type: 'warning' })
    await deleteItem(itemId)
    ElMessage.success('已删除')
    router.push('/lost-found')
  } catch { /* 取消 */ }
}

async function handleClose() {
  closing.value = true
  try {
    await closeItem(itemId)
    item.value.status = 1
    ElMessage.success('已标记为关闭')
  } catch { /* 拦截器已提示 */ }
  finally { closing.value = false }
}

/* ----- 权限 ----- */

const isOwner = () => userStore.user?.id === item.value?.userId
const isAdmin = () => userStore.user?.role === 'admin'

const categoryLabels = {
  'electronics': '电子产品',
  'documents': '证件卡片',
  'clothing': '衣物饰品',
  'books': '书籍文具',
  'keys': '钥匙',
  'other': '其他',
}
</script>

<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="item" class="detail-card">
      <!-- 标题 -->
      <div class="header">
        <h1>
          <el-tag :type="item.type === 'lost' ? 'warning' : 'success'" size="default" style="margin-right:8px">
            {{ item.typeText }}
          </el-tag>
          <el-tag :type="item.status === 0 ? '' : 'info'" size="default" style="margin-right:8px">
            {{ item.statusText }}
          </el-tag>
          {{ item.title }}
        </h1>
        <div class="header-meta">
          <el-avatar :size="36" :src="item.authorAvatar" />
          <span class="author">{{ item.authorNickname }}</span>
          <span class="time">{{ item.createdAt }}</span>
          <span class="views">👁 {{ item.viewCount }} 阅读</span>
        </div>
      </div>

      <el-divider />

      <!-- 物品图片 -->
      <div v-if="item.imageUrl" class="image-area">
        <el-image :src="item.imageUrl" fit="contain" style="max-height:300px;border-radius:6px"
                  :preview-src-list="[item.imageUrl]" />
      </div>

      <!-- 信息栏 -->
      <el-descriptions :column="2" border size="default" style="margin-bottom:20px">
        <el-descriptions-item label="类型">
          <el-tag :type="item.type === 'lost' ? 'warning' : 'success'" size="small">{{ item.typeText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="分类">{{ categoryLabels[item.category] || item.category }}</el-descriptions-item>
        <el-descriptions-item label="地点" :span="2">{{ item.location || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ item.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="item.status === 0 ? '' : 'info'" size="small">{{ item.statusText }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 描述 -->
      <div v-if="item.description" class="description">
        <h4>📋 详细描述</h4>
        <p>{{ item.description }}</p>
      </div>

      <!-- 联系方式 — 突出显示 -->
      <div v-if="item.contact" class="contact-box">
        <h4>📞 联系方式</h4>
        <p class="contact-text">{{ item.contact }}</p>
      </div>

      <el-divider />

      <!-- 操作栏 -->
      <div class="actions">
        <template v-if="isOwner()">
          <el-button @click="handleEdit">编辑</el-button>
          <el-button v-if="item.status === 0" type="success" :loading="closing" @click="handleClose">
            {{ item.type === 'lost' ? '✅ 已找到' : '✅ 已归还' }}
          </el-button>
        </template>
        <el-button v-if="isOwner() || isAdmin()" type="danger" @click="handleDelete">删除</el-button>
        <el-button @click="router.push('/lost-found')">返回列表</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page {
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
.image-area {
  margin-bottom: 20px;
  text-align: center;
}
.description {
  margin-bottom: 16px;
}
.description h4 {
  font-size: 15px;
  color: #606266;
  margin-bottom: 8px;
}
.description p {
  font-size: 15px;
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}
.contact-box {
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 6px;
  padding: 14px 18px;
  margin-bottom: 10px;
}
.contact-box h4 {
  font-size: 15px;
  color: #409eff;
  margin: 0 0 6px 0;
}
.contact-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
