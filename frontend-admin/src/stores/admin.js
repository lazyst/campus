import { defineStore } from 'pinia'

const TOKEN_KEY = 'admin_token'
const INFO_KEY = 'admin_info'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || null,
    adminInfo: JSON.parse(localStorage.getItem(INFO_KEY) || 'null')
  }),

  getters: {
    isLoggedIn: (state) => {
      if (!state.token) return false
      // 检查 token 是否过期
      try {
        const payload = JSON.parse(atob(state.token.split('.')[1]))
        return payload.exp * 1000 > Date.now()
      } catch {
        return false
      }
    },

    getToken: (state) => state.token,

    getAdminInfo: (state) => state.adminInfo
  },

  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem(TOKEN_KEY, token)
    },

    setAdminInfo(info) {
      this.adminInfo = info
      localStorage.setItem(INFO_KEY, JSON.stringify(info))
    },

    login(token, adminInfo) {
      this.setToken(token)
      this.setAdminInfo(adminInfo)
    },

    logout() {
      this.token = null
      this.adminInfo = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(INFO_KEY)
    },

    // 检查 token 是否过期
    isTokenExpired() {
      if (!this.token) return true
      try {
        const payload = JSON.parse(atob(this.token.split('.')[1]))
        return payload.exp * 1000 < Date.now()
      } catch {
        return true
      }
    }
  }
})
