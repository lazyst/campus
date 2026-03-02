/**
 * WebSocket STOMP 服务
 * 支持自动重连和粘性会话
 */

import { ref } from 'vue'

// 配置常量
const MAX_RECONNECT_ATTEMPTS = 10
const BASE_RECONNECT_DELAY = 1000
const MAX_RECONNECT_DELAY = 30000
const CONNECT_TIMEOUT = 10000
const HEARTBEAT_INCOMING = 15000
const HEARTBEAT_OUTGOING = 15000
const HEALTH_CHECK_INTERVAL = 30000

// 状态变量
let stompClient = null
let isConnected = ref(false)
let connectionPromise = null
let currentToken = null
let reconnectAttempts = 0
let healthCheckTimer = null
let lastHeartbeatTime = Date.now()

// 订阅管理
const subscriptions = new Map()
const messageHandlers = new Set()
const notificationHandlers = new Set()
const unreadCountHandlers = new Set()

// WebSocket 地址
const protocol = typeof window !== 'undefined' && window.location.protocol === 'https:' ? 'wss:' : 'ws:'
const host = typeof window !== 'undefined' ? window.location.host : ''
const envWsUrl = typeof import.meta !== 'undefined' ? (import.meta.env.VITE_WS_URL || import.meta.env.VITE_WS_URL_DEV) : ''
const WS_URL = envWsUrl || `${protocol}//${host}/ws/`

// 执行 WebSocket 连接
async function doConnect() {
  // 动态导入 STOMP
  const StompModule = await import('stompjs')
  const Stomp = StompModule.default || StompModule

  const socket = new WebSocket(WS_URL)

  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => {
      if (!isConnected.value) {
        reject(new Error('WebSocket 连接超时'))
      }
    }, CONNECT_TIMEOUT)

    socket.onclose = (event) => {
      isConnected.value = false
      console.warn(`[WebSocket] 连接关闭 code=${event.code}, reason=${event.reason || '无'}`)

      subscriptions.forEach((sub) => {
        try { sub.unsubscribe() } catch (e) { console.error('[WebSocket] 取消订阅失败:', e) }
      })
      subscriptions.clear()

      if (currentToken) {
        attemptReconnect()
      }
    }

    socket.onerror = (error) => {
      console.error('[WebSocket] 连接错误:', error)
    }

    stompClient = Stomp.over(socket, {
      heartbeat: { incoming: HEARTBEAT_INCOMING, outgoing: HEARTBEAT_OUTGOING },
      reconnectDelay: 0
    })

    stompClient.connect(
      { Authorization: `Bearer ${currentToken}` },
      () => {
        clearTimeout(timeout)
        isConnected.value = true
        reconnectAttempts = 0
        lastHeartbeatTime = Date.now()
        console.log('[WebSocket] 连接成功')
        subscribeQueues(stompClient)
        startHealthCheck()
        resolve()
      },
      (error) => {
        clearTimeout(timeout)
        isConnected.value = false
        stompClient = null
        console.error('[WebSocket] 连接失败:', error)
        reject(error)
      }
    )
  })
}

// 订阅消息队列
function subscribeQueues(client) {
  const createHandler = (handlers) => (message) => {
    try {
      const data = JSON.parse(message.body)
      updateHeartbeat()
      handlers.forEach(handler => handler(data))
    } catch (e) {
      console.error('[WebSocket] 消息解析失败:', e)
    }
  }

  subscriptions.set('messages', client.subscribe('/user/queue/messages', createHandler(messageHandlers)))
  subscriptions.set('notifications', client.subscribe('/user/queue/notifications', createHandler(notificationHandlers)))
  subscriptions.set('unread-count', client.subscribe('/user/queue/unread-count', createHandler(unreadCountHandlers)))
}

