import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'

// 创建 axios 实例
const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
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
    // ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // ElMessage.error(error.message || '网络错误')
    if (error.response?.status === 401) {
      localStorage.removeItem('admin_token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api

// 封装的请求方法
export const get = <T>(url: string, params?: object): Promise<T> => {
  return api.get(url, { params }).then((res) => res.data)
}

export const post = <T>(url: string, data?: object): Promise<T> => {
  return api.post(url, data).then((res) => res.data)
}

export const put = <T>(url: string, data?: object): Promise<T> => {
  return api.put(url, data).then((res) => res.data)
}

export const del = <T>(url: string): Promise<T> => {
  return api.delete(url).then((res) => res.data)
}
