<template>
  <div class="forum-detail-page">
    <!-- 状态栏 -->
    <div class="detail-status-bar">
      <button class="detail-back-btn" @click="goBack">‹</button>
      <span class="detail-page-title">详情</span>
      <button class="detail-more-btn">⋯</button>
    </div>

    <!-- 骨架屏 - 加载中 -->
    <div v-if="loading" class="detail-content">
      <div class="detail-author-section">
        <div class="skeleton-avatar"></div>
        <div class="skeleton-info">
          <div class="skeleton-text skeleton-author"></div>
          <div class="skeleton-text skeleton-meta"></div>
        </div>
      </div>
      <div class="skeleton-title"></div>
      <div class="skeleton-text skeleton-content"></div>
      <div class="skeleton-text skeleton-content"></div>
      <div class="skeleton-text skeleton-content short"></div>
    </div>

    <!-- 帖子内容 - 加载完成 -->
    <template v-else-if="post">
      <div class="detail-content">
        <!-- 作者信息 -->
        <div class="detail-author-section">
          <div class="detail-author-avatar">
            <img v-if="post.userAvatar" :src="getImageUrl(post.userAvatar)" alt="头像" />
            <span v-else>{{ post.userNickname?.charAt(0) || '匿名' }}</span>
          </div>
          <div class="detail-author-info">
            <span class="detail-author-name">{{ post.userNickname || '匿名用户' }}</span>
            <span class="detail-post-meta">{{ formatTime(post.createdAt) }} · {{ post.boardName }}</span>
          </div>
        </div>

        <!-- 帖子标题 -->
        <h1 class="detail-post-title">{{ post.title }}</h1>

        <!-- 帖子正文 -->
        <div class="detail-post-body">
          {{ post.content }}
        </div>

        <!-- Post Images -->
        <div v-if="postImages.length > 0" class="detail-post-images">
          <div 
            v-for="(img, index) in postImages" 
            :key="index"
            class="detail-post-image"
          >
            <img :src="getImageUrl(img)" :alt="'图片' + (index + 1)" @click="previewImage(index)" />
          </div>
        </div>

        <!-- 互动操作 -->
        <div class="detail-post-actions">
          <button 
            class="detail-action-item" 
            :class="{ 'detail-action-item--active': isLiked }"
            @click="handleLike"
          >
            <span class="detail-action-label">{{ isLiked ? '已赞' : '点赞' }}</span>
            <span class="detail-action-count">{{ post.likeCount || 0 }}</span>
          </button>
          <button class="detail-action-item" @click="focusComment">
            <span class="detail-action-label">评论</span>
            <span class="detail-action-count">{{ comments.length }}</span>
          </button>
          <button 
            class="detail-action-item" 
            :class="{ 'detail-action-item--active': isCollected }"
            @click="handleCollect"
          >
            <span class="detail-action-label">{{ isCollected ? '已收藏' : '收藏' }}</span>
            <span class="detail-action-count">{{ post.collectCount || 0 }}</span>
          </button>
        </div>
      </div>
    </template>

    <!-- 帖子不存在 -->
    <div v-else class="detail-content">
      <div class="empty-state">
        <p>帖子不存在或已被删除</p>
      </div>
    </div>

    <!-- 评论区域 -->
    <div class="detail-comments-section">
      <h2 class="detail-comments-title">评论 {{ comments.length }}</h2>

      <!-- 评论骨架屏 -->
      <div v-if="commentsLoading" class="detail-comments-list">
        <div v-for="n in 3" :key="n" class="detail-comment-item">
          <div class="skeleton-avatar small"></div>
          <div class="skeleton-comment-info">
            <div class="skeleton-text skeleton-comment-author"></div>
            <div class="skeleton-text skeleton-comment-content"></div>
          </div>
        </div>
      </div>

      <!-- 评论列表 -->
      <div v-else-if="comments.length > 0" class="detail-comments-list">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="detail-comment-item"
          :class="{ 'detail-comment-item--highlighted': comment.id === highlightedCommentId }"
          :ref="el => { if (comment.id === highlightedCommentId) highlightedCommentRef = el as HTMLElement }"
        >
          <div class="detail-comment-avatar">
            <img v-if="comment.userAvatar" :src="getImageUrl(comment.userAvatar)" alt="头像" />
            <span v-else>{{ comment.userNickname?.charAt(0) || '匿名' }}</span>
          </div>
          <div class="detail-comment-content">
            <div class="detail-comment-header">
              <span class="detail-comment-author">{{ comment.userNickname || '匿名用户' }}</span>
              <span class="detail-comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            <p class="detail-comment-text">{{ comment.content }}</p>
          </div>
        </div>
      </div>

      <!-- 无评论 -->
      <div v-else class="empty-comments">
        <p>暂无评论，快来抢沙发~</p>
      </div>
    </div>

    <!-- 评论输入框 -->
    <div class="detail-comment-input-area">
      <input 
        v-model="newComment"
        type="text"
        class="detail-comment-input"
        placeholder="说点什么..."
        @keyup.enter="handleSubmitComment"
      />
      <button 
        class="detail-send-btn" 
        @click="handleSubmitComment"
        :disabled="isSubmitting || !newComment.trim()"
      >
        {{ isSubmitting ? '发送中...' : '发送' }}
      </button>
    </div>

    <!-- 登录确认对话框 -->
    <Dialog
      v-model:visible="showLoginDialog"
      title="提示"
      message="该操作需要登录，是否前往登录？"
      theme="default"
      confirmText="去登录"
      cancelText="取消"
      @confirm="goToLogin"
      @cancel="showLoginDialog = false"
    />

    <!-- 图片预览组件 -->
    <ImagePreview
      v-model:visible="showImagePreview"
      :images="postImages"
      :initial-index="previewImageIndex"
      @close="showImagePreview = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getPostById, toggleLikePost, checkPostLiked, toggleCollectPost, checkPostCollected } from '@/api/modules/post';
