import axios from 'axios'
import request from '.'

export interface LoginParams {
  phone: string
  password: string
}

export interface RegisterParams {
  phone: string
  password: string
  nickname: string
}

export interface UserInfo {
  id: number
  phone: string
  nickname: string
  avatar: string
  gender: number
  bio: string
}

export interface RegisterResponse {
  id: number
  phone: string
  nickname: string
  avatar: string
  gender: number
  bio: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface RegisterResult {
  user: RegisterResponse
  token: string
}

// 创建原始axios实例（无拦截器）用于register
const rawAxios = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export function login(data: LoginParams) {
  return request.post<any, string>('/auth/login', data)
}

export function register(data: RegisterParams) {
  // 使用原始axios获取完整响应（包含token）
  return rawAxios.post('/auth/register', data).then((response) => {
    return {
      user: response.data.data,
      token: response.data.token
    }
  })
}

export function logout() {
  return request.post('/auth/logout')
}

export function getUserInfo() {
  return request.get<any, UserInfo>('/user/profile')
}
