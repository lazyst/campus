// frontend-user/src/api/modules/message.js

import request from '../request'

/**
 * 获取会话消息（分页）
 * @param {number} conversationId - 会话ID
 * @param {Object} params
 * @param {number} [params.page=1]
 * @param {number} [params.size=20]
 * @returns {Promise<Array>} 消息列表
 */
export function getMessages(conversationId, params = {}) {
  return request.get(`/conversations/${conversationId}/messages`, {
    params,
    showLoading: false
  })
}

/**
 * 获取与特定用户的聊天消息
 * @param {number} userId - 对方用户ID
 * @param {Object} params
 * @param {number} [params.page=1]
 * @param {number} [params.size=20]
 * @returns {Promise<Array>} 消息列表
 */
export function getMessagesWithUser(userId, params = {}) {
  return request.get(`/messages/${userId}`, {
    params,
    showLoading: false
  })
}