import { getCommentsByPost, createComment } from '@/api/modules/comment';
import { useUserStore } from '@/stores/user';
import { showToast } from '@/services/toastService';
import Dialog from '@/components/interactive/Dialog.vue';
import ImagePreview from '@/components/ImagePreview.vue';
import { getImageUrl } from '@/utils/imageUrl';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const postId = Number(route.params.id);

const isLiked = ref(false);
const isCollected = ref(false);
const newComment = ref('');
const isSubmitting = ref(false);
const loading = ref(true);
const commentsLoading = ref(false);
const showLoginDialog = ref(false);
const showImagePreview = ref(false);
const previewImageIndex = ref(0);

// 记录需要登录的操作，用于对话框确认后执行
let pendingAction: (() => void) | null = null;

// 帖子数据
const post = ref<any>(null);

// 解析图片数组
const postImages = computed(() => {
  if (!post.value?.images) return []
  try {
    const images = JSON.parse(post.value.images)
    return Array.isArray(images) ? images : [images]
  } catch {
    return post.value.images ? [post.value.images] : []
  }
})

function previewImage(index: number) {
  previewImageIndex.value = index;
  showImagePreview.value = true;
}

// 评论列表
const comments = ref<any[]>([]);

// 高亮的评论ID
const highlightedCommentId = ref<number | null>(null);

// 高亮评论的 DOM 引用
const highlightedCommentRef = ref<HTMLElement | null>(null);

// 格式化时间
function formatTime(time: string) {
  if (!time) return '';
  return dayjs(time).fromNow();
}

// 检查登录状态，如果未登录则显示对话框
function checkLogin(action: () => void) {
  if (!userStore.token) {
    pendingAction = action;
    showLoginDialog.value = true;
    return false;
  }
  return true;
}

// 前往登录页面
function goToLogin() {
  showLoginDialog.value = false;
  router.push('/login');
}

// 获取帖子详情
async function fetchPost() {
  loading.value = true;
  try {
    const data = await getPostById(postId);
    post.value = data;
    
    // 获取点赞和收藏状态
    if (userStore.token) {
      const [liked, collected] = await Promise.all([
        checkPostLiked(postId),
        checkPostCollected(postId)
      ]);
      isLiked.value = liked;
      isCollected.value = collected;
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error);
    post.value = null;
  } finally {
    loading.value = false;
  }
}

// 获取评论列表
async function fetchComments() {
  commentsLoading.value = true;
  try {
    const data = await getCommentsByPost(postId);
    comments.value = data || [];
  } catch (error) {
    console.error('获取评论列表失败:', error);
    comments.value = [];
  } finally {
    commentsLoading.value = false;
  }
}

