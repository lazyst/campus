/**
 * Dialog 类型定义
 *
 * 根据 BUSINESS_LOGIC_DIAGRAMS.md 业务逻辑规范：
 * - 确认对话框：用于危险操作确认
 * - 提示对话框：用于登录引导等场景
 */

// Dialog 主题
export const DialogTheme = {
  DEFAULT: 'default',
  WARNING: 'warning',
  DANGER: 'danger'
}

// Dialog 选项
export function createDialogOptions(message, title, theme, confirmText, cancelText, onConfirm, onCancel, closeOnClickOverlay) {
  return {
    title,
    message,
    theme,
    confirmText,
    cancelText,
    onConfirm,
    onCancel,
    closeOnClickOverlay
  }
}

// Dialog 结果
export function createDialogActionResult(confirmed) {
  return {
    confirmed
  }
}
