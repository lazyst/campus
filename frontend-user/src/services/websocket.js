/**
 * WebSocket STOMP 服务
 * 使用原生 WebSocket 配合 STOMP
 */

import { ref } from 'vue'

let stompClient = null
let isConnected = ref(false)
let connectionPromise = null
const messageHandlers = new Set()

// 原生 WebSocket 连接地址
const WS_URL = `ws://localhost:8080/ws`

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
export function connect(token) {
  if (connectionPromise) {
    return connectionPromise
  }

  connectionPromise = new Promise(async (resolve, reject) => {
    if (stompClient && isConnected.value) {
      resolve()
      return
    }

    try {
      // 动态导入 STOMP
      const StompModule = await import('stompjs')
      const Stomp = StompModule.default || StompModule

      console.log('创建原生 WebSocket 连接...')

      // 使用原生 WebSocket 创建连接
      const socket = new WebSocket(WS_URL)

      socket.onopen = () => {
        console.log('WebSocket 连接已建立')
      }

      socket.onclose = (event) => {
        console.log('WebSocket 关闭:', event.code, event.reason)
        isConnected.value = false
        connectionPromise = null
        stompClient = null
      }

      socket.onerror = (error) => {
        console.error('WebSocket 错误:', error)
      }

      // 创建 STOMP 客户端
      stompClient = Stomp.over(socket, {
        heartbeat: false,
        reconnectDelay: 0
      })

      // 连接 STOMP
      stompClient.connect(
        { Authorization: `Bearer ${token}` },
        () => {
          console.log('STOMP 连接成功!')
          isConnected.value = true

          // 订阅个人消息队列
          stompClient.subscribe(
            '/user/queue/messages',
            (message) => {
              try {
                const data = JSON.parse(message.body)
                console.log('收到消息:', data)
                messageHandlers.forEach(handler => handler(data))
              } catch (e) {
                console.error('解析消息失败:', e)
              }
            }
          )

          resolve()
        },
        (error) => {
          console.error('STOMP 连接失败:', error)
          isConnected.value = false
          connectionPromise = null
          stompClient = null
          reject(error)
        }
      )

      // 超时处理
      setTimeout(() => {
        if (!isConnected.value) {
          connectionPromise = null
          reject(new Error('WebSocket 连接超时'))
        }
      }, 10000)

    } catch (error) {
      console.error('创建 WebSocket 失败:', error)
      connectionPromise = null
      reject(error)
    }
  })

  return connectionPromise
}

/**
 * 断开 WebSocket 连接
 */
export function disconnect() {
  if (stompClient) {
    try {
      stompClient.disconnect()
    } catch (e) {
      // 忽略错误
    }
    stompClient = null
  }

  isConnected.value = false
  connectionPromise = null
  console.log('WebSocket 已断开')
}

/**
 * 发送消息
 * @param {number} receiverId - 接收者ID
 * @param {string} content - 消息内容
 */
export function sendMessage(receiverId, content) {
  if (!stompClient || !isConnected.value) {
    throw new Error('WebSocket 未连接，请先调用 connect()')
  }

  stompClient.send(
    `/app/chat.send/${receiverId}`,
    { Authorization: `Bearer ${localStorage.getItem('token')}` },
    JSON.stringify({ content })
  )

  console.log('消息已发送:', content)
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

export default {
  connect,
  disconnect,
  sendMessage,
  onMessage,
  getStompClient,
  getIsConnected
}
