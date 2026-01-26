// frontend-user/src/api/modules/auth.js

import request from '../request'

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.phone - 手机号
 * @param {string} data.password - 密码
 * @returns {Promise<string>} JWT token
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * 用户注册
 * @param {Object} data - 注册数据
 * @param {string} data.phone - 手机号
 * @param {string} data.password - 密码
 * @param {string} data.nickname - 昵称
 * @returns {Promise<Object>} { user: Object, token: string }
 */
export function register(data) {
  // 注册成功后自动保存token到localStorage
  const rawAxios = axios.create({
    baseURL: '/api',
    timeout: 10000
  })

  return rawAxios.post('/auth/register', data).then(response => {
    const { data: user, token } = response.data
    if (token) {
      localStorage.setItem('token', token)
    }
    return { user, token }
  })
}

/**
 * 用户登出
 * @returns {Promise<void>}
 */
export function logout() {
  return request.post('/auth/logout').finally(() => {
    localStorage.removeItem('token')
  })
}
