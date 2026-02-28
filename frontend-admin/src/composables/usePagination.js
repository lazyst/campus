/**
 * 分页 composable
 * 提取通用的分页状态和逻辑
 */

import { ref, computed } from 'vue'

export function usePagination(options = {}) {
  const {
    defaultPage = 1,
    defaultPageSize = 10,
    defaultPageSizes = [10, 20, 50, 100]
  } = options

  const currentPage = ref(defaultPage)
  const pageSize = ref(defaultPageSize)
  const pageSizes = ref(defaultPageSizes)
  const total = ref(0)
  const loading = ref(false)

  const pagination = computed(() => ({
    page: currentPage.value,
    size: pageSize.value
  }))

  const resetPagination = () => {
    currentPage.value = defaultPage
  }

  const handleCurrentChange = (page) => {
    currentPage.value = page
  }

  const handleSizeChange = (size) => {
    pageSize.value = size
    currentPage.value = 1 // 切换每页数量时重置到第一页
  }

  return {
    currentPage,
    pageSize,
    pageSizes,
    total,
    loading,
    pagination,
    resetPagination,
    handleCurrentChange,
    handleSizeChange
  }
}
