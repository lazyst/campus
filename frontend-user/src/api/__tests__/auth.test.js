// frontend-user/src/api/__tests__/auth.test.js

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'

// Mock axios first, before importing auth module
vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      post: vi.fn(),
      get: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() }
      }
    }))
  }
}))

describe('Auth API Tests', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('login()', () => {
    it('should login successfully with valid credentials', async () => {
      const { login } = await import('../modules/auth')

      const mockCredentials = {
        phone: '13800138000',
        password: '123456'
      }

      // Create a fresh axios instance that returns the expected response
      const mockAxiosInstance = {
        post: vi.fn().mockResolvedValue({
          data: {
            code: 200,
            message: '登录成功',
            data: 'mock-jwt-token-123456'
          }
        }),
        interceptors: {
          request: { use: vi.fn() },
          response: { use: vi.fn() }
        }
      }

      axios.create.mockReturnValue(mockAxiosInstance)

      // Import request module to set up the mock instance
      const requestModule = await import('../request')
      requestModule.default = mockAxiosInstance

      const result = await login(mockCredentials)

      expect(localStorage.getItem('token')).toBe('mock-jwt-token-123456')
      expect(result).toBe('mock-jwt-token-123456')
    })

    it('should handle login failure with wrong password', async () => {
      const { login } = await import('../modules/auth')

      const mockCredentials = {
        phone: '13800138000',
        password: 'wrongpassword'
      }

      const mockError = new Error('手机号或密码错误')
      const mockAxiosInstance = {
        post: vi.fn().mockRejectedValue(mockError),
        interceptors: {
          request: { use: vi.fn() },
          response: { use: vi.fn() }
        }
      }

      axios.create.mockReturnValue(mockAxiosInstance)

      await expect(login(mockCredentials)).rejects.toThrow('手机号或密码错误')
    })
  })

  describe('register()', () => {
    it('should register successfully with valid data', async () => {
      const { register } = await import('../modules/auth')

      const mockUserData = {
        phone: '13800138000',
        password: '123456',
        nickname: '测试用户'
      }

      const mockResponse = {
        data: {
          code: 200,
          message: '注册成功',
          data: {
            user: {
              id: 1,
              phone: '13800138000',
              nickname: '测试用户'
            },
            token: 'mock-jwt-token-after-register'
          }
        }
      }

      // Register uses raw axios.create
      const rawAxiosInstance = {
        post: vi.fn().mockResolvedValue(mockResponse),
        defaults: { baseURL: '/api', timeout: 10000 }
      }

      axios.create.mockReturnValue(rawAxiosInstance)

      const result = await register(mockUserData)

      expect(localStorage.getItem('token')).toBe('mock-jwt-token-after-register')
      expect(result.user).toBeDefined()
      expect(result.user.nickname).toBe('测试用户')
      expect(result.token).toBe('mock-jwt-token-after-register')
    })

    it('should handle registration with duplicate phone number', async () => {
      const { register } = await import('../modules/auth')

      const mockUserData = {
        phone: '13800138000',
        password: '123456',
        nickname: '测试用户'
      }

      const mockError = new Error('手机号已存在')
      const rawAxiosInstance = {
        post: vi.fn().mockRejectedValue(mockError),
        defaults: { baseURL: '/api', timeout: 10000 }
      }

      axios.create.mockReturnValue(rawAxiosInstance)

      await expect(register(mockUserData)).rejects.toThrow('手机号已存在')
    })
  })

  describe('logout()', () => {
    it('should logout successfully', async () => {
      const { logout } = await import('../modules/auth')

      localStorage.setItem('token', 'existing-token')

      const mockResponse = {
        data: {
          code: 200,
          message: '退出成功'
        }
      }

      const mockAxiosInstance = {
        post: vi.fn().mockResolvedValue(mockResponse),
        interceptors: {
          request: { use: vi.fn() },
          response: { use: vi.fn() }
        }
      }

      axios.create.mockReturnValue(mockAxiosInstance)

      const requestModule = await import('../request')
      requestModule.default = mockAxiosInstance

      const result = await logout()

      expect(result).toBe('退出成功')
    })

    it('should handle logout when not logged in', async () => {
      const { logout } = await import('../modules/auth')

      const mockError = new Error('未登录')
      const mockAxiosInstance = {
        post: vi.fn().mockRejectedValue(mockError),
        interceptors: {
          request: { use: vi.fn() },
          response: { use: vi.fn() }
        }
      }

      axios.create.mockReturnValue(mockAxiosInstance)

      await expect(logout()).rejects.toThrow('未登录')
    })
  })
})
