<template>
  <router-view />

  <!-- 全局登录确认对话框 -->
  <Teleport to="body">
    <Dialog
      v-model:visible="loginDialogVisible"
      title="提示"
      message="该操作需要登录，是否前往登录？"
      theme="warning"
      confirmText="去登录"
      cancelText="取消"
      @confirm="handleLogin"
      @cancel="handleCancel"
    />
  </Teleport>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch, provide } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Dialog from '@/components/interactive/Dialog.vue'
import { getLoginDialogVisible, hideLoginConfirm } from '@/stores/loginConfirm'
import { connect, disconnect, onMessage, onNotification } from '@/services/websocket'
import { getTotalUnreadCount } from '@/api/modules/conversation'
import { getUnreadCount } from '@/api/modules/notification'

const router = useRouter()
const userStore = useUserStore()

const loginDialogVisible = ref(false)

// 全局未读消息数
const totalUnreadCount = ref(0)
// 全局未读通知数
const totalNotificationUnreadCount = ref(0)
// 已处理的消息 ID 集合，用于去重
const processedMessageIds = new Set<number>()
// 已处理的通知 ID 集合，用于去重
const processedNotificationIds = new Set<number>()
// 消息更新事件总线
const messageUpdateEvent = ref(0)
// 通知更新事件总线
const notificationUpdateEvent = ref(0)
// 当前正在聊天的对方用户 ID（如果在聊天详情页）
const currentChatUserId = ref<number | null>(null)

let unsubscribeMessage: (() => void) | null = null
let unsubscribeNotification: (() => void) | null = null

// 提供全局未读数给子组件
provide('totalUnreadCount', totalUnreadCount)

// 提供全局通知未读数给子组件
provide('totalNotificationUnreadCount', totalNotificationUnreadCount)

// 提供通知更新事件给子组件
provide('notificationUpdateEvent', notificationUpdateEvent)

// 提供消息更新事件给子组件（用于触发列表刷新）
provide('messageUpdateEvent', messageUpdateEvent)

// 提供设置当前聊天用户 ID 的方法
provide('setCurrentChatUserId', (userId: number | null) => {
  console.log('App.vue 设置当前聊天用户 ID:', userId)
  currentChatUserId.value = userId
})

// 提供更新会话未读数的方法（用于同步更新消息列表）
const updateConversationUnread = (userId: number, unreadCount: number) => {
  console.log('App.vue 更新会话未读数:', userId, unreadCount)
  // 派发全局自定义事件，同步更新消息列表
  window.dispatchEvent(new CustomEvent('chat-unread-update', {
    detail: { userId, unreadCount }
  }))
  // 同时存储到 localStorage，供消息列表页面加载时读取
  try {
    const storageKey = `chat_unread_${userId}`
    localStorage.setItem(storageKey, String(unreadCount))
  } catch (e) {
    // 忽略 localStorage 错误
  }
}

// 监听全局聊天未读数更新事件（用于同步更新 localStorage）
window.addEventListener('chat-unread-update', (e: CustomEvent) => {
  const { userId, unreadCount } = e.detail
  try {
    const storageKey = `chat_unread_${userId}`
    localStorage.setItem(storageKey, String(unreadCount))
  } catch (err) {
    // 忽略 localStorage 错误
  }
})

// 挂载到 window 上供 onMessage 回调中使用
;(window as any).__updateConversationUnread = updateConversationUnread

provide('updateConversationUnread', updateConversationUnread)

// 应用启动时初始化用户状态
onMounted(async () => {
  await userStore.initialize()

  // 如果已经有 token，建立连接
  if (userStore.token) {
    await fetchTotalUnreadCount()
    await fetchNotificationUnreadCount()
    await setupWebSocket()
  }

  // 监听聊天页面触发的未读清除事件
  window.addEventListener('unread-cleared', handleUnreadCleared)
})

// 应用卸载时清理
onUnmounted(() => {
  teardownWebSocket()
  window.removeEventListener('unread-cleared', handleUnreadCleared)
})

function handleLogin() {
  hideLoginConfirm()
  router.push('/login')
}

function handleCancel() {
  hideLoginConfirm()
}

// 处理未读清除事件
function handleUnreadCleared(e: CustomEvent) {
  console.log('App.vue 收到 unread-cleared 事件，触发消息列表刷新')
  // 触发消息更新事件，让消息列表刷新
  messageUpdateEvent.value++
}

// 获取总未读消息数
async function fetchTotalUnreadCount() {
  if (!userStore.token) {
    totalUnreadCount.value = 0
    return
  }

  try {
    const count = await getTotalUnreadCount()
    totalUnreadCount.value = count || 0
  } catch (error) {
    console.error('获取总未读消息数失败:', error)
    totalUnreadCount.value = 0
  }
}

