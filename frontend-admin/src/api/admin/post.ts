import { get, del } from '../index'

// 帖子信息
export interface Post {
  id: number
  userId: number
  boardId: number
  title: string
  content: string
  images: string[]
  viewCount: number
  likeCount: number
  commentCount: number
  collectCount: number
  status: number
  userNickname: string
  userAvatar: string
  createdAt: string
}

// 帖子列表查询参数
export interface PostQueryParams {
  page?: number
  size?: number
  boardId?: number
  userId?: number
  keyword?: string
  status?: number
}

// 帖子分页结果
export interface PostPageResult {
  records: Post[]
  total: number
  current: number
  size: number
}

// 获取帖子列表
export function getPostList(params: PostQueryParams): Promise<PostPageResult> {
  return get('/admin/posts', params)
}

// 获取帖子详情
export function getPostDetail(postId: number): Promise<Post> {
  return get(`/admin/posts/${postId}`)
}

// 删除帖子
export function deletePost(postId: number): Promise<void> {
  return del(`/admin/posts/${postId}`)
}

// 获取帖子统计
export function getPostStats(): Promise<{ total: number; today: number; thisWeek: number }> {
  return get('/admin/posts/stats')
}
