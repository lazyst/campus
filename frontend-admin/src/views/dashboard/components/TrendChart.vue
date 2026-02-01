<template>
  <el-card shadow="hover" class="trend-chart-card">
    <template #header>
      <div class="chart-header">
        <span>数据趋势</span>
        <span class="chart-subtitle">近7天数据</span>
      </div>
    </template>
    <div class="chart-container">
      <div class="chart-legend">
        <span class="legend-item">
          <span class="legend-dot" style="background: #1E3A8A;"></span>
          新增用户
        </span>
        <span class="legend-item">
          <span class="legend-dot" style="background: #10B981;"></span>
          发布帖子
        </span>
      </div>
      <div class="chart-bars">
        <div v-for="(item, index) in chartData" :key="index" class="bar-group">
          <div class="bars-wrapper">
            <div
              class="bar bar-users"
              :style="{ height: getBarHeight(item.users) + 'px' }"
              :title="`用户: ${item.users}`"
            >
              <span class="bar-value">{{ item.users }}</span>
            </div>
            <div
              class="bar bar-posts"
              :style="{ height: getBarHeight(item.posts) + 'px' }"
              :title="`帖子: ${item.posts}`"
            >
              <span class="bar-value">{{ item.posts }}</span>
            </div>
          </div>
          <span class="bar-label">{{ item.date }}</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
interface ChartDataItem {
  date: string
  users: number
  posts: number
}

interface Props {
  data: {
    dates: string[]
    userRegistrations: number[]
    postCreations: number[]
  }
}

const props = defineProps<Props>()

const chartData = computed(() => {
  const { dates, userRegistrations, postCreations } = props.data
  return dates.map((date, index) => ({
    date,
    users: userRegistrations[index] || 0,
    posts: postCreations[index] || 0
  }))
})

const maxValue = computed(() => {
  const allValues = [
    ...props.data.userRegistrations,
    ...props.data.postCreations
  ]
  return Math.max(...allValues, 1)
})

const getBarHeight = (value: number) => {
  const maxHeight = 120
  return Math.max((value / maxValue.value) * maxHeight, 8)
}
</script>

<style scoped lang="scss">
.trend-chart-card {
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
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  }
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 500;
  color: #374151;
}

.chart-subtitle {
  font-size: 12px;
  color: #9CA3AF;
  font-weight: normal;
}

.chart-container {
  padding: 10px 0;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6B7280;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 160px;
  padding: 0 10px;
}

.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.bars-wrapper {
  display: flex;
  gap: 6px;
  align-items: flex-end;
}

.bar {
  width: 20px;
  border-radius: 4px 4px 0 0;
  position: relative;
  transition: all 0.3s;
  cursor: pointer;
  display: flex;
  justify-content: center;

  &:hover {
    opacity: 0.85;
    transform: scaleY(1.02);
  }
}

.bar-users {
  background: linear-gradient(180deg, #1E3A8A 0%, #3B82F6 100%);
}

.bar-posts {
  background: linear-gradient(180deg, #10B981 0%, #34D399 100%);
}

.bar-value {
  position: absolute;
  top: -20px;
  font-size: 10px;
  color: #6B7280;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.2s;
}

.bar:hover .bar-value {
  opacity: 1;
}

.bar-label {
  font-size: 11px;
  color: #9CA3AF;
}
</style>