// 初始化数据
async function initData() {
  await Promise.all([fetchPost(), fetchComments()]);
}

// 页面加载时获取数据
onMounted(async () => {
  await initData();

  // 检查路由参数中是否有 commentId（从通知点击进入）
  const commentId = route.query.commentId;
  if (commentId) {
    const id = Number(commentId);
    if (!isNaN(id)) {
      highlightedCommentId.value = id;

      // 等待 DOM 更新后滚动到高亮的评论
      await nextTick();
      setTimeout(() => {
        if (highlightedCommentRef.value) {
          highlightedCommentRef.value.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      }, 100);

      // 3秒后取消高亮
      setTimeout(() => {
        highlightedCommentId.value = null;
      }, 3000);
    }
  }
});

// 监听路由变化，重新获取数据
watch(() => route.params.id, () => {
  if (route.path.includes('/forum/detail/')) {
    initData();
  }
});

function goBack() {
  router.back();
}

// 处理点赞
async function handleLike() {
  if (checkLogin(() => handleLike())) {
    // 乐观更新
  const previousLiked = isLiked.value;
  const previousCount = post.value?.likeCount || 0;
  
  isLiked.value = !isLiked.value;
  post.value.likeCount = isLiked.value ? previousCount + 1 : Math.max(0, previousCount - 1);
    
    try {
      await toggleLikePost(postId);
      // API 成功，不需要额外处理，乐观更新已生效
    } catch (error) {
      console.error('点赞失败:', error);
      // 回滚
      isLiked.value = previousLiked;
      post.value.likeCount = previousCount;
    }
  }
}

  // 处理收藏
  async function handleCollect() {
    if (checkLogin(() => handleCollect())) {
      // 乐观更新
      const previousCollected = isCollected.value;
      const previousCount = post.value?.collectCount || 0;
      isCollected.value = !isCollected.value;
      post.value.collectCount = isCollected.value ? previousCount + 1 : Math.max(0, previousCount - 1);
      
      try {
        await toggleCollectPost(postId);
        // 重新获取帖子数据以同步收藏数
        await fetchPost();
      } catch (error) {
        console.error('收藏失败:', error);
        // 回滚
        isCollected.value = previousCollected;
        post.value.collectCount = previousCount;
      }
    }
  }

function focusComment() {
  // 聚焦评论输入框
  const input = document.querySelector('.detail-comment-input') as HTMLInputElement;
  input?.focus();
}

// 处理发表评论
async function handleSubmitComment() {
  if (!newComment.value.trim()) return;
  
  if (checkLogin(() => handleSubmitComment())) {
    isSubmitting.value = true;
    
    try {
      await createComment({
        postId: postId,
        content: newComment.value.trim()
      });
      
      // 刷新评论列表
      await fetchComments();
      
      newComment.value = '';
      showToast('评论成功', 'success');
    } catch (error) {
      console.error('发表评论失败:', error);
    } finally {
      isSubmitting.value = false;
    }
  }
}

// 保留旧的函数名作为别名（保持兼容性）
const submitComment = handleSubmitComment;
const toggleLike = handleLike;
const toggleCollect = handleCollect;
</script>

<style scoped>
.forum-detail-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-bottom: 70px;
}

.detail-status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
}

.detail-back-btn {
  font-size: 28px;
  color: var(--text-primary);
  background: none;
  border: none;
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
}

.detail-back-btn:active {
  background-color: var(--bg-secondary);
}

.detail-page-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.detail-more-btn {
  font-size: 24px;
  color: var(--text-primary);
  background: none;
  border: none;
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
}

.detail-more-btn:active {
  background-color: var(--bg-secondary);
}

.detail-content {
  padding: var(--space-4);
}

.detail-author-section {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.detail-author-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  background-color: var(--color-primary-100);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-700);
  overflow: hidden;
}

.detail-author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-author-name {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.detail-post-meta {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.detail-post-title {
  font-size: var(--text-xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-4) 0;
  line-height: var(--line-height-tight);
}

.detail-post-body {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--line-height-relaxed);
  white-space: pre-wrap;
  margin-bottom: var(--space-4);
}

.detail-post-images {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.detail-post-image {
  width: 150px;
  height: 150px;
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
}

.detail-post-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-fast);
}

.detail-post-image:active img {
  transform: scale(0.95);
}

