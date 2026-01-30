import { get, post, put, del } from '../index'

// 获取板块列表
export function getBoardList(params) {
  return get('/admin/boards', params)
}

// 获取板块详情
export function getBoardDetail(boardId) {
  return get(`/admin/boards/${boardId}`)
}

// 创建板块
export function createBoard(data) {
  return post('/admin/boards', data)
}

// 更新板块
export function updateBoard(boardId, data) {
  return put(`/admin/boards/${boardId}`, data)
}

// 删除板块
export function deleteBoard(boardId) {
  return del(`/admin/boards/${boardId}`)
}

// 获取板块统计
export function getBoardStats() {
  return get('/admin/boards/stats')
}
