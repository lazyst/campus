<template>
  <div class="my-posts min-h-screen bg-gray-100 pt-2.5">
    <NavBar title="我的帖子" :left-arrow="true" @click-left="onClickLeft" />
    
    <div class="p-2.5">
      <div v-for="i in 5" :key="i" class="post-item bg-white rounded-lg p-3 mb-2.5">
        <div class="post-title text-base font-bold mb-2">我的帖子标题 {{ i }}</div>
        <div class="post-content text-sm text-gray-500 mb-2">这是帖子的内容摘要...</div>
        <div class="post-meta flex gap-4 text-xs text-gray-400">
          <span>{{ i * 10 }} 浏览</span>
          <span>{{ i }} 评论</span>
          <span>2026-01-2{{ i }}</span>
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
