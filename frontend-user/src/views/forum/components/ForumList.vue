<template>
  <div class="forum-list">
    <!-- Loading State with Skeleton -->
    <div v-if="loading && list.length === 0" class="forum-list__skeleton">
      <Skeleton :rows="3" :loading="true" :title="true" />
    </div>

    <!-- Empty State -->
    <div v-else-if="list.length === 0" class="forum-list__state">
      <span class="forum-list__state-text">暂无帖子</span>
    </div>

    <!-- Post List -->
    <div v-else class="forum-list__content">
      <div
        v-for="post in list"
        :key="post.id"
        class="forum-list__card"
        @click="onPostClick(post)"
      >
        <!-- 用户信息（头像+昵称） -->
        <div class="forum-list__card-user">
          <div class="forum-list__card-avatar">
            <img v-if="post.userAvatar" :src="getImageUrl(post.userAvatar)" alt="头像" />
            <span v-else>{{ (post.userNickname || '匿名').charAt(0) }}</span>
          </div>
          <span class="forum-list__card-username">{{ post.userNickname || '匿名用户' }}</span>
        </div>

        <!-- Post Title -->
        <h3 class="forum-list__card-title">{{ post.title }}</h3>

        <!-- Post Content Preview -->
        <p class="forum-list__card-preview">{{ post.content }}</p>

        <!-- Post Images (最多显示3张) -->
        <div v-if="post.images && post.images.length > 0" class="forum-list__card-images">
          <div 
            v-for="(img, index) in post.images.slice(0, 3)" 
            :key="index"
            class="forum-list__card-image"
          >
            <img :src="getImageUrl(img)" :alt="'图片' + (index + 1)" />
          </div>
          <div v-if="post.images.length > 3" class="forum-list__card-image-more">
            +{{ post.images.length - 3 }}
          </div>
        </div>

        <!-- Post Footer -->
        <div class="forum-list__card-footer">
          <div class="forum-list__card-stats">
            <span class="forum-list__stat">点赞 {{ post.likeCount }}</span>
            <span class="forum-list__stat">评论 {{ post.commentCount }}</span>
          </div>
        </div>
      </div>

      <!-- Loading More -->
      <div v-if="loading && list.length > 0" class="forum-list__loading-more">
        <Skeleton :rows="1" :loading="true" />
      </div>

      <!-- Finished -->
      <div v-if="finished && list.length > 0" class="forum-list__state">
        <span class="forum-list__state-text forum-list__state-text--muted">没有更多了</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPosts } from '@/api/modules'
import Skeleton from '@/components/feedback/Skeleton.vue'
import { getImageUrl } from '@/utils/imageUrl'

interface Post {
  id: number
  title: string
  content: string
  images: string[] | null
  userNickname: string
  userAvatar: string
  likeCount: number
  commentCount: number
}

interface Props {
  boardId?: number
}

const props = withDefaults(defineProps<Props>(), {
  boardId: undefined
})

const router = useRouter()
const list = ref<Post[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

async function loadPosts() {
  try {
    loading.value = true
    const response = await getPosts({
      boardId: props.boardId,
      page: page.value,
      size: 10
    }) as { records: Post[], total: number }

    // 解析images字段（JSON字符串转数组）
    const newList = response.records.map(post => ({
      ...post,
      images: parseImages(post.images)
    }))
    
    list.value.push(...newList)
    page.value++

    if (list.value.length >= response.total) {
      finished.value = true
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
  } finally {
    loading.value = false
  }
}

function parseImages(images: string | null): string[] {
  if (!images) return []
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : [parsed]
  } catch {
    return images ? [images] : []
  }
}

function onLoad() {
  loadPosts()
}

function onPostClick(post: Post) {
  router.push(`/forum/detail/${post.id}`)
}

// Reset list when boardId changes
watch(() => props.boardId, () => {
  list.value = []
  page.value = 1
  finished.value = false
  loadPosts()
})

// Initial load
onMounted(() => {
  onLoad()
})
</script>

<style scoped>
.forum-list {
  padding: var(--space-3);
}

.forum-list__state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) 0;
}

.forum-list__state-text {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.forum-list__state-text--muted {
  font-size: var(--text-xs);
}

.forum-list__content {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.forum-list__card {
  padding: var(--space-4);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.forum-list__card:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

.forum-list__card-user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.forum-list__card-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background-color: var(--color-primary-100);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-primary-700);
  overflow: hidden;
}

.forum-list__card-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.forum-list__card-username {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
}

.forum-list__card-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
  line-height: var(--line-height-tight);
}

.forum-list__card-preview {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-3);
  line-height: var(--line-height-normal);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.forum-list__card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.forum-list__card-stats {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.forum-list__stat {
  color: var(--text-tertiary);
}

.forum-list__card-images {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.forum-list__card-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  overflow: hidden;
  flex-shrink: 0;
}

.forum-list__card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.forum-list__card-image-more {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  background-color: var(--color-gray-200);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-lg);
  font-weight: var(--font-weight-medium);
  color: var(--text-tertiary);
  flex-shrink: 0;
}
</style>
