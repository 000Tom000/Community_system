<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { logout as logoutApi } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 侧边栏默认展开的菜单
const defaultOpeneds = computed(() => {
  const path = route.path
  if (path.startsWith('/notice') || path.startsWith('/lost-found') || path.startsWith('/secondhand'))
    return ['info']
  if (path.startsWith('/forum') || path.startsWith('/qa'))
    return ['community']
  if (path.startsWith('/resources'))
    return ['resource']
  if (path.startsWith('/profile') || path.startsWith('/settings'))
    return ['personal']
  return []
})

/** 退出登录 */
async function handleLogout() {
  try {
    await logoutApi()
  } catch { /* 忽略 */ }
  userStore.clearUser()
  router.push('/login')
}

/** 去登录 */
function goLogin() {
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <!-- ====== 左侧边栏 ====== -->
    <el-aside width="220px" class="aside">
      <div class="logo" @click="router.push('/')">
        <span class="logo-text">🎓 校园社区</span>
      </div>

      <el-menu
        :default-openeds="defaultOpeneds"
        :default-active="route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <!-- 首页 -->
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <!-- 信息广场 -->
        <el-sub-menu index="info">
          <template #title>
            <el-icon><Notification /></el-icon>
            <span>信息广场</span>
          </template>
          <el-menu-item index="/notices">📋 校园公告</el-menu-item>
          <el-menu-item index="/lost-found">🔍 失物招领</el-menu-item>
          <el-menu-item index="/secondhand">🛒 二手市场</el-menu-item>
        </el-sub-menu>

        <!-- 交流社区 -->
        <el-sub-menu index="community">
          <template #title>
            <el-icon><ChatDotRound /></el-icon>
            <span>交流社区</span>
          </template>
          <el-menu-item index="/forum">💬 论坛广场</el-menu-item>
          <el-menu-item index="/qa">❓ 问答中心</el-menu-item>
        </el-sub-menu>

        <!-- 学习资源 -->
        <el-sub-menu index="resource">
          <template #title>
            <el-icon><FolderOpened /></el-icon>
            <span>学习资源</span>
          </template>
          <el-menu-item index="/resources">📚 资料库</el-menu-item>
        </el-sub-menu>

        <!-- 个人中心 -->
        <el-sub-menu index="personal">
          <template #title>
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </template>
          <el-menu-item index="/settings">⚙️ 账号设置</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- ====== 右侧内容区 ====== -->
    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown>
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.user?.avatar" />
                <span class="nickname">{{ userStore.user?.nickname }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push(`/profile/${userStore.user?.id}`)">
                    我的主页
                  </el-dropdown-item>
                  <el-dropdown-item @click="router.push('/settings')">
                    账号设置
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" size="small" @click="goLogin">登录</el-button>
          </template>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}

/* ---- 侧边栏 ---- */
.aside {
  background-color: #304156;
  overflow-y: auto;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 2px;
}
.aside .el-menu {
  border-right: none;
}

/* ---- 顶栏 ---- */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
  height: 60px;
}
.header-left {
  display: flex;
  align-items: center;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.nickname {
  font-size: 14px;
  color: #333;
}

/* ---- 内容区 ---- */
.main-content {
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}
</style>
