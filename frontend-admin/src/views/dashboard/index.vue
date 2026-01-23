<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <el-icon :size="24" color="#409EFF"><User /></el-icon>
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
              <el-icon :size="24" color="#67C23A"><Document /></el-icon>
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
              <el-icon :size="24" color="#E6A23C"><ShoppingCart /></el-icon>
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
              <el-icon :size="24" color="#909399"><Grid /></el-icon>
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
  :deep(.el-card__header) {
    padding: 15px 20px;
  }
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 500;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
  text-align: center;
  padding: 20px 0;
}

.stat-footer {
  display: flex;
  justify-content: space-around;
  font-size: 14px;
  color: #909399;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}
</style>
