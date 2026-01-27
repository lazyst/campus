<template>
  <div class="messages-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">消息</h1>
    </div>

    <!-- 消息列表 -->
    <div class="messages-list">
      <div 
        v-for="message in messages" 
        :key="message.id"
        class="message-item"
        @click="goToChat(message.id)"
      >
        <div class="message-avatar-container">
          <div class="message-avatar">
            <span>{{ message.avatar }}</span>
          </div>
          <span v-if="message.unread" class="message-unread-badge">
            {{ message.unread }}
          </span>
        </div>
        
        <div class="message-content">
          <div class="message-header">
            <span class="message-sender">{{ message.sender }}</span>
            <span class="message-time">{{ message.time }}</span>
          </div>
          <p class="message-preview">{{ message.preview }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 模拟消息数据
const messages = ref([
  {
    id: 1,
    sender: '小明学长',
    avatar: '小',
    preview: '可以便宜50块，我在图书馆三楼，随时可以交易',
    time: '10:30',
    unread: 3,
  },
  {
    id: 2,
    sender: '系统通知',
    avatar: '系',
    preview: '您的帖子「出售MacBook Pro」已有3人浏览',
    time: '昨天',
    unread: 0,
  },
  {
    id: 3,
    sender: '学习达人',
    avatar: '学',
    preview: '好的，谢谢！我加你微信了',
    time: '昨天',
    unread: 0,
  },
  {
    id: 4,
    sender: '管理员',
    avatar: '管',
    preview: '您的闲置「考研资料」已通过审核',
    time: '周一',
    unread: 0,
  },
]);

function goToChat(id: number) {
  router.push(`/messages/chat/${id}`);
}
</script>

<style scoped>
.messages-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-bottom: var(--tabbar-height);
}

.page-header {
  padding: var(--space-4);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
}

.page-title {
  font-size: var(--text-xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin: 0;
}

.messages-list {
  padding: var(--space-2) 0;
}

.message-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background-color: var(--bg-card);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.message-item:active {
  background-color: var(--bg-secondary);
}

.message-avatar-container {
  position: relative;
  flex-shrink: 0;
}

.message-avatar {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-full);
  background-color: var(--color-primary-100);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-700);
}

.message-unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: var(--radius-full);
  background-color: var(--color-error-500);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: var(--font-weight-medium);
  color: var(--text-inverse);
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-1);
}

.message-sender {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.message-time {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.message-preview {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
