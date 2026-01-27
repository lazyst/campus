<template>
  <button
    :class="[
      'base-button',
      `base-button--${type}`,
      `base-button--${size}`,
      {
        'base-button--block': block,
        'base-button--round': round,
        'base-button--disabled': disabled || loading
      }
    ]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="loading" class="base-button__spinner"></span>
    <span :class="{ 'base-button__text--loading': loading }">
      <slot></slot>
    </span>
  </button>
</template>

<script setup lang="ts">
interface Props {
  type?: 'primary' | 'success' | 'danger' | 'default'
  size?: 'normal' | 'small' | 'large'
  disabled?: boolean
  loading?: boolean
  block?: boolean
  round?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'normal',
  disabled: false,
  loading: false,
  block: false,
  round: true
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

function handleClick(event: MouseEvent) {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped>
.base-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: var(--font-weight-medium);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-normal);
  outline: none;
  position: relative;
  overflow: hidden;
}

.base-button:active {
  transform: scale(0.98);
}

/* Size variants */
.base-button--normal {
  height: 44px;
  padding: 0 var(--space-5);
  font-size: var(--text-base);
}

.base-button--small {
  height: 36px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
}

.base-button--large {
  height: 48px;
  padding: 0 var(--space-6);
  font-size: var(--text-lg);
}

/* Type variants */
.base-button--primary {
  background-color: var(--color-primary-700);
  color: var(--text-inverse);
  box-shadow: var(--shadow-primary-md);
}

.base-button--primary:hover:not(.base-button--disabled) {
  background-color: var(--color-primary-800);
}

.base-button--primary:active:not(.base-button--disabled) {
  background-color: var(--color-primary-900);
  box-shadow: var(--shadow-sm);
}

.base-button--success {
  background-color: var(--color-success-500);
  color: var(--text-inverse);
}

.base-button--success:hover:not(.base-button--disabled) {
  background-color: var(--color-success-600);
}

.base-button--success:active:not(.base-button--disabled) {
  background-color: var(--color-success-700);
}

.base-button--danger {
  background-color: var(--color-error-500);
  color: var(--text-inverse);
}

.base-button--danger:hover:not(.base-button--disabled) {
  background-color: var(--color-error-600);
}

.base-button--danger:active:not(.base-button--disabled) {
  background-color: var(--color-error-700);
}

.base-button--default {
  background-color: var(--bg-card);
  color: var(--text-primary);
  border-color: var(--border-default);
}

.base-button--default:hover:not(.base-button--disabled) {
  background-color: var(--bg-secondary);
}

.base-button--default:active:not(.base-button--disabled) {
  background-color: var(--bg-tertiary);
}

/* Block button */
.base-button--block {
  width: 100%;
}

/* Disabled state */
.base-button--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Spinner */
.base-button__spinner {
  width: 18px;
  height: 18px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-right: var(--space-2);
}

.base-button__text--loading {
  margin-left: var(--space-1);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
