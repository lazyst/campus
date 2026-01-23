<template>
  <div class="notification-messages min-h-screen bg-gray-100 pt-2.5">
    <NavBar title="消息通知" :left-arrow="true" @click-left="onClickLeft" />
    
    <div class="p-2.5">
      <div v-for="i in 5" :key="i" class="notify-item flex bg-white rounded-lg p-3 mb-2.5">
        <div class="notify-icon w-10 h-10 bg-primary text-white rounded-full flex items-center justify-center mr-3 text-xs">
          系统
        </div>
        <div class="notify-content flex-1">
          <div class="notify-title font-bold mb-1">系统通知 {{ i }}</div>
          <div class="notify-desc text-sm text-gray-500 mb-1">您有新的消息，请注意查收...</div>
          <div class="notify-time text-xs text-gray-400">2026-01-2{{ i }}</div>
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
