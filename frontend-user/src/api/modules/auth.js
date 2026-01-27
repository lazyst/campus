// frontend-user/src/api/modules/auth.js

import request from '../request'
import axios from 'axios'

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.phone - 手机号
 * @param {string} data.password - 密码
 * @returns {Promise<string>} JWT token
 */
export function login(data) {
  return request.post('/auth/login', data, {
    showSuccess: false,  // 由组件手动控制提示
    showError: true
  })
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
  return request.post('/auth/register', data, {
    showSuccess: false,  // 我们手动控制提示
    showError: true
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
