<template>
  <el-card shadow="hover" class="quick-actions-card">
    <template #header>
      <div class="actions-header">
        <span>快捷操作</span>
      </div>
    </template>
    <div class="actions-content">
      <div class="action-item" @click="handleAction('users')">
        <div class="action-icon users-icon">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
          </svg>
        </div>
        <span class="action-label">用户管理</span>
      </div>
      <div class="action-item" @click="handleAction('posts')">
        <div class="action-icon posts-icon">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
          </svg>
        </div>
        <span class="action-label">帖子管理</span>
      </div>
      <div class="action-item" @click="handleAction('items')">
        <div class="action-icon items-icon">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M20 6h-4V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H4c-1.11 0-2 .89-2 2v12c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-6 0h-4V4h4v2z"/>
          </svg>
        </div>
        <span class="action-label">物品管理</span>
      </div>
      <div class="action-item" @click="handleAction('boards')">
        <div class="action-icon boards-icon">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/>
          </svg>
        </div>
        <span class="action-label">板块管理</span>
      </div>
      <div class="action-item" @click="handleAction('storage')">
        <div class="action-icon storage-icon">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z"/>
          </svg>
        </div>
        <span class="action-label">文件管理</span>
      </div>
      <div class="action-item" @click="handleAction('refresh')">
        <div class="action-icon refresh-icon">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
          </svg>
        </div>
        <span class="action-label">刷新数据</span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const emit = defineEmits(['refresh'])

const handleAction = (action: string) => {
  switch (action) {
    case 'users':
      router.push('/users')
      break
    case 'posts':
      router.push('/posts')
      break
    case 'items':
      router.push('/items')
      break
    case 'boards':
      router.push('/boards')
      break
    case 'storage':
      router.push('/storage')
      break
    case 'refresh':
      emit('refresh')
      break
  }
}
</script>

<style scoped lang="scss">
.quick-actions-card {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  transition: all 0.3s;

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

.actions-header {
  font-size: 15px;
  font-weight: 500;
  color: #374151;
}

.actions-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #F8FAFC;

    .action-icon {
      transform: scale(1.1);
    }

    .action-label {
      color: #1E3A8A;
    }
  }

  &:active {
    transform: scale(0.98);
  }
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}

.users-icon {
  background: linear-gradient(135deg, #DBEAFE 0%, #93C5FD 100%);
  color: #1E3A8A;
}

.posts-icon {
  background: linear-gradient(135deg, #D1FAE5 0%, #6EE7B7 100%);
  color: #059669;
}

.items-icon {
  background: linear-gradient(135deg, #FEF3C7 0%, #FCD34D 100%);
  color: #D97706;
}

.boards-icon {
  background: linear-gradient(135deg, #E5E7EB 0%, #9CA3AF 100%);
  color: #4B5563;
}

.storage-icon {
  background: linear-gradient(135deg, #EDE9FE 0%, #C4B5FD 100%);
  color: #7C3AED;
}

.refresh-icon {
  background: linear-gradient(135deg, #F0F9FF 0%, #7DD3FC 100%);
  color: #0284C7;
}

.action-label {
  font-size: 12px;
  color: #6B7280;
  text-align: center;
  transition: color 0.2s;
}
</style>
