<template>
  <div class="trade-list">
    <!-- Category Tabs -->
    <div class="flex gap-3 px-4 py-3 bg-white overflow-x-auto whitespace-nowrap border-b border-gray-100">
      <span 
        :class="[
          'px-3 py-1.5 rounded-full text-sm cursor-pointer transition-colors flex-shrink-0',
          activeTab === 0 ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
        ]"
        @click="activeTab = 0"
      >
        出售
      </span>
      <span 
        :class="[
          'px-3 py-1.5 rounded-full text-sm cursor-pointer transition-colors flex-shrink-0',
          activeTab === 1 ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
        ]"
        @click="activeTab = 1"
      >
        收购
      </span>
    </div>
    
    <!-- Item List -->
    <div class="p-3">
      <!-- Empty State -->
      <div v-if="list.length === 0 && !loading" class="text-center py-8">
        <span class="text-gray-400">暂无闲置物品</span>
      </div>
      
      <!-- Item Cards -->
      <div 
        v-for="item in list" 
        :key="item.id" 
        class="bg-white rounded-lg p-3 mb-2.5 flex cursor-pointer"
        @click="onItemClick(item)"
      >
        <!-- Item Image/Type Badge -->
        <div 
          class="w-20 h-20 rounded-lg flex items-center justify-center text-white font-bold text-lg mr-3 flex-shrink-0"
          :class="item.type === 1 ? 'bg-success' : 'bg-primary'"
        >
          {{ item.type === 1 ? '收' : '售' }}
        </div>
        
        <!-- Item Content -->
        <div class="flex-1 min-w-0">
          <div class="font-semibold text-gray-800 mb-1 truncate">{{ item.title }}</div>
          <div class="text-lg font-bold text-danger mb-1">¥{{ item.price }}</div>
          <div class="flex justify-between text-xs text-gray-400">
            <span>{{ item.nickname }}</span>
            <span>浏览 {{ item.viewCount }}</span>
          </div>
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

interface TradeItem {
  id: number
  type: number // 1: 收购, 2: 出售
  title: string
  price: number
  nickname: string
  viewCount: number
}

const router = useRouter()
const activeTab = ref(0)
const list = ref<TradeItem[]>([])
const loading = ref(false)
const finished = ref(false)

function onLoad() {
  setTimeout(() => {
    for (let i = 0; i < 5; i++) {
      list.value.push({
        id: list.value.length + 1,
        type: activeTab.value + 1,
        title: `物品 ${list.value.length + 1}`,
        price: Math.floor(Math.random() * 1000) + 100,
        nickname: '用户' + (list.value.length + 1),
        viewCount: Math.floor(Math.random() * 200)
      })
    }
    loading.value = false
    if (list.value.length >= 20) {
      finished.value = true
    }
  }, 500)
}

function onTabChange() {
  list.value = []
  finished.value = false
  onLoad()
}

function onItemClick(item: TradeItem) {
  router.push(`/trade/${item.id}`)
}

// Initial load
onLoad()
</script>
