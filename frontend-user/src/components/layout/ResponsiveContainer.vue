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

@media (max-width: 639px) {
  .responsive-container {
    padding-left: var(--space-4);
    padding-right: var(--space-4);
  }
}
</style>
