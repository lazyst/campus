// frontend-user/src/api/__tests__/notification.test.js

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getNotifications, markAsRead, markAllAsRead } from '../modules/notification'

// Mock the request instance
const mockRequest = vi.fn()
vi.mock('../request', () => ({
  default: mockRequest
}))

describe('Notification API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getNotifications()', () => {
    it('should get notifications list', async () => {
      const mockNotifications = {
        records: [
          { id: 1, type: 'like', content: '用户A赞了你的帖子', isRead: false },
          { id: 2, type: 'comment', content: '用户B评论了你的帖子', isRead: false }
        ],
        total: 2
      }

      mockRequest.mockResolvedValue(mockNotifications)

      const result = await getNotifications({ page: 1, size: 10 })

      expect(result.records).toHaveLength(2)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/notifications',
        method: 'get',
        params: { page: 1, size: 10 }
      })
    })
  })

  describe('markAsRead()', () => {
    it('should mark notification as read', async () => {
      mockRequest.mockResolvedValue({ message: '标记成功' })

      const result = await markAsRead(1)

      expect(result.message).toBe('标记成功')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/notifications/1/read',
        method: 'put'
      })
    })
  })

  describe('markAllAsRead()', () => {
    it('should mark all notifications as read', async () => {
      mockRequest.mockResolvedValue({ message: '全部标记成功' })

      const result = await markAllAsRead()

      expect(result.message).toBe('全部标记成功')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/notifications/read-all',
        method: 'put'
      })
    })
  })
})
