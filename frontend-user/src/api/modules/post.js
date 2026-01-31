// frontend-user/src/api/modules/post.js

import request from '../request'

/**
 * 获取帖子列表（分页）
 * @param {Object} params - 查询参数
 * @param {number} [params.boardId] - 板块ID
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.size=20] - 每页大小
 * @returns {Promise<{records: Array, total: number}>}
 */
export function getPosts(params = {}) {
  return request.get('/posts', { params, showLoading: false })
}

/**
 * 获取帖子详情
 * @param {number} id - 帖子ID
 * @returns {Promise<Object>} 帖子详情
 */
export function getPostById(id) {
  return request.get(`/posts/${id}`)
}

/**
 * 获取我的帖子
 * @param {Object} params
 * @param {number} [params.page=1]
 * @param {number} [params.size=20]
 * @returns {Promise<{records: Array, total: number}>}
 */
export function getMyPosts(params = {}) {
  return request.get('/posts/my', { params })
}

/**
 * 按用户ID获取帖子
 * @param {number} userId - 用户ID
 * @param {Object} params
 * @param {number} [params.page=1]
 * @param {number} [params.size=10]
 * @returns {Promise<{records: Array, total: number}>}
 */
export function getPostsByUserId(userId, params = {}) {
  return request.get(`/posts/user/${userId}`, { params, showLoading: false })
}

/**
 * 发布帖子
 * @param {Object} data - 帖子数据
 * @param {number} data.boardId - 板块ID
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 * @param {string} [data.images] - 图片JSON数组
 * @returns {Promise<Object>} 创建的帖子
 */
export function createPost(data) {
  return request.post('/posts', data, {
    loadingText: '发布中...'
  })
}

/**
 * 更新帖子
 * @param {number} id - 帖子ID
 * @param {Object} data - 更新数据
 * @returns {Promise<Object>} 更新后的帖子
 */
export function updatePost(id, data) {
  return request.put(`/posts/${id}`, data, {
    loadingText: '更新中...'
  })
}

/**
 * 删除帖子
 * @param {number} id - 帖子ID
 * @returns {Promise<void>}
 */
export function deletePost(id) {
  return request.delete(`/posts/${id}`, {
    loadingText: '删除中...'
  })
}

/**
 * 点赞/取消点赞帖子
 * @param {number} id - 帖子ID
 * @returns {Promise<Object>} { isLiked: boolean, likeCount: number }
 */
export function toggleLikePost(id) {
  return request.post(`/posts/${id}/like`, null, {
    showSuccess: false,
    showError: false
  })
}

/**
 * 检查是否已点赞
 * @param {number} id - 帖子ID
 * @returns {Promise<boolean>}
 */
export function checkPostLiked(id) {
  return request.get(`/posts/${id}/like/check`, { showLoading: false })
}

/**
 * 收藏/取消收藏帖子
 * @param {number} id - 帖子ID
 * @returns {Promise<boolean>} true=收藏成功, false=取消收藏成功
 */
export function toggleCollectPost(id) {
  return request.post(`/posts/${id}/collect`, null, {
    showSuccess: false,
    showError: false
  })
}

/**
 * 检查是否已收藏
 * @param {number} id - 帖子ID
 * @returns {Promise<boolean>}
 */
export function checkPostCollected(id) {
  return request.get(`/posts/${id}/collect/check`, { showLoading: false })
}

/**
 * 获取当前用户的收藏帖子列表
 * @returns {Promise<Array>}
 */
export function getMyCollections() {
  return request.get('/posts/collections', { showLoading: false })
}
