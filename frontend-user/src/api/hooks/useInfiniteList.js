// frontend-user/src/api/hooks/useInfiniteList.js

import { ref, computed } from 'vue'

/**
 * 无限滚动Hook
 * @param {Function} apiFunc - API函数
 * @param {Object} [options] - 配置选项
 * @param {number} [options.pageSize=20] - 每页大小
 * @param {boolean} [options.immediate=false] - 是否立即加载
 * @returns {Object} 无限滚动控制对象
 *
 * @example
 * const { data, loading, hasMore, loadMore } = useInfiniteList(getPosts)
 */
export function useInfiniteList(apiFunc, options = {}) {
  const { pageSize = 20, immediate = false } = options

  const page = ref(1)
  const total = ref(0)
  const records = ref([])
  const loading = ref(false)
  const error = ref(null)

  const data = computed(() => records.value)

  const hasMore = computed(() => {
    return records.value.length < total.value
  })

  const loadMore = async () => {
    if (loading.value) return
    if (records.value.length > 0 && !hasMore.value) return

    loading.value = true
    error.value = null

    try {
      const result = await apiFunc({
        page: page.value,
        size: pageSize
      })

      records.value.push(...(result.records || []))
      total.value = result.total || 0
      page.value++

      return result
    } catch (err) {
      error.value = err
      throw err
    } finally {
      loading.value = false
    }
  }

  const refresh = async () => {
    page.value = 1
    records.value = []
    total.value = 0
    return loadMore()
  }

  const reset = () => {
    page.value = 1
    records.value = []
    total.value = 0
    error.value = null
  }

  if (immediate) {
    loadMore()
  }

  return {
    data,
    loading,
    hasMore,
    error,
    loadMore,
    refresh,
    reset
  }
}
