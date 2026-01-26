// frontend-user/src/api/hooks/usePagination.js

import { ref, computed } from 'vue'

/**
 * 分页请求Hook
 * @param {Function} apiFunc - API函数
 * @param {Object} [options] - 配置选项
 * @param {number} [options.pageSize=20] - 每页大小
 * @param {boolean} [options.immediate=false] - 是否立即加载
 * @returns {Object} 分页控制对象
 *
 * @example
 * const { data, loading, page, nextPage, prevPage } = usePagination(getPosts)
 */
export function usePagination(apiFunc, options = {}) {
  const { pageSize = 20, immediate = false } = options

  const page = ref(1)
  const total = ref(0)
  const records = ref([])
  const loading = ref(false)

  const data = computed(() => records.value)

  const hasNext = computed(() => {
    return page.value * pageSize < total.value
  })

  const hasPrev = computed(() => {
    return page.value > 1
  })

  const loadPage = async (pageNum) => {
    loading.value = true
    try {
      const result = await apiFunc({
        page: pageNum,
        size: pageSize
      })

      page.value = pageNum
      records.value = result.records || []
      total.value = result.total || 0

      return result
    } finally {
      loading.value = false
    }
  }

  const nextPage = () => {
    if (hasNext.value) {
      loadPage(page.value + 1)
    }
  }

  const prevPage = () => {
    if (hasPrev.value) {
      loadPage(page.value - 1)
    }
  }

  const refresh = () => {
    loadPage(page.value)
  }

  if (immediate) {
    loadPage(1)
  }

  return {
    data,
    loading,
    page,
    total,
    pageSize,
    hasNext,
    hasPrev,
    loadPage,
    nextPage,
    prevPage,
    refresh
  }
}
