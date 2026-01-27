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
        <!-- Post Title -->
        <h3 class="forum-list__card-title">{{ post.title }}</h3>

        <!-- Post Content Preview -->
        <p class="forum-list__card-preview">{{ post.content }}</p>

        <!-- Post Footer -->
        <div class="forum-list__card-footer">
          <span class="forum-list__card-author">{{ post.userNickname }}</span>
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

interface Post {
  id: number
  title: string
  content: string
  userNickname: string
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

    const newList = response.records
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

function onLoad() {
  loadPosts()
}

function onPostClick(post: Post) {
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

.forum-list__card-author {
  font-weight: var(--font-weight-medium);
  color: var(--text-secondary);
}

.forum-list__card-stats {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.forum-list__stat {
  color: var(--text-tertiary);
}
</style>
