<template>
  <div class="my-items min-h-screen bg-gray-100 pt-2.5">
    <NavBar title="我的闲置" :left-arrow="true" @click-left="onClickLeft" />
    
    <div class="p-2.5">
      <div v-for="i in 5" :key="i" class="item-row flex bg-white rounded-lg p-3 mb-2.5">
        <div class="item-image w-20 h-20 bg-primary text-white rounded-lg flex items-center justify-center mr-3">
          物
        </div>
        <div class="item-info flex-1">
          <div class="item-title text-base font-bold mb-2">闲置物品 {{ i }}</div>
          <div class="item-price text-lg text-red-500 mb-2">¥{{ i * 100 }}</div>
          <div class="item-status">
            <span 
              :class="[
                'text-xs px-2 py-0.5 rounded',
                i % 2 === 0 ? 'bg-green-100 text-green-600' : 'bg-yellow-100 text-yellow-600'
              ]"
            >
              {{ i % 2 === 0 ? '已上架' : '已下架' }}
            </span>
          </div>
        </div>
      </div>
    </div>
    <div class="text-center py-4 text-gray-400 text-sm">没有更多了</div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'

const router = useRouter()

function onClickLeft() {
  router.back()
}
</script>
