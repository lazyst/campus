<template>
  <div 
    :class="[
      'flex items-center px-4 py-4 bg-white',
      { 'cursor-pointer active:bg-gray-50 transition-colors': clickable },
      { 'border-b border-gray-100': bordered }
    ]"
    @click="handleClick"
  >
    <div v-if="$slots.icon" class="mr-3 flex-shrink-0">
      <slot name="icon"></slot>
    </div>
    <div class="flex-1 min-w-0">
      <div v-if="title" class="text-gray-800 font-medium">
        <slot>{{ title }}</slot>
      </div>
      <slot v-else></slot>
    </div>
    <div v-if="value || isLink || $slots.value" class="ml-3 flex-shrink-0 text-right">
      <slot name="value">
        <span v-if="value" class="text-sm text-gray-400">{{ value }}</span>
        <span v-if="isLink" class="ml-1 text-gray-400">›</span>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title?: string
  value?: string | number
  isLink?: boolean
  clickable?: boolean
  bordered?: boolean
}

withDefaults(defineProps<Props>(), {
  clickable: false,
  isLink: false,
  bordered: true
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

function handleClick(event: MouseEvent) {
  emit('click', event)
}
</script>
