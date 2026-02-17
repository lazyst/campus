// frontend-user/src/api/__tests__/board.test.js

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getBoards, getBoardById } from '../modules/board'

// Use vi.hoisted to get reference to mock functions before they're hoisted
const { mockRequest } = vi.hoisted(() => ({
  mockRequest: vi.fn()
}))

vi.mock('../request', () => ({
  default: mockRequest
}))

describe('Board API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getBoards()', () => {
    it('should get all boards successfully', async () => {
      const mockBoards = [
        { id: 1, name: '交流', description: '学习交流区' },
        { id: 2, name: '生活', description: '生活分享区' },
        { id: 3, name: '交易', description: '二手交易区' }
      ]

      mockRequest.mockResolvedValue(mockBoards)

      const result = await getBoards()

      expect(result).toHaveLength(3)
      expect(result[0].name).toBe('交流')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/boards',
        method: 'get'
      })
    })
  })

  describe('getBoardById()', () => {
    it('should get board detail by id', async () => {
      const mockBoard = {
        id: 1,
        name: '交流',
        description: '学习交流区',
        postCount: 100
      }

      mockRequest.mockResolvedValue(mockBoard)

      const result = await getBoardById(1)

      expect(result.id).toBe(1)
      expect(result.name).toBe('交流')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/boards/1',
        method: 'get'
      })
    })
  })
})
