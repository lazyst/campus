<template>
  <div class="nav-bar">
    <div class="nav-bar__left" @click="handleLeftClick">
      <slot name="left">
        <div v-if="leftArrow" class="nav-bar__back">
          <span class="nav-bar__back-icon">‹</span>
          <span class="nav-bar__back-text">返回</span>
        </div>
      </slot>
    </div>
    <div class="nav-bar__title">
      <slot name="title">
        <h1 class="nav-bar__title-text">{{ title }}</h1>
      </slot>
    </div>
    <div class="nav-bar__right">
      <slot name="right"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title?: string
  leftArrow?: boolean
}

withDefaults(defineProps<Props>(), {
  title: '',
  leftArrow: false
})

const emit = defineEmits<{
  'click-left': [event: MouseEvent]
}>()

function handleLeftClick(event: MouseEvent) {
  emit('click-left', event)
}
</script>

<style scoped>
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--nav-height);
  display: flex;
  align-items: center;
  padding: 0 var(--page-padding);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  z-index: var(--z-sticky);
}

.nav-bar__left {
  width: 64px;
  cursor: pointer;
}

.nav-bar__title {
  flex: 1;
  overflow: hidden;
  text-align: center;
}

.nav-bar__title-text {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-bar__right {
  width: 64px;
  text-align: right;
}

.nav-bar__back {
  display: flex;
  align-items: center;
  color: var(--text-secondary);
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
}

.nav-bar__back-icon {
  font-size: 20px;
  margin-right: 2px;
}

.nav-bar__back-text {
  margin-left: 2px;
}
</style>
