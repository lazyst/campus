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
