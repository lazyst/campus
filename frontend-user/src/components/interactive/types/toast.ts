/**
 * Toast 类型定义
 * 
 * 根据 BUSINESS_LOGIC_DIAGRAMS.md 业务逻辑规范：
 * - success: 操作成功反馈
 * - warning: 警告提示（未登录、Token过期等）
 * - error: 错误提示（权限不足、操作失败等）
 * - info: 一般信息提示
 */

export type ToastType = 'success' | 'warning' | 'error' | 'info'

export interface ToastOptions {
  type?: ToastType
  message: string
  duration?: number
  closeable?: boolean
  onClose?: () => void
}

export interface ToastInstance {
  id: string
  type: ToastType
  message: string
  duration: number
  closeable: boolean
  onClose?: () => void
}

export interface ToastQueueItem {
  id: string
  options: ToastOptions
  resolve?: (value: boolean) => void
}
