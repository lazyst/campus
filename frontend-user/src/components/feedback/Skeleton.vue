<template>
  <div v-if="loading" class="skeleton">
    <div v-if="title" class="skeleton__title">
      <SkeletonItem variant="rect" :width="titleWidth" :height="titleHeight" :animated="animated" />
    </div>

    <div class="skeleton__rows">
      <div
        v-for="(row, index) in rows"
        :key="index"
        class="skeleton__row"
      >
        <SkeletonItem
          v-if="row.image"
          variant="rect"
          :width="row.imageWidth || 60"
          :height="row.imageHeight || 60"
          :animated="animated"
        />
        <div class="skeleton__row-content">
          <SkeletonItem
            v-if="row.title"
            variant="text"
            :width="row.titleWidth || '60%'"
            :height="row.titleHeight || 16"
            :animated="animated"
          />
          <SkeletonItem
            v-for="(line, lineIndex) in (row.lines || 1)"
            :key="`line-${lineIndex}`"
            variant="text"
            :width="row.lineWidth || '100%'"
            :height="row.lineHeight || 12"
            :animated="animated"
            class="skeleton__line"
          />
        </div>
      </div>
    </div>

    <slot name="footer"></slot>
  </div>
  <slot v-else></slot>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import SkeletonItem from './SkeletonItem.vue'

interface SkeletonRow {
  image?: boolean
  imageWidth?: string | number
  imageHeight?: string | number
  title?: boolean
  titleWidth?: string | number
  titleHeight?: string | number
  lines?: number
  lineWidth?: string | number
  lineHeight?: string | number
}

interface Props {
  loading?: boolean
  animated?: boolean
  rows?: number
  title?: boolean
  titleWidth?: string | number
  titleHeight?: string | number
  avatar?: boolean
  avatarSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  loading: true,
  animated: true,
  rows: 3,
  title: true,
  titleWidth: '40%',
  titleHeight: 20,
  avatar: false,
  avatarSize: 40
})

const defaultRows = computed<SkeletonRow[]>(() => {
  const rows: SkeletonRow[] = []
  
  for (let i = 0; i < props.rows; i++) {
    rows.push({
      image: props.avatar,
      imageWidth: props.avatarSize,
      imageHeight: props.avatarSize,
      title: i === 0,
      titleWidth: '50%',
      titleHeight: 16,
      lines: i === 0 ? 1 : 2,
      lineWidth: '100%',
      lineHeight: 12
    })
  }
  
  return rows
})
</script>

<style scoped>
.skeleton {
  width: 100%;
}

.skeleton__title {
  margin-bottom: var(--space-4);
}

.skeleton__rows {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.skeleton__row {
  display: flex;
  gap: var(--space-3);
}

.skeleton__row-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.skeleton__line {
  margin-top: var(--space-2);
}
</style>
