// frontend-user/src/api/__tests__/auth.test.js

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'
import { login, register, logout } from '../modules/auth'

// Mock axios
vi.mock('axios')

describe('Auth API Tests', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear()
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('login()', () => {
    it('should login successfully with valid credentials', async () => {
      const mockCredentials = {
        phone: '13800138000',
        password: '123456'
      }

      const mockResponse = {
        data: {
          code: 200,
          message: '登录成功',
          data: 'mock-jwt-token-123456'
        }
      }

      axios.create.mockReturnValue({
        post: vi.fn().mockResolvedValue(mockResponse)
      })

      const result = await login(mockCredentials)

      // Verify token was saved to localStorage
      expect(localStorage.getItem('token')).toBe('mock-jwt-token-123456')
      expect(result).toBe('mock-jwt-token-123456')
    })

    it('should handle login failure with wrong password', async () => {
      const mockCredentials = {
        phone: '13800138000',
        password: 'wrongpassword'
      }

      const mockError = new Error('手机号或密码错误')
      axios.create.mockReturnValue({
        post: vi.fn().mockRejectedValue(mockError)
      })

      await expect(login(mockCredentials)).rejects.toThrow('手机号或密码错误')
    })
  })

  describe('register()', () => {
    it('should register successfully with valid data', async () => {
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

      // Register uses raw axios, not the request instance
      const rawAxiosSpy = vi.spyOn(axios, 'create').mockReturnValue({
        post: vi.fn().mockResolvedValue(mockResponse),
        defaults: { baseURL: '/api', timeout: 10000 }
      })

      const result = await register(mockUserData)

      // Verify token was saved
      expect(localStorage.getItem('token')).toBe('mock-jwt-token-after-register')
      expect(result.user).toBeDefined()
      expect(result.user.nickname).toBe('测试用户')
      expect(result.token).toBe('mock-jwt-token-after-register')

      rawAxiosSpy.mockRestore()
    })

    it('should handle registration with duplicate phone number', async () => {
      const mockUserData = {
        phone: '13800138000',
        password: '123456',
        nickname: '测试用户'
      }

      const mockError = new Error('手机号已存在')
      vi.spyOn(axios, 'create').mockReturnValue({
        post: vi.fn().mockRejectedValue(mockError),
        defaults: { baseURL: '/api', timeout: 10000 }
      })

      await expect(register(mockUserData)).rejects.toThrow('手机号已存在')
    })
  })

  describe('logout()', () => {
    it('should logout successfully', async () => {
      // Set a token in localStorage
      localStorage.setItem('token', 'existing-token')

      const mockResponse = {
        data: {
          code: 200,
          message: '退出成功'
        }
      }

      // Mock the request instance
      const mockRequestInstance = {
        post: vi.fn().mockResolvedValue(mockResponse)
      }

      // Import request to get the instance
      const requestModule = await import('../request')
      requestModule.default = mockRequestInstance

      const result = await logout()

      expect(result).toBe('退出成功')
      // Note: Token clearing is handled by the component calling logout
      // The API itself just sends the logout request
    })

    it('should handle logout when not logged in', async () => {
      const mockError = new Error('未登录')
      const mockRequestInstance = {
        post: vi.fn().mockRejectedValue(mockError)
      }

      const requestModule = await import('../request')
      requestModule.default = mockRequestInstance

      await expect(logout()).rejects.toThrow('未登录')
    })
  })
})