.detail-post-actions {
  display: flex;
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-light);
}

.detail-action-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  background: none;
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--text-secondary);
}

.detail-action-item:active {
  background-color: var(--bg-secondary);
}

.detail-action-item--active {
  color: var(--color-error-500);
}

.detail-action-label {
  font-size: var(--text-sm);
}

.detail-action-count {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.detail-comments-section {
  padding: var(--space-4);
  background-color: var(--bg-card);
  border-top: 1px solid var(--border-light);
}

.detail-comments-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-4) 0;
}

.detail-comments-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.detail-comment-item {
  display: flex;
  gap: var(--space-3);
}

.detail-comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background-color: var(--color-primary-100);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-primary-700);
  flex-shrink: 0;
  overflow: hidden;
}

.detail-comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-comment-content {
  flex: 1;
}

.detail-comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-1);
}

.detail-comment-author {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
}

.detail-comment-time {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.detail-comment-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
  line-height: var(--line-height-normal);
}

.detail-comment-input-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  padding-bottom: calc(var(--space-3) + env(safe-area-inset-bottom));
  background-color: var(--bg-card);
  border-top: 1px solid var(--border-light);
  z-index: var(--z-fixed);
}

.detail-comment-input {
  flex: 1;
  height: 40px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
  background-color: var(--bg-tertiary);
  border: none;
  border-radius: var(--radius-full);
  outline: none;
}

.detail-comment-input:focus {
  background-color: var(--bg-card);
  box-shadow: 0 0 0 2px var(--color-primary-200);
}

.detail-send-btn {
  width: 64px;
  height: 36px;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-inverse);
  background-color: var(--color-primary-700);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
}

.detail-send-btn:active {
  background-color: var(--color-primary-800);
}

.detail-send-btn:disabled {
  background-color: var(--color-gray-300);
  cursor: not-allowed;
}

/* 骨架屏样式 */
.skeleton-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  background: linear-gradient(90deg, var(--color-gray-200) 25%, var(--color-gray-100) 50%, var(--color-gray-200) 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
}

.skeleton-avatar.small {
  width: 36px;
  height: 36px;
}

.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-text {
  height: 14px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, var(--color-gray-200) 25%, var(--color-gray-100) 50%, var(--color-gray-200) 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
}

.skeleton-author {
  width: 80px;
  height: 16px;
}

.skeleton-meta {
  width: 120px;
}

.skeleton-title {
  width: 100%;
  height: 28px;
  border-radius: var(--radius-md);
  margin-bottom: var(--space-4);
  background: linear-gradient(90deg, var(--color-gray-200) 25%, var(--color-gray-100) 50%, var(--color-gray-200) 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
}

.skeleton-content {
  width: 100%;
  height: 16px;
  margin-bottom: var(--space-2);
}

.skeleton-content.short {
  width: 60%;
}

.skeleton-comment-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.skeleton-comment-author {
  width: 60px;
  height: 14px;
}

.skeleton-comment-content {
  width: 100%;
  height: 14px;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 空状态 */
.empty-state,
.empty-comments {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
  color: var(--text-tertiary);
}

/* 评论高亮样式 - 综合方案 */
.detail-comment-item {
  position: relative;
  padding: var(--space-2);
  border-radius: var(--radius-lg);
  transition: background-color 0.4s ease, box-shadow 0.4s ease;
}

.detail-comment-item--highlighted {
  background-color: var(--color-primary-50);
  box-shadow: 0 0 0 1px var(--color-primary-200), 0 2px 8px rgba(30, 58, 138, 0.08);
}

.detail-comment-item--highlighted::before {
  content: '';
  position: absolute;
  top: var(--space-2);
  bottom: var(--space-2);
  left: 0;
  width: 3px;
  background-color: var(--color-primary-600);
  border-radius: 2px;
  animation: borderSlideIn 0.5s ease;
}

.detail-comment-item--highlighted .detail-comment-avatar {
  transform: scale(1.05);
  transition: transform 0.3s ease;
}

.detail-comment-item--highlighted:hover .detail-comment-avatar {
  transform: scale(1.1);
}

@keyframes borderSlideIn {
  from {
    opacity: 0;
    transform: scaleY(0);
  }
  to {
    opacity: 1;
    transform: scaleY(1);
  }
}
</style>
