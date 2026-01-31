<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <span class="stat-icon" style="color: #1E3A8A;">用</span>
              <span>用户总数</span>
            </div>
          </template>
          <div class="stat-value">{{ userStats.total }}</div>
          <div class="stat-footer">
            <span>正常: {{ userStats.normal }}</span>
            <span>已封禁: {{ userStats.banned }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <span class="stat-icon" style="color: #16A34A;">帖</span>
              <span>帖子总数</span>
            </div>
          </template>
          <div class="stat-value">{{ postStats.total }}</div>
          <div class="stat-footer">
            <span>今日: {{ postStats.today }}</span>
            <span>本周: {{ postStats.thisWeek }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <span class="stat-icon" style="color: #D97706;">物</span>
              <span>闲置物品</span>
            </div>
          </template>
          <div class="stat-value">{{ itemStats.total }}</div>
          <div class="stat-footer">
            <span>在售: {{ itemStats.selling }}</span>
            <span>已下架: {{ itemStats.offline }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <span class="stat-icon" style="color: #4B5563;">板</span>
              <span>板块数量</span>
            </div>
          </template>
          <div class="stat-value">{{ boardStats.total }}</div>
          <div class="stat-footer">
            <span>启用: {{ boardStats.active }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUserStats } from '@/api/admin/user'
import { getPostStats } from '@/api/admin/post'
import { getItemStats } from '@/api/admin/item'
import { getBoardStats } from '@/api/admin/board'

const userStats = ref({ total: 0, normal: 0, banned: 0 })
const postStats = ref({ total: 0, today: 0, thisWeek: 0 })
const itemStats = ref({ total: 0, selling: 0, completed: 0, offline: 0 })
const boardStats = ref({ total: 0, active: 0 })

onMounted(async () => {
  try {
    userStats.value = await getUserStats()
    postStats.value = await getPostStats()
    itemStats.value = await getItemStats()
    boardStats.value = await getBoardStats()
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 0;
}

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
    border-color: #D1D5DB;
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
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #111827;
  text-align: center;
  padding: 16px 0;
}

.stat-footer {
  display: flex;
  justify-content: space-around;
  font-size: 13px;
  color: #6B7280;
  padding-top: 12px;
  border-top: 1px solid #F3F4F6;
}
</style>
