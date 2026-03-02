<template>
  <div class="chat-panel">
    <!-- 聊天头部 -->
    <div class="chat-panel-header">
      <span class="chat-panel-title">
        <span class="chat-panel-back" @click="$emit('close')">‹</span>
        {{ userNickname }}
      </span>
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

        <!-- 图片消息 -->
        <div
          v-if="msg.type === 2"
          class="chat-panel-image"
          :class="{ sent: msg.senderId === currentUserId }"
          @click="openImagePreview(messages.indexOf(msg))"
        >
          <img :src="getImageUrl(msg.content)" alt="图片" />
        </div>
        <!-- 商品卡片消息 -->
        <div
          v-else-if="msg.type === 3"
          class="chat-panel-item-card"
          :class="{ sent: msg.senderId === currentUserId }"
          @click="msg.itemId && goToItemDetail(msg.itemId)"
        >
          <div class="item-card-image">
            <img v-if="msg.itemImage" :src="msg.itemImage" :alt="msg.itemTitle" />
            <div v-else class="item-card-placeholder">
              <span class="item-card-placeholder-text">物品介绍</span>
            </div>
            <span v-if="msg.itemType" class="item-card-type" :class="{ 'item-card-type--buy': msg.itemType === 1 }">
              {{ msg.itemType === 1 ? '求购' : '出售' }}
            </span>
          </div>
          <div class="item-card-content">
            <!-- 用户信息 -->
            <div class="item-card-user" v-if="msg.itemUserNickname">
              <div class="item-card-user-avatar">
                <span>{{ (msg.itemUserNickname || '匿名').charAt(0) }}</span>
              </div>
              <span class="item-card-user-name">{{ msg.itemUserNickname }}</span>
            </div>
            <!-- 商品标题 -->
            <h3 class="item-card-title">{{ msg.itemTitle }}</h3>
            <!-- 价格和时间 -->
            <div class="item-card-footer">
              <span class="item-card-price">¥{{ msg.itemPrice }}</span>
              <span class="item-card-time">{{ formatTime(msg.createdAt) }}</span>
            </div>
          </div>
        </div>
        <!-- 文本消息 -->
        <div v-else class="chat-panel-bubble" :class="{ sent: msg.senderId === currentUserId }">
          {{ msg.content }}
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-panel-input">
      <button class="chat-panel-add-btn" @click="showItemSelector = true" title="发送收藏的闲置">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 5v14M5 12h14" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <input type="file" ref="imageInput" accept="image/*" style="display: none" @change="handleImageSelect" />
      <button class="chat-panel-add-btn" @click="triggerImageUpload" title="发送图片">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
          <circle cx="8.5" cy="8.5" r="1.5" />
          <polyline points="21,15 16,10 5,21" />
        </svg>
      </button>
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

    <!-- 闲置商品选择器弹窗 -->
    <div v-if="showItemSelector" class="item-selector-overlay" @click="showItemSelector = false">
      <div class="item-selector" @click.stop>
        <div class="item-selector-header">
          <h3>选择要发送的闲置商品</h3>
          <button class="item-selector-close" @click="showItemSelector = false">×</button>
        </div>
        <div class="item-selector-content">
          <div v-if="loadingItems" class="item-selector-loading">
            <div class="loading-spinner"></div>
            <span>加载中...</span>
          </div>
          <div v-else-if="collectedItems.length === 0" class="item-selector-empty">
            <div class="empty-icon">🛍️</div>
            <p>暂无收藏的闲置物品</p>
          </div>
          <div v-else class="item-selector-list">
            <div
              v-for="item in collectedItems"
              :key="item.id"
              class="item-selector-item"
              @click="selectItem(item)"
            >
              <div class="item-selector-item-image">
                <img v-if="item.image" :src="item.image" :alt="item.title" />
                <div v-else class="item-card-placeholder">
                  <span class="item-card-placeholder-text">物品介绍</span>
                </div>
                <span v-if="item.type" class="item-card-type" :class="{ 'item-card-type--buy': item.type === 1 }">
                  {{ item.type === 1 ? '求购' : '出售' }}
                </span>
              </div>
              <div class="item-selector-item-content">
                <!-- 用户信息 -->
                <div class="item-selector-item-user" v-if="item.userNickname">
                  <div class="item-selector-item-user-avatar">
                    <span>{{ (item.userNickname || '匿名').charAt(0) }}</span>
                  </div>
                  <span class="item-selector-item-user-name">{{ item.userNickname }}</span>
                </div>
                <!-- 商品标题 -->
                <h4 class="item-selector-item-title">{{ item.title }}</h4>
                <!-- 价格和时间 -->
                <div class="item-selector-item-footer">
                  <span class="item-selector-item-price">¥{{ item.price }}</span>
                  <span class="item-selector-item-time">{{ formatTime(item.createdAt || item.updatedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片预览组件 -->
    <ImagePreview
      v-model:visible="showImagePreview"
      :images="[messages[previewImageIndex]?.content]"
      :initial-index="0"
      @close="showImagePreview = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMessagesWithUser } from '@/api/modules/message'
import { getCollectedItems } from '@/api/modules/itemCollect'
import { getImageUrl } from '@/utils/imageUrl'
import { uploadImage } from '@/api/modules/upload'
import ImagePreview from '@/components/ImagePreview.vue'
import { onMessage, sendMessage as sendStompMessage, connect as connectWs, getIsConnected } from '@/services/websocket'

interface Props {
  userId: number
  userNickname?: string
  userAvatar?: string
}

interface CollectedItem {
  id: number
  title: string
  price: number
  type: number
  images: string | null
  image?: string
  userId?: number
  userNickname?: string
  userAvatar?: string
  createdAt?: string
  updatedAt?: string
}

const props = defineProps<Props>()
const emit = defineEmits(['close', 'message-sent', 'message-updated'])

const router = useRouter()
const userStore = useUserStore()
const currentUserId = userStore.userInfo?.id

const messages = ref<any[]>([])
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const imageInput = ref<HTMLInputElement | null>(null)
const loading = ref(false)
const isSending = ref(false)
const showItemSelector = ref(false)
const showImagePreview = ref(false)
const previewImageIndex = ref(0)
const collectedItems = ref<CollectedItem[]>([])
const loadingItems = ref(false)

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

async function sendMessage(content?: string, type: number = 1, itemId?: number) {
  const messageContent = content || inputMessage.value.trim()
  if (!messageContent || !props.userId || isSending.value) return

  isSending.value = true

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
    if (type === 3 && itemId) {
      // 发送商品卡片消息
      sendStompMessage(props.userId, messageContent, type, itemId)
      
      // 添加到本地显示
      const selectedItem = collectedItems.value.find(item => item.id === itemId)
      messages.value.push({
        id: Date.now(),
        content: messageContent,
        type: 3,
        itemId: itemId,
        itemTitle: selectedItem?.title,
        itemPrice: selectedItem?.price,
        itemImage: selectedItem?.image,
        itemType: selectedItem?.type,
        itemUserNickname: selectedItem?.userNickname,
        itemUserAvatar: selectedItem?.userAvatar,
        senderId: currentUserId,
        receiverId: props.userId,
        createdAt: new Date().toISOString()
      })
    } else {
      // 发送文本消息
      sendStompMessage(props.userId, messageContent, 1)
      
      messages.value.push({
        id: Date.now(),
        content: messageContent,
        type: 1,
        senderId: currentUserId,
        receiverId: props.userId,
        createdAt: new Date().toISOString()
      })
    }

    if (!content) {
      inputMessage.value = ''
    }
    scrollToBottom()
    emit('message-sent')
    emit('message-updated')
  } catch (error) {
    console.error('发送消息失败:', error)
  } finally {
    isSending.value = false
  }
}

async function loadCollectedItems() {
  if (loadingItems.value || collectedItems.value.length > 0) return
  
  loadingItems.value = true
  try {
    const result = await getCollectedItems()
    collectedItems.value = (result || []).map(transformItem)
  } catch (error) {
    console.error('获取收藏物品失败:', error)
  } finally {
    loadingItems.value = false
  }
}

function transformItem(item: any): CollectedItem {
  let image = ''
  if (item.images) {
    try {
      const images = JSON.parse(item.images)
      image = Array.isArray(images) && images.length > 0 ? getImageUrl(images[0]) : ''
    } catch {
      image = getImageUrl(item.images)
    }
  }

  return {
    ...item,
    image,
  }
}

function formatTime(dateString: string): string {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

function selectItem(item: CollectedItem) {
  sendMessage(item.title, 3, item.id)
  showItemSelector.value = false
}

function openImagePreview(index: number) {
  previewImageIndex.value = index
  showImagePreview.value = true
}

function goToItemDetail(itemId: number) {
  router.push(`/trade/${itemId}`)
}

function triggerImageUpload() {
  imageInput.value?.click()
}

async function handleImageSelect(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || !props.userId) return

  isSending.value = true

  try {
    const imageUrl = await uploadImage(file)

    // 确保 WebSocket 已连接
    if (!getIsConnected()) {
      await connectWs(userStore.token)
    }

    // 发送图片消息 (type = 2)
    sendStompMessage(props.userId, imageUrl, 2)

    // 添加到本地显示
    messages.value.push({
      id: Date.now(),
      content: imageUrl,
      type: 2,
      senderId: currentUserId,
      receiverId: props.userId,
      createdAt: new Date().toISOString()
    })

    scrollToBottom()
  } catch (error) {
    console.error('上传图片失败:', error)
  } finally {
    isSending.value = false
    input.value = ''
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
      const message: any = {
        id: data.id || Date.now(),
        content: data.content,
        type: data.type || 1,
        senderId: data.senderId,
        receiverId: data.receiverId,
        createdAt: data.createdAt || new Date().toISOString()
      }
      
      // 如果是商品卡片消息，填充商品信息
      if (data.type === 3 && data.itemId) {
        message.itemId = data.itemId
        message.itemTitle = data.itemTitle
        message.itemPrice = data.itemPrice
        message.itemImage = data.itemImage
        message.itemType = data.itemType
        message.itemUserNickname = data.itemUserNickname
        message.itemUserAvatar = data.itemUserAvatar
      }
      
      messages.value.push(message)
      scrollToBottom()
      emit('message-updated')
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
  loadCollectedItems()
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
  justify-content: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  position: relative;
}

.chat-panel-title {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.chat-panel-back {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #1f2937;
  cursor: pointer;
  margin-right: 8px;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  position: absolute;
  left: 20px;
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

.chat-panel-input button.chat-panel-add-btn {
  width: 40px;
  padding: 0;
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #6b7280;
}

.chat-panel-input button.chat-panel-add-btn svg {
  width: 20px;
  height: 20px;
}

.chat-panel-input button.chat-panel-add-btn:hover {
  background: #f9fafb;
  color: #4f46e5;
  border-color: #4f46e5;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.1);
}

.chat-panel-input button.chat-panel-add-btn:active {
  transform: translateY(0);
}

/* 商品卡片消息样式 */
.chat-panel-item-card {
  width: 50%;
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
}

