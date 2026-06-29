<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<template>
  <div class="home">
    <h2>👋 欢迎来到校园交流社区</h2>
    <p class="subtitle">信息发布 · 互动交流 · 资源共享</p>

    <el-row :gutter="20" class="feature-cards">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="card-icon">📢</div>
          <h3>信息广场</h3>
          <p>校园公告、失物招领、二手交易，校园信息一站聚合</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="card-icon">💬</div>
          <h3>交流社区</h3>
          <p>论坛讨论、问答互助，找到志同道合的伙伴</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="card-icon">📚</div>
          <h3>学习资源</h3>
          <p>课件、笔记、书单共享，互助学习共同进步</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 未登录提示 -->
    <el-alert
      v-if="!userStore.isLoggedIn"
      title="登录后即可发布内容、参与讨论"
      type="info"
      show-icon
      :closable="false"
      style="margin-top: 30px"
    />
  </div>
</template>

<style scoped>
.home {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 0;
}
h2 {
  text-align: center;
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
}
.subtitle {
  text-align: center;
  color: #909399;
  margin-bottom: 40px;
  font-size: 16px;
}
.feature-cards .el-card {
  text-align: center;
  cursor: pointer;
  min-height: 200px;
}
.card-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.feature-cards h3 {
  margin-bottom: 10px;
  color: #303133;
}
.feature-cards p {
  color: #909399;
  font-size: 14px;
  line-height: 1.6;
}
</style>
