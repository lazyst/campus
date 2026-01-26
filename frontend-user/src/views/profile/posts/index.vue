<template>
  <div class="my-posts min-h-screen bg-gray-100 pt-2.5">
    <NavBar title="我的帖子" :left-arrow="true" @click-left="onClickLeft" />

    <div class="p-2.5">
      <!-- Loading State -->
      <div v-if="loading && posts.length === 0" class="text-center py-8">
        <span class="text-gray-400">加载中...</span>
      </div>

      <!-- Empty State -->
      <div v-else-if="posts.length === 0" class="text-center py-8">
        <span class="text-gray-400">暂无帖子</span>
      </div>

      <!-- Post List -->
      <div
        v-for="post in posts"
        :key="post.id"
        class="post-item bg-white rounded-lg p-3 mb-2.5"
        @click="onPostClick(post)"
      >
        <div class="post-title text-base font-bold mb-2">{{ post.title }}</div>
        <div class="post-content text-sm text-gray-500 mb-2 line-clamp-2">{{ post.content }}</div>
        <div class="post-meta flex gap-4 text-xs text-gray-400">
          <span>{{ post.viewCount }} 浏览</span>
          <span>{{ post.commentCount }} 评论</span>
          <span>{{ post.likeCount }} 点赞</span>
          <span>{{ formatDate(post.createdAt) }}</span>
        </div>
      </div>

      <!-- Loading More -->
      <div v-if="loading && posts.length > 0" class="text-center py-4">
        <span class="text-gray-400">加载中...</span>
      </div>

      <!-- Finished -->
      <div v-if="finished && posts.length > 0" class="text-center py-4">
        <span class="text-gray-400 text-sm">没有更多了</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyPosts } from '@/api/modules'
import NavBar from '@/components/navigation/NavBar.vue'

const router = useRouter()
const posts = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

async function loadPosts() {
  if (loading.value || finished.value) return

  try {
    loading.value = true
    const { records, total } = await getMyPosts({
      page: page.value,
      size: 10
    })
    posts.value.push(...records)
    page.value++

    if (posts.value.length >= total) {
      finished.value = true
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
  } finally {
    loading.value = false
  }
}

function onPostClick(post) {
  router.push(`/forum/${post.id}`)
}

function onClickLeft() {
  router.back()
}

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  loadPosts()
})
</script>
