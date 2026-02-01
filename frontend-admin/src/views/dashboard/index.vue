<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1 class="dashboard-title">数据概览</h1>
      <span class="dashboard-subtitle">管理后台</span>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <StatCard
          title="用户总数"
          :value="stats.users?.total || 0"
          card-color="#1E3A8A"
        >
          <template #icon>用</template>
          <template #footer>
            <span>正常: {{ stats.users?.normal || 0 }}</span>
            <span>已封禁: {{ stats.users?.banned || 0 }}</span>
          </template>
        </StatCard>
      </el-col>
      <el-col :span="6">
        <StatCard
          title="帖子总数"
          :value="stats.posts?.total || 0"
          card-color="#10B981"
        >
          <template #icon>帖</template>
          <template #footer>
            <span>今日: {{ stats.posts?.today || 0 }}</span>
            <span>本周: {{ stats.posts?.thisWeek || 0 }}</span>
          </template>
        </StatCard>
      </el-col>
      <el-col :span="6">
        <StatCard
          title="闲置物品"
          :value="stats.items?.total || 0"
          card-color="#F59E0B"
        >
          <template #icon>物</template>
          <template #footer>
            <span>在售: {{ stats.items?.selling || 0 }}</span>
            <span>已成交: {{ stats.items?.completed || 0 }}</span>
          </template>
        </StatCard>
      </el-col>
      <el-col :span="6">
        <StatCard
          title="板块数量"
          :value="stats.boards?.total || 0"
          card-color="#6B7280"
        >
          <template #icon>板</template>
          <template #footer>
            <span>启用: {{ stats.boards?.active || 0 }}</span>
          </template>
        </StatCard>
      </el-col>
    </el-row>

    <!-- 第二行：趋势图 + 快捷操作 -->
    <el-row :gutter="20" class="middle-row">
      <el-col :span="16">
        <TrendChart v-if="hasTrendData" :data="trend" />
        <el-card v-else shadow="hover" class="placeholder-card">
          <el-skeleton :rows="5" animated />
        </el-card>
      </el-col>
      <el-col :span="8">
        <QuickActions @refresh="fetchData" />
      </el-col>
    </el-row>

    <!-- 第三行：最近活跃 + 系统状态 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="12">
        <RecentActivity v-if="hasRecentData" :data="recent" />
        <el-card v-else shadow="hover" class="placeholder-card">
          <el-skeleton :rows="6" animated />
        </el-card>
      </el-col>
      <el-col :span="12">
        <SystemStatus v-if="hasStatusData" :data="status" />
        <el-card v-else shadow="hover" class="placeholder-card">
          <el-skeleton :rows="5" animated />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import StatCard from './components/StatCard.vue'
import TrendChart from './components/TrendChart.vue'
import RecentActivity from './components/RecentActivity.vue'
import SystemStatus from './components/SystemStatus.vue'
import QuickActions from './components/QuickActions.vue'
import { getDashboardOverview } from '@/api/admin/dashboard'

// 数据状态
const stats = ref<Record<string, any>>({})
const trend = ref({ dates: [], userRegistrations: [], postCreations: [] })
const recent = ref({ posts: [], users: [] })
const status = ref<Record<string, any>>({})

// 加载状态
const loading = ref(true)

// 计算属性
const hasTrendData = computed(() => trend.value.dates?.length > 0)
const hasRecentData = computed(() => recent.value.posts?.length > 0 || recent.value.users?.length > 0)
const hasStatusData = computed(() => Object.keys(status.value).length > 0)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDashboardOverview()
    // res 格式: { code, message, data: {...}, ... }
    // res.data 包含: { stats, trend, recent, status }
    if (res.code === 0 || res.code === 200) {
      const data = res.data
      stats.value = data.stats || {}
      trend.value = data.trend || { dates: [], userRegistrations: [], postCreations: [] }
      recent.value = data.recent || { posts: [], users: [] }
      status.value = data.status || {}
    }
  } catch (error) {
    console.error('获取首页数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 0;
}

.dashboard-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}

.dashboard-title {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.dashboard-subtitle {
  font-size: 14px;
  color: #9CA3AF;
}

.stats-row {
  margin-bottom: 20px;
}

.middle-row {
  margin-bottom: 20px;
}

.bottom-row {
  margin-bottom: 20px;
}

.placeholder-card {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 12px;

  :deep(.el-card__body) {
    padding: 20px;
  }
}

:deep(.el-card) {
  border: none;
}
</style>
