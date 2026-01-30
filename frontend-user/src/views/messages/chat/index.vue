<template>
  <div class="chat-detail-page">
    <!-- 状态栏 -->
    <div class="status-bar">
      <span class="back-btn" @click="goBack">‹</span>
      <span class="chat-title">{{ chatName }}</span>
      <span class="more-btn">⋯</span>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-messages">
      <div v-if="messages.length === 0" class="empty-chat">
        <span>暂无消息，开始聊天吧</span>
      </div>

      <div
        v-for="msg in messages"
        :key="msg.id"
        class="message-row"
        :class="{ sent: msg.senderId === currentUserId }"
      >
        <div v-if="msg.senderId !== currentUserId" class="message-avatar">
          <span>{{ otherUserAvatar || '用' }}</span>
        </div>

        <div class="message-bubble" :class="{ sent: msg.senderId === currentUserId }">
          {{ msg.content }}
        </div>

        <div v-if="msg.senderId === currentUserId" class="message-avatar my-avatar">
          <span>我</span>
        </div>
      </div>
    </div>

    <!-- 快捷回复 -->
    <div class="quick-replies">
      <button
        v-for="reply in quickReplies"
        :key="reply"
        class="quick-btn"
        @click="sendQuickReply(reply)"
      >
        {{ reply }}
      </button>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <input
        v-model="inputMessage"
        type="text"
        class="message-input"
        placeholder="发送消息..."
        @keyup.enter="handleSendMessage"
      />
      <button class="emoji-btn">😊</button>
      <button class="image-btn">🖼️</button>
      <button class="send-btn" @click="handleSendMessage">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMessagesWithUser } from '@/api/modules/message'
import { getUserPublicInfo } from '@/api/modules/user'
import { clearUnreadCount } from '@/api/modules/conversation'
import { useUserStore } from '@/stores/user'
import { sendMessage as sendStompMessage, onMessage, connect as connectWs, getIsConnected } from '@/services/websocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 注入 App.vue 提供的方法
const setCurrentChatUserId = inject('setCurrentChatUserId')

const chatName = ref('')
const inputMessage = ref('')
const otherUserAvatar = ref('')
const otherUserNickname = ref('')
const otherUserId = ref(0)
const currentUserId = ref(0)

const messages = ref([])
let unsubscribeMessage = null

const quickReplies = ['好的', '多少钱？', '在吗？', '可以便宜吗']

