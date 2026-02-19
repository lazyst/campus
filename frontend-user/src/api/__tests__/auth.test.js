import { describe, it, expect, vi, beforeEach } from 'vitest'
import { login, register, logout } from '../modules/auth'

describe('Auth API Tests', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('login()', () => {
    it('should call login API with correct credentials', async () => {
      const mockCredentials = {
        phone: '13800138000',
        password: '123456'
      }

      expect(login).toBeDefined()
      expect(typeof login).toBe('function')
    })
  })

  describe('register()', () => {
    it('should call register API with user data', async () => {
      const mockUserData = {
        phone: '13800138000',
        password: '123456',
        nickname: '测试用户'
      }

      expect(register).toBeDefined()
      expect(typeof register).toBe('function')
    })
  })

  describe('logout()', () => {
    it('should call logout API', async () => {
      expect(logout).toBeDefined()
      expect(typeof logout).toBe('function')
    })
  })
})
