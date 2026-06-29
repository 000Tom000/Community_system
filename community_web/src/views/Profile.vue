<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserById } from '@/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const profile = ref(null)
const loading = ref(true)

onMounted(async () => {
  const id = route.params.id
  // 如果是自己的 id，直接用 store 里的
  if (userStore.isLoggedIn && Number(id) === userStore.user?.id) {
    profile.value = userStore.user
    loading.value = false
    return
  }
  try {
    const res = await getUserById(id)
    profile.value = res.data
  } catch {
    router.push('/')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="profile" v-loading="loading">
    <el-card v-if="profile">
      <div class="profile-header">
        <el-avatar :size="80" :src="profile.avatar" />
        <div class="profile-info">
          <h2>{{ profile.nickname }}</h2>
          <p class="username">@{{ profile.username }}</p>
          <p v-if="profile.bio" class="bio">{{ profile.bio }}</p>
        </div>
        <el-button
          v-if="userStore.isLoggedIn && userStore.user?.id === profile.id"
          type="primary"
          @click="router.push('/settings')"
        >
          编辑资料
        </el-button>
      </div>

      <el-divider />

      <el-descriptions :column="2" border>
        <el-descriptions-item label="学校" v-if="profile.school">
          {{ profile.school }}
        </el-descriptions-item>
        <el-descriptions-item label="专业" v-if="profile.major">
          {{ profile.major }}
        </el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ { 0: '未设置', 1: '男', 2: '女' }[profile.gender] || '未设置' }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱" v-if="profile.email">
          {{ profile.email }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号" v-if="profile.phone">
          {{ profile.phone }}
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ profile.createdAt }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 后续扩展: 用户发布的帖子列表、收藏等 -->
      <el-empty description="暂无更多内容" style="margin-top: 30px" />
    </el-card>
  </div>
</template>

<style scoped>
.profile {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}
.profile-info {
  flex: 1;
}
.profile-info h2 {
  font-size: 22px;
  margin-bottom: 4px;
}
.username {
  color: #909399;
  font-size: 14px;
  margin-bottom: 6px;
}
.bio {
  color: #606266;
  font-size: 14px;
}
</style>
