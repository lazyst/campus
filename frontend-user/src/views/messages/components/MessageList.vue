<template>
  <div class="message-list p-3">
    <!-- Empty State -->
    <div v-if="list.length === 0 && !loading" class="text-center py-8">
      <span class="text-gray-400">暂无消息</span>
    </div>
    
    <!-- Message Items -->
    <div v-else>
      <div 
        v-for="msg in list" 
        :key="msg.id" 
        class="bg-white rounded-lg p-3 mb-2.5 flex items-center cursor-pointer"
        @click="onItemClick(msg)"
      >
        <!-- Avatar -->
        <div class="w-10 h-10 rounded-full bg-primary text-white flex items-center justify-center text-base mr-3 flex-shrink-0">
          {{ msg.nickname.charAt(0) }}
        </div>
        
        <!-- Content -->
        <div class="flex-1 min-w-0">
          <div class="flex justify-between items-center mb-1">
            <span class="font-semibold text-gray-800">{{ msg.nickname }}</span>
            <span class="text-xs text-gray-400">{{ msg.time }}</span>
          </div>
          <div class="text-sm text-gray-500 truncate">{{ msg.preview }}</div>
        </div>
      </div>
      
      <!-- Loading -->
      <div v-if="loading" class="text-center py-4">
        <span class="text-gray-400">加载中...</span>
      </div>
      
      <!-- Finished -->
      <div v-if="finished && list.length > 0" class="text-center py-4">
        <span class="text-gray-400 text-sm">没有更多了</span>
      </div>
    </div>
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

// Initial load
onLoad()
</script>
