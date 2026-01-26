// frontend-user/src/api/request.js

import axios from 'axios'
import { showToast, showLoading, hideLoading } from './toast'

/**
 * 创建axios实例
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求队列管理（用于Loading状态）
let requestCount = 0

/**
 * 请求拦截器
 */
request.interceptors.request.use(
  (config) => {
    // 1. 自动添加Token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 2. Loading状态管理
    if (config.showLoading !== false) {
      requestCount++
      showLoading(config.loadingText || '加载中...')
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 */
request.interceptors.response.use(
  (response) => {
    // 1. 关闭Loading
    finishLoading()

    // 2. 统一处理响应数据
    const { code, message, data, token } = response.data

    // 成功响应
    if (code === 200) {
      // 自动保存新token
      if (token) {
        localStorage.setItem('token', token)
      }

      // 成功提示（可选）
      if (response.config.showSuccess !== false && response.method !== 'get') {
        showToast(message || '操作成功', 'success')
      }

      return data
    }

    // 业务错误
    handleBusinessError(code, message, response.config)
    return Promise.reject(new Error(message))
  },
  (error) => {
    // 1. 关闭Loading
    finishLoading()

    // 2. 网络错误或超时
    if (!error.response) {
      handleNetworkError(error)
      return Promise.reject(error)
    }

    // 3. HTTP错误状态码
    handleHttpError(error)
    return Promise.reject(error)
  }
)

/**
 * 关闭Loading
 */
function finishLoading() {
  requestCount--
  if (requestCount <= 0) {
    requestCount = 0
    hideLoading()
  }
}

/**
 * 业务错误处理
 */
function handleBusinessError(code, message, config) {
  if (config.showError !== false) {
    switch (code) {
      case 400:
        showToast(message || '请求参数错误', 'error')
        break
      case 401:
        showToast('登录已过期，请重新登录', 'warning')
        localStorage.removeItem('token')
        setTimeout(() => {
          window.location.href = '/login'
        }, 1500)
        break
      case 403:
        showToast('没有权限执行此操作', 'error')
        break
      default:
        showToast(message || '操作失败', 'error')
    }
  }
}

/**
 * HTTP错误处理
 */
function handleHttpError(error) {
  const status = error.response?.status
  const message = error.response?.data?.message || '请求失败'

  if (error.config?.showError !== false) {
    switch (status) {
      case 404:
        showToast('请求的资源不存在', 'error')
        break
      case 500:
        showToast('服务器错误，请稍后重试', 'error')
        break
      case 502:
      case 503:
      case 504:
        showToast('服务暂时不可用，请稍后重试', 'error')
        break
      default:
        showToast(message, 'error')
    }
  }
}

/**
 * 网络错误处理
 */
function handleNetworkError(error) {
  if (error.config?.showError !== false) {
    if (error.code === 'ECONNABORTED') {
      showToast('请求超时，请检查网络', 'error')
    } else {
      showToast('网络连接失败，请检查网络', 'error')
    }
  }
}

export default request
