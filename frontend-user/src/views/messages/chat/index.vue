<template>
  <div class="chat-detail-page">
    <!-- 状态栏 -->
    <div class="status-bar">
      <span class="back-btn" @click="goBack">‹</span>
      <span class="chat-title">{{ chatName }}</span>
      <span class="more-btn">⋯</span>
    </div>

    <!-- 聊天区域 -->
    <div ref="messagesContainer" class="chat-messages" @scroll="handleScroll">
      <div v-if="messages.length === 0" class="empty-chat">
        <span>暂无消息，开始聊天吧</span>
      </div>

      <div
        v-for="msg in messages"
        :key="msg.id"
        class="message-row"
        :class="{ sent: msg.senderId === currentUserId }"
      >
        <!-- 对方头像：显示真实头像 -->
        <div v-if="msg.senderId !== currentUserId" class="message-avatar">
          <img
            v-if="otherUserAvatar"
            :src="getImageUrl(otherUserAvatar)"
            alt="头像"
            class="avatar-img"
          />
          <span v-else>{{ otherUserNickname.charAt(0) || '用' }}</span>
        </div>

        <!-- 图片消息 -->
        <div
          v-if="isImageMessage(msg)"
          class="message-image-container"
          :class="{ sent: msg.senderId === currentUserId }"
        >
          <img
            v-if="msg.content"
            :src="getImageUrl(msg.content)"
            class="message-image"
            @click="previewMessageImage(msg.content)"
          />
        </div>

        <!-- 文本消息 -->
        <div v-else class="message-bubble" :class="{ sent: msg.senderId === currentUserId }">
          {{ msg.content }}
        </div>

        <!-- 自己头像：不显示 -->
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <!-- 图片按钮 -->
      <button class="image-btn" @click="triggerImageSelect">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
          <circle cx="8.5" cy="8.5" r="1.5" />
          <polyline points="21 15 16 10 5 21" />
        </svg>
      </button>

      <!-- 隐藏的文件输入框 -->
      <input
        ref="imageInput"
        type="file"
        accept="image/*"
        class="hidden-image-input"
        @change="handleImageSelect"
      />

      <input
        v-model="inputMessage"
        type="text"
        class="message-input"
        placeholder="发送消息..."
        @keyup.enter="handleSendMessage"
      />
      <button class="send-btn" @click="handleSendMessage">发送</button>
    </div>

    <!-- 图片预览组件 -->
    <ImagePreview
      v-model:visible="showImagePreview"
      :images="previewImages"
      :initial-index="previewImageIndex"
      @close="showImagePreview = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, inject, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMessagesWithUser } from '@/api/modules/message'
import { getUserPublicInfo } from '@/api/modules/user'
import { clearUnreadCount } from '@/api/modules/conversation'
import { useUserStore } from '@/stores/user'
import { sendMessage as sendStompMessage, onMessage, connect as connectWs, getIsConnected } from '@/services/websocket'
import { uploadImage } from '@/api/modules/upload'
import { getImageUrl } from '@/utils/imageUrl'
import ImagePreview from '@/components/ImagePreview.vue'
import { showToast } from '@/services/toastService'

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

// 用户自己的头像
const myAvatar = ref('')

// 图片相关
const imageInput = ref(null)
const isUploading = ref(false)

// 图片预览
const showImagePreview = ref(false)
const previewImages = ref([])
const previewImageIndex = ref(0)

const messagesContainer = ref(null)
const messages = ref([])
let unsubscribeMessage = null

// 图片预览相关
function previewMessageImage(imageUrl) {
  previewImages.value = [imageUrl]
  previewImageIndex.value = 0
  showImagePreview.value = true
}

// 判断是否是图片消息
function isImageMessage(msg) {
  // 明确指定是图片消息
  if (msg.type === 2) return true
  // 历史消息兼容：根据 URL 判断
  if (msg.content && typeof msg.content === 'string') {
    const lowerContent = msg.content.toLowerCase()
    return (
      lowerContent.startsWith('/uploads/') ||
      lowerContent.startsWith('http://') ||
      lowerContent.startsWith('https://')
    ) && (
      lowerContent.endsWith('.png') ||
      lowerContent.endsWith('.jpg') ||
      lowerContent.endsWith('.jpeg') ||
      lowerContent.endsWith('.gif') ||
      lowerContent.endsWith('.webp')
    )
  }
  return false
}

function triggerImageSelect() {
  imageInput.value?.click()
}

async function handleImageSelect(event) {
  const file = event.target.files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    showToast('请选择图片文件')
    return
  }

  // 验证文件大小 (5MB)
  if (file.size > 5 * 1024 * 1024) {
    showToast('图片大小不能超过5MB')
    return
  }

  // 清空 input，允许重复选择同一文件
  event.target.value = ''

  await sendImageMessage(file)
}

