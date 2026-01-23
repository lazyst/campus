<template>
  <div class="flex items-center h-11 px-4 bg-white border-b border-gray-100 fixed top-0 left-0 right-0 z-50">
    <div class="w-16 cursor-pointer" @click="handleLeftClick">
      <slot name="left">
        <span v-if="leftArrow" class="flex items-center text-gray-600 font-medium">
          <span class="mr-1">‹</span>
          返回
        </span>
      </slot>
    </div>
    <div class="flex-1 text-center overflow-hidden">
      <slot name="title">
        <h1 class="text-lg font-semibold text-gray-800 truncate">{{ title }}</h1>
      </slot>
    </div>
    <div class="w-16 text-right">
      <slot name="right"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title?: string
  leftArrow?: boolean
}

withDefaults(defineProps<Props>(), {
  title: '',
  leftArrow: false
})

const emit = defineEmits<{
  'click-left': [event: MouseEvent]
}>()

function handleLeftClick(event: MouseEvent) {
  emit('click-left', event)
}
</script>
