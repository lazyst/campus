<template>
  <Teleport to="body">
    <Transition name="dialog-fade">
      <div
        v-if="visible"
        class="dialog__backdrop"
        @click="handleBackdropClick"
      >
        <div class="dialog" :class="dialogClass">
          <div class="dialog__header">
            <div v-if="showIcon" class="dialog__icon">
              {{ iconText }}
            </div>
            <h3 class="dialog__title">{{ title }}</h3>
          </div>

          <div class="dialog__content">
            <p class="dialog__message">{{ message }}</p>
          </div>

          <div class="dialog__actions">
            <button
              class="dialog__btn dialog__btn--cancel"
              @click="handleCancel"
            >
              {{ cancelText }}
            </button>
            <button
              class="dialog__btn dialog__btn--confirm"
              :class="confirmClass"
              :disabled="loading"
              @click="handleConfirm"
            >
              <span v-if="loading" class="dialog__spinner"></span>
              <span v-else>{{ confirmText }}</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { DialogTheme } from './types/dialog'

interface Props {
  visible: boolean
  title?: string
  message: string
  theme?: DialogTheme
  confirmText?: string
  cancelText?: string
  loading?: boolean
  closeOnClickOverlay?: boolean
  showIcon?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '提示',
  theme: 'default',
  confirmText: '确定',
  cancelText: '取消',
  loading: false,
  closeOnClickOverlay: true,
  showIcon: false
})

const emit = defineEmits<{
  confirm: []
  cancel: []
  'update:visible': [value: boolean]
}>()

const iconMap: Record<DialogTheme, string> = {
  default: '提',
  warning: '警',
  danger: '危'
}

const iconText = computed(() => iconMap[props.theme] || '提')

const dialogClass = computed(() => `dialog--theme-${props.theme}`)

const confirmClass = computed(() => {
  const map: Record<DialogTheme, string> = {
    default: 'dialog__btn--primary',
    warning: 'dialog__btn--warning',
    danger: 'dialog__btn--danger'
  }
  return map[props.theme] || 'dialog__btn--primary'
})

function handleBackdropClick() {
  if (props.closeOnClickOverlay && !props.loading) {
    emit('update:visible', false)
    emit('cancel')
  }
}

function handleConfirm() {
  if (props.loading) return
  emit('confirm')
}

function handleCancel() {
  if (props.loading) return
  emit('update:visible', false)
  emit('cancel')
}

watch(() => props.visible, (val) => {
  if (!val) {
    document.body.style.overflow = ''
  } else {
    document.body.style.overflow = 'hidden'
  }
})
</script>

<style scoped>
.dialog__backdrop {
  position: fixed;
  inset: 0;
  background-color: var(--bg-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
  z-index: var(--z-modal-backdrop);
}

.dialog {
  background-color: var(--bg-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  width: 100%;
  max-width: 320px;
  overflow: hidden;
}

.dialog__header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-6) var(--space-4) var(--space-3);
}

.dialog__icon {
  width: 56px;
  height: 56px;
  background-color: var(--color-primary-100);
  color: var(--color-primary-700);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--space-3);
}

.dialog--theme-warning .dialog__icon {
  background-color: var(--color-warning-100);
  color: var(--color-warning-700);
}

.dialog--theme-danger .dialog__icon {
  background-color: var(--color-error-100);
  color: var(--color-error-700);
}

.dialog__title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  text-align: center;
}

.dialog__content {
  padding: 0 var(--space-4) var(--space-4);
}

.dialog__message {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--line-height-normal);
  text-align: center;
}

.dialog__actions {
  display: flex;
  border-top: 1px solid var(--border-light);
}

.dialog__btn {
  flex: 1;
  height: 52px;
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  border: none;
  background: none;
  cursor: pointer;
  transition: background-color var(--transition-fast);
  position: relative;
}

.dialog__btn:first-child {
  border-right: 1px solid var(--border-light);
}

.dialog__btn--cancel {
  color: var(--text-secondary);
}

.dialog__btn--cancel:hover {
  background-color: var(--bg-secondary);
}

.dialog__btn--confirm {
  color: var(--color-primary-700);
}

.dialog__btn--confirm:hover:not(:disabled) {
  background-color: var(--color-primary-50);
}

.dialog__btn--warning.dialog__btn--confirm {
  color: var(--color-warning-700);
}

.dialog__btn--warning.dialog__btn--confirm:hover:not(:disabled) {
  background-color: var(--color-warning-50);
}

.dialog__btn--danger.dialog__btn--confirm {
  color: var(--color-error-600);
}

.dialog__btn--danger.dialog__btn--confirm:hover:not(:disabled) {
  background-color: var(--color-error-50);
}

.dialog__btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.dialog__spinner {
  width: 20px;
  height: 20px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: all var(--transition-normal);
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}

.dialog-fade-enter-from .dialog,
.dialog-fade-leave-to .dialog {
  transform: scale(0.9);
}
</style>
