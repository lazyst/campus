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

// Token 刷新相关
let isRefreshing = false
let requestsQueue = []

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
 * Token 刷新函数
 */
function refreshToken() {
  return request.post('/auth/refresh', {}, {
    showLoading: false,
    showError: false
  }).then(res => {
    if (res.token) {
      localStorage.setItem('token', res.token)
      return res.token
    }
    throw new Error('Token刷新失败')
  }).catch(() => {
    // 刷新失败，清除 token 并提示登录
    localStorage.removeItem('token')
    showToast('登录已过期，请重新登录', 'error')
    import('@/stores/loginConfirm').then(({ showLoginConfirm }) => {
      showLoginConfirm()
    })
    return null
  })
}

/**
 * 处理 token 刷新
 */
function handleTokenRefresh(error) {
  const originalRequest = error.config

  // 如果不是 401 或者已经在刷新中或者没有重试过
  if (!error.response || error.response.status !== 401 || isRefreshing || originalRequest._retry) {
    return Promise.reject(error)
  }

  originalRequest._retry = true

  if (!isRefreshing) {
    isRefreshing = true

    return refreshToken().then(newToken => {
      isRefreshing = false

      if (newToken) {
        // 更新原请求的 token 并重试
        originalRequest.headers.Authorization = `Bearer ${newToken}`

        // 处理队列中的请求
        requestsQueue.forEach(cb => cb(newToken))
        requestsQueue = []

        return request(originalRequest)
      }

      return Promise.reject(error)
    }).catch(() => {
      isRefreshing = false
      requestsQueue = []
      return Promise.reject(error)
    })
  }

  // 如果正在刷新，把请求加入队列
  return new Promise((resolve, reject) => {
    requestsQueue.push((token) => {
      originalRequest.headers.Authorization = `Bearer ${token}`
      resolve(request(originalRequest))
    })
  })
}

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

      // 返回data,确保不返回undefined
      return data !== undefined ? data : response.data
    }

    // 业务错误
    handleBusinessError(code, message, response.config)
    return Promise.reject(new Error(message))
  },
  (error) => {
    // 1. 关闭Loading
    finishLoading()

    // 2. 处理 401 错误 - 尝试刷新 token
    if (error.response?.status === 401) {
      return handleTokenRefresh(error)
    }

    // 3. 网络错误或超时
    if (!error.response) {
      handleNetworkError(error)
      return Promise.reject(error)
    }

    // 4. HTTP错误状态码
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
        // 登录接口返回 401 时显示"用户名或密码错误"
        // 其他需要认证的操作触发登录确认对话框
        const authRequiredUrls = ['/auth/', '/posts/my', '/posts/collections']
        const isAuthRequired = authRequiredUrls.some(url => config.url?.includes(url))
        const isLoginUrl = config.url?.includes('/auth/login')

        if (isLoginUrl) {
          // 登录接口的 401 错误
          showToast(message || '用户名或密码错误', 'error')
        } else if (isAuthRequired) {
          // 需要登录的操作
          localStorage.removeItem('token')
          import('@/stores/loginConfirm').then(({ showLoginConfirm }) => {
            showLoginConfirm()
          })
        } else {
          // 其他401错误
          console.warn('操作需要登录:', message)
        }
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
