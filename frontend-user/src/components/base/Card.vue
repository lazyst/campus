<template>
  <div class="bg-white rounded-xl border border-gray-100 overflow-hidden shadow-sm">
    <div v-if="$slots.header || title" class="px-4 py-3 border-b border-gray-100">
      <slot name="header">
        <h3 class="font-semibold text-gray-800">{{ title }}</h3>
      </slot>
    </div>
    <div class="p-4">
      <slot></slot>
    </div>
    <div v-if="$slots.footer" class="px-4 py-3 border-t border-gray-100 bg-gray-50">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title?: string
}

defineProps<Props>()
</script>
