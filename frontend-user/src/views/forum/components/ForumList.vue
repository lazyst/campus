<template>
  <div class="p-3">
    <!-- Loading State -->
    <div v-if="loading && list.length === 0" class="text-center py-8">
      <span class="text-gray-400">加载中...</span>
    </div>
    
    <!-- Empty State -->
    <div v-else-if="list.length === 0" class="text-center py-8">
      <span class="text-gray-400">暂无帖子</span>
    </div>
    
    <!-- Post List -->
    <div v-else>
      <div 
        v-for="post in list" 
        :key="post.id" 
        class="bg-white rounded-lg p-3 mb-2.5 cursor-pointer"
        @click="onPostClick(post)"
      >
        <!-- Post Title -->
        <div class="font-semibold text-gray-800 mb-2">{{ post.title }}</div>
        
        <!-- Post Content Preview -->
        <div class="text-sm text-gray-500 mb-2 line-clamp-2">{{ post.content }}</div>
        
        <!-- Post Footer -->
        <div class="flex justify-between text-xs text-gray-400">
          <span>{{ post.userNickname }}</span>
          <div class="flex gap-3">
            <span>点赞 {{ post.likeCount }}</span>
            <span>评论 {{ post.commentCount }}</span>
          </div>
        </div>
      </div>
      
      <!-- Loading More -->
      <div v-if="loading && list.length > 0" class="text-center py-4">
        <span class="text-gray-400">加载中...</span>
      </div>
      
      <!-- Finished -->
      <div v-if="finished && list.length > 0" class="text-center py-4">
        <span class="text-gray-400 text-sm">没有更多了</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getPosts } from '@/api/modules'

const router = useRouter()
const props = defineProps({
  boardId: {
    type: Number,
    default: undefined
  }
})

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

async function loadPosts() {
  try {
    loading.value = true
    const { records: newList, total } = await getPosts({ 
      boardId: props.boardId,
      page: page.value,
      size: 10 
    })
    list.value.push(...newList)
    page.value++
    loading.value = false
    if (list.value.length >= total) {
      finished.value = true
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    loading.value = false
  }
}

function onLoad() {
  loadPosts()
}

function onPostClick(post) {
  router.push(`/forum/${post.id}`)
}

// Reset list when boardId changes
watch(() => props.boardId, () => {
  list.value = []
  page.value = 1
  finished.value = false
  loadPosts()
})

// Initial load
onLoad()
</script>
