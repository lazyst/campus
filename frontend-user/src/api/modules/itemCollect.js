// frontend-user/src/api/modules/itemCollect.js

import request from '../request'

/**
 * 收藏/取消收藏物品
 * @param {number} id - 物品ID
 * @returns {Promise<Object>} { isCollected: boolean }
 */
export function toggleItemCollect(id) {
  return request.post(`/items/${id}/collect`)
}

/**
 * 检查是否已收藏
 * @param {number} id - 物品ID
 * @returns {Promise<boolean>}
 */
export function checkItemCollected(id) {
  return request.get(`/items/${id}/collect/check`, { showLoading: false })
}

/**
 * 获取收藏列表
 * @param {Object} params
 * @param {number} [params.page=1]
 * @param {number} [params.size=20]
 * @returns {Promise<{records: Array, total: number}>}
 */
export function getCollectedItems(params = {}) {
  return request.get('/items/collected', { params })
}
