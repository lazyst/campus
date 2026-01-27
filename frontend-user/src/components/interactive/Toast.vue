<template>
  <Teleport to="body">
    <Transition name="toast-fade">
      <div
        v-if="visible"
        :class="[
          'toast',
          `toast--${type}`
        ]"
        :style="toastStyle"
      >
        <div class="toast__content">
          <div class="toast__icon">
            {{ iconText }}
          </div>
          <span class="toast__message">{{ message }}</span>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import type { ToastType } from './types/toast'

interface Props {
  type?: ToastType
  message: string
  duration?: number
  onClose?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  duration: 2000
})

const visible = ref(false)
let timer: ReturnType<typeof setTimeout> | null = null

const iconMap: Record<ToastType, string> = {
  success: '成',
  warning: '警',
  error: '错',
  info: '信'
}

const iconText = computed(() => iconMap[props.type] || '信')

const toastStyle = computed(() => ({
  '--toast-bg-color': getBgColor(props.type),
  '--toast-text-color': getTextColor(props.type),
  '--toast-icon-bg': getIconBg(props.type)
}))

function getBgColor(type: ToastType): string {
  const colors: Record<ToastType, string> = {
    success: 'var(--color-success-100)',
    warning: 'var(--color-warning-100)',
    error: 'var(--color-error-100)',
    info: 'var(--color-info-100)'
  }
  return colors[type] || 'var(--bg-page)'
}

function getTextColor(type: ToastType): string {
  const colors: Record<ToastType, string> = {
    success: 'var(--color-success-700)',
    warning: 'var(--color-warning-700)',
    error: 'var(--color-error-700)',
    info: 'var(--color-info-700)'
  }
  return colors[type] || 'var(--text-primary)'
}

function getIconBg(type: ToastType): string {
  const colors: Record<ToastType, string> = {
    success: 'var(--color-success-500)',
    warning: 'var(--color-warning-500)',
    error: 'var(--color-error-500)',
    info: 'var(--color-info-500)'
  }
  return colors[type] || 'var(--color-primary)'
}

function startTimer() {
  if (props.duration > 0) {
    timer = setTimeout(() => {
      close()
    }, props.duration)
  }
}

function close() {
  visible.value = false
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
  props.onClose?.()
}

onMounted(() => {
  visible.value = true
  startTimer()
})

onUnmounted(() => {
  if (timer) {
    clearTimeout(timer)
  }
})

defineExpose({
  close
})
</script>

<style scoped>
.toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: var(--toast-bg-color);
  color: var(--toast-text-color);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  z-index: var(--z-toast);
  max-width: 280px;
  min-width: 200px;
}

.toast__content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
}

.toast__icon {
  width: 24px;
  height: 24px;
  background-color: var(--toast-icon-bg);
  color: white;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-xs);
  font-weight: var(--font-weight-semibold);
  flex-shrink: 0;
}

.toast__message {
  font-size: var(--text-sm);
  line-height: var(--line-height-normal);
  text-align: center;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all var(--transition-normal);
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.9);
}
</style>
