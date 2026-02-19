import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useRequest, usePagination, useInfiniteList } from '../hooks'

describe('API Hooks Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('useRequest()', () => {
    it('should execute request and return data', async () => {
      const mockApiFunc = vi.fn().mockResolvedValue({ id: 1, name: '测试数据' })

      const { data, loading, execute } = useRequest(mockApiFunc)

      expect(loading.value).toBe(false)

      const result = await execute()

      expect(result).toEqual({ id: 1, name: '测试数据' })
      expect(data.value).toEqual({ id: 1, name: '测试数据' })
      expect(loading.value).toBe(false)
    })

    it('should handle loading state correctly', async () => {
      let resolveApi
      const mockApiFunc = vi.fn().mockImplementation(() => new Promise(resolve => {
        resolveApi = resolve
      }))

      const { loading, execute } = useRequest(mockApiFunc)

      const promise = execute()

      expect(loading.value).toBe(true)

      resolveApi({ id: 1 })
      await promise

      expect(loading.value).toBe(false)
    })

    it('should handle errors correctly', async () => {
      const mockError = new Error('API错误')
      const mockApiFunc = vi.fn().mockRejectedValue(mockError)

      const onError = vi.fn()
      const { error, execute } = useRequest(mockApiFunc, { onError })

      await expect(execute()).rejects.toThrow('API错误')

      expect(error.value).toBe(mockError)
      expect(onError).toHaveBeenCalledWith(mockError)
    })

    it('should reset state correctly', async () => {
      const mockApiFunc = vi.fn().mockResolvedValue({ id: 1 })

      const { data, execute, reset } = useRequest(mockApiFunc)

      execute()
      data.value = { id: 1, name: '测试' }

      reset()

      expect(data.value).toBe(null)
    })
  })

  describe('usePagination()', () => {
    it('should load page correctly', async () => {
      const mockListApiFunc = vi.fn().mockResolvedValue({
        records: [
          { id: 1, name: 'Item 1' },
          { id: 2, name: 'Item 2' }
        ],
        total: 10
      })

      const { data, page, total, loadPage } = usePagination(mockListApiFunc)

      await loadPage(1)

      expect(page.value).toBe(1)
      expect(total.value).toBe(10)
      expect(data.value).toHaveLength(2)
    })

    it('should navigate to next page', async () => {
      const mockListApiFunc = vi.fn().mockResolvedValue({
        records: [{ id: 1 }, { id: 2 }],
        total: 30
      })

      const { page, nextPage, hasNext, loadPage } = usePagination(mockListApiFunc, { pageSize: 10 })

      await loadPage(1)
      expect(hasNext.value).toBe(true)

      await nextPage()

      expect(page.value).toBe(2)
    })

    it('should navigate to previous page', async () => {
      const mockListApiFunc = vi.fn().mockResolvedValue({
        records: [{ id: 1 }, { id: 2 }],
        total: 30
      })

      const { page, loadPage, prevPage, hasPrev } = usePagination(mockListApiFunc, { pageSize: 10 })

      await loadPage(2)

      expect(hasPrev.value).toBe(true)

      await prevPage()

      expect(page.value).toBe(1)
    })

    it('should refresh current page', async () => {
      const mockListApiFunc = vi.fn().mockResolvedValue({
        records: [{ id: 1 }],
        total: 10
      })

      const { data, refresh, loadPage } = usePagination(mockListApiFunc)

      await loadPage(1)
      await refresh()

      expect(data.value).toHaveLength(1)
    })
  })

  describe('useInfiniteList()', () => {
    it('should load more items and append to list', async () => {
      const mockListApiFunc = vi.fn().mockResolvedValue({
        records: [{ id: 1 }, { id: 2 }],
        total: 10
      })

      const { data, loadMore } = useInfiniteList(mockListApiFunc, { pageSize: 2 })

      await loadMore()

      expect(data.value).toHaveLength(2)

      await loadMore()

      expect(data.value).toHaveLength(4)
    })

    it('should detect hasMore correctly', async () => {
      const mockListApiFunc = vi.fn().mockResolvedValue({
        records: [{ id: 1 }, { id: 2 }],
        total: 10
      })

      const { hasMore, loadMore } = useInfiniteList(mockListApiFunc, { pageSize: 2 })

      await loadMore()

      expect(hasMore.value).toBe(true)
    })

    it('should stop loading when no more items', async () => {
      const mockListApiFunc = vi.fn().mockResolvedValue({
        records: [{ id: 1 }, { id: 2 }],
        total: 2
      })

      const { hasMore, loadMore } = useInfiniteList(mockListApiFunc, { pageSize: 2 })

      await loadMore()

      expect(hasMore.value).toBe(false)
    })

    it('should reset correctly', async () => {
      const mockListApiFunc = vi.fn().mockResolvedValue({
        records: [{ id: 1 }, { id: 2 }],
        total: 10
      })

      const { data, loadMore, reset } = useInfiniteList(mockListApiFunc, { pageSize: 2 })

      await loadMore()
      expect(data.value).toHaveLength(2)

      reset()

      expect(data.value).toHaveLength(0)
    })

    it('should refresh correctly', async () => {
      const mockListApiFunc = vi.fn()
        .mockResolvedValueOnce({
          records: [{ id: 1 }, { id: 2 }],
          total: 10
        })
        .mockResolvedValueOnce({
          records: [{ id: 3 }, { id: 4 }],
          total: 10
        })

      const { data, loadMore, refresh } = useInfiniteList(mockListApiFunc, { pageSize: 2 })

      await loadMore()
      expect(data.value).toHaveLength(2)

      await refresh()
      expect(data.value).toHaveLength(2)
      expect(data.value[0].id).toBe(3)
    })
  })
})
