<template>
  <div class="category-card" :style="cardStyle" @click="handleClick">
    <span class="category-name">{{ name }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { categoryColors } from '@/styles/theme';

interface Props {
  name: string;
  color?: string;
  size?: 'small' | 'medium' | 'large';
}

const props = withDefaults(defineProps<Props>(), {
  size: 'medium',
});

const emit = defineEmits<{
  click: [name: string];
}>();

const colorScheme = computed(() => {
  return categoryColors[props.name] || categoryColors['交流'];
});

const cardStyle = computed(() => {
  const sizes = {
    small: { width: 60, height: 36 },
    medium: { width: 72, height: 48 },
    large: { width: 88, height: 56 },
  };
  
  return {
    backgroundColor: colorScheme.value.bg,
    color: colorScheme.value.text,
    width: `${sizes[props.size].width}px`,
    height: `${sizes[props.size].height}px`,
    borderRadius: '12px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    cursor: 'pointer',
    transition: 'all 0.2s ease',
  };
});

const nameStyle = computed(() => {
  const sizes = {
    small: { fontSize: '11px' },
    medium: { fontSize: '13px' },
    large: { fontSize: '15px' },
  };
  
  return {
    fontSize: sizes[props.size].fontSize,
    fontWeight: '500',
  };
});

function handleClick() {
  emit('click', props.name);
}
</script>

<style scoped>
.category-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.category-name {
  user-select: none;
}
</style>
