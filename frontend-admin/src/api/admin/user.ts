import { get, put, del } from '../index'

// 用户信息
export interface User {
  id: number
  phone: string
  nickname: string
  gender: number
  bio: string
  avatar: string
  status: number
  createdAt: string
}

// 用户列表查询参数
export interface UserQueryParams {
  page?: number
  size?: number
  keyword?: string
  status?: number
}

// 用户分页结果
export interface UserPageResult {
  records: User[]
  total: number
  current: number
  size: number
}

// 获取用户列表
export function getUserList(params: UserQueryParams): Promise<UserPageResult> {
  return get('/admin/users', params)
}

// 获取用户详情
export function getUserDetail(userId: number): Promise<User> {
  return get(`/admin/users/${userId}`)
}

// 更新用户状态
export function updateUserStatus(userId: number, status: number): Promise<void> {
  return put(`/admin/users/${userId}/status`, { status })
}

// 封禁用户
export function banUser(userId: number): Promise<void> {
  return put(`/admin/users/${userId}/ban`)
}

// 解封用户
export function unbanUser(userId: number): Promise<void> {
  return put(`/admin/users/${userId}/unban`)
}

// 删除用户
export function deleteUser(userId: number): Promise<void> {
  return del(`/admin/users/${userId}`)
}

// 获取用户统计
export function getUserStats(): Promise<{ total: number; normal: number; banned: number }> {
  return get('/admin/users/stats')
}
