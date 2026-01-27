/**
 * Dialog 类型定义
 * 
 * 根据 BUSINESS_LOGIC_DIAGRAMS.md 业务逻辑规范：
 * - 确认对话框：用于危险操作确认
 * - 提示对话框：用于登录引导等场景
 */

export type DialogTheme = 'default' | 'warning' | 'danger'

export interface DialogOptions {
  title?: string
  message: string
  theme?: DialogTheme
  confirmText?: string
  cancelText?: string
  onConfirm?: () => void | Promise<void>
  onCancel?: () => void
  closeOnClickOverlay?: boolean
}

export interface DialogInstance {
  id: string
  options: DialogOptions
  resolve: (value: boolean) => void
}

export interface DialogActionResult {
  confirmed: boolean
}
