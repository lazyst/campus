<template>
  <div class="post-detail">
    <van-nav-bar :title="post?.title || '帖子详情'" left-arrow @click-left="onClickLeft" />
    
    <div v-if="post" class="post-content">
      <div class="post-header">
        <div class="post-author">作者：{{ post.nickname }}</div>
        <div class="post-time">{{ post.time }}</div>
      </div>
      <div class="post-body">{{ post.content }}</div>
      <div class="post-actions">
        <van-button icon="like-o"> {{ post.likeCount }}</van-button>
        <van-button icon="comment-o"> {{ post.commentCount }}</van-button>
      </div>
    </div>
    <van-skeleton v-else title :row="5" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

interface Post {
  title: string
  nickname: string
  time: string
  content: string
  likeCount: number
  commentCount: number
}

const route = useRoute()
const router = useRouter()
const post = ref<Post | null>(null)

onMounted(() => {
  // Simulate fetching post data
  setTimeout(() => {
    post.value = {
      title: `帖子详情 - ID: ${route.params.id}`,
      nickname: '测试用户',
      time: '2026-01-22 10:00',
      content: '这是帖子的详细内容，包含正文部分。用户可以在这里看到帖子的完整内容，包括所有的文字描述和图片。',
      likeCount: 15,
      commentCount: 8
    }
  }, 500)
})

function onClickLeft() {
  router.back()
}
</script>

<style scoped>
.post-detail { min-height: 100vh; background: #f5f5f5; }
.post-content { padding: 16px; background: #fff; }
.post-header { display: flex; justify-content: space-between; margin-bottom: 16px; font-size: 14px; color: #666; }
.post-body { font-size: 16px; line-height: 1.8; margin-bottom: 20px; }
.post-actions { display: flex; gap: 20px; }
</style>
