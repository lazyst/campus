<template>
  <div class="chat-detail-page">
    <!-- 状态栏 -->
    <div class="status-bar">
      <span class="back-btn" @click="goBack">‹</span>
      <span class="chat-title">{{ chatName }}</span>
      <span class="more-btn">⋯</span>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-messages">
      <div class="time-divider">
        <span>10:30</span>
      </div>

      <div 
        v-for="msg in messages" 
        :key="msg.id"
        class="message-row"
        :class="{ sent: msg.isMe }"
      >
        <div v-if="!msg.isMe" class="message-avatar">
          <span>{{ otherUserAvatar }}</span>
        </div>
        
        <div class="message-bubble" :class="{ sent: msg.isMe }">
          {{ msg.content }}
        </div>

        <div v-if="msg.isMe" class="message-avatar my-avatar">
          <span>我</span>
        </div>
      </div>
    </div>

    <!-- 快捷回复 -->
    <div class="quick-replies">
      <button 
        v-for="reply in quickReplies" 
        :key="reply"
        class="quick-btn"
        @click="sendQuickReply(reply)"
      >
        {{ reply }}
      </button>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <input 
        v-model="inputMessage"
        type="text"
        class="message-input"
        placeholder="发送消息..."
        @keyup.enter="sendMessage"
      />
      <button class="emoji-btn">😊</button>
      <button class="image-btn">🖼️</button>
      <button class="send-btn" @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const chatName = ref('');
const inputMessage = ref('');
const otherUserAvatar = ref('小');

interface Message {
  id: number;
  content: string;
  isMe: boolean;
  time: string;
}

const messages = ref<Message[]>([]);

const quickReplies = ['好的', '多少钱？', '在吗？', '可以便宜吗'];

onMounted(() => {
  chatName.value = `用户${route.params.id}`;
  // 初始化示例消息
  messages.value = [
    { id: 1, content: '你好，请问这个MacBook还在吗？', isMe: false, time: '10:30' },
    { id: 2, content: '在的，诚心要可以优惠', isMe: true, time: '10:31' },
    { id: 3, content: '可以便宜多少？我想当面交易，看看电脑实际情况', isMe: false, time: '10:32' },
    { id: 4, content: '可以便宜50块，我在图书馆三楼，随时可以交易', isMe: true, time: '10:33' },
  ];
});

function goBack() {
  router.back();
}

function sendMessage() {
  if (!inputMessage.value.trim()) return;
  
  messages.value.push({
    id: Date.now(),
    content: inputMessage.value,
    isMe: true,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  });
  
  inputMessage.value = '';
}

function sendQuickReply(reply: string) {
  messages.value.push({
    id: Date.now(),
    content: reply,
    isMe: true,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  });
}
</script>

<style scoped>
.chat-detail-page {
  min-height: 100vh;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
  padding-top: 44px;
}

.status-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
  z-index: 10;
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
  padding: 16px;
  overflow-y: auto;
}

.time-divider {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.time-divider span {
  font-size: 12px;
  color: #94A3B8;
  background: white;
  padding: 4px 12px;
  border-radius: 12px;
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

.quick-replies {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: white;
  overflow-x: auto;
}

.quick-btn {
  padding: 8px 16px;
  background: #F1F5F9;
  border: none;
  border-radius: 16px;
  font-size: 13px;
  color: #64748B;
  cursor: pointer;
  white-space: nowrap;
}

.input-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #E2E8F0;
}

.message-input {
  flex: 1;
  height: 36px;
  background: #F1F5F9;
  border: none;
  border-radius: 18px;
  padding: 0 16px;
  font-size: 14px;
  outline: none;
}

.message-input:focus {
  background: white;
  box-shadow: 0 0 0 2px #6366F1;
}

.emoji-btn,
.image-btn {
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
}

.send-btn {
  width: 60px;
  height: 36px;
  background: #6366F1;
  border: none;
  border-radius: 18px;
  font-size: 14px;
  color: white;
  cursor: pointer;
}
</style>
