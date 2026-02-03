// frontend-user/src/api/toast.js

/**
 * Toast提示工具
 * 使用项目的自定义 Toast 组件 (components/interactive/Toast.vue)
 */

import { showToast as showToastService } from '@/services/toastService'

let loadingInstance = null

/**
 * 显示Toast提示
 * @param {string} message - 提示内容
 * @param {string} type - 类型: success | error | warning | info
 * @param {number} duration - 持续时间（毫秒）
 */
export function showToast(message, type = 'info', duration = 2000) {
  if (typeof message === 'object' && message !== null) {
    // 兼容对象参数: { message, type, duration }
    showToastService(message)
  } else {
    // 兼容字符串参数: (message, type, duration)
    showToastService({ message, type, duration })
  }
}

/**
 * 显示Loading (控制台输出，简化处理)
 * @param {string} text - 提示文字
 */
export function showLoading(text = '加载中...') {
  if (loadingInstance) return
  console.log('[LOADING]', text)
}

/**
 * 隐藏Loading
 */
export function hideLoading() {
  if (loadingInstance) {
    loadingInstance.close()
    loadingInstance = null
  }
}
