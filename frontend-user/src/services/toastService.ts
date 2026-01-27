/**
 * Toast 全局服务
 * 
 * 提供全局统一的 Toast 调用方式，支持：
 * - 快捷方法：success(), warning(), error(), info()
 * - 通用方法：show()
 * - 支持 Promise 风格的调用
 */

import { createApp, ref, nextTick } from 'vue'
import ToastComponent from '@/components/interactive/Toast.vue'
import type { ToastOptions, ToastType } from '@/components/interactive/types/toast'

interface ToastRef {
  close: () => void
}

const toastInstance = ref<ToastRef | null>(null)
let toastApp: ReturnType<typeof createApp> | null = null

function mountToastComponent() {
  if (toastApp) return

  const container = document.createElement('div')
  container.id = 'toast-container'
  document.body.appendChild(container)

  toastApp = createApp(ToastComponent)
  const toastVm = toastApp.mount(container)

  toastInstance.value = toastVm as ToastRef
}

function ensureMounted() {
  if (!toastInstance.value) {
    mountToastComponent()
  }
}

interface ToastAPI {
  show: (options: ToastOptions) => ToastRef
  success: (message: string, duration?: number) => ToastRef
  warning: (message: string, duration?: number) => ToastRef
  error: (message: string, duration?: number) => ToastRef
  info: (message: string, duration?: number) => ToastRef
}

function createToast(): ToastAPI {
  return {
    show: (options: ToastOptions) => {
      ensureMounted()
      if (!toastInstance.value) {
        throw new Error('Toast component not mounted')
      }

      const { message, type = 'info', duration = 2000, onClose } = options

      toastInstance.value.close()

      nextTick(() => {
        const props = {
          message,
          type,
          duration,
          onClose: () => {
            onClose?.()
          }
        }
        toastInstance.value?.close()
      })

      return {
        close: () => toastInstance.value?.close()
      }
    },

    success: (message: string, duration?: number) => {
      return createToast().show({ type: 'success', message, duration })
    },

    warning: (message: string, duration?: number) => {
      return createToast().show({ type: 'warning', message, duration })
    },

    error: (message: string, duration?: number) => {
      return createToast().show({ type: 'error', message, duration })
    },

    info: (message: string, duration?: number) => {
      return createToast().show({ type: 'info', message, duration })
    }
  }
}

export const toast = createToast()

export function showToast(options: ToastOptions): ToastRef
export function showToast(message: string, type?: ToastType, duration?: number): ToastRef

export function showToast(
  message: string | ToastOptions,
  type?: ToastType,
  duration?: number
): ToastRef {
  if (typeof message === 'string') {
    return toast.show({ type, message, duration } as ToastOptions)
  }
  return toast.show(message)
}

export function success(message: string, duration?: number): ToastRef {
  return toast.success(message, duration)
}

export function warning(message: string, duration?: number): ToastRef {
  return toast.warning(message, duration)
}

export function error(message: string, duration?: number): ToastRef {
  return toast.error(message, duration)
}

export function info(message: string, duration?: number): ToastRef {
  return toast.info(message, duration)
}

export default toast
