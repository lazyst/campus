// frontend-user/src/api/toast.js

/**
 * Toast提示工具
 * 使用Element Plus的ElMessage组件
 */

let loadingInstance = null

/**
 * 显示Toast提示
 * @param {string} message - 提示内容
 * @param {string} type - 类型: success | error | warning | info
 * @param {number} duration - 持续时间（毫秒）
 */
export function showToast(message, type = 'info', duration = 2000) {
  // 使用Element Plus的Message组件
  // 如果项目中没有Element Plus，可以替换为其他Toast库
  if (window.ElmMessage) {
    window.ElmMessage({
      message,
      type,
      duration
    })
  } else {
    // 降级方案：使用console
    console.log(`[${type.toUpperCase()}] ${message}`)
    // TODO: 集成项目使用的Toast组件
  }
}

/**
 * 显示Loading
 * @param {string} text - 提示文字
 */
export function showLoading(text = '加载中...') {
  if (loadingInstance) return

  if (window.ElLoading) {
    loadingInstance = window.ElLoading.service({
      fullscreen: true,
      text,
      background: 'rgba(0, 0, 0, 0.7)'
    })
  } else {
    console.log('[LOADING]', text)
  }
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
