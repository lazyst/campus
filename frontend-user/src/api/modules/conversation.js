// frontend-user/src/api/modules/conversation.js

import request from '../request'

/**
 * 获取会话列表
 * @returns {Promise<Array>} 会话列表
 */
export function getConversations() {
  return request.get('/conversations', { showLoading: false })
}

/**
 * 创建会话
 * @param {number} userId - 对方用户ID
 * @returns {Promise<Object>} 会话信息
 */
export function createConversation(userId) {
  return request.post('/conversations', { userId })
}

/**
 * 清除与指定用户的未读消息数
 * @param {number} userId - 对方用户ID
 * @returns {Promise<void>}
 */
export function clearUnreadCount(userId) {
  return request.post(`/conversations/${userId}/read`)
}

/**
 * 获取用户的总未读消息数
 * @returns {Promise<number>} 总未读消息数
 */
export function getTotalUnreadCount() {
  return request.get('/conversations/unread/count', { showLoading: false })
}
