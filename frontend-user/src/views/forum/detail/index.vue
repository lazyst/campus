<template>
  <div class="post-detail min-h-screen bg-gray-100">
    <NavBar :title="post?.title || '帖子详情'" :left-arrow="true" @click-left="onClickLeft" />
    
    <div v-if="post" class="post-content bg-white p-4">
      <div class="flex justify-between items-center mb-4 text-sm text-gray-500">
        <div class="post-author">作者：{{ post.nickname }}</div>
        <div class="post-time">{{ post.time }}</div>
      </div>
      <div class="post-body text-base leading-relaxed mb-5">{{ post.content }}</div>
      <div class="post-actions flex gap-5">
        <BaseButton type="default" size="small" round>
          赞 {{ post.likeCount }}
        </BaseButton>
        <BaseButton type="default" size="small" round>
          评论 {{ post.commentCount }}
        </BaseButton>
      </div>
    </div>
    <div v-else class="p-4 bg-white">
      <!-- Loading skeleton - simple HTML/CSS -->
      <div class="animate-pulse">
        <div class="flex justify-between items-center mb-4">
          <div class="h-4 bg-gray-200 rounded w-1/3"></div>
          <div class="h-4 bg-gray-200 rounded w-1/4"></div>
        </div>
        <div class="h-6 bg-gray-200 rounded mb-4 w-3/4"></div>
        <div class="h-4 bg-gray-200 rounded mb-2"></div>
        <div class="h-4 bg-gray-200 rounded mb-2"></div>
        <div class="h-4 bg-gray-200 rounded mb-2 w-5/6"></div>
        <div class="h-10 bg-gray-200 rounded w-1/3 mt-6"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'
import BaseButton from '@/components/base/Button.vue'

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
