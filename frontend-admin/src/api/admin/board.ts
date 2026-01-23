import { get, post, put, del } from '../index'

// 板块信息
export interface Board {
  id: number
  name: string
  description: string
  icon: string
  sort: number
  status: number
  createdAt: string
}

// 板块列表查询参数
export interface BoardQueryParams {
  page?: number
  size?: number
}

// 板块分页结果
export interface BoardPageResult {
  records: Board[]
  total: number
  current: number
  size: number
}

// 创建板块请求
export interface CreateBoardRequest {
  name: string
  description: string
  icon: string
}

// 更新板块请求
export interface UpdateBoardRequest {
  name?: string
  description?: string
  icon?: string
  sort?: number
}

// 获取板块列表
export function getBoardList(params: BoardQueryParams): Promise<BoardPageResult> {
  return get('/admin/boards', params)
}

// 获取板块详情
export function getBoardDetail(boardId: number): Promise<Board> {
  return get(`/admin/boards/${boardId}`)
}

// 创建板块
export function createBoard(data: CreateBoardRequest): Promise<Board> {
  return post('/admin/boards', data)
}

// 更新板块
export function updateBoard(boardId: number, data: UpdateBoardRequest): Promise<Board> {
  return put(`/admin/boards/${boardId}`, data)
}

// 删除板块
export function deleteBoard(boardId: number): Promise<void> {
  return del(`/admin/boards/${boardId}`)
}

// 获取板块统计
export function getBoardStats(): Promise<{ total: number; active: number }> {
  return get('/admin/boards/stats')
}
