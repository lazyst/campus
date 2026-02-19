import { describe, it, expect, vi, beforeEach } from 'vitest'
import { toggleItemCollect, checkItemCollected, getCollectedItems } from '../modules/itemCollect'

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

describe('ItemCollect API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('toggleItemCollect()', () => {
    it('should collect an item', async () => {
      mockRequest.post.mockResolvedValue({
        collected: true,
        collectCount: 1
      })

      const result = await toggleItemCollect(1)

      expect(result.collected).toBe(true)
      expect(result.collectCount).toBe(1)
      expect(mockRequest.post).toHaveBeenCalledWith('/items/1/collect')
    })

    it('should uncollect an item (toggle)', async () => {
      mockRequest.post.mockResolvedValue({
        collected: false,
        collectCount: 0
      })

      const result = await toggleItemCollect(1)

      expect(result.collected).toBe(false)
      expect(result.collectCount).toBe(0)
    })
  })

  describe('checkItemCollected()', () => {
    it('should check if item is collected', async () => {
      mockRequest.get.mockResolvedValue({ collected: true })

      const result = await checkItemCollected(1)

      expect(result.collected).toBe(true)
      expect(mockRequest.get).toHaveBeenCalledWith('/items/1/collect/check', { showLoading: false })
    })
  })

  describe('getCollectedItems()', () => {
    it('should get collected items list', async () => {
      const mockItems = {
        records: [
          { id: 1, title: '收藏的物品1' },
          { id: 2, title: '收藏的物品2' }
        ],
        total: 2
      }

      mockRequest.get.mockResolvedValue(mockItems)

      const result = await getCollectedItems({ page: 1, size: 10 })

      expect(result.records).toHaveLength(2)
      expect(mockRequest.get).toHaveBeenCalledWith('/items/collected', { params: { page: 1, size: 10 } })
    })
  })
})