// 启动健康检查
function startHealthCheck() {
  stopHealthCheck()
  healthCheckTimer = setInterval(() => {
    if (!isConnected.value || !currentToken) return

    const now = Date.now()
    const timeSinceLastHeartbeat = now - lastHeartbeatTime

    // 如果超过健康检查间隔没有收到心跳，强制重连
    if (timeSinceLastHeartbeat > HEALTH_CHECK_INTERVAL * 1.5) {
      console.warn('[WebSocket] 心跳超时，强制重连')
      // 触发 onclose 逻辑
      if (stompClient) {
        stompClient.disconnect()
      }
      isConnected.value = false
      attemptReconnect()
    }
  }, HEALTH_CHECK_INTERVAL)
}

// 停止健康检查
function stopHealthCheck() {
  if (healthCheckTimer) {
    clearInterval(healthCheckTimer)
    healthCheckTimer = null
  }
}

// 更新心跳时间（在收到消息时调用）
function updateHeartbeat() {
  lastHeartbeatTime = Date.now()
}

// 尝试重连
function attemptReconnect() {
  if (!currentToken) {
    console.log('[WebSocket] 无token，跳过重连')
    return
  }

  reconnectAttempts++
  if (reconnectAttempts > MAX_RECONNECT_ATTEMPTS) {
    console.error('[WebSocket] 超过最大重连次数，停止重连')
    return
  }

  // 指数退避 + 随机抖动
  const delay = Math.min(BASE_RECONNECT_DELAY * Math.pow(2, reconnectAttempts), MAX_RECONNECT_DELAY) + Math.random() * 1000
  console.log(`[WebSocket] ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS} 次重连，${delay.toFixed(0)}ms 后尝试...`)

  setTimeout(() => {
    connectionPromise = null
    stompClient = null
    connect(currentToken)
      .then(() => console.log('[WebSocket] 重连成功'))
      .catch((error) => console.warn('[WebSocket] 重连失败:', error.message))
  }, delay)
}

// 公开 API
export function getStompClient() {
  return stompClient
}

export function getIsConnected() {
  return isConnected.value
}

export async function connect(token) {
  if (connectionPromise) {
    return connectionPromise
  }

  currentToken = token

  if (stompClient && isConnected.value) {
    return Promise.resolve()
  }

  connectionPromise = doConnect()

  try {
    await connectionPromise
  } catch (error) {
    connectionPromise = null
    throw error
  }

  return connectionPromise
}

export function disconnect() {
  currentToken = null
  reconnectAttempts = 0
  stopHealthCheck()

  if (stompClient) {
    try {
      stompClient.disconnect()
    } catch (e) {
      console.warn('[WebSocket] 断开连接时出错:', e)
    }
    stompClient = null
  }

  isConnected.value = false
  connectionPromise = null
}

export function sendMessage(receiverId, content, type = 1, itemId = null) {
  if (!stompClient || !isConnected.value) {
    throw new Error('WebSocket 未连接，请先调用 connect()')
  }

  const payload = { content, type }
  if (type === 3 && itemId) {
    payload.itemId = itemId
  }

  stompClient.send(
    `/app/chat.send/${receiverId}`,
    { Authorization: `Bearer ${localStorage.getItem('token')}` },
    JSON.stringify(payload)
  )
}

export function onMessage(handler) {
  messageHandlers.add(handler)
  return () => messageHandlers.delete(handler)
}

export function onNotification(handler) {
  notificationHandlers.add(handler)
  return () => notificationHandlers.delete(handler)
}

export function onUnreadCount(handler) {
  unreadCountHandlers.add(handler)
  return () => unreadCountHandlers.delete(handler)
}

export function getReconnectStatus() {
  return {
    isConnected: isConnected.value,
    reconnectAttempts,
    maxReconnectAttempts: MAX_RECONNECT_ATTEMPTS
  }
}

export default {
  connect,
  disconnect,
  sendMessage,
  onMessage,
  onNotification,
  onUnreadCount,
  getStompClient,
  getIsConnected,
  getReconnectStatus
}
