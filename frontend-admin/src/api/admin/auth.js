import { get, post } from '../index'

// 登录
export function login(data) {
  return post('/admin/auth/login', data)
}

// 获取当前管理员信息
export function getAdminInfo() {
  return get('/admin/auth/info')
}

// 退出登录
export function logout() {
  return post('/admin/auth/logout')
}
