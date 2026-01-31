<template>
  <div class="profile-posts-page">
    <NavBar title="我的帖子" :left-arrow="true" @click-left="goBack" />

    <!-- Loading -->
    <div v-if="loading && posts.length === 0" class="posts-state">
      <span class="posts-state-text">加载中...</span>
    </div>

    <!-- Empty -->
    <div v-else-if="posts.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M20 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4Z" stroke="currentColor" stroke-width="1.5"/>
          <path d="M12 11L8 7M12 11L16 7M12 11L12 15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
      </div>
      <p class="empty-text">暂无帖子</p>
      <p class="empty-hint">快去发布一个吧~</p>
    </div>

    <!-- 帖子列表 -->
    <div v-else class="posts-content">
      <div
        v-for="post in posts"
        :key="post.id"
        class="posts-card"
        @click="goToDetail(post.id)"
      >
        <!-- 用户信息 -->
        <div class="posts-card-user">
          <div class="posts-card-avatar">
            <span>{{ (post.userNickname || '匿名').charAt(0) }}</span>
          </div>
          <span class="posts-card-username">{{ post.userNickname || '匿名用户' }}</span>
        </div>

        <!-- 标题 -->
        <h3 class="posts-card-title">{{ post.title }}</h3>

        <!-- 内容预览 -->
        <p class="posts-card-preview">{{ post.content }}</p>

        <!-- 图片（最多显示3张） -->
        <div v-if="post.images && post.images.length > 0" class="posts-card-images">
          <div 
            v-for="(img, index) in post.images.slice(0, 3)" 
            :key="index"
            class="posts-card-image"
          >
            <img :src="getImageUrl(img)" :alt="'图片' + (index + 1)" />
          </div>
          <div v-if="post.images.length > 3" class="posts-card-image-more">
            +{{ post.images.length - 3 }}
          </div>
        </div>

        <!-- 底部 -->
        <div class="posts-card-footer">
          <div class="posts-card-stats">
            <!-- 点赞 -->
            <span class="posts-stat">
              <svg class="posts-stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
              </svg>
              {{ post.likeCount || 0 }}
            </span>
            <!-- 评论 -->
            <span class="posts-stat">
              <svg class="posts-stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
              </svg>
              {{ post.commentCount || 0 }}
            </span>
          </div>
          <!-- 删除按钮 -->
          <button 
            class="posts-delete-btn"
            @click.stop="showDeleteDialog(post)"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Loading More -->
    <div v-if="loading && posts.length > 0" class="posts-state">
      <span class="posts-state-text">加载中...</span>
    </div>

    <!-- Finished -->
    <div v-if="finished && posts.length > 0" class="posts-state">
      <span class="posts-state-text posts-state-text--muted">没有更多了</span>
    </div>

    <!-- 删除确认弹窗 -->
    <Dialog
      v-model:visible="deleteDialogVisible"
      title="删除帖子"
      message="确定要删除这篇帖子吗？"
      theme="danger"
      confirm-text="删除"
      :loading="deleteLoading"
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getMyPosts, deletePost } from '@/api/modules/post';
import { showToast } from '@/services/toastService';
import NavBar from '@/components/navigation/NavBar.vue';
import Dialog from '@/components/interactive/Dialog.vue';
import { getImageUrl } from '@/utils/imageUrl';

interface Post {
  id: number;
  title: string;
  content: string;
  images?: string[] | null;
  userNickname?: string;
  likeCount?: number;
  commentCount?: number;
  createdAt?: string;
}

const router = useRouter();
const loading = ref(false);
const finished = ref(false);
const page = ref(1);
const posts = ref<Post[]>([]);

// 删除弹窗相关
const deleteDialogVisible = ref(false);
const deleteLoading = ref(false);
const postToDelete = ref<Post | null>(null);

function parseImages(images: string | null): string[] {
  if (!images) return [];
  try {
    const parsed = JSON.parse(images);
    return Array.isArray(parsed) ? parsed : [parsed];
  } catch {
    return images ? [images] : [];
  }
}

async function loadPosts() {
  if (loading.value || finished.value) return;

  try {
    loading.value = true;
    const response = await getMyPosts({ page: page.value, size: 20 });

    // 兼容 Page 对象和直接数组格式
    let records: any[] = [];
    if (Array.isArray(response)) {
      records = response;
    } else if (response && typeof response === 'object') {
      records = (response as { records?: any[] }).records || [];
    }

    // 解析 images 字段
    const newList = records.map(post => ({
      ...post,
      images: parseImages(post.images)
    }));

    posts.value.push(...newList);
    page.value++;

    if (newList.length < 20) {
      finished.value = true;
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error);
  } finally {
    loading.value = false;
  }
}

function showDeleteDialog(post: Post) {
  postToDelete.value = post;
  deleteDialogVisible.value = true;
}

function cancelDelete() {
  postToDelete.value = null;
  deleteDialogVisible.value = false;
}

async function confirmDelete() {
  if (!postToDelete.value) return;

  try {
    deleteLoading.value = true;
    await deletePost(postToDelete.value.id);
    // 从列表中移除
    posts.value = posts.value.filter(p => p.id !== postToDelete.value!.id);
    showToast('删除成功', 'success');
  } catch (error) {
    console.error('删除帖子失败:', error);
    showToast('删除失败，请重试', 'error');
  } finally {
    deleteLoading.value = false;
    postToDelete.value = null;
    deleteDialogVisible.value = false;
  }
}

function goBack() {
  router.back();
}

function goToDetail(id: number) {
  router.push(`/forum/detail/${id}`);
}

onMounted(() => {
  loadPosts();
});
</script>

<style scoped>
.profile-posts-page {
  min-height: 100vh;
  background-color: var(--bg-page);
}

.posts-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: calc(var(--nav-height) + var(--space-8)) var(--space-4) var(--space-8);
  min-height: 50vh;
}

.posts-state-text {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.posts-state-text--muted {
  font-size: var(--text-xs);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: calc(var(--nav-height) + var(--space-8)) var(--space-4) var(--space-8);
  min-height: 50vh;
}

.empty-icon {
  color: var(--text-tertiary);
  margin-bottom: var(--space-4);
}

.empty-icon svg {
  stroke: currentColor;
}

.empty-text {
  font-family: var(--font-family-sans);
  font-size: var(--text-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.empty-hint {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
}

.posts-content {
  padding: calc(var(--nav-height) + var(--space-3)) var(--space-3) var(--space-3);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.posts-card {
  padding: var(--space-4);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.posts-card:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

.posts-card-user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.posts-card-avatar {
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
}

.posts-card-username {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
}

.posts-card-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
  line-height: var(--line-height-tight);
}

.posts-card-preview {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-3);
  line-height: var(--line-height-normal);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.posts-card-images {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.posts-card-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  overflow: hidden;
  flex-shrink: 0;
}

.posts-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.posts-card-image-more {
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

.posts-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.posts-card-stats {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.posts-stat {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--text-tertiary);
}

.posts-stat-icon {
  width: 14px;
  height: 14px;
}

.posts-delete-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-tertiary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.posts-delete-btn:hover {
  background-color: var(--color-error-50);
  border-color: var(--color-error-200);
  color: var(--color-error-600);
}

.posts-delete-btn:active {
  transform: scale(0.95);
}
</style>
