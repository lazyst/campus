// frontend-user/src/api/modules/user.js

import request from '../request'

/**
 * 获取个人信息
 * @returns {Promise<Object>} 用户信息
 */
export function getUserInfo() {
  return request.get('/user/profile')
}

/**
 * 更新个人资料
 * @param {Object} data - 更新数据
 * @param {string} [data.nickname] - 昵称
 * @param {string} [data.bio] - 个人简介
 * @param {number} [data.gender] - 性别（0=未知，1=男，2=女）
 * @returns {Promise<Object>} 更新后的用户信息
 */
export function updateProfile(data) {
  return request.put('/user/profile', data)
}

/**
 * 获取用户公开信息
 * @param {number} userId - 用户ID
 * @returns {Promise<Object>} 用户公开信息
 */
export function getUserPublicInfo(userId) {
  return request.get(`/user/public/${userId}`)
}

/**
 * 上传头像
 * @param {File} file - 头像文件
 * @returns {Promise<string>} 头像URL
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post('/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    showLoading: true,
    loadingText: '上传中...'
  })
}
