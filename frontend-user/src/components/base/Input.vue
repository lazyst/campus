<template>
  <div class="w-full">
    <label v-if="label" class="block text-sm font-medium text-gray-700 mb-2">{{ label }}</label>
    <div class="relative rounded-md shadow-sm">
      <input
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        class="block w-full rounded-md border px-4 py-3 text-base outline-none transition-colors duration-200"
        :class="[
          inputClasses,
          { 'bg-gray-50 cursor-not-allowed': disabled }
        ]"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

interface Props {
  modelValue?: string | number
  label?: string
  placeholder?: string
  disabled?: boolean
  type?: 'text' | 'password' | 'email' | 'tel' | 'number'
  error?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  disabled: false,
  type: 'text',
  error: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  focus: [event: FocusEvent]
  blur: [event: FocusEvent]
}>()

const isFocused = ref(false)

const inputClasses = computed(() => {
  if (props.error) {
    return 'border-danger focus:border-danger focus:ring-1 focus:ring-danger'
  }
  return 'border-gray-300 focus:border-primary focus:ring-1 focus:ring-primary'
})

function handleInput(event: Event) {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

function handleFocus(event: FocusEvent) {
  isFocused.value = true
  emit('focus', event)
}

function handleBlur(event: FocusEvent) {
  isFocused.value = false
  emit('blur', event)
}
</script>
