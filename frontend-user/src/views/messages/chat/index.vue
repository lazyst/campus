<template>
  <div class="chat-detail min-h-screen bg-gray-100 flex flex-col">
    <NavBar :title="chatName || '聊天详情'" :left-arrow="true" @click-left="onClickLeft" />
    
    <div class="chat-messages flex-1 p-4 overflow-y-auto">
      <div v-for="msg in messages" :key="msg.id" :class="['flex mb-3', msg.isMe ? 'justify-end' : 'justify-start']">
        <div 
          :class="[
            'max-w-[70%] px-3 py-2.5 rounded-lg text-sm',
            msg.isMe ? 'bg-primary text-white' : 'bg-white'
          ]"
        >
          {{ msg.content }}
        </div>
      </div>
    </div>
    
    <div class="chat-input flex items-center p-2.5 bg-white border-t border-gray-100">
      <input
        v-model="inputMessage"
        placeholder="请输入消息..."
        class="flex-1 border border-gray-300 rounded-full px-4 py-2 text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary mr-2"
        @keypress.enter="onSend"
      />
      <BaseButton type="primary" size="small" round @click="onSend">
        发送
      </BaseButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'
import BaseButton from '@/components/base/Button.vue'

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
