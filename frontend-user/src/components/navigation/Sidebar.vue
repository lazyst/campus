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
  background: linear-gradient(180deg, #FFFFFF 0%, #F8FAFC 100%);
  border-right: 1px solid var(--color-gray-200);
  display: flex;
  flex-direction: column;
  z-index: var(--z-fixed);
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.04);
}

.sidebar__header {
  padding: var(--space-6) var(--space-5);
  border-bottom: 1px solid var(--color-gray-100);
}

.sidebar__logo {
  font-size: var(--text-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary-800);
  letter-spacing: -0.02em;
  position: relative;
}

.sidebar__logo::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 32px;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary-600), var(--color-primary-400));
  border-radius: 2px;
}

.sidebar__nav {
  flex: 1;
  padding: var(--space-4) var(--space-3);
  overflow-y: auto;
}

.sidebar__item {
  width: 100%;
  display: flex;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  background: none;
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
  color: var(--text-secondary);
  text-align: left;
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  margin-bottom: var(--space-1);
  position: relative;
  overflow: hidden;
}

.sidebar__item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: var(--color-primary-600);
  border-radius: 0 2px 2px 0;
  transition: height var(--transition-fast) var(--ease-out);
}

.sidebar__item:hover {
  background: linear-gradient(90deg, rgba(37, 99, 235, 0.06) 0%, transparent 100%);
  color: var(--text-primary);
}

.sidebar__item--active {
  color: var(--color-primary-700);
  background: linear-gradient(90deg, rgba(37, 99, 235, 0.12) 0%, rgba(37, 99, 235, 0.04) 100%);
  font-weight: var(--font-weight-semibold);
}

.sidebar__item--active::before {
  height: 28px;
  background: var(--color-primary-600);
}

.sidebar__label {
  padding-left: var(--space-3);
  position: relative;
  z-index: 1;
}

.sidebar__footer {
  padding: var(--space-4) var(--space-4);
  border-top: 1px solid var(--color-gray-100);
}

.sidebar__badge {
  padding: var(--space-3) var(--space-4);
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  color: #B45309;
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  text-align: center;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.15);
  border: 1px solid rgba(245, 158, 11, 0.2);
}

@media (max-width: 1023px) {
  .sidebar {
    display: none;
  }
}
</style>
