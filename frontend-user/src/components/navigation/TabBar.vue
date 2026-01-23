<template>
  <div class="fixed bottom-0 left-0 right-0 h-14 flex bg-white border-t border-gray-100 z-50 pb-safe">
    <div
      v-for="item in items"
      :key="item.name"
      :class="[
        'flex-1 flex flex-col items-center justify-center cursor-pointer transition-colors duration-200',
        { 'text-primary': modelValue === item.name },
        { 'text-gray-500': modelValue !== item.name }
      ]"
      @click="select(item.name)"
    >
      <span class="text-xs mt-0.5">{{ item.label }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
interface TabItem {
  name: string
  label: string
}

interface Props {
  modelValue?: string
  items: TabItem[]
}

withDefaults(defineProps<Props>(), {
  modelValue: '',
  items: () => []
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  change: [name: string]
}>()

function select(name: string) {
  emit('update:modelValue', name)
  emit('change', name)
}
</script>
