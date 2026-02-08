import { get, post } from '../index'

// 登录 - 后端路径是 /api/admin/auth/login
export function login(data) {
  return post('/api/admin/auth/login', data)
}

// 获取当前管理员信息
export function getAdminInfo() {
  return get('/api/admin/auth/info')
}

// 退出登录
export function logout() {
  return post('/api/admin/auth/logout')
}
