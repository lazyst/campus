// frontend-user/src/api/modules/notification.js

import request from '../request'

/**
 * 获取通知列表
 * @param {Object} [params] - 查询参数
 * @param {number} [params.page=1]
 * @param {number} [params.size=20]
 * @returns {Promise<Array>} 通知列表
 */
export function getNotifications(params = {}) {
  return request.get('/notifications', { params, showLoading: false })
}

/**
 * 标记通知为已读
 * @param {number} id - 通知ID
 * @returns {Promise<void>}
 */
export function markAsRead(id) {
  return request.put(`/notifications/${id}/read`)
}

/**
 * 批量标记所有通知为已读
 * @returns {Promise<void>}
 */
export function markAllAsRead() {
  return request.put('/notifications/read/all')
}
