// frontend-user/src/api/__tests__/item.test.js

import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getItems,
  getItemById,
  getMyItems,
  createItem,
  updateItem,
  deleteItem,
  contactSeller,
  offlineItem,
  onlineItem,
  completeItem
} from '../modules/item'

// Mock the request instance
const mockRequest = vi.fn()
vi.mock('../request', () => ({
  default: mockRequest
}))

describe('Item API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getItems()', () => {
    it('should get items list with filters', async () => {
      const mockItems = {
        records: [
          { id: 1, title: '物品1', type: 2, price: 50.00 },
          { id: 2, title: '物品2', type: 1, price: 100.00 }
        ],
        total: 2
      }

      mockRequest.mockResolvedValue(mockItems)

      const result = await getItems({ type: 2, page: 1, size: 10 })

      expect(result.records).toHaveLength(2)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/items',
        method: 'get',
        params: { type: 2, page: 1, size: 10 }
      })
    })
  })

  describe('getItemById()', () => {
    it('should get item detail by id', async () => {
      const mockItem = {
        id: 1,
        title: '二手书',
        type: 2,
        price: 50.00,
        description: '九成新',
        viewCount: 100,
        contactCount: 5
      }

      mockRequest.mockResolvedValue(mockItem)

      const result = await getItemById(1)

      expect(result.id).toBe(1)
      expect(result.title).toBe('二手书')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/items/1',
        method: 'get'
      })
    })
  })

  describe('getMyItems()', () => {
    it('should get current user items', async () => {
      const mockItems = {
        records: [
          { id: 1, title: '我的物品1' },
          { id: 2, title: '我的物品2' }
        ],
        total: 2
      }

      mockRequest.mockResolvedValue(mockItems)

      const result = await getMyItems({ page: 1, size: 10 })

      expect(result.records).toHaveLength(2)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/user/items',
        method: 'get',
        params: { page: 1, size: 10 }
      })
    })
  })

  describe('createItem()', () => {
    it('should create a new item successfully', async () => {
      const newItemData = {
        type: 2,
        title: '出售自行车',
        price: 200.00,
        description: '八成新'
      }

      const createdItem = {
        id: 1,
        ...newItemData,
        status: 1,
        createdAt: '2026-01-27T10:00:00'
      }

      mockRequest.mockResolvedValue(createdItem)

      const result = await createItem(newItemData)

      expect(result.id).toBe(1)
      expect(result.title).toBe('出售自行车')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/items',
        method: 'post',
        data: newItemData
      })
    })
  })

  describe('updateItem()', () => {
    it('should update item successfully', async () => {
      const updateData = {
        title: '更新后的标题',
        price: 150.00
      }

      const updatedItem = {
        id: 1,
        ...updateData
      }

      mockRequest.mockResolvedValue(updatedItem)

      const result = await updateItem(1, updateData)

      expect(result.title).toBe('更新后的标题')
      expect(result.price).toBe(150.00)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/items/1',
        method: 'put',
        data: updateData
      })
    })
  })

  describe('deleteItem()', () => {
    it('should delete item successfully', async () => {
      mockRequest.mockResolvedValue({ message: '删除成功' })

      const result = await deleteItem(1)

      expect(result.message).toBe('删除成功')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/items/1',
        method: 'delete'
      })
    })
  })

  describe('contactSeller()', () => {
    it('should contact seller successfully', async () => {
      mockRequest.mockResolvedValue({
        message: '已联系卖家',
        contactCount: 6
      })

      const result = await contactSeller(1)

      expect(result.contactCount).toBe(6)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/items/1/contact',
        method: 'post'
      })
    })
  })

  describe('offlineItem()', () => {
    it('should take item offline successfully', async () => {
      mockRequest.mockResolvedValue({
        id: 1,
        status: 3
      })

      const result = await offlineItem(1)

      expect(result.status).toBe(3)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/items/1/offline',
        method: 'put'
      })
    })
  })

  describe('onlineItem()', () => {
    it('should put item online successfully', async () => {
      mockRequest.mockResolvedValue({
        id: 1,
        status: 1
      })

      const result = await onlineItem(1)

      expect(result.status).toBe(1)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/items/1/online',
        method: 'put'
      })
    })
  })

  describe('completeItem()', () => {
    it('should mark item as completed successfully', async () => {
      mockRequest.mockResolvedValue({
        id: 1,
        status: 2
      })

      const result = await completeItem(1)

      expect(result.status).toBe(2)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/items/1/complete',
        method: 'put'
      })
    })
  })
})
