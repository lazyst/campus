<template>
  <div class="search-bar" :class="{ focused }">
    <span class="search-icon">🔍</span>
    <input 
      :value="modelValue"
      @input="handleInput"
      @focus="focused = true"
      @blur="focused = false"
      :placeholder="placeholder"
      class="search-input"
      type="text"
    />
    <button 
      v-if="showClear" 
      class="clear-btn" 
      @click="handleClear"
    >
      ✕
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

interface Props {
  modelValue?: string;
  placeholder?: string;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '搜索帖子、闲置...',
});

const emit = defineEmits<{
  'update:modelValue': [value: string];
  search: [query: string];
}>();

const focused = ref(false);

const showClear = computed(() => {
  return props.modelValue && props.modelValue.length > 0;
});

function handleInput(event: Event) {
  const target = event.target as HTMLInputElement;
  emit('update:modelValue', target.value);
}

function handleClear() {
  emit('update:modelValue', '');
  emit('search', '');
}
</script>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
  background: #F1F5F9;
  border-radius: 18px;
  padding: 0 16px;
  height: 36px;
  transition: all 0.2s ease;
}

.search-bar.focused {
  background: white;
  box-shadow: 0 0 0 2px #6366F1;
}

.search-icon {
  font-size: 14px;
  margin-right: 8px;
  opacity: 0.5;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #1E293B;
  outline: none;
}

.search-input::placeholder {
  color: #94A3B8;
}

.clear-btn {
  background: none;
  border: none;
  font-size: 14px;
  color: #94A3B8;
  cursor: pointer;
  padding: 4px;
  margin-left: 8px;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: #E2E8F0;
  color: #64748B;
}
</style>
