<template>
  <div class="tab-bar">
    <button
      v-for="item in items"
      :key="item.name"
      class="tab-bar__item"
      :class="{ 'tab-bar__item--active': modelValue === item.name }"
      @click="select(item.name)"
    >
      <span class="tab-bar__label">{{ item.label }}</span>
    </button>
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

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: var(--tabbar-height);
  display: flex;
  align-items: center;
  background-color: var(--bg-card);
  border-top: 1px solid var(--border-light);
  box-shadow: var(--shadow-tabbar);
  z-index: var(--z-fixed);
  padding-bottom: var(--page-safe-bottom);
}

.tab-bar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-1);
  background: none;
  border: none;
  cursor: pointer;
  transition: all var(--transition-normal);
  color: var(--text-secondary);
}

.tab-bar__item:active {
  background-color: var(--bg-secondary);
}

.tab-bar__item--active {
  color: var(--color-primary-700);
  font-weight: var(--font-weight-medium);
}

.tab-bar__label {
  font-size: var(--text-xs);
  line-height: 1.2;
}
</style>
