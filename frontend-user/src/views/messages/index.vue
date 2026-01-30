<template>
  <div class="messages-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">消息</h1>
    </div>

    <!-- 未登录状态 -->
    <div v-if="!isLoggedIn" class="empty-state">
      <div class="empty-icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M20 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4ZM20 6L12 11L4 6V6L12 11L20 6V6Z" fill="currentColor"/>
          <path d="M12 13C13.66 13 15 12.34 15 11V5C15 3.34 13.66 3 12 3C10.34 3 9 3.34 9 5V11C9 12.34 10.34 13 12 13Z" fill="currentColor"/>
        </svg>
      </div>
      <p class="empty-text">登录后查看消息</p>
      <button class="empty-btn" @click="goToLogin">去登录</button>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p class="loading-text">加载中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <p class="error-text">{{ error }}</p>
      <button class="error-btn" @click="retryLoad">重试</button>
    </div>

    <!-- 空状态 -->
    <div v-else-if="conversations.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M20 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4ZM20 6L12 11L4 6V6L12 11L20 6V6Z" fill="currentColor"/>
        </svg>
      </div>
      <p class="empty-text">暂无消息</p>
      <p class="empty-hint">快去和别人聊天吧~</p>
    </div>

    <!-- 消息列表 -->
    <div v-else class="messages-list">
      <div
        v-for="conversation in conversations"
        :key="conversation.id"
        class="message-item"
        @click="goToChat(conversation)"
      >
        <div class="message-avatar-container">
          <div class="message-avatar">
            <span>{{ conversation.otherUserNickname?.charAt(0) || '匿' }}</span>
          </div>
          <span v-if="conversation.unreadCount > 0" class="message-unread-badge">
            {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
          </span>
        </div>

        <div class="message-content">
          <div class="message-header">
            <span class="message-sender">{{ conversation.otherUserNickname || '未知用户' }}</span>
            <span class="message-time">{{ formatTime(conversation.lastMessageTime) }}</span>
          </div>
          <p class="message-preview">{{ conversation.lastMessageContent || '暂无消息' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, inject, Ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { getConversations } from '@/api/modules/conversation';
import { showToast } from '@/services/toastService';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';

dayjs.locale('zh-cn');

const router = useRouter();
const userStore = useUserStore();

// 注入来自 App.vue 的消息更新事件
const messageUpdateEvent = inject<Ref<number>>('messageUpdateEvent', ref(0))

// 计算属性：是否已登录
const isLoggedIn = computed(() => !!userStore.token);

// 响应式数据
const conversations = ref<any[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

// 格式化时间
function formatTime(time: string) {
  if (!time) return '';
  const date = dayjs(time);
  const now = dayjs();
  const diffDays = now.diff(date, 'day');

  if (diffDays === 0) {
    return date.format('HH:mm');
  } else if (diffDays === 1) {
    return '昨天';
  } else if (diffDays < 7) {
    return date.format('dddd');
  } else {
    return date.format('YYYY-MM-DD');
  }
}

// 获取会话列表
async function loadConversations() {
  loading.value = true;
  error.value = null;

  try {
    const data = await getConversations();
    // 后端已返回 unreadCount，直接使用
    conversations.value = (data || []).map((conv: any) => {
      // 确保 unreadCount 是数字
      conv.unreadCount = conv.unreadCount || 0;
      return conv;
    });
  } catch (err: any) {
    console.error('获取会话列表失败:', err);
    // 检查是否为401错误（未登录或token过期）
    if (err.message?.includes('401') || err.message?.includes('登录已过期')) {
      // 401错误时清空会话数据
      conversations.value = [];
    }
    error.value = err.message || '加载失败，请重试';
  } finally {
    loading.value = false;
  }
}

// 重试加载
function retryLoad() {
  loadConversations();
}

// 跳转登录
function goToLogin() {
  router.push('/login?redirect=/messages');
}

// 跳转聊天 - 使用正确的路由路径 /messages/:id
function goToChat(conversation: any) {
  if (conversation.otherUserId) {
    router.push(`/messages/${conversation.otherUserId}`);
  } else {
    console.error('会话缺少 otherUserId:', conversation);
  }
}

// 监听登录状态变化
watch(() => userStore.token, (newToken) => {
  if (newToken) {
    // 登录后加载会话列表
    loadConversations();
  } else {
    // 登出时清空数据
    conversations.value = [];
    loading.value = false;
    error.value = null;
  }
});

// 页面加载时获取数据
onMounted(async () => {
  if (isLoggedIn.value) {
    // 先加载一次数据
    await loadConversations();

    // 监听 storage 事件（跨标签页通信）
    window.addEventListener('storage', handleStorageChange);

    // 监听页面可见性变化（从聊天页面返回时刷新）
    document.addEventListener('visibilitychange', handleVisibilityChange);

    // 监听自定义事件（聊天页面清除未读数时刷新列表）
    window.addEventListener('unread-cleared', handleUnreadCleared);

    // 监听 App.vue 触发的消息更新事件，刷新列表
    const unwatchMessageUpdate = watch(messageUpdateEvent, () => {
      console.log('Messages 页面收到消息更新事件，刷新列表')
      loadConversations()
    })

    // 保存取消监听函数
    cleanupFunctions.push(unwatchMessageUpdate)
  } else {
    // 确保未登录时清空数据
    conversations.value = []
    loading.value = false
    error.value = null
  }
})

// 清理函数数组
const cleanupFunctions: (() => void)[] = []

// 处理 storage 变化（跨标签页通信）
function handleStorageChange(e: StorageEvent) {
  // 监听聊天详情页面的已读标记
  if (e.key && e.key.startsWith('unread_read_') && e.newValue === 'read') {
    // 刷新会话列表
    console.log('收到 storage 事件，刷新消息列表')
    loadConversations()
  }
}

// 处理聊天页面清除未读数的事件
function handleUnreadCleared(e: CustomEvent) {
  console.log('收到 unread-cleared 事件，刷新消息列表')
  loadConversations()
}

// 监听页面可见性变化，当从聊天页面返回时刷新
function handleVisibilityChange() {
  if (!document.hidden && isLoggedIn.value) {
    // 页面重新可见时刷新会话列表和未读数
    loadConversations()
  }
}

// 页面卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('unread-cleared', handleUnreadCleared)

  // 执行所有清理函数
  cleanupFunctions.forEach(fn => fn())
  cleanupFunctions.length = 0
})
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

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
  min-height: 60vh;
}

.empty-icon {
  color: var(--text-tertiary);
  margin-bottom: var(--space-4);
}

.empty-text {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  margin: 0 0 var(--space-2) 0;
}

.empty-hint {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0 0 var(--space-6) 0;
}

.empty-btn {
  padding: var(--space-3) var(--space-8);
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-inverse);
  background-color: var(--color-primary-700);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
}

.empty-btn:active {
  background-color: var(--color-primary-800);
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
  min-height: 60vh;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-gray-200);
  border-top-color: var(--color-primary-700);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin-top: var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

/* 错误状态 */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
  min-height: 60vh;
}

.error-text {
  font-size: var(--text-base);
  color: var(--color-error-500);
  margin: 0 0 var(--space-4) 0;
}

.error-btn {
  padding: var(--space-2) var(--space-6);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-primary-700);
  background-color: var(--color-primary-50);
  border: 1px solid var(--color-primary-200);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.error-btn:active {
  background-color: var(--color-primary-100);
}

/* 消息列表 */
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
