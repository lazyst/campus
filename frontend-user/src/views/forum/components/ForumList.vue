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
            <!-- 点赞 -->
            <button
              class="forum-list__action-btn"
              :class="{ 'forum-list__action-btn--active': post.isLiked }"
              @click.stop="handleLike(post)"
            >
              <svg class="forum-list__stat-icon" viewBox="0 0 24 24" :fill="post.isLiked ? 'currentColor' : 'none'" :stroke="post.isLiked ? 'currentColor' : 'currentColor'" stroke-width="2">
                <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
              </svg>
              {{ post.likeCount }}
            </button>
            <!-- 评论 -->
            <button class="forum-list__action-btn" @click.stop="onPostClick(post)">
              <svg class="forum-list__stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
              </svg>
              {{ post.commentCount }}
            </button>
            <!-- 收藏 -->
            <button
              class="forum-list__action-btn"
              :class="{ 'forum-list__action-btn--collected': post.isCollected }"
              @click.stop="handleCollect(post)"
            >
              <svg class="forum-list__stat-icon" viewBox="0 0 24 24" :fill="post.isCollected ? 'currentColor' : 'none'" :stroke="post.isCollected ? 'currentColor' : 'currentColor'" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
              </svg>
              {{ post.collectCount || 0 }}
            </button>
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

    <!-- 登录确认弹窗 -->
    <Dialog
      v-model:visible="showLoginDialog"
      title="提示"
      message="该操作需要登录，是否前往登录？"
      theme="warning"
      confirmText="去登录"
      cancelText="取消"
      @confirm="goToLogin"
      @cancel="showLoginDialog = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPosts } from '@/api/modules'
import { toggleLikePost, checkPostLiked, toggleCollectPost, checkPostCollected } from '@/api/modules/post'
import { useUserStore } from '@/stores/user'
import Skeleton from '@/components/feedback/Skeleton.vue'
import Dialog from '@/components/interactive/Dialog.vue'
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
  collectCount?: number
  isLiked?: boolean
  isCollected?: boolean
}

interface Props {
  boardId?: number
}

const props = withDefaults(defineProps<Props>(), {
  boardId: undefined
})

const router = useRouter()
const userStore = useUserStore()
const list = ref<Post[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const showLoginDialog = ref(false)
const isOperating = ref(false) // 防止重复点击

// 待执行的操作
let pendingAction: (() => void) | null = null

// 检查登录状态
function checkLogin(action: () => void) {
  if (!userStore.token) {
    pendingAction = action
    showLoginDialog.value = true
    return false
  }
  return true
}

// 前往登录
function goToLogin() {
  showLoginDialog.value = false
  router.push('/login')
}

// 处理点赞
async function handleLike(post: Post) {
  // 防止重复点击
  if (isOperating.value) return

  if (checkLogin(() => handleLike(post))) {
    isOperating.value = true
    const previousLiked = post.isLiked || false
    const previousCount = post.likeCount || 0

    // 乐观更新
    post.isLiked = !previousLiked
    post.likeCount = post.isLiked ? previousCount + 1 : Math.max(0, previousCount - 1)

    try {
      await toggleLikePost(post.id)
    } catch (error: any) {
      // 检查是否是401错误（登录过期）
      if (error.message?.includes('登录已过期') || error.message?.includes('请先登录')) {
        // 401错误已经被全局处理清除了token，这里需要刷新页面恢复状态
        // 暂时不做处理，让用户重新操作
      } else {
        console.error('点赞失败:', error)
      }
      // 回滚
      post.isLiked = previousLiked
      post.likeCount = previousCount
    } finally {
      isOperating.value = false
    }
  }
}

// 处理收藏
async function handleCollect(post: Post) {
  // 防止重复点击
  if (isOperating.value) return

  if (checkLogin(() => handleCollect(post))) {
    isOperating.value = true
    const previousCollected = post.isCollected || false
    const previousCount = post.collectCount || 0

    // 乐观更新
    post.isCollected = !previousCollected
    post.collectCount = post.isCollected ? previousCount + 1 : Math.max(0, previousCount - 1)

    try {
      await toggleCollectPost(post.id)
    } catch (error: any) {
      // 检查是否是401错误（登录过期）
      if (error.message?.includes('登录已过期') || error.message?.includes('请先登录')) {
        // 401错误已经被全局处理清除了token，这里需要刷新页面恢复状态
        // 暂时不做处理，让用户重新操作
      } else {
        console.error('收藏失败:', error)
      }
      // 回滚
      post.isCollected = previousCollected
      post.collectCount = previousCount
    } finally {
      isOperating.value = false
    }
  }
}

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
  justify-content: flex-end;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.forum-list__card-stats {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.forum-list__stat {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--text-tertiary);
}

.forum-list__stat-icon {
  width: 14px;
  height: 14px;
}

.forum-list__action-btn {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-2);
  background: none;
  border: none;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.forum-list__action-btn:active {
  background-color: var(--bg-secondary);
}

.forum-list__action-btn--active {
  color: var(--color-error-500);
}

.forum-list__action-btn--active .forum-list__stat-icon {
  fill: var(--color-error-500);
}

.forum-list__action-btn--collected {
  color: #F59E0B;
}

.forum-list__action-btn--collected .forum-list__stat-icon {
  fill: #F59E0B;
  stroke: #F59E0B;
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
