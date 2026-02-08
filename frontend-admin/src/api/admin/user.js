import { get, put, del } from '../index'

// 获取用户列表
export function getUserList(params) {
  return get('/api/admin/users', params)
}

// 获取用户详情
export function getUserDetail(userId) {
  return get(`/api/admin/users/${userId}`)
}

// 更新用户状态
export function updateUserStatus(userId, status) {
  return put(`/api/admin/users/${userId}/status`, { status })
}

// 封禁用户
export function banUser(userId) {
  return put(`/api/admin/users/${userId}/ban`)
}

// 解封用户
export function unbanUser(userId) {
  return put(`/api/admin/users/${userId}/unban`)
}

// 删除用户
export function deleteUser(userId) {
  return del(`/api/admin/users/${userId}/delete`)
}

// 获取用户统计
export function getUserStats() {
  return get('/api/admin/users/stats')
}
