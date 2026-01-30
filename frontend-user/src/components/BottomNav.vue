<template>
  <div class="bottom-nav">
    <div 
      v-for="item in navItems" 
      :key="item.name"
      class="nav-item"
      :class="{ active: active === item.name }"
      @click="handleNavClick(item)"
    >
      <span class="nav-icon">{{ item.icon }}</span>
      <span class="nav-name">{{ item.label }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface NavItem {
  name: string;
  label: string;
  icon: string;
}

interface Props {
  active?: string;
}

const props = withDefaults(defineProps<Props>(), {
  active: 'home',
});

const emit = defineEmits<{
  change: [name: string];
}>();

const navItems = computed<NavItem[]>(() => [
  { name: 'home', label: '首页', icon: '🏠' },
  { name: 'trade', label: '闲置', icon: '🛒' },
  { name: 'publish', label: '发布', icon: '+' },
  { name: 'messages', label: '消息', icon: '💬' },
  { name: 'profile', label: '我的', icon: '👤' },
]);

function handleNavClick(item: NavItem) {
  emit('change', item.name);
}
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: var(--tabbar-height);
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: var(--space-2) var(--space-1);
  box-shadow: var(--shadow-tabbar);
  z-index: 100;
  border-top: 1px solid var(--border-light);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 4px 12px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
  min-width: 60px;
}

.nav-item:hover {
  background: var(--color-primary-50);
}

.nav-item.active {
  color: var(--color-primary-700);
}

.nav-item.active .nav-icon {
  transform: scale(1.1);
}

.nav-icon {
  font-size: 22px;
  line-height: 1;
  transition: transform 0.2s ease;
}

.nav-name {
  font-size: var(--text-xs);
  font-weight: var(--font-weight-medium);
  line-height: 1.2;
}
</style>
