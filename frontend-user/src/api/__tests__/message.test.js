import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getMessages, getMessagesWithUser } from '../modules/message'

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

describe('Message API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getMessages()', () => {
    it('should get messages for a conversation', async () => {
      const mockMessages = {
        records: [
          { id: 1, conversationId: 1, senderId: 2, content: '你好', createdAt: '2026-01-27T10:00:00' },
          { id: 2, conversationId: 1, senderId: 1, content: '在吗', createdAt: '2026-01-27T10:01:00' }
        ],
        total: 2
      }

      mockRequest.get.mockResolvedValue(mockMessages)

      const result = await getMessages(1, { page: 1, size: 10 })

      expect(result.records).toHaveLength(2)
      expect(mockRequest.get).toHaveBeenCalledWith('/conversations/1/messages', { params: { page: 1, size: 10 }, showLoading: false })
    })
  })

  describe('getMessagesWithUser()', () => {
    it('should get messages with a specific user', async () => {
      const mockMessages = {
        records: [
          { id: 1, senderId: 2, content: '第一条消息', createdAt: '2026-01-27T10:00:00' },
          { id: 2, senderId: 1, content: '第二条消息', createdAt: '2026-01-27T10:01:00' }
        ],
        total: 2
      }

      mockRequest.get.mockResolvedValue(mockMessages)

      const result = await getMessagesWithUser(2, { page: 1, size: 10 })

      expect(result.records).toHaveLength(2)
      expect(mockRequest.get).toHaveBeenCalledWith('/messages/2', { params: { page: 1, size: 10 }, showLoading: false })
    })
  })
})
