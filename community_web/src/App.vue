<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 启动时尝试恢复登录状态
onMounted(() => {
  userStore.fetchUser()
})
</script>

<template>
  <router-view />
</template>

<style>
/* 全局 reset，element-plus 之上微调 */
body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', Arial, sans-serif;
}

a {
  text-decoration: none;
  color: inherit;
}
</style>
