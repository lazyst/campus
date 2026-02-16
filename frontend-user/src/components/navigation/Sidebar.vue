<template>
  <nav class="sidebar">
    <div class="sidebar__header">
      <div class="sidebar__logo">校园互助</div>
    </div>
    <div class="sidebar__nav">
      <button
        v-for="item in items"
        :key="item.name"
        class="sidebar__item"
        :class="{ 'sidebar__item--active': modelValue === item.name }"
        @click="select(item.name)"
      >
        <span class="sidebar__label">{{ item.label }}</span>
      </button>
    </div>
    <div class="sidebar__footer">
      <div v-if="unreadCount > 0" class="sidebar__badge">
        {{ unreadCount > 99 ? '99+' : unreadCount }} 条未读
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
interface TabItem {
  name: string
  label: string
}

interface Props {
  modelValue?: string
  items: TabItem[]
  unreadCount?: number
}

withDefaults(defineProps<Props>(), {
  modelValue: '',
  items: () => [],
  unreadCount: 0
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
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: var(--sidebar-width);
  background-color: var(--bg-card);
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  z-index: var(--z-fixed);
}

.sidebar__header {
  padding: var(--space-6) var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.sidebar__logo {
  font-size: var(--text-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary-700);
}

.sidebar__nav {
  flex: 1;
  padding: var(--space-4) 0;
}

.sidebar__item {
  width: 100%;
  display: flex;
  align-items: center;
  padding: var(--space-4);
  background: none;
  border: none;
  cursor: pointer;
  transition: all var(--transition-normal);
  color: var(--text-secondary);
  text-align: left;
  font-size: var(--text-base);
}

.sidebar__item:hover {
  background-color: var(--bg-secondary);
}

.sidebar__item--active {
  color: var(--color-primary-700);
  background-color: var(--color-primary-50);
  font-weight: var(--font-weight-medium);
}

.sidebar__label {
  padding-left: var(--space-4);
}

.sidebar__footer {
  padding: var(--space-4);
  border-top: 1px solid var(--border-light);
}

.sidebar__badge {
  padding: var(--space-2) var(--space-4);
  background-color: var(--color-error-50);
  color: var(--color-error-600);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  text-align: center;
}

@media (max-width: 1023px) {
  .sidebar {
    display: none;
  }
}
</style>