// 获取通知未读数
async function fetchNotificationUnreadCount() {
  if (!userStore.token) {
    totalNotificationUnreadCount.value = 0
    return
  }

  try {
    const count = await getUnreadCount()
    totalNotificationUnreadCount.value = count || 0
  } catch (error) {
    console.error('获取通知未读数失败:', error)
    totalNotificationUnreadCount.value = 0
  }
}

// 建立 WebSocket 连接
async function setupWebSocket() {
  if (!userStore.token) return

  // 如果已经订阅了消息，不再重复订阅
  if (unsubscribeMessage) {
    return
  }

  try {
    await connect(userStore.token)

    // 订阅新消息，实时更新未读数（每个消息只处理一次）
    unsubscribeMessage = onMessage((data) => {
      console.log('App.vue 收到新消息:', data)

      // 使用消息 ID 去重，如果没有 ID 则使用时间戳
      const messageId = data.id || new Date(data.createdAt || Date.now()).getTime()

      if (processedMessageIds.has(messageId)) {
        console.log('消息已处理过，忽略:', messageId)
        return
      }

      // 标记消息为已处理
      processedMessageIds.add(messageId)

      // 如果正在与发送者聊天，不增加未读数（因为已经在聊天详情页查看了）
      const senderId = data.senderId
      console.log('收到消息:', { senderId, currentChatUserId: currentChatUserId.value, isChatting: currentChatUserId.value === senderId })
      if (currentChatUserId.value === senderId) {
        console.log('正在与发送者聊天，不增加未读数')
        // 仍然触发消息更新事件，让聊天页面可以刷新消息列表
        messageUpdateEvent.value++
        // 通过 provide 方法同步更新消息列表页面的会话未读数为0
        // 注意：我们需要在注入 context7 的环境中调用 updateConversationUnread
        // 这里直接调用 provide 的方法
        const updateFn = (window as any).__updateConversationUnread
        if (updateFn) {
          console.log('调用 updateConversationUnread:', senderId, 0)
          updateFn(senderId, 0)
        }
      } else {
        // 收到新消息时增加未读数
        totalUnreadCount.value = (totalUnreadCount.value || 0) + 1
        console.log('未读数增加，当前:', totalUnreadCount.value)
        // 触发消息更新事件，让消息列表刷新
        messageUpdateEvent.value++
      }

      // 限制 Set 大小，防止内存泄漏
      if (processedMessageIds.size > 1000) {
        const first = processedMessageIds.values().next().value
        processedMessageIds.delete(first)
      }
    })

    // 订阅新通知，实时更新通知未读数
    unsubscribeNotification = onNotification((data) => {
      console.log('App.vue 收到新通知:', data)

      // 使用通知 ID 去重
      const notificationId = data.id || new Date(data.createdAt || Date.now()).getTime()

      if (processedNotificationIds.has(notificationId)) {
        console.log('通知已处理过，忽略:', notificationId)
        return
      }

      // 标记通知为已处理
      processedNotificationIds.add(notificationId)

      // 增加通知未读数
      totalNotificationUnreadCount.value = (totalNotificationUnreadCount.value || 0) + 1
      console.log('通知未读数增加，当前:', totalNotificationUnreadCount.value)

      // 触发通知更新事件，让通知列表刷新
      notificationUpdateEvent.value++

      // 限制 Set 大小，防止内存泄漏
      if (processedNotificationIds.size > 1000) {
        const first = processedNotificationIds.values().next().value
        processedNotificationIds.delete(first)
      }
    })

    console.log('App.vue WebSocket 连接成功')
  } catch (error) {
    console.error('App.vue WebSocket 连接失败:', error)
  }
}

// 清理 WebSocket
function teardownWebSocket() {
  if (unsubscribeMessage) {
    unsubscribeMessage()
    unsubscribeMessage = null
  }
  if (unsubscribeNotification) {
    unsubscribeNotification()
    unsubscribeNotification = null
  }
  disconnect()
}

// 监听登录确认对话框状态变化
watch(getLoginDialogVisible, (visible) => {
  loginDialogVisible.value = visible
})

// 监听登录状态变化
watch(() => userStore.token, async (newToken) => {
  if (newToken) {
    // 登录后获取未读数并建立 WebSocket 连接
    await fetchTotalUnreadCount()
    await fetchNotificationUnreadCount()
    await setupWebSocket()
  } else {
    // 登出时清空未读数和已处理消息 ID 并断开连接
    totalUnreadCount.value = 0
    totalNotificationUnreadCount.value = 0
    processedMessageIds.clear()
    processedNotificationIds.clear()
    teardownWebSocket()
  }
}, { immediate: true })
</script>

<style scoped>
</style>
