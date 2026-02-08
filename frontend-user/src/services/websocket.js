/**
 * WebSocket STOMP 服务
 * 支持自动重连和粘性会话
 */

import { ref } from 'vue'

let stompClient = null
let isConnected = ref(false)
let connectionPromise = null
let currentToken = null
let reconnectAttempts = 0
const maxReconnectAttempts = 10
const baseReconnectDelay = 1000

// 订阅管理
const subscriptions = new Map()
const messageHandlers = new Set()
const notificationHandlers = new Set()

// 根据当前环境动态生成WebSocket地址
const getWebSocketUrl = () => {
  const isDev = import.meta.env.DEV || window.location.port === '3000'
  if (isDev) {
    return `ws://localhost:8080/ws`
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws`
}

const WS_URL = getWebSocketUrl()

/**
 * 获取重连延迟时间（指数退避）
 */
const getReconnectDelay = () => {
  const delay = Math.min(baseReconnectDelay * Math.pow(2, reconnectAttempts), 30000)
  return delay + Math.random() * 1000 // 添加随机抖动
}

/**
 * 订阅消息队列
 */
const subscribeQueues = (client) => {
  // 订阅个人消息
  const msgSub = client.subscribe(
    '/user/queue/messages',
    (message) => {
      try {
        const data = JSON.parse(message.body)
        messageHandlers.forEach(handler => handler(data))
      } catch {
        // 忽略消息解析错误
      }
    }
  )
  subscriptions.set('messages', msgSub)

  // 订阅通知队列
  const notifSub = client.subscribe(
    '/user/queue/notifications',
    (message) => {
      try {
        const data = JSON.parse(message.body)
        notificationHandlers.forEach(handler => handler(data))
      } catch {
        // 忽略消息解析错误
      }
    }
  )
  subscriptions.set('notifications', notifSub)
}

/**
 * 执行WebSocket连接
 */
const doConnect = () => {
  return new Promise(async (resolve, reject) => {
    try {
      // 动态导入 STOMP
      const StompModule = await import('stompjs')
      const Stomp = StompModule.default || StompModule

      const socket = new WebSocket(WS_URL)

      socket.onclose = (event) => {
        isConnected.value = false
        console.warn(`[WebSocket] 连接关闭 code=${event.code}, reason=${event.reason || '无'}`)

        // 清除订阅
        subscriptions.forEach((sub) => {
          try { sub.unsubscribe() } catch {}
        })
        subscriptions.clear()

        // 不是主动断开，尝试重连
        if (currentToken) {
          attemptReconnect()
        }
      }

      socket.onerror = (error) => {
        console.error('[WebSocket] 连接错误:', error)
      }

      // 创建 STOMP 客户端
      stompClient = Stomp.over(socket, {
        heartbeat: false,
        reconnectDelay: 0 // 我们自己控制重连
      })

      // 连接 STOMP
      stompClient.connect(
        { Authorization: `Bearer ${currentToken}` },
        () => {
          isConnected.value = true
          reconnectAttempts = 0
          console.log('[WebSocket] 连接成功')

          // 订阅队列
          subscribeQueues(stompClient)

          resolve()
        },
        (error) => {
          isConnected.value = false
          stompClient = null
          console.error('[WebSocket] 连接失败:', error)
          reject(error)
        }
      )

      // 超时处理
      setTimeout(() => {
        if (!isConnected.value) {
          reject(new Error('WebSocket 连接超时'))
        }
      }, 10000)

    } catch (error) {
      reject(error)
    }
  })
}

/**
 * 尝试重连
 */
const attemptReconnect = () => {
  if (!currentToken) {
    console.log('[WebSocket] 无token，跳过重连')
    return
  }

  reconnectAttempts++
  if (reconnectAttempts > maxReconnectAttempts) {
    console.error('[WebSocket] 超过最大重连次数，停止重连')
    return
  }

  const delay = getReconnectDelay()
  console.log(`[WebSocket] ${reconnectAttempts}/${maxReconnectAttempts} 次重连，${delay.toFixed(0)}ms 后尝试...`)

  setTimeout(async () => {
    connectionPromise = null
    stompClient = null
    try {
      await connect(currentToken)
      console.log('[WebSocket] 重连成功')
    } catch (error) {
      console.warn('[WebSocket] 重连失败:', error.message)
    }
  }, delay)
}

export function getStompClient() {
  return stompClient
}

export function getIsConnected() {
  return isConnected.value
}

/**
 * 连接 WebSocket
 * @param {string} token - JWT token
 * @returns {Promise<void>}
 */
export async function connect(token) {
  if (connectionPromise) {
    return connectionPromise
  }

  currentToken = token

  // 如果已连接，直接返回
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

/**
 * 断开 WebSocket 连接
 */
export function disconnect() {
  currentToken = null
  reconnectAttempts = 0

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

/**
 * 发送消息
 * @param {number} receiverId - 接收者ID
 * @param {string} content - 消息内容
 * @param {number} type - 消息类型 (1=文本, 2=图片)
 */
export function sendMessage(receiverId, content, type = 1) {
  if (!stompClient || !isConnected.value) {
    throw new Error('WebSocket 未连接，请先调用 connect()')
  }

  stompClient.send(
    `/app/chat.send/${receiverId}`,
    { Authorization: `Bearer ${localStorage.getItem('token')}` },
    JSON.stringify({ content, type })
  )
}

/**
 * 订阅消息
 * @param {Function} handler - 消息处理函数
 * @returns {Function} 取消订阅函数
 */
export function onMessage(handler) {
  messageHandlers.add(handler)
  return () => messageHandlers.delete(handler)
}

/**
 * 订阅通知
 * @param {Function} handler - 通知处理函数
 * @returns {Function} 取消订阅函数
 */
export function onNotification(handler) {
  notificationHandlers.add(handler)
  return () => notificationHandlers.delete(handler)
}

/**
 * 获取重连状态（用于UI显示）
 */
export function getReconnectStatus() {
  return {
    isConnected: isConnected.value,
    reconnectAttempts,
    maxReconnectAttempts
  }
}

export default {
  connect,
  disconnect,
  sendMessage,
  onMessage,
  onNotification,
  getStompClient,
  getIsConnected,
  getReconnectStatus
}
