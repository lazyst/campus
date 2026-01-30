/**
 * Toast 全局服务
 *
 * 提供全局统一的 Toast 调用方式，支持：
 * - 快捷方法：success(), warning(), error(), info()
 * - 通用方法：show()
 * - 支持 Promise 风格的调用
 */

import { createApp, ref, nextTick, markRaw } from 'vue'
import ToastComponent from '@/components/interactive/Toast.vue'

let toastApp = null
let toastContainer = null
const toastRef = ref(null)

function ensureToastMounted() {
  if (toastContainer) return

  toastContainer = document.createElement('div')
  toastContainer.id = 'toast-container'
  document.body.appendChild(toastContainer)

  toastApp = createApp(ToastComponent, {
    message: '',
    type: 'info',
    duration: 2000
  })
  const vm = toastApp.mount(toastContainer)
  toastRef.value = markRaw(vm)
}

export function showToast(message, type, duration) {
  ensureToastMounted()

  let toastMessage
  let toastType = 'info'
  let toastDuration = 2000
  let onClose

  if (typeof message === 'string') {
    toastMessage = message
    toastType = type || 'info'
    toastDuration = duration || 2000
  } else {
    toastMessage = message.message
    toastType = message.type || 'info'
    toastDuration = message.duration || 2000
    onClose = message.onClose
  }

  // 重新挂载组件以触发更新
  if (toastApp && toastContainer) {
    toastApp.unmount()
    toastApp = createApp(ToastComponent, {
      message: toastMessage,
      type: toastType,
      duration: toastDuration,
      onClose
    })
    const vm = toastApp.mount(toastContainer)
    toastRef.value = markRaw(vm)
  }

  return {
    close: () => {
      if (toastRef.value && typeof toastRef.value.close === 'function') {
        toastRef.value.close()
      }
    }
  }
}

export function success(message, duration) {
  return showToast({ type: 'success', message, duration })
}

export function warning(message, duration) {
  return showToast({ type: 'warning', message, duration })
}

export function error(message, duration) {
  return showToast({ type: 'error', message, duration })
}

export function info(message, duration) {
  return showToast({ type: 'info', message, duration })
}

export default {
  show: showToast,
  success,
  warning,
  error,
  info
}
