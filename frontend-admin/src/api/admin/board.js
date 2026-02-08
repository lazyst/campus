import { get, post, put, del } from '../index'

// 获取板块列表
export function getBoardList(params) {
  return get('/api/admin/boards', params)
}

// 获取板块详情
export function getBoardDetail(boardId) {
  return get(`/api/admin/boards/${boardId}`)
}

// 创建板块
export function createBoard(data) {
  return post('/api/admin/boards', data)
}

// 更新板块
export function updateBoard(boardId, data) {
  return put(`/api/admin/boards/${boardId}`, data)
}

// 删除板块
export function deleteBoard(boardId) {
  return del(`/api/admin/boards/${boardId}`)
}

// 获取板块统计
export function getBoardStats() {
  return get('/api/admin/boards/stats')
}
