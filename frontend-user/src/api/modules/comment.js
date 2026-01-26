// frontend-user/src/api/modules/comment.js

import request from '../request'

/**
 * 获取帖子的评论列表
 * @param {number} postId - 帖子ID
 * @returns {Promise<Array>} 评论列表
 */
export function getCommentsByPost(postId) {
  return request.get(`/comments/post/${postId}`, { showLoading: false })
}

/**
 * 发表评论
 * @param {Object} data - 评论数据
 * @param {number} data.postId - 帖子ID
 * @param {string} data.content - 评论内容
 * @returns {Promise<Object>} 创建的评论
 */
export function createComment(data) {
  return request.post('/comments', data, {
    loadingText: '发表中...'
  })
}

/**
 * 删除评论
 * @param {number} id - 评论ID
 * @returns {Promise<void>}
 */
export function deleteComment(id) {
  return request.delete(`/comments/${id}`, {
    loadingText: '删除中...'
  })
}
