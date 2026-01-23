<template>
  <button
    :class="[
      'inline-flex items-center justify-center font-medium border border-transparent rounded-full cursor-pointer transition-all duration-200',
      typeClasses,
      sizeClasses,
      {
        'w-full': block,
        'opacity-50 cursor-not-allowed': disabled || loading,
        'rounded-full': round
      }
    ]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <!-- Loading spinner -->
    <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>
    
    <!-- Button text -->
    <slot></slot>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

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

// Type classes mapping
const typeClasses = computed(() => {
  const map: Record<string, string> = {
    'primary': 'bg-primary text-white hover:bg-blue-600 active:bg-blue-700',
    'success': 'bg-success text-white hover:bg-green-600 active:bg-green-700',
    'danger': 'bg-danger text-white hover:bg-red-600 active:bg-red-700',
    'default': 'bg-white text-default border border-gray-300 hover:bg-gray-50 active:bg-gray-100'
  }
  return map[props.type] || map.primary
})

// Size classes mapping
const sizeClasses = computed(() => {
  const map: Record<string, string> = {
    'normal': 'h-11 px-5 text-base',
    'small': 'h-9 px-4 text-sm',
    'large': 'h-12 px-6 text-lg'
  }
  return map[props.size] || map.normal
})

function handleClick(event: MouseEvent) {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>
