import { get, del } from '../index'

// 获取帖子列表
export function getPostList(params) {
  return get('/admin/posts', params)
}

// 获取帖子详情
export function getPostDetail(postId) {
  return get(`/admin/posts/${postId}`)
}

// 删除帖子
export function deletePost(postId) {
  return del(`/admin/posts/${postId}`)
}

// 获取帖子统计
export function getPostStats() {
  return get('/admin/posts/stats')
}

// 获取帖子的评论列表
export function getPostComments(postId) {
  return get(`/admin/posts/${postId}/comments`)
}

// 删除评论
export function deleteComment(commentId) {
  return del(`/admin/posts/comments/${commentId}`)
}
