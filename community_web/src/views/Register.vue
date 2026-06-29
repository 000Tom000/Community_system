<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { register as registerApi } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: '',
  password2: '',
  nickname: '',
})
const loading = ref(false)

async function handleRegister() {
  if (!form.username || !form.password || !form.nickname) {
    ElMessage.warning('请填写所有必填字段')
    return
  }
  if (form.username.length < 3) {
    ElMessage.warning('用户名至少 3 个字符')
    return
  }
  if (form.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  if (form.password !== form.password2) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    const res = await registerApi(form.username, form.password, form.nickname)
    userStore.setUser(res.data)
    ElMessage.success('注册成功')
    router.push('/')
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <h2>🎓 欢迎注册</h2>
      <el-form :model="form" label-width="0" size="large" @keyup.enter="handleRegister">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名（3~50 个字符）" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.nickname" placeholder="昵称（必填）" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码（至少 6 位）" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password2" type="password" placeholder="确认密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="extra">
        <span>已有账号？<router-link to="/login">去登录</router-link></span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}
.register-card {
  width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}
h2 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 22px;
  color: #303133;
}
.extra {
  text-align: center;
  font-size: 14px;
  color: #909399;
}
.extra a {
  color: #409eff;
}
</style>