async function sendImageMessage(file) {
  const receiverId = otherUserId.value

  // 确保 WebSocket 已连接
  if (!getIsConnected()) {
    try {
      await connectWs(userStore.token)
    } catch (error) {
      showToast('连接失败，请稍后重试')
      return
    }
  }

  isUploading.value = true

  try {
    // 上传图片
    const result = await uploadImage(file)
    const imageUrl = result.url || result

    // 通过 WebSocket 发送图片消息 (type=2 表示图片消息)
    sendStompMessage(receiverId, imageUrl, 2)

    // 添加到本地显示
    messages.value.push({
      id: Date.now(),
      content: imageUrl,
      type: 2, // 图片消息
      senderId: currentUserId.value,
      receiverId: receiverId,
      createdAt: new Date().toISOString()
    })

    // 滚动到底部
    scrollToBottom()
  } catch (error) {
    console.error('图片上传失败:', error)
    showToast('图片发送失败，请稍后重试')
  } finally {
    isUploading.value = false
  }
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 滚动事件处理（检测是否已滚动到底部）
function handleScroll() {
  if (messagesContainer.value) {
    const { scrollTop, scrollHeight, clientHeight } = messagesContainer.value
    const isAtBottom = scrollHeight - scrollTop - clientHeight < 50
    return isAtBottom
  }
  return false
}

async function loadMessages() {
  const targetUserId = Number(route.params.id)
  otherUserId.value = targetUserId

  // 获取当前用户ID和头像
  currentUserId.value = userStore.userInfo?.id || 0
  myAvatar.value = userStore.userInfo?.avatar || ''

  try {
    // 获取对方用户公开信息
    const userInfo = await getUserPublicInfo(targetUserId)
    if (userInfo && userInfo.nickname) {
      otherUserNickname.value = userInfo.nickname
      chatName.value = userInfo.nickname
      // 设置对方头像
      otherUserAvatar.value = userInfo.avatar || ''
    } else {
      chatName.value = `用户${targetUserId}`
      otherUserAvatar.value = ''
    }
  } catch (error) {
    // 忽略错误
    chatName.value = `用户${targetUserId}`
    otherUserAvatar.value = ''
  }

  try {
    // request.js 已解构返回 data，所以直接使用返回的数组
    const messagesData = await getMessagesWithUser(targetUserId)
    messages.value = Array.isArray(messagesData) ? messagesData : []
    // 消息加载完成后滚动到底部
    scrollToBottom()
  } catch (error) {
    // 忽略错误
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
      // 忽略错误
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

    // 清空输入框并滚动到底部
    inputMessage.value = ''
    scrollToBottom()
  } catch (error) {
    // 忽略错误
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
      // 忽略错误
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

    // 滚动到底部
    scrollToBottom()
  } catch (error) {
    // 忽略错误
    showToast('消息发送失败，请稍后重试')
  }
}

onMounted(async () => {
  await loadMessages()

  // 再次滚动到底部，确保所有内容加载完成后定位
  nextTick(() => {
    scrollToBottom()
  })

  // 通知 App.vue 当前正在与哪个用户聊天（不增加该用户的未读数）
  if (typeof setCurrentChatUserId === 'function') {
    setCurrentChatUserId(otherUserId.value)
  }

  // 进入聊天页面，清空该会话的未读消息（调用API）
  try {
    await clearUnreadCount(otherUserId.value)
  } catch (err) {
    // 忽略错误
  }

  // 触发自定义事件通知消息列表页面更新
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
        type: data.type || 1, // type=2 表示图片消息，默认文本消息 type=1
        senderId: data.senderId,
        receiverId: data.receiverId,
        createdAt: data.createdAt || new Date().toISOString()
      })
      // 收到新消息时滚动到底部
      scrollToBottom()
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
  height: 100vh;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.status-bar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #E2E8F0;
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
  overflow-y: auto;
  padding: 16px;
  -webkit-overflow-scrolling: touch;
}

.empty-chat {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-tertiary);
  font-size: var(--text-sm);
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
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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

.input-area {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #E2E8F0;
}

.message-input {
  flex: 1;
  height: 40px;
  background: #F1F5F9;
  border: none;
  border-radius: 20px;
  padding: 0 16px;
  font-size: 14px;
  outline: none;
}

.message-input:focus {
  background: white;
  box-shadow: 0 0 0 2px #6366F1;
}

.send-btn {
  width: 70px;
  height: 40px;
  background: #6366F1;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: white;
  cursor: pointer;
  flex-shrink: 0;
}

.send-btn:active {
  opacity: 0.8;
}

/* 图片按钮 */
.image-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.image-btn:active {
  opacity: 0.6;
}

.image-btn svg {
  width: 24px;
  height: 24px;
}

/* 隐藏的文件输入 */
.hidden-image-input {
  display: none;
}

/* 图片消息容器 */
.message-image-container {
  max-width: 200px;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.message-image-container.sent {
  border-radius: var(--radius-md);
}

.message-image {
  width: 180px;
  height: auto;
  display: block;
  cursor: pointer;
  object-fit: cover;
}
</style>
