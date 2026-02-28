import axios from 'axios'

// 创建 axios 实例
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''
// 开发环境（空值）使用相对路径走 vite proxy；生产环境使用完整URL
const baseURL = apiBaseUrl
  ? (apiBaseUrl.includes('/api') ? apiBaseUrl : `${apiBaseUrl}/api`)
  : ''  // 空字符串表示相对路径
const api = axios.create({
  baseURL: baseURL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 0 || res.code === 200) {
      return res
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
      window.location.href = '/admin/login'
    }
    return Promise.reject(error)
  }
)

export default api

export function get(url, params) {
  return api.get(url, { params })
}

export function post(url, data) {
  return api.post(url, data)
}

export function put(url, data) {
  return api.put(url, data)
}

export function del(url) {
  return api.delete(url)
}
