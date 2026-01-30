/**
 * Toast 类型定义
 *
 * 根据 BUSINESS_LOGIC_DIAGRAMS.md 业务逻辑规范：
 * - success: 操作成功反馈
 * - warning: 警告提示（未登录、Token过期等）
 * - error: 错误提示（权限不足、操作失败等）
 * - info: 一般信息提示
 */

// Toast 类型
export const ToastType = {
  SUCCESS: 'success',
  WARNING: 'warning',
  ERROR: 'error',
  INFO: 'info'
}

// Toast 选项
export function createToastOptions(message, type = 'info', duration = 2000, closeable = false, onClose) {
  return {
    type,
    message,
    duration,
    closeable,
    onClose
  }
}

// Toast 队列项
export function createToastQueueItem(id, options, resolve) {
  return {
    id,
    options,
    resolve
  }
}
