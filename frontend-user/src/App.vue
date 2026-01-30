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
import { connect, disconnect, onMessage } from '@/services/websocket'
import { getTotalUnreadCount } from '@/api/modules/conversation'

const router = useRouter()
const userStore = useUserStore()

const loginDialogVisible = ref(false)

// 全局未读消息数
const totalUnreadCount = ref(0)
// 已处理的消息 ID 集合，用于去重
const processedMessageIds = new Set<number>()
// 消息更新事件总线
const messageUpdateEvent = ref(0)
// 当前正在聊天的对方用户 ID（如果在聊天详情页）
const currentChatUserId = ref<number | null>(null)

let unsubscribeMessage: (() => void) | null = null

// 提供全局未读数给子组件
provide('totalUnreadCount', totalUnreadCount)

// 提供消息更新事件给子组件（用于触发列表刷新）
provide('messageUpdateEvent', messageUpdateEvent)

// 提供设置当前聊天用户 ID 的方法
provide('setCurrentChatUserId', (userId: number | null) => {
  console.log('App.vue 设置当前聊天用户 ID:', userId)
  currentChatUserId.value = userId
})

// 应用启动时初始化用户状态
onMounted(async () => {
  await userStore.initialize()

  // 如果已经有 token，建立连接
  if (userStore.token) {
    await fetchTotalUnreadCount()
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

// 获取总未读数
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
      if (currentChatUserId.value === senderId) {
        console.log('正在与发送者聊天，不增加未读数')
        // 仍然触发消息更新事件，让聊天页面可以刷新消息列表
        messageUpdateEvent.value++
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
    await setupWebSocket()
  } else {
    // 登出时清空未读数和已处理消息 ID 并断开连接
    totalUnreadCount.value = 0
    processedMessageIds.clear()
    teardownWebSocket()
  }
}, { immediate: true })
</script>

<style scoped>
</style>
