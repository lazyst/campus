<template>
  <div class="forum-detail-page">
    <!-- 状态栏 -->
    <div class="detail-status-bar">
      <button class="detail-back-btn" @click="goBack">‹</button>
      <span class="detail-page-title">详情</span>
      <button class="detail-more-btn">⋯</button>
    </div>

    <!-- 帖子内容 -->
    <div class="detail-content">
      <!-- 作者信息 -->
      <div class="detail-author-section">
        <div class="detail-author-avatar">
          <span>{{ post.author.charAt(0) }}</span>
        </div>
        <div class="detail-author-info">
          <span class="detail-author-name">{{ post.author }}</span>
          <span class="detail-post-meta">{{ post.time }} · {{ post.category }}</span>
        </div>
      </div>

      <!-- 帖子标题 -->
      <h1 class="detail-post-title">{{ post.title }}</h1>

      <!-- 帖子正文 -->
      <div class="detail-post-body">
        {{ post.content }}
      </div>

      <!-- 互动操作 -->
      <div class="detail-post-actions">
        <button 
          class="detail-action-item" 
          :class="{ 'detail-action-item--active': isLiked }"
          @click="toggleLike"
        >
          <span class="detail-action-label">{{ isLiked ? '已赞' : '点赞' }}</span>
          <span class="detail-action-count">{{ post.likes }}</span>
        </button>
        <button class="detail-action-item" @click="focusComment">
          <span class="detail-action-label">评论</span>
          <span class="detail-action-count">{{ post.comments }}</span>
        </button>
        <button 
          class="detail-action-item" 
          :class="{ 'detail-action-item--active': isCollected }"
          @click="toggleCollect"
        >
          <span class="detail-action-label">{{ isCollected ? '已收藏' : '收藏' }}</span>
        </button>
      </div>
    </div>

    <!-- 评论区域 -->
    <div class="detail-comments-section">
      <h2 class="detail-comments-title">评论 {{ comments.length }}</h2>

      <!-- 评论列表 -->
      <div class="detail-comments-list">
        <div 
          v-for="comment in comments" 
          :key="comment.id"
          class="detail-comment-item"
        >
          <div class="detail-comment-avatar">
            <span>{{ comment.author.charAt(0) }}</span>
          </div>
          <div class="detail-comment-content">
            <div class="detail-comment-header">
              <span class="detail-comment-author">{{ comment.author }}</span>
              <span class="detail-comment-time">{{ comment.time }}</span>
            </div>
            <p class="detail-comment-text">{{ comment.content }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 评论输入框 -->
    <div class="detail-comment-input-area">
      <input 
        v-model="newComment"
        type="text"
        class="detail-comment-input"
        placeholder="说点什么..."
        @keyup.enter="submitComment"
      />
      <button class="detail-send-btn" @click="submitComment">发送</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

const isLiked = ref(false);
const isCollected = ref(false);
const newComment = ref('');

// 模拟帖子数据
const post = ref({
  id: 1,
  author: '小明学长',
  title: '出售MacBook Pro 13寸 2020款 M1芯片',
  content: `自用MacBook Pro，2020款M1芯片，8+256GB配置。买来主要用于写论文和日常办公，现在毕业了用不到，准备转让给有需要的学弟学妹。

外观保护得很好，没有任何划痕和磕碰。电池健康度98%，循环次数不到100次。充电器、数据线、说明书等配件齐全。

购买时花了9999，现在便宜出，有意向的可以私信我，可以小刀。最好是当面交易，可以现场验机。`,
  time: '2小时前',
  category: '电脑数码',
  likes: 42,
  comments: 12,
});

// 模拟评论数据
const comments = ref([
  {
    id: 1,
    author: '学习达人',
    content: '学长，这个电脑还在吗？可以优惠吗？',
    time: '1小时前',
  },
  {
    id: 2,
    author: '校园小助手',
    content: '在的，诚心要可以便宜点',
    time: '30分钟前',
  },
]);

function goBack() {
  router.back();
}

function toggleLike() {
  isLiked.value = !isLiked.value;
  post.value.likes += isLiked.value ? 1 : -1;
}

function toggleCollect() {
  isCollected.value = !isCollected.value;
}

function focusComment() {
  // 聚焦评论输入框
}

function submitComment() {
  if (!newComment.value.trim()) return;
  
  comments.value.unshift({
    id: Date.now(),
    author: '我',
    content: newComment.value,
    time: '刚刚',
  });
  
  post.value.comments++;
  newComment.value = '';
}
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
  gap: var(--space-4);
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
</style>
