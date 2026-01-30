import { get, put, del } from '../index'

// 获取用户列表
export function getUserList(params) {
  return get('/admin/users', params)
}

// 获取用户详情
export function getUserDetail(userId) {
  return get(`/admin/users/${userId}`)
}

// 更新用户状态
export function updateUserStatus(userId, status) {
  return put(`/admin/users/${userId}/status`, { status })
}

// 封禁用户
export function banUser(userId) {
  return put(`/admin/users/${userId}/ban`)
}

// 解封用户
export function unbanUser(userId) {
  return put(`/admin/users/${userId}/unban`)
}

// 删除用户
export function deleteUser(userId) {
  return del(`/admin/users/${userId}`)
}

// 获取用户统计
export function getUserStats() {
  return get('/admin/users/stats')
}