.chat-panel-item-card:active {
  transform: scale(0.98);
}

.chat-panel-item-card.sent {
  background: #e0e7ff;
}

.item-card-image {
  position: relative;
  width: 100%;
  aspect-ratio: 4/3;
  background-color: var(--bg-tertiary);
  overflow: hidden;
}

.item-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-card-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fdf8f0;
  position: relative;
}

.item-card-placeholder::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.08'/%3E%3C/svg%3E");
  pointer-events: none;
}

.item-card-placeholder-text {
  font-size: 12px;
  font-weight: bold;
  color: #c45c5c;
  letter-spacing: 1px;
  text-transform: uppercase;
  font-family: 'Georgia', serif;
  transform: rotate(-4deg);
  border: 2px dashed #c45c5c;
  padding: 4px 8px;
  border-radius: 4px;
}

.item-card-type {
  position: absolute;
  top: var(--space-2);
  left: var(--space-2);
  padding: 4px var(--space-2);
  font-size: 10px;
  font-weight: var(--font-weight-semibold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background-color: var(--color-primary-700);
  color: white;
  border-radius: var(--radius-sm);
}

.item-card-type--buy {
  background-color: var(--color-success-600);
}

.item-card-content {
  padding: 12px;
}

.item-card-title {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
  line-height: var(--line-height-normal);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 商品卡片用户信息 */
.item-card-user {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.item-card-user-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.item-card-user-name {
  font-size: 11px;
  color: #6b7280;
  font-weight: 500;
}

.item-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--space-2);
  padding-top: var(--space-2);
  border-top: 1px solid var(--border-light);
}

.item-card-price {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-error-600);
}

