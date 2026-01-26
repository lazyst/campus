// frontend-user/src/api/hooks/useRequest.js

import { ref } from 'vue'

/**
 * 基础请求Hook
 * @param {Function} apiFunc - API函数
 * @param {Object} [options] - 配置选项
 * @param {boolean} [options.immediate=false] - 是否立即执行
 * @param {Function} [options.onSuccess] - 成功回调
 * @param {Function} [options.onError] - 失败回调
 * @returns {Object} { data, loading, error, execute, reset }
 *
 * @example
 * const { data, loading, execute } = useRequest(getPostById)
 * await execute(1)
 */
export function useRequest(apiFunc, options = {}) {
  const { immediate = false, onSuccess, onError } = options

  const data = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const execute = async (...args) => {
    loading.value = true
    error.value = null

    try {
      const result = await apiFunc(...args)
      data.value = result
      onSuccess?.(result)
      return result
    } catch (err) {
      error.value = err
      onError?.(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    data.value = null
    loading.value = false
    error.value = null
  }

  if (immediate) {
    execute()
  }

  return {
    data,
    loading,
    error,
    execute,
    reset
  }
}
