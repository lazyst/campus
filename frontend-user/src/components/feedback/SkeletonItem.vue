<template>
  <div
    :class="[
      'skeleton-item',
      `skeleton-item--${variant}`
    ]"
    :style="itemStyle"
  >
    <div v-if="$slots.default" class="skeleton-item__content">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'text' | 'circle' | 'rect' | 'round'
  width?: string | number
  height?: string | number
  animated?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'text',
  animated: true
})

const itemStyle = computed(() => {
  const style: Record<string, string> = {}
  
  if (props.width) {
    style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
  }
  
  if (props.height) {
    style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
  }
  
  return style
})
</script>

<style scoped>
.skeleton-item {
  background: linear-gradient(
    90deg,
    var(--bg-tertiary) 25%,
    var(--bg-secondary) 50%,
    var(--bg-tertiary) 75%
  );
  background-size: 200% 100%;
  border-radius: var(--radius-sm);
}

.skeleton-item--text {
  height: var(--text-sm);
  width: 100%;
}

.skeleton-item--circle {
  border-radius: var(--radius-full);
}

.skeleton-item--rect {
  border-radius: var(--radius-md);
}

.skeleton-item--round {
  border-radius: var(--radius-lg);
}

.skeleton-item--text.skeleton-item--animated,
.skeleton-item--circle.skeleton-item--animated,
.skeleton-item--rect.skeleton-item--animated,
.skeleton-item--round.skeleton-item--animated {
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
