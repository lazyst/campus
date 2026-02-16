<template>
  <el-card shadow="hover" class="stat-card" :style="{ '--card-color': cardColor }">
    <template #header>
      <div class="stat-header">
        <span class="stat-icon">
          <slot name="icon">{{ icon }}</slot>
        </span>
        <span>{{ title }}</span>
      </div>
    </template>
    <div class="stat-value">{{ displayValue }}</div>
    <div class="stat-footer">
      <slot name="footer">
        <span v-for="(item, index) in footerItems" :key="index">
          {{ item.label }}: {{ item.value }}
        </span>
      </slot>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  title: string
  value: string | number
  icon?: string
  cardColor?: string
  footerItems?: Array<{ label: string; value: string | number }>
}

const props = withDefaults(defineProps<Props>(), {
  icon: '',
  cardColor: '#1E3A8A',
  footerItems: () => []
})

const displayValue = computed(() => props.value ?? '-')
</script>

<style scoped lang="scss">
.stat-card {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  transition: all 0.3s;

  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #F3F4F6;
    background: #FAFAFA;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
    border-color: var(--card-color);
  }
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  font-weight: 500;
  color: #374151;
}

.stat-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F3F4F6;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--card-color);
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #111827;
  text-align: center;
  padding: 16px 0;

  @media (max-width: 480px) {
    font-size: 28px;
    padding: 12px 0;
  }
}

.stat-footer {
  display: flex;
  justify-content: space-around;
  font-size: 13px;
  color: #6B7280;
  padding-top: 12px;
  border-top: 1px solid #F3F4F6;

  @media (max-width: 480px) {
    flex-wrap: wrap;
    gap: 8px;
    justify-content: space-between;
    font-size: 12px;
  }
}
</style>
