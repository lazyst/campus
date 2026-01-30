import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { login as loginApi, register as registerApi, logout as logoutApi, getUserInfo } from '@/api/modules'
import router from '@/router'

// 从 localStorage 恢复数据
function loadState() {
  try {
    const stored = localStorage.getItem('user')
    if (stored) {
      const parsed = JSON.parse(stored)
      return {
        token: parsed.token || localStorage.getItem('token'),
        userInfo: parsed.userInfo || null
      }
    }
  } catch (e) {
    console.warn('恢复用户状态失败:', e)
  }
  return {
    token: localStorage.getItem('token'),
    userInfo: null
  }
}

export const useUserStore = defineStore('user', () => {
  const { token: storedToken, userInfo: storedUserInfo } = loadState()

  const token = ref(storedToken)
  const userInfo = ref(storedUserInfo)
  const isInitialized = ref(false)

  // 监听 userInfo 变化，自动持久化
  watch(userInfo, (newValue) => {
    try {
      const state = {
        token: token.value,
        userInfo: newValue
      }
      localStorage.setItem('user', JSON.stringify(state))
    } catch (e) {
      console.warn('保存用户状态失败:', e)
    }
  }, { deep: true })

  // 监听 token 变化，同步保存
  watch(token, (newValue) => {
    if (newValue) {
      localStorage.setItem('token', newValue)
    } else {
      localStorage.removeItem('token')
    }
  })

  function setToken(t) {
    token.value = t
  }

  function removeToken() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function login(params) {
    const result = await loginApi(params)
    if (typeof result === 'string' && result.startsWith('eyJ')) {
      setToken(result)
    }
    await fetchUserInfo()
    return result
  }

  async function register(params) {
    const result = await registerApi(params)
    if (result.token) {
      token.value = result.token
    }
    userInfo.value = result.user
    return result
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      console.warn('登出API调用失败:', e)
    }
    removeToken()
    router.push('/login')
  }

  async function fetchUserInfo() {
    try {
      const data = await getUserInfo()
      userInfo.value = data
    } catch (error) {
      console.warn('获取用户信息失败:', error)
    }
  }

  // 初始化函数
  async function initialize() {
    if (token.value && !isInitialized.value) {
      await fetchUserInfo()
      isInitialized.value = true
    }
  }

  return {
    token,
    userInfo,
    isInitialized,
    login,
    register,
    logout,
    fetchUserInfo,
    initialize
  }
})
