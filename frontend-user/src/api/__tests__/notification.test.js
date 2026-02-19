import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getNotifications, markAsRead, markAllAsRead } from '../modules/notification'

const { mockRequest } = vi.hoisted(() => ({
  mockRequest: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

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

      mockRequest.get.mockResolvedValue(mockNotifications)

      const result = await getNotifications({ page: 1, size: 10 })

      expect(result.records).toHaveLength(2)
      expect(mockRequest.get).toHaveBeenCalledWith('/notifications', { params: { page: 1, size: 10 }, showLoading: false })
    })
  })

  describe('markAsRead()', () => {
    it('should mark notification as read', async () => {
      mockRequest.put.mockResolvedValue({ message: '标记成功' })

      const result = await markAsRead(1)

      expect(result.message).toBe('标记成功')
      expect(mockRequest.put).toHaveBeenCalledWith('/notifications/1/read')
    })
  })

  describe('markAllAsRead()', () => {
    it('should mark all notifications as read', async () => {
      mockRequest.put.mockResolvedValue({ message: '全部标记成功' })

      const result = await markAllAsRead()

      expect(result.message).toBe('全部标记成功')
      expect(mockRequest.put).toHaveBeenCalledWith('/notifications/read/all')
    })
  })
})
