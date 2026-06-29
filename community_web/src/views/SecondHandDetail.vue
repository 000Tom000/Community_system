<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getItem, deleteItem, markSold } from '@/api/secondhand'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const item = ref(null)
const loading = ref(true)
const selling = ref(false)

const itemId = Number(route.params.id)

onMounted(async () => {
  try {
    const res = await getItem(itemId)
    item.value = res.data
  } catch { router.push('/secondhand') }
  finally { loading.value = false }
})

function handleEdit() { router.push(`/secondhand/${itemId}/edit`) }

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除此商品？', '提示', { type: 'warning' })
    await deleteItem(itemId)
    ElMessage.success('已删除')
    router.push('/secondhand')
  } catch { /* 取消 */ }
}

async function handleSold() {
  selling.value = true
  try {
    await markSold(itemId)
    item.value.status = 1
    ElMessage.success('已标记为已售')
  } catch { /* 拦截器已提示 */ }
  finally { selling.value = false }
}

const isOwner = () => userStore.user?.id === item.value?.userId
const isAdmin = () => userStore.user?.role === 'admin'

const categoryLabels = { electronics:'数码电子', books:'书籍教材', clothing:'服饰鞋包', sports:'运动户外', daily:'生活日用', other:'其他' }
</script>

<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="item" class="detail-card">
      <!-- 标题 -->
      <div class="header">
        <h1>
          <el-tag v-if="item.status === 0" type="success" size="default" style="margin-right:8px">在售</el-tag>
          <el-tag v-if="item.status === 1" type="info" size="default" style="margin-right:8px">已售</el-tag>
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

      <!-- 价格 — 大号红色 -->
      <div class="price-area">
        <span class="price">¥{{ item.price }}</span>
        <span v-if="item.originalPrice" class="original">原价 ¥{{ item.originalPrice }}</span>
      </div>

      <!-- 图片 -->
      <div v-if="item.imageUrl" class="image-area">
        <el-image :src="item.imageUrl" fit="contain" style="max-height:300px;border-radius:6px"
                  :preview-src-list="[item.imageUrl]" />
      </div>

      <!-- 信息栏 -->
      <el-descriptions :column="2" border style="margin:20px 0">
        <el-descriptions-item label="分类">{{ categoryLabels[item.category] || item.category }}</el-descriptions-item>
        <el-descriptions-item label="成色">{{ item.conditionText }}</el-descriptions-item>
        <el-descriptions-item label="议价">
          <el-tag :type="item.isNegotiable ? 'warning' : 'info'" size="small">
            {{ item.isNegotiable ? '支持议价' : '不议价' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="item.status === 0 ? 'success' : 'info'" size="small">{{ item.statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地点" :span="2">{{ item.location || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ item.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">
          <span class="contact-highlight">{{ item.contact }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 描述 -->
      <div v-if="item.description" class="desc">
        <h4>📋 商品描述</h4>
        <p>{{ item.description }}</p>
      </div>

      <!-- 联系方式突出 -->
      <div v-if="item.contact" class="contact-box">
        <h4>📞 联系方式</h4>
        <p>{{ item.contact }}</p>
      </div>

      <el-divider />

      <div class="actions">
        <template v-if="isOwner()">
          <el-button @click="handleEdit">编辑</el-button>
          <el-button v-if="item.status === 0" type="success" :loading="selling" @click="handleSold">✅ 标记已售</el-button>
        </template>
        <el-button v-if="isOwner() || isAdmin()" type="danger" @click="handleDelete">删除</el-button>
        <el-button @click="router.push('/secondhand')">返回列表</el-button>
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
.price-area { text-align: center; padding: 10px 0 16px; }
.price { font-size: 36px; font-weight: 700; color: #f56c6c; }
.original { font-size: 14px; color: #c0c4cc; text-decoration: line-through; margin-left: 12px; }
.image-area { margin-bottom: 10px; text-align: center; }
.desc { margin-bottom: 16px; }
.desc h4 { font-size: 15px; color: #606266; margin-bottom: 8px; }
.desc p { font-size: 15px; line-height: 1.7; color: #303133; white-space: pre-wrap; word-break: break-word; }
.contact-box { background: #ecf5ff; border: 1px solid #d9ecff; border-radius: 6px; padding: 14px 18px; margin-bottom: 10px; }
.contact-box h4 { font-size: 15px; color: #409eff; margin: 0 0 6px 0; }
.contact-box p { font-size: 18px; font-weight: 600; color: #303133; margin: 0; }
.contact-highlight { font-weight: 600; color: #303133; }
.actions { display: flex; flex-wrap: wrap; gap: 10px; }
</style>
