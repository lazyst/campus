import { get, post } from '../index'

// 登录请求参数
export interface LoginParams {
  username: string
  password: string
}

// 登录响应
export interface LoginResult {
  token: string
  admin: {
    id: number
    username: string
    nickname: string
    avatar: string
    role: number
  }
}

// 管理员信息
export interface AdminInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  role: number
  status: number
  lastLoginTime: string
}

// 登录
export function login(data: LoginParams): Promise<LoginResult> {
  return post('/admin/auth/login', data)
}

// 获取当前管理员信息
export function getAdminInfo(): Promise<AdminInfo> {
  return get('/admin/auth/info')
}

// 退出登录
export function logout(): Promise<void> {
  return post('/admin/auth/logout')
}
