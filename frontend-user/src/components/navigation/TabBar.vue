<template>
  <div class="tab-bar">
    <button
      v-for="item in items"
      :key="item.name"
      class="tab-bar__item"
      :class="{ 'tab-bar__item--active': modelValue === item.name }"
      @click="select(item.name)"
    >
      <!-- 消息图标显示未读数徽章 -->
      <span v-if="item.name === 'Messages' && unreadCount > 0" class="tab-bar__badge">
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
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
  align-items: center;
  justify-content: center;
  padding: var(--space-2) var(--space-1);
  background: none;
  border: none;
  cursor: pointer;
  transition: all var(--transition-normal);
  color: var(--text-secondary);
  position: relative;
  min-height: var(--tabbar-height);
}

.tab-bar__item:active {
  background-color: var(--bg-secondary);
}

.tab-bar__item--active {
  color: var(--color-primary-700);
  font-weight: var(--font-weight-medium);
}

.tab-bar__badge {
  position: absolute;
  top: 12px;
  right: 50%;
  transform: translateX(16px);
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: var(--radius-full);
  background-color: var(--color-error-500);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: var(--font-weight-medium);
  color: var(--text-inverse);
}

.tab-bar__label {
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  line-height: 1;
}
</style>
