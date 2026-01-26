// frontend-user/src/api/modules/item.js

import request from '../request'

/**
 * 获取物品列表（分页）
 * @param {Object} params - 查询参数
 * @param {number} [params.type] - 物品类型（1=求购，2=出售）
 * @param {number} [params.page=1]
 * @param {number} [params.size=20]
 * @returns {Promise<{records: Array, total: number}>}
 */
export function getItems(params = {}) {
  return request.get('/items', { params, showLoading: false })
}

/**
 * 获取物品详情
 * @param {number} id - 物品ID
 * @returns {Promise<Object>} 物品详情
 */
export function getItemById(id) {
  return request.get(`/items/${id}`)
}

/**
 * 获取我的物品
 * @param {Object} params
 * @param {number} [params.page=1]
 * @param {number} [params.size=20]
 * @returns {Promise<{records: Array, total: number}>}
 */
export function getMyItems(params = {}) {
  return request.get('/items/my', { params })
}

/**
 * 发布物品
 * @param {Object} data - 物品数据
 * @param {number} data.type - 类型（1=求购，2=出售）
 * @param {string} data.title - 标题
 * @param {string} data.description - 描述
 * @param {string} data.price - 价格
 * @returns {Promise<Object>} 创建的物品
 */
export function createItem(data) {
  return request.post('/items', data, {
    loadingText: '发布中...'
  })
}

/**
 * 更新物品
 * @param {number} id - 物品ID
 * @param {Object} data - 更新数据
 * @returns {Promise<Object>} 更新后的物品
 */
export function updateItem(id, data) {
  return request.put(`/items/${id}`, data, {
    loadingText: '更新中...'
  })
}

/**
 * 删除物品
 * @param {number} id - 物品ID
 * @returns {Promise<void>}
 */
export function deleteItem(id) {
  return request.delete(`/items/${id}`, {
    loadingText: '删除中...'
  })
}

/**
 * 联系卖家
 * @param {number} id - 物品ID
 * @returns {Promise<void>}
 */
export function contactSeller(id) {
  return request.post(`/items/${id}/contact`)
}

/**
 * 下架物品
 * @param {number} id - 物品ID
 * @returns {Promise<void>}
 */
export function offlineItem(id) {
  return request.put(`/items/${id}/offline`)
}

/**
 * 重新上架物品
 * @param {number} id - 物品ID
 * @returns {Promise<void>}
 */
export function onlineItem(id) {
  return request.put(`/items/${id}/online`)
}

/**
 * 标记物品为已完成
 * @param {number} id - 物品ID
 * @returns {Promise<void>}
 */
export function completeItem(id) {
  return request.put(`/items/${id}/complete`)
}