async function loadMessages() {
  const targetUserId = Number(route.params.id)
  otherUserId.value = targetUserId
  otherUserAvatar.value = targetUserId.toString().slice(-1)

  // 获取当前用户ID
  currentUserId.value = userStore.userInfo?.id || 0

  try {
    // 获取对方用户公开信息
    const userInfo = await getUserPublicInfo(targetUserId)
    if (userInfo && userInfo.nickname) {
      otherUserNickname.value = userInfo.nickname
      chatName.value = userInfo.nickname
    } else {
      chatName.value = `用户${targetUserId}`
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    chatName.value = `用户${targetUserId}`
  }

  try {
    // request.js 已解构返回 data，所以直接使用返回的数组
    const messagesData = await getMessagesWithUser(targetUserId)
    messages.value = Array.isArray(messagesData) ? messagesData : []
  } catch (error) {
    console.error('获取消息失败:', error)
    messages.value = []
  }
}

function goBack() {
  router.back()
}

async function handleSendMessage() {
  if (!inputMessage.value.trim()) return

  const content = inputMessage.value.trim()
  const receiverId = otherUserId.value

  // 确保 WebSocket 已连接
  if (!getIsConnected()) {
    try {
      await connectWs(userStore.token)
    } catch (error) {
      console.error('WebSocket 连接失败:', error)
      showToast('消息发送失败，请稍后重试')
      return
    }
  }

  // 通过 WebSocket 发送消息
  try {
    sendStompMessage(receiverId, content)

    // 添加到本地显示
    messages.value.push({
      id: Date.now(),
      content: content,
      senderId: currentUserId.value,
      receiverId: receiverId,
      createdAt: new Date().toISOString()
    })

    // 清空输入框
    inputMessage.value = ''
  } catch (error) {
    console.error('发送消息失败:', error)
    showToast('消息发送失败，请稍后重试')
  }
}

async function sendQuickReply(reply) {
  const receiverId = otherUserId.value

  // 确保 WebSocket 已连接
  if (!getIsConnected()) {
    try {
      await connectWs(userStore.token)
    } catch (error) {
      console.error('WebSocket 连接失败:', error)
      showToast('消息发送失败，请稍后重试')
      return
    }
  }

  // 通过 WebSocket 发送消息
  try {
    sendStompMessage(receiverId, reply)

    // 添加到本地显示
    messages.value.push({
      id: Date.now(),
      content: reply,
      senderId: currentUserId.value,
      receiverId: receiverId,
      createdAt: new Date().toISOString()
    })
  } catch (error) {
    console.error('发送消息失败:', error)
    showToast('消息发送失败，请稍后重试')
  }
}

onMounted(async () => {
  await loadMessages()

  // 通知 App.vue 当前正在与哪个用户聊天（不增加该用户的未读数）
  if (typeof setCurrentChatUserId === 'function') {
    setCurrentChatUserId(otherUserId.value)
  }

  // 进入聊天页面，清空该会话的未读消息（调用API）
  console.log('开始清除未读数, otherUserId:', otherUserId.value)
  try {
    await clearUnreadCount(otherUserId.value)
    console.log('清除未读数 API 调用完成')
  } catch (err) {
    console.error('清除未读数失败:', err)
  }

  // 触发自定义事件通知消息列表页面更新
  console.log('触发 unread-cleared 事件')
  window.dispatchEvent(new CustomEvent('unread-cleared', {
    detail: { userId: otherUserId.value }
  }))

  // 订阅新消息（WebSocket 连接由 App.vue 统一管理）
  unsubscribeMessage = onMessage((data) => {
    // 只显示接收到的、来自当前聊天对象的消息
    if (data.senderId === otherUserId.value) {
      messages.value.push({
        id: data.id || Date.now(),
        content: data.content,
        senderId: data.senderId,
        receiverId: data.receiverId,
        createdAt: data.createdAt || new Date().toISOString()
      })
    }
  })
})

onUnmounted(() => {
  // 取消订阅（不断开 WebSocket，由 App.vue 统一管理）
  if (unsubscribeMessage) {
    unsubscribeMessage()
    unsubscribeMessage = null
  }

  // 通知 App.vue 离开了聊天页面
  if (typeof setCurrentChatUserId === 'function') {
    setCurrentChatUserId(null)
  }
})
</script>

<style scoped>
.chat-detail-page {
  min-height: 100vh;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
  padding-top: 44px;
}

.status-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
  z-index: 10;
}

.back-btn {
  font-size: 28px;
  color: #1E293B;
  cursor: pointer;
}

.chat-title {
  font-size: 17px;
  font-weight: 600;
  color: #1E293B;
}

.more-btn {
  font-size: 24px;
  color: #1E293B;
  cursor: pointer;
}

.chat-messages {
  flex: 1;
  padding: 60px 16px 16px; /* 顶部留出空间避免被状态栏遮挡 */
  overflow-y: auto;
}

.empty-chat {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-tertiary);
  font-size: var(--text-sm);
}

.time-divider {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.time-divider span {
  font-size: 12px;
  color: #94A3B8;
  background: white;
  padding: 4px 12px;
  border-radius: 12px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 12px;
}

.message-row.sent {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #64748B;
  flex-shrink: 0;
}

.message-avatar.my-avatar {
  background: #818CF8;
  color: white;
}

.message-bubble {
  max-width: 240px;
  padding: 12px 16px;
  background: white;
  border-radius: 16px;
  font-size: 15px;
  color: #1E293B;
  line-height: 1.5;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.message-bubble.sent {
  background: #6366F1;
  color: white;
}

.quick-replies {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: white;
  overflow-x: auto;
}

.quick-btn {
  padding: 8px 16px;
  background: #F1F5F9;
  border: none;
  border-radius: 16px;
  font-size: 13px;
  color: #64748B;
  cursor: pointer;
  white-space: nowrap;
}

.input-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #E2E8F0;
}

.message-input {
  flex: 1;
  height: 36px;
  background: #F1F5F9;
  border: none;
  border-radius: 18px;
  padding: 0 16px;
  font-size: 14px;
  outline: none;
}

.message-input:focus {
  background: white;
  box-shadow: 0 0 0 2px #6366F1;
}

.emoji-btn,
.image-btn {
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
}

.send-btn {
  width: 60px;
  height: 36px;
  background: #6366F1;
  border: none;
  border-radius: 18px;
  font-size: 14px;
  color: white;
  cursor: pointer;
}
</style>
