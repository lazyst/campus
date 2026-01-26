import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, logout as logoutApi, getUserInfo } from '@/api/modules'
import router from '@/router'

// 本地类型定义
interface UserInfo {
  id: number
  phone: string
  nickname: string
  avatar?: string
  gender?: number
  bio?: string
}

interface LoginParams {
  phone: string
  password: string
}

interface RegisterParams {
  phone: string
  password: string
  nickname: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(null)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
  }

  function removeToken() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
  }

  async function login(params: LoginParams) {
    const data = await loginApi(params)
    setToken(data)
    await fetchUserInfo()
    return data
  }

  async function register(params) {
    const result = await registerApi(params)
    // 新的register API已经在内部处理了token保存
    // result包含 { user, token }
    if (result.token) {
      token.value = result.token
    }
    userInfo.value = result.user
    return result
  }

  async function logout() {
    await logoutApi()
    removeToken()
    router.push('/login')
  }

  async function fetchUserInfo() {
    try {
      const data = await getUserInfo()
      userInfo.value = data
    } catch (error) {
      removeToken()
    }
  }

  return {
    token,
    userInfo,
    login,
    register,
    logout,
    fetchUserInfo
  }
})
