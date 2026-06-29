<script setup>
/**
 * 通用占位页面 — 后续逐步替换为实际功能
 */
defineProps({
  title: { type: String, required: true },
  icon: { type: String, default: '📄' },
  desc: { type: String, default: '' },
})
</script>

<template>
  <div class="placeholder">
    <div class="placeholder-card">
      <div class="icon">{{ icon }}</div>
      <h2>{{ title }}</h2>
      <p v-if="desc">{{ desc }}</p>
      <el-divider />
      <p class="tip">🚧 该功能正在建设中，敬请期待…</p>
    </div>
  </div>
</template>

<style scoped>
.placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
.placeholder-card {
  text-align: center;
  background: #fff;
  padding: 60px 80px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.icon {
  font-size: 64px;
  margin-bottom: 16px;
}
h2 {
  font-size: 22px;
  color: #303133;
  margin-bottom: 8px;
}
p {
  color: #909399;
  margin-bottom: 0;
}
.tip {
  color: #c0c4cc;
  font-size: 14px;
}
</style>
