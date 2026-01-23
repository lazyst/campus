import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, logout as logoutApi, getUserInfo, type RegisterResult } from '@/api/auth'
import type { LoginParams, RegisterParams, UserInfo } from '@/api/auth'
import router from '@/router'

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

  async function register(params: RegisterParams) {
    const result: RegisterResult = await registerApi(params)
    setToken(result.token)
    userInfo.value = {
      id: result.user.id,
      phone: result.user.phone,
      nickname: result.user.nickname,
      avatar: result.user.avatar,
      gender: result.user.gender,
      bio: result.user.bio
    }
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
