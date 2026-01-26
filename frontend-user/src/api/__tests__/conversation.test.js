// frontend-user/src/api/__tests__/conversation.test.js

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getConversations, createConversation } from '../modules/conversation'

// Mock the request instance
const mockRequest = vi.fn()
vi.mock('../request', () => ({
  default: mockRequest
}))

describe('Conversation API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getConversations()', () => {
    it('should get conversations list', async () => {
      const mockConversations = [
        { id: 1, targetId: 2, targetNickname: '用户B', lastMessage: '你好', updatedAt: '2026-01-27' },
        { id: 2, targetId: 3, targetNickname: '用户C', lastMessage: '在吗', updatedAt: '2026-01-26' }
      ]

      mockRequest.mockResolvedValue(mockConversations)

      const result = await getConversations()

      expect(result).toHaveLength(2)
      expect(result[0].targetNickname).toBe('用户B')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/conversations',
        method: 'get'
      })
    })
  })

  describe('createConversation()', () => {
    it('should create a new conversation', async () => {
      const newConversation = {
        id: 1,
        targetId: 2,
        createdAt: '2026-01-27T10:00:00'
      }

      mockRequest.mockResolvedValue(newConversation)

      const result = await createConversation(2)

      expect(result.targetId).toBe(2)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/conversations',
        method: 'post',
        data: { targetId: 2 }
      })
    })
  })
})
