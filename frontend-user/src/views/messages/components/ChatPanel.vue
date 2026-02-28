<template>
  <div class="chat-panel">
    <!-- 聊天头部 -->
    <div class="chat-panel-header">
      <span class="chat-panel-title">{{ userNickname }}</span>
      <button class="chat-panel-close" @click="$emit('close')">×</button>
    </div>

    <!-- 消息列表 -->
    <div ref="messagesContainer" class="chat-panel-messages">
      <div v-if="messages.length === 0" class="chat-panel-empty">
        <span>暂无消息，开始聊天吧</span>
      </div>

      <div
        v-for="msg in messages"
        :key="msg.id"
        class="chat-panel-message"
        :class="{ sent: msg.senderId === currentUserId }"
      >
        <div v-if="msg.senderId !== currentUserId" class="chat-panel-avatar">
          <img
            v-if="props.userAvatar"
            :src="getImageUrl(props.userAvatar)"
            alt="头像"
            class="chat-panel-avatar-img"
          />
          <span v-else>{{ userNickname?.charAt(0) || '用' }}</span>
        </div>

        <div
          v-if="msg.messageType === 2"
          class="chat-panel-image"
          :class="{ sent: msg.senderId === currentUserId }"
        >
          <img :src="getImageUrl(msg.content)" alt="图片" />
        </div>
        <div v-else class="chat-panel-bubble" :class="{ sent: msg.senderId === currentUserId }">
          {{ msg.content }}
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-panel-input">
      <input
        v-model="inputMessage"
        type="text"
        placeholder="发送消息..."
        @keyup.enter="sendMessage"
      />
      <button :disabled="isSending" @click="sendMessage">
        {{ isSending ? '发送中' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMessagesWithUser } from '@/api/modules/message'
import { getImageUrl } from '@/utils/imageUrl'
import { onMessage, sendMessage as sendStompMessage, connect as connectWs, getIsConnected } from '@/services/websocket'

interface Props {
  userId: number
  userNickname?: string
  userAvatar?: string
}

const props = defineProps<Props>()
const emit = defineEmits(['close', 'message-sent'])

const userStore = useUserStore()
const currentUserId = userStore.userInfo?.id

const messages = ref<any[]>([])
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const loading = ref(false)
const isSending = ref(false)

let unsubscribeMessage: (() => void) | null = null

async function loadMessages() {
  if (!props.userId) return

  loading.value = true
  try {
    const data = await getMessagesWithUser(props.userId)
    messages.value = Array.isArray(data) ? data : (data?.list || [])
    scrollToBottom()
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loading.value = false
  }
}

async function sendMessage() {
  if (!inputMessage.value.trim() || !props.userId || isSending.value) return

  isSending.value = true
  const content = inputMessage.value.trim()

  // 确保 WebSocket 已连接
  if (!getIsConnected()) {
    try {
      await connectWs(userStore.token)
    } catch (error) {
      console.error('WebSocket 连接失败')
      isSending.value = false
      return
    }
  }

  try {
    // 使用 WebSocket 发送消息
    sendStompMessage(props.userId, content, 1)

    // 添加到本地显示
    messages.value.push({
      id: Date.now(),
      content: content,
      type: 1,
      senderId: currentUserId,
      receiverId: props.userId,
      createdAt: new Date().toISOString()
    })

    inputMessage.value = ''
    scrollToBottom()
    emit('message-sent')
  } catch (error) {
    console.error('发送消息失败:', error)
  } finally {
    isSending.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 订阅 WebSocket 消息
function setupWebSocket() {
  // 订阅新消息
  unsubscribeMessage = onMessage((data) => {
    // 只处理当前聊天对象发来的消息
    if (data.senderId === props.userId) {
      messages.value.push({
        id: data.id || Date.now(),
        content: data.content,
        type: data.type || 1,
        senderId: data.senderId,
        receiverId: data.receiverId,
        createdAt: data.createdAt || new Date().toISOString()
      })
      scrollToBottom()
    }
  })
}

watch(() => props.userId, () => {
  // 切换会话时：先取消旧订阅，再建立新订阅
  if (unsubscribeMessage) {
    unsubscribeMessage()
    unsubscribeMessage = null
  }
  loadMessages()
  setupWebSocket()
})

onMounted(() => {
  loadMessages()
  setupWebSocket()
})

onUnmounted(() => {
  if (unsubscribeMessage) {
    unsubscribeMessage()
    unsubscribeMessage = null
  }
})
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #fff;
}

.chat-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.chat-panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.chat-panel-close {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  font-size: 24px;
  color: #6b7280;
  cursor: pointer;
  border-radius: 4px;
}

.chat-panel-close:hover {
  background: #f3f4f6;
}

.chat-panel-messages {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

.chat-panel-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9ca3af;
}

.chat-panel-message {
  display: flex;
  align-items: flex-start;
  margin: 12px 16px;
}

.chat-panel-message.sent {
  flex-direction: row-reverse;
}

.chat-panel-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e0e7ff;
  color: #4f46e5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
  overflow: hidden;
}

.chat-panel-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.chat-panel-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  background: #f3f4f6;
  color: #1f2937;
  margin-left: 12px;
  word-break: break-word;
}

.chat-panel-message.sent .chat-panel-bubble {
  background: #4f46e5;
  color: #fff;
  margin-left: 0;
  margin-right: 12px;
}

.chat-panel-image {
  max-width: 60%;
  margin-left: 12px;
  border-radius: 8px;
  overflow: hidden;
}

.chat-panel-image.sent {
  margin-left: 0;
  margin-right: 12px;
}

.chat-panel-image img {
  width: 100%;
  display: block;
}

.chat-panel-input {
  display: flex;
  padding: 16px 20px;
  border-top: 1px solid #e5e7eb;
  gap: 12px;
}

.chat-panel-input input {
  flex: 1;
  height: 40px;
  padding: 0 16px;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  outline: none;
  font-size: 14px;
}

.chat-panel-input input:focus {
  border-color: #4f46e5;
}

.chat-panel-input button {
  height: 40px;
  padding: 0 20px;
  background: #4f46e5;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

.chat-panel-input button:hover {
  background: #4338ca;
}
</style>
