import request from '.'

// 板块接口
export interface Board {
  id: number
  name: string
  description: string
}

// 帖子接口
export interface Post {
  id: number
  boardId: number
  title: string
  content: string
  userNickname: string
  likeCount: number
  commentCount: number
  createdAt: string
}

// 创建帖子参数
export interface CreatePostParams {
  boardId: number
  title: string
  content: string
}

// 获取板块列表
export function getBoards() {
  return request.get<any, Board[]>('/boards')
}

// 获取帖子列表
export function getPosts(params?: { boardId?: number; page?: number; size?: number }) {
  return request.get<any, { records: Post[], total: number }>('/posts', { params })
}

// 创建帖子
export function createPost(data: CreatePostParams) {
  return request.post<any, Post>('/posts', data)
}
