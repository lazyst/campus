<template>
  <div class="forum-list">
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
    >
      <div v-for="post in list" :key="post.id" class="post-item" @click="onPostClick(post)">
        <div class="post-header">
          <span class="post-title">{{ post.title }}</span>
        </div>
        <div class="post-content">{{ post.content }}</div>
        <div class="post-footer">
          <span>{{ post.userNickname }}</span>
          <span>{{ post.likeCount }} 点赞</span>
          <span>{{ post.commentCount }} 评论</span>
        </div>
      </div>
    </van-list>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getPosts, type Post } from '@/api/forum'

const router = useRouter()
const props = defineProps<{
  boardId?: number
}>()

const list = ref<Post[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

function onLoad() {
  loadPosts()
}

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

function onPostClick(post: Post) {
  router.push(`/forum/${post.id}`)
}

watch(() => props.boardId, () => {
  list.value = []
  page.value = 1
  finished.value = false
  loadPosts()
})
</script>

<style scoped>
.forum-list {
  padding: 10px;
}
.post-item {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
}
.post-title {
  font-size: 16px;
  font-weight: bold;
}
.post-content {
  font-size: 14px;
  color: #666;
  margin: 8px 0;
}
.post-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}
</style>
