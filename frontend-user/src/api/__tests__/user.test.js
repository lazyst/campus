// frontend-user/src/api/__tests__/user.test.js

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getUserInfo, updateProfile, getUserPublicInfo, uploadAvatar } from '../modules/user'

// Mock the request instance
const { mockRequest } = vi.hoisted(() => ({
  mockRequest: vi.fn()
}))

vi.mock('../request', () => ({
  default: mockRequest
}))

describe('User API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getUserInfo()', () => {
    it('should get current user info successfully', async () => {
      const mockUserInfo = {
        id: 1,
        phone: '13800138000',
        nickname: '测试用户',
        avatar: 'http://example.com/avatar.jpg',
        gender: 1,
        bio: '这是个人简介'
      }

      mockRequest.mockResolvedValue({
        id: 1,
        phone: '13800138000',
        nickname: '测试用户',
        avatar: 'http://example.com/avatar.jpg',
        gender: 1,
        bio: '这是个人简介'
      })

      const result = await getUserInfo()

      expect(result).toEqual(mockUserInfo)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/user/profile',
        method: 'get'
      })
    })
  })

  describe('updateProfile()', () => {
    it('should update user profile successfully', async () => {
      const updateData = {
        nickname: '新昵称',
        bio: '新的个人简介',
        gender: 2
      }

      const updatedUserInfo = {
        id: 1,
        phone: '13800138000',
        nickname: '新昵称',
        avatar: 'http://example.com/avatar.jpg',
        gender: 2,
        bio: '新的个人简介'
      }

      mockRequest.mockResolvedValue(updatedUserInfo)

      const result = await updateProfile(updateData)

      expect(result.nickname).toBe('新昵称')
      expect(result.bio).toBe('新的个人简介')
      expect(result.gender).toBe(2)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/user/profile',
        method: 'put',
        data: updateData
      })
    })

    it('should update partial profile fields', async () => {
      const updateData = {
        nickname: '只修改昵称'
      }

      mockRequest.mockResolvedValue({
        id: 1,
        nickname: '只修改昵称'
      })

      const result = await updateProfile(updateData)

      expect(result.nickname).toBe('只修改昵称')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/user/profile',
        method: 'put',
        data: updateData
      })
    })
  })

  describe('getUserPublicInfo()', () => {
    it('should get public info of another user', async () => {
      const userId = 2
      const mockPublicInfo = {
        id: 2,
        nickname: '其他用户',
        avatar: 'http://example.com/user2.jpg',
        gender: 1,
        bio: '这是公开信息'
      }

      mockRequest.mockResolvedValue(mockPublicInfo)

      const result = await getUserPublicInfo(userId)

      expect(result.id).toBe(2)
      expect(result.nickname).toBe('其他用户')
      expect(mockRequest).toHaveBeenCalledWith({
        url: `/user/public/${userId}`,
        method: 'get'
      })
    })
  })

  describe('uploadAvatar()', () => {
    it('should upload avatar successfully', async () => {
      // Create a mock file
      const mockFile = new File(['avatar content'], 'avatar.jpg', { type: 'image/jpeg' })

      const mockAvatarUrl = 'http://example.com/new-avatar.jpg'

      mockRequest.mockResolvedValue({ url: mockAvatarUrl })

      const result = await uploadAvatar(mockFile)

      expect(result.url).toBe(mockAvatarUrl)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/user/avatar',
        method: 'post',
        data: expect.any(FormData),
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
    })

    it('should handle upload failure', async () => {
      const mockFile = new File(['avatar content'], 'avatar.jpg', { type: 'image/jpeg' })

      const mockError = new Error('文件格式不支持')
      mockRequest.mockRejectedValue(mockError)

      await expect(uploadAvatar(mockFile)).rejects.toThrow('文件格式不支持')
    })
  })
})
