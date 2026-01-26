// frontend-user/src/api/modules/board.js

import request from '../request'

/**
 * 获取所有板块
 * @returns {Promise<Array>} 板块列表
 */
export function getBoards() {
  return request.get('/boards')
}

/**
 * 获取板块详情
 * @param {number} id - 板块ID
 * @returns {Promise<Object>} 板块详情
 */
export function getBoardById(id) {
  return request.get(`/boards/${id}`)
}