.item-card-time {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

/* 闲置商品选择器弹窗样式 */
.item-selector-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.item-selector {
  width: 100%;
  max-width: 500px;
  max-height: 80vh;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.item-selector-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.item-selector-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.item-selector-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.item-selector-close:hover {
  background: #f3f4f6;
}

.item-selector-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.item-selector-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #9ca3af;
}

.item-selector-loading .loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e5e7eb;
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.item-selector-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.item-selector-empty .empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.item-selector-empty p {
  margin: 0;
  font-size: 14px;
}

.item-selector-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.item-selector-item {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
}

.item-selector-item:active {
  transform: scale(0.98);
  border-color: #4f46e5;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.1);
}

.item-selector-item-image {
  position: relative;
  width: 100%;
  aspect-ratio: 4/3;
  background: #f9fafb;
  overflow: hidden;
}

.item-selector-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-selector-item-content {
  padding: 10px;
}

/* 商品选择器 - 用户信息 */
.item-selector-item-user {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.item-selector-item-user-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.item-selector-item-user-name {
  font-size: 11px;
  color: #6b7280;
  font-weight: 500;
}

.item-selector-item-title {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
  margin: 0 0 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.3;
}

.item-selector-item-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid #f3f4f6;
}

.item-selector-item-price {
  font-size: 16px;
  font-weight: 700;
  color: #ef4444;
}

.item-selector-item-time {
  font-size: 11px;
  color: #9ca3af;
}

@media (min-width: 768px) {
  .item-selector-list {
    grid-template-columns: repeat(3, 1fr);
  }

  .chat-panel-back {
    display: none;
  }
}
</style>
