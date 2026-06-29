<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateProfile, changePassword, uploadAvatar } from '@/api/user'
import { ElMessage } from 'element-plus'
const router = useRouter()
const userStore = useUserStore()

// 未登录
if (!userStore.isLoggedIn) {
  router.push('/login')
}

// ---- 资料表单 ----
const profileForm = reactive({
  nickname: '',
  avatar: '',
  email: '',
  phone: '',
  gender: 0,
  bio: '',
  school: '',
  major: '',
})
const profileLoading = ref(false)
const avatarUploading = ref(false)

onMounted(() => {
  const u = userStore.user
  if (u) {
    Object.assign(profileForm, {
      nickname: u.nickname || '',
      avatar: u.avatar || '',
      email: u.email || '',
      phone: u.phone || '',
      gender: u.gender ?? 0,
      bio: u.bio || '',
      school: u.school || '',
      major: u.major || '',
    })
  }
})

/** 头像上传 */
async function handleAvatarChange(file) {
  // 客户端校验
  if (!file.raw.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.raw.size > 2 * 1024 * 1024) {
    ElMessage.warning('头像不能超过 2MB')
    return
  }
  avatarUploading.value = true
  try {
    const res = await uploadAvatar(file.raw)
    profileForm.avatar = res.data.url
    ElMessage.success('头像上传成功')
  } catch { /* 拦截器已提示 */ }
  finally { avatarUploading.value = false }
}

/** 获取头像完整 URL */
function avatarUrl() {
  const av = profileForm.avatar
  if (!av) return ''
  // 外部链接直接使用，相对路径由 Vite proxy 转发
  return av
}

async function handleSaveProfile() {
  profileLoading.value = true
  try {
    await updateProfile(profileForm)
    ElMessage.success('资料已更新')
    await userStore.fetchUser()
  } catch {
    // 拦截器已提示
  } finally {
    profileLoading.value = false
  }
}

// ---- 密码修改 ----
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  newPassword2: '',
})
const pwdLoading = ref(false)

async function handleChangePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  if (pwdForm.newPassword !== pwdForm.newPassword2) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  pwdLoading.value = true
  try {
    await changePassword(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    userStore.clearUser()
    router.push('/login')
  } catch {
    // 拦截器已提示
  } finally {
    pwdLoading.value = false
  }
}
</script>

<template>
  <div class="settings">
    <el-row :gutter="20">
      <!-- 左侧：基本资料 -->
      <el-col :span="14">
        <el-card>
          <template #header><span>📝 基本资料</span></template>
          <el-form :model="profileForm" label-width="80px">
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" />
            </el-form-item>
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-avatar :size="80" :src="avatarUrl()" style="margin-right:16px" />
                <el-upload
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleAvatarChange"
                  accept="image/*"
                >
                  <el-button :loading="avatarUploading" type="primary" size="small">
                    点击上传
                  </el-button>
                  <template #tip>
                    <div class="el-upload__tip">支持 JPG/PNG/GIF，最大 2MB</div>
                  </template>
                </el-upload>
              </div>
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="profileForm.gender">
                <el-radio :value="0">未设置</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="profileForm.bio" type="textarea" :rows="3" placeholder="介绍一下自己…" />
            </el-form-item>
            <el-form-item label="学校">
              <el-input v-model="profileForm.school" placeholder="你的学校" />
            </el-form-item>
            <el-form-item label="专业">
              <el-input v-model="profileForm.major" placeholder="你的专业" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="联系邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="手机号码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileLoading" @click="handleSaveProfile">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：安全设置 -->
      <el-col :span="10">
        <el-card>
          <template #header><span>🔒 安全设置</span></template>
          <el-form :model="pwdForm" label-width="100px">
            <el-form-item label="当前密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="pwdForm.newPassword2" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :loading="pwdLoading" @click="handleChangePwd">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.settings {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px 0;
}
.avatar-upload {
  display: flex;
  align-items: center;
}
</style>
