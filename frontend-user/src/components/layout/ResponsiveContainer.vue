<template>
  <div
    class="responsive-container"
    :class="[
      `responsive-container--${size}`,
      { 'responsive-container--full-height': fullHeight }
    ]"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
interface Props {
  size?: 'sm' | 'md' | 'lg' | 'full'
  fullHeight?: boolean
}

withDefaults(defineProps<Props>(), {
  size: 'md',
  fullHeight: false
})
</script>

<style scoped>
.responsive-container {
  width: 100%;
  margin-left: auto;
  margin-right: auto;
  padding-left: var(--page-padding);
  padding-right: var(--page-padding);
}

.responsive-container--sm {
  max-width: var(--container-tablet-width);
}

.responsive-container--md {
  max-width: var(--container-max-width);
}

.responsive-container--lg {
  max-width: 960px;
}

.responsive-container--full {
  max-width: 100%;
}

.responsive-container--full-height {
  min-height: 100vh;
}

/* PC端增强 */
@media (min-width: 1024px) {
  .responsive-container {
    padding-left: var(--space-8);
    padding-right: var(--space-8);
  }

  .responsive-container--sm {
    max-width: 560px;
  }

  .responsive-container--md {
    max-width: 720px;
  }

  .responsive-container--lg {
    max-width: 1100px;
  }

  /* PC端内容区域增加微妙的内阴影 */
  .responsive-container {
    position: relative;
  }

  .responsive-container::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 100%;
    height: 100%;
    max-width: inherit;
    pointer-events: none;
    background: radial-gradient(
      ellipse at center top,
      rgba(37, 99, 235, 0.02) 0%,
      transparent 70%
    );
  }
}

@media (max-width: 639px) {
  .responsive-container {
    padding-left: var(--space-4);
    padding-right: var(--space-4);
  }
}
</style>
