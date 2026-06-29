import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMe } from '@/api/user'

/**
 * 用户状态 — 全局共享，任意组件都能拿当前登录用户
 */
export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const isLoggedIn = ref(false)

  /** 设置用户信息 */
  function setUser(u) {
    user.value = u
    isLoggedIn.value = !!u
    if (u?.token) {
      localStorage.setItem('token', u.token)
    }
  }

  /** 从服务端拉取最新用户信息 */
  async function fetchUser() {
    const token = localStorage.getItem('token')
    if (!token) {
      clearUser()
      return
    }
    try {
      const res = await getMe()
      setUser(res.data)
    } catch {
      clearUser()
    }
  }

  /** 退出登录 */
  function clearUser() {
    user.value = null
    isLoggedIn.value = false
    localStorage.removeItem('token')
  }

  return { user, isLoggedIn, setUser, fetchUser, clearUser }
})
