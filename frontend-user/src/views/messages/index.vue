<template>
  <!-- PC端布局 -->
  <template v-if="isPC">
    <div class="messages-page messages-page--pc">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">消息</h1>
      </div>

      <!-- PC端分栏布局 -->
      <div class="messages-container">
        <!-- 左侧：消息列表 -->
        <div class="messages-sidebar">
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
                <p class="message-preview">{{ formatMessagePreview(conversation.lastMessageContent, conversation.lastMessageType) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：聊天详情/提示区域 -->
        <div class="messages-detail">
          <!-- 未登录 -->
          <div v-if="!isLoggedIn" class="messages-detail-empty">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 8H19C20.1 8 21 8.9 21 10V20C21 21.1 20.1 22 19 22H5C3.9 22 3 21.1 3 20V10C3 8.9 3.9 8 5 8H6V6C6 3.9 7.9 2 10 2C12.1 2 14 3.9 14 6V8H18ZM10 4C8.9 4 8 4.9 8 6V8H16V6C16 4.9 15.1 4 14 4C12.1 4 10 4 10 4Z" fill="currentColor"/>
              <path d="M12 12C13.66 12 15 12.67 15 14V17H9V14C9 12.67 10.34 12 12 12Z" fill="currentColor"/>
            </svg>
            <p>登录后与好友聊天</p>
          </div>
          <!-- 已登录但无会话 -->
          <div v-else-if="conversations.length === 0 && !loading" class="messages-detail-empty">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 2H4C2.9 2 2 2.9 2 4V22L6 18H20C21.1 18 22 17.1 22 16V4C22 2.9 21.1 2 20 2ZM20 16H5.17L4 17.17V4H20V16Z" fill="currentColor"/>
              <path d="M7 9H17V11H7V9ZM7 6H17V8H7V6ZM7 12H14V14H7V12Z" fill="currentColor"/>
            </svg>
            <p>暂无会话，快去找好友聊天吧</p>
          </div>
          <!-- 有会话但未选择 -->
          <div v-else class="messages-detail-empty">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 2H4C2.9 2 2 2.9 2 4V22L6 18H20C21.1 18 22 17.1 22 16V4C22 2.9 21.1 2 20 2ZM20 16H5.17L4 17.17V4H20V16Z" fill="currentColor"/>
              <path d="M7 9H17V11H7V9ZM7 6H17V8H7V6ZM7 12H14V14H7V12Z" fill="currentColor"/>
            </svg>
            <p>选择会话开始聊天</p>
          </div>
        </div>
      </div>
    </div>
  </template>

  <!-- 移动端布局 -->
  <template v-else>
    <div class="messages-page">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">消息</h1>
      </div>

      <ResponsiveContainer>
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
            <p class="message-preview">{{ formatMessagePreview(conversation.lastMessageContent, conversation.lastMessageType) }}</p>
          </div>
        </div>
      </div>
    </ResponsiveContainer>
    </div>
  </template>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, inject, Ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { getConversations } from '@/api/modules/conversation';
import { showToast } from '@/services/toastService';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import ResponsiveContainer from '@/components/layout/ResponsiveContainer.vue';

dayjs.locale('zh-cn');

const router = useRouter();
const userStore = useUserStore();

// 注入来自 App.vue 的消息更新事件
const messageUpdateEvent = inject<Ref<number>>('messageUpdateEvent', ref(0))

// 计算属性：是否已登录
const isLoggedIn = computed(() => !!userStore.token);

// PC端检测
const isPC = ref(typeof window !== 'undefined' ? window.innerWidth >= 1024 : false);

if (typeof window !== 'undefined') {
  window.addEventListener('resize', () => {
    isPC.value = window.innerWidth >= 1024;
  });
}

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

// 格式化消息预览内容
function formatMessagePreview(content: string, messageType?: number): string {
  if (!content) return '暂无消息';
  // type=2 表示图片消息
  if (messageType === 2) {
    return '[图片]';
  }
  // 兼容处理：根据 URL 判断是否是图片
  const trimmedContent = content.trim().toLowerCase();
  if (
    trimmedContent.startsWith('/uploads/') ||
    trimmedContent.startsWith('http://') ||
    trimmedContent.startsWith('https://')
  ) {
    const imageExtensions = /\.(jpg|jpeg|png|gif|webp|bmp)$/;
    if (imageExtensions.test(trimmedContent)) {
      return '[图片]';
    }
  }
  return content;
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
      // 检查 localStorage 中是否有该会话的未读数更新
      try {
        const storageKey = `chat_unread_${conv.otherUserId}`
        const storedValue = localStorage.getItem(storageKey)
        if (storedValue !== null) {
          conv.unreadCount = parseInt(storedValue, 10)
          console.log('从 localStorage 恢复未读数:', conv.otherUserId, conv.unreadCount)
        }
      } catch (e) {
        // 忽略 localStorage 错误
      }
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

    // 监听 App.vue 发送的聊天未读数更新事件
    window.addEventListener('chat-unread-update', handleChatUnreadUpdate);

    // 监听 App.vue 触发的消息更新事件，刷新列表
    const unwatchMessageUpdate = watch(messageUpdateEvent, () => {
      console.log('Messages 页面收到消息更新事件，刷新列表')
      // 先立即更新本地数据
      syncLocalUnreadCounts()
      // 再异步刷新列表
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
  // 监听聊天详情页面的已读标记 - 设置为已读
  if (e.key && e.key.startsWith('unread_read_') && e.newValue === 'read') {
    const userId = e.key.replace('unread_read_', '');
    console.log('收到 storage 事件，设置用户', userId, '的未读数为0');
    // 立即更新本地数据
    updateLocalUnreadCount(userId, 0);
    // 刷新会话列表
    loadConversations();
  }
  // 监听未读数增加事件
  if (e.key && e.key.startsWith('unread_increment_')) {
    const userId = e.key.replace('unread_increment_', '');
    // 获取新的未读数
    const newUnreadCount = parseInt(e.newValue || '0', 10);
    console.log('收到 storage 事件，用户', userId, '的未读数增加为', newUnreadCount);
    // 立即更新本地数据
    updateLocalUnreadCount(userId, newUnreadCount);
    // 刷新会话列表
    loadConversations();
  }
}

// 处理聊天页面清除未读数的事件
function handleUnreadCleared(e: CustomEvent) {
  const { userId } = e.detail;
  console.log('收到 unread-cleared 事件，刷新消息列表', userId);
  // 立即更新本地数据
  updateLocalUnreadCount(userId, 0);
  // 刷新会话列表
  loadConversations();
}

// 立即更新本地会话的未读数
function updateLocalUnreadCount(userId: string | number, unreadCount: number) {
  console.log('updateLocalUnreadCount:', { userId, unreadCount, conversations: conversations.value.map(c => ({ id: c.id, otherUserId: c.otherUserId, unreadCount: c.unreadCount })) });
  // 确保类型一致，使用 == 进行宽松比较
  const conv = conversations.value.find(c => Number(c.otherUserId) === Number(userId));
  console.log('找到的会话:', conv);
  if (conv) {
    conv.unreadCount = unreadCount;
    console.log('已更新未读数:', conv.otherUserId, unreadCount);
  }
}

// 同步本地未读数（兼容旧逻辑）
function syncLocalUnreadCounts() {
  console.log('syncLocalUnreadCounts 被调用，但已改用事件监听');
}

// 监听页面可见性变化，当从聊天页面返回时刷新
function handleVisibilityChange() {
  if (!document.hidden && isLoggedIn.value) {
    // 页面重新可见时刷新会话列表和未读数
    console.log('页面可见，刷新会话列表')
    loadConversations()
  }
}

// 监听 App.vue 发送的聊天未读数更新事件
function handleChatUnreadUpdate(e: CustomEvent) {
  const { userId, unreadCount } = e.detail
  console.log('收到 chat-unread-update 事件:', { userId, unreadCount })
  // 立即更新本地数据
  updateLocalUnreadCount(Number(userId), unreadCount)
}

// 页面卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('unread-cleared', handleUnreadCleared)
  window.removeEventListener('chat-unread-update', handleChatUnreadUpdate)

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

.messages-page--pc {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.messages-page--pc .page-header {
  flex-shrink: 0;
  display: block;
  background: #FFFFFF;
  padding: var(--space-4) var(--space-8);
  border-bottom: 1px solid var(--color-gray-200);
}

.messages-page--pc .page-title {
  font-size: var(--text-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0;
}

/* PC端分栏布局 */
.messages-page--pc .messages-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.messages-page--pc .messages-sidebar {
  width: 500px;
  flex-shrink: 0;
  background: #FFFFFF;
  border-right: 1px solid var(--color-gray-200);
  overflow-y: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.messages-page--pc .messages-sidebar .empty-state {
  text-align: center;
  padding: var(--space-8);
}

.messages-page--pc .messages-detail {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F8FAFC;
}

.messages-page--pc .messages-detail-empty {
  text-align: center;
  color: var(--text-tertiary);
}

.messages-page--pc .messages-detail-empty svg {
  width: 96px;
  height: 96px;
  color: var(--color-gray-300);
  margin-bottom: var(--space-4);
}

.messages-page--pc .messages-detail-empty p {
  font-size: var(--text-lg);
  color: var(--text-secondary);
}

/* 移动端样式增强 */
@media (min-width: 1024px) {
  .messages-page {
    background: #F1F5F9;
    padding-bottom: 0;
    display: flex;
    flex-direction: column;
  }

  .page-header {
    display: none;
  }

  /* 移动端内容隐藏 */
  .mobile-messages {
    display: none;
  }

  /* 分栏容器 */
  .messages-container {
    flex: 1;
    display: flex;
    width: 100%;
    height: calc(100vh - 64px);
    overflow: hidden;
  }

  /* 左侧列表 - 增加宽度 */
  .messages-sidebar {
    width: 480px;
    flex-shrink: 0;
    background: #FFFFFF;
    border-right: 1px solid var(--color-gray-200);
    overflow-y: auto;
    display: flex;
    flex-direction: column;
  }

  .messages-sidebar .responsive-container {
    flex: 1;
    overflow-y: auto;
  }

  .messages-sidebar .messages-list {
    padding: 0;
  }

  .messages-sidebar .message-item {
    padding: var(--space-4) var(--space-5);
    border-radius: 0;
    border-bottom: 1px solid var(--color-gray-100);
    margin-bottom: 0;
    cursor: pointer;
  }

  .messages-sidebar .message-item:hover {
    background-color: var(--color-primary-50);
    transform: none;
    box-shadow: none;
    border-color: var(--color-primary-100);
  }

  .messages-sidebar .message-avatar {
    width: 48px;
    height: 48px;
    font-size: var(--text-lg);
  }

  .messages-sidebar .message-sender {
    font-size: var(--text-base);
  }

  .messages-sidebar .message-preview {
    font-size: var(--text-sm);
  }

  /* 右侧详情 */
  .messages-detail {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #FFFFFF;
  }

  .messages-detail-empty {
    text-align: center;
    color: var(--text-tertiary);
    padding: var(--space-8);
  }

  .messages-detail-empty svg {
    width: 96px;
    height: 96px;
    color: var(--color-gray-200);
    margin-bottom: var(--space-6);
  }

  .messages-detail-empty p {
    font-size: var(--text-lg);
    color: var(--text-secondary);
  }

  /* 左侧空状态 */
  .messages-sidebar .empty-state {
    padding: var(--space-8);
  }

  .messages-sidebar .empty-icon {
    width: 64px;
    height: 64px;
    margin-bottom: var(--space-4);
  }

  .messages-sidebar .empty-icon svg {
    width: 32px;
    height: 32px;
  }

  .messages-sidebar .empty-text {
    font-size: var(--text-base);
    margin-bottom: var(--space-2);
  }
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
  border-bottom: 1px solid var(--border-light);
}

.message-item:last-child {
  border-bottom: none;
}

.message-item:hover {
  background-color: var(--bg-secondary);
}

.message-item:active {
  background-color: var(--bg-tertiary);
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

/* 移动端内容在PC端隐藏 */
.mobile-messages {
  display: block;
}

@media (min-width: 1024px) {
  .mobile-messages {
    display: none;
  }
}
</style>
