<template>
  <el-card shadow="hover" class="system-status-card">
    <template #header>
      <div class="status-header">
        <span>系统状态</span>
      </div>
    </template>
    <div class="status-content">
      <!-- 服务器状态 -->
      <div class="status-section">
        <div class="section-title">
          <span class="section-icon server-icon"></span>
          <span>服务器</span>
        </div>
        <div class="server-info">
          <div class="server-status">
            <span class="status-dot" :class="{ online: server.serverStatus === '正常运行' }"></span>
            <span>{{ server.serverStatus }}</span>
          </div>
          <div class="server-uptime">运行时间: {{ server.uptime }}</div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
interface ServerInfo {
  uptime: string
  serverStatus: string
}

interface Props {
  data: {
    server: ServerInfo
  }
}

const props = defineProps<Props>()

const server = computed(() => props.data.server || { uptime: '-', serverStatus: '正常运行' })
</script>

<style scoped lang="scss">
.system-status-card {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  transition: all 0.3s;
  height: 100%;

  :deep(.el-card__header) {
    padding: 12px 20px;
    border-bottom: 1px solid #F3F4F6;
    background: #FAFAFA;
  }

  :deep(.el-card__body) {
    padding: 16px 20px;
  }

  &:hover {
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  }
}

.status-header {
  font-size: 15px;
  font-weight: 500;
  color: #374151;
}

.status-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.status-section {
  padding-bottom: 16px;
  border-bottom: 1px solid #F3F4F6;

  &:last-child {
    padding-bottom: 0;
    border-bottom: none;
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 12px;
}

.section-icon {
  width: 24px;
  height: 24px;
  border-radius: 6px;
}

.server-icon {
  background: linear-gradient(135deg, #10B981 0%, #34D399 100%);
}

.server-info {
  padding-left: 34px;
}

.server-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #374151;
  margin-bottom: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #EF4444;

  &.online {
    background: #10B981;
  }
}

.server-uptime {
  font-size: 12px;
  color: #9CA3AF;
}
</style>
