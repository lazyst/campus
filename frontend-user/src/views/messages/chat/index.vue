<template>
  <div class="chat-detail">
    <van-nav-bar :title="chatName || '聊天详情'" left-arrow @click-left="onClickLeft" />
    
    <div class="chat-messages" ref="chatContainer">
      <div v-for="msg in messages" :key="msg.id" :class="['message-row', msg.isMe ? 'me' : 'other']">
        <div :class="['message-bubble', msg.isMe ? 'my-bubble' : 'other-bubble']">
          {{ msg.content }}
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <van-field
        v-model="inputMessage"
        placeholder="请输入消息..."
        @keypress.enter="onSend"
      />
      <van-button type="primary" size="small" @click="onSend">发送</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

interface Message {
  id: number
  content: string
  isMe: boolean
  time: string
}

const route = useRoute()
const router = useRouter()
const chatName = ref('')
const inputMessage = ref('')
const messages = ref<Message[]>([])

onMounted(() => {
  chatName.value = `用户${route.params.id}`
  // Initialize with some demo messages
  messages.value = [
    { id: 1, content: '你好，请问这个物品还在吗？', isMe: false, time: '10:00' },
    { id: 2, content: '在的，请问您想要吗？', isMe: true, time: '10:01' },
    { id: 3, content: '是的，怎么交易？', isMe: false, time: '10:02' }
  ]
})

function onClickLeft() {
  router.back()
}

function onSend() {
  if (!inputMessage.value.trim()) return
  messages.value.push({
    id: Date.now(),
    content: inputMessage.value,
    isMe: true,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  inputMessage.value = ''
}
</script>

<style scoped>
.chat-detail { min-height: 100vh; background: #f5f5f5; display: flex; flex-direction: column; }
.chat-messages { flex: 1; padding: 16px; overflow-y: auto; }
.message-row { display: flex; margin-bottom: 12px; }
.message-row.me { justify-content: flex-end; }
.message-row.other { justify-content: flex-start; }
.message-bubble { max-width: 70%; padding: 10px 14px; border-radius: 12px; font-size: 14px; }
.my-bubble { background: #1989fa; color: #fff; }
.other-bubble { background: #fff; }
.chat-input { display: flex; align-items: center; padding: 10px; background: #fff; border-top: 1px solid #eee; }
.chat-input .van-field { flex: 1; margin-right: 10px; }
</style>
