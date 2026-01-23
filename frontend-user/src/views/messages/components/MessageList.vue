<template>
  <div class="message-list">
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
    >
      <div v-for="msg in list" :key="msg.id" class="message-item" @click="onItemClick(msg)">
        <div class="message-avatar">{{ msg.nickname.charAt(0) }}</div>
        <div class="message-content">
          <div class="message-header">
            <span class="message-nickname">{{ msg.nickname }}</span>
            <span class="message-time">{{ msg.time }}</span>
          </div>
          <div class="message-preview">{{ msg.preview }}</div>
        </div>
      </div>
    </van-list>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

interface Message {
  id: number
  nickname: string
  preview: string
  time: string
}

const router = useRouter()
const list = ref<Message[]>([])
const loading = ref(false)
const finished = ref(false)

function onLoad() {
  setTimeout(() => {
    for (let i = 0; i < 5; i++) {
      list.value.push({
        id: list.value.length + 1,
        nickname: '用户' + (list.value.length + 1),
        preview: '最后一条消息内容...',
        time: '10:' + (30 + i)
      })
    }
    loading.value = false
    if (list.value.length >= 20) {
      finished.value = true
    }
  }, 500)
}

function onItemClick(msg: Message) {
  router.push(`/messages/${msg.id}`)
}
</script>

<style scoped>
.message-list {
  padding: 10px;
}
.message-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
}
.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #1989fa;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  margin-right: 12px;
}
.message-content {
  flex: 1;
}
.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}
.message-nickname {
  font-weight: bold;
}
.message-time {
  font-size: 12px;
  color: #999;
}
.message-preview {
  font-size: 14px;
  color: #666;
}
</style>
