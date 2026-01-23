<template>
  <div class="my-posts">
    <van-nav-bar title="我的帖子" left-arrow @click-left="onClickLeft" />
    
    <van-list finished-text="没有更多了">
      <div v-for="i in 5" :key="i" class="post-item">
        <div class="post-title">我的帖子标题 {{ i }}</div>
        <div class="post-content">这是帖子的内容摘要...</div>
        <div class="post-meta">
          <span>{{ i * 10 }} 浏览</span>
          <span>{{ i }} 评论</span>
          <span>2026-01-2{{ i }}</span>
        </div>
      </div>
    </van-list>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function onClickLeft() {
  router.back()
}
</script>

<style scoped>
.my-posts { min-height: 100vh; background: #f5f5f5; padding: 10px; }
.post-item { background: #fff; border-radius: 8px; padding: 12px; margin-bottom: 10px; }
.post-title { font-size: 16px; font-weight: bold; margin-bottom: 8px; }
.post-content { font-size: 14px; color: #666; margin-bottom: 8px; }
.post-meta { display: flex; gap: 16px; font-size: 12px; color: #999; }
</style>
