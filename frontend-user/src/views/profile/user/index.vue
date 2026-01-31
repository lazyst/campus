<template>
  <div class="user-profile-page">
    <!-- 导航栏 -->
    <NavBar :title="pageTitle" :left-arrow="true" @click-left="goBack" />

    <!-- 加载中 -->
    <div v-if="loading" class="profile-loading">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <!-- 用户信息 -->
    <template v-else-if="user">
      <!-- 头部信息 -->
      <div class="profile-header">
        <div class="profile-avatar">
          <img v-if="user.avatar" :src="getImageUrl(user.avatar)" alt="头像" />
          <span v-else>{{ user.nickname?.charAt(0) || '匿名' }}</span>
        </div>
        <div class="profile-info">
          <h2 class="profile-nickname">{{ user.nickname || '匿名用户' }}</h2>
          <div class="profile-meta">
            <span v-if="user.gender" class="profile-gender">
              {{ user.gender === 1 ? '男' : '女' }}
            </span>
            <span v-if="user.grade || user.major" class="profile-school">
              {{ user.grade }}{{ user.major }}
            </span>
          </div>
          <div class="profile-join-time" v-if="user.createdAt">
            加入于 {{ formatJoinTime(user.createdAt) }}
          </div>
        </div>
      </div>

      <!-- 个人简介 -->
      <div v-if="user.bio" class="profile-bio">
        <p>{{ user.bio }}</p>
      </div>

      <!-- 分类导航 -->
      <div class="profile-tabs">
        <button
          class="profile-tab"
          :class="{ 'profile-tab--active': activeTab === 'posts' }"
          @click="switchTab('posts')"
        >
          <span class="tab-text">帖子</span>
          <span class="tab-count">{{ posts.length }}</span>
          <div class="tab-underline"></div>
        </button>
        <button
          class="profile-tab"
          :class="{ 'profile-tab--active': activeTab === 'items' }"
          @click="switchTab('items')"
        >
          <span class="tab-text">闲置</span>
          <span class="tab-count">{{ items.length }}</span>
          <div class="tab-underline"></div>
        </button>
      </div>

      <!-- 帖子列表 -->
      <div v-show="activeTab === 'posts'" class="profile-content">
        <div v-if="postsLoading" class="profile-section-loading">
          <div class="loading-spinner small"></div>
        </div>
        <div v-else-if="posts.length === 0" class="profile-section-empty">
          <p>暂无帖子</p>
        </div>
        <div v-else class="posts-grid">
          <div
            v-for="post in posts"
            :key="post.id"
            class="post-card"
            @click="goToPostDetail(post.id)"
          >
            <div class="post-card-header">
              <span class="post-card-title">{{ post.title }}</span>
            </div>
            <p class="post-card-preview">{{ truncateContent(post.content) }}</p>
            <div class="post-card-footer">
              <span>{{ formatTime(post.createdAt) }}</span>
              <span class="post-card-likes">
                <svg class="post-card-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
                </svg>
                {{ post.likeCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 闲置列表 -->
      <div v-show="activeTab === 'items'" class="profile-content">
        <div v-if="itemsLoading" class="profile-section-loading">
          <div class="loading-spinner small"></div>
        </div>
        <div v-else-if="items.length === 0" class="profile-section-empty">
          <p>暂无闲置</p>
        </div>
        <div v-else class="items-waterfall">
          <div
            v-for="item in items"
            :key="item.id"
            class="item-card"
            @click="goToItemDetail(item.id)"
          >
            <div class="item-card-image">
              <img v-if="item.image" :src="item.image" :alt="item.title" />
              <div v-else class="item-card-placeholder">
                <span>物品</span>
              </div>
              <span v-if="item.type === 1" class="item-card-type">求购</span>
              <span v-else class="item-card-type item-card-type--sell">出售</span>
            </div>
            <div class="item-card-content">
              <h4 class="item-card-title">{{ item.title }}</h4>
              <div class="item-card-footer">
                <span class="item-card-price">¥{{ item.price }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 用户不存在 -->
    <div v-else class="profile-not-found">
      <p>用户不存在</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getUserDetailInfo } from '@/api/modules/user';
import { getPostsByUserId } from '@/api/modules/post';
import { getItemsByUserId } from '@/api/modules/item';
import NavBar from '@/components/navigation/NavBar.vue';
import { getImageUrl } from '@/utils/imageUrl';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

const router = useRouter();
const route = useRoute();

const userId = computed(() => Number(route.params.id));

const loading = ref(true);
const postsLoading = ref(true);
const itemsLoading = ref(true);
const activeTab = ref('posts');
const user = ref<any>(null);
const posts = ref<any[]>([]);
const items = ref<any[]>([]);

const pageTitle = computed(() => {
  return user.value?.nickname ? `${user.value.nickname}的主页` : '用户主页';
});

function formatTime(time: string) {
  if (!time) return '';
  return dayjs(time).fromNow();
}

function formatJoinTime(time: string) {
  if (!time) return '';
  return dayjs(time).format('YYYY年MM月');
}

function truncateContent(content: string, maxLength = 50) {
  if (!content) return '';
  return content.length > maxLength ? content.substring(0, maxLength) + '...' : content;
}

function goBack() {
  router.back();
}

function switchTab(tab: string) {
  activeTab.value = tab;
}

function goToPostDetail(postId: number) {
  router.push(`/forum/detail/${postId}`);
}

function goToItemDetail(itemId: number) {
  router.push(`/trade/${itemId}`);
}

async function loadUserProfile() {
  loading.value = true;
  try {
    const data = await getUserDetailInfo(userId.value);
    user.value = data;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    user.value = null;
  } finally {
    loading.value = false;
  }
}

async function loadUserPosts() {
  postsLoading.value = true;
  try {
    const data = await getPostsByUserId(userId.value);
    posts.value = data?.records || data || [];
  } catch (error) {
    console.error('获取用户帖子失败:', error);
    posts.value = [];
  } finally {
    postsLoading.value = false;
  }
}

async function loadUserItems() {
  itemsLoading.value = true;
  try {
    const data = await getItemsByUserId(userId.value);
    items.value = (data || []).map((item: any) => {
      // 处理图片字段
      let image = '';
      if (item.images) {
        try {
          const images = JSON.parse(item.images);
          image = Array.isArray(images) && images.length > 0 ? getImageUrl(images[0]) : '';
        } catch {
          image = getImageUrl(item.images);
        }
      }
      return { ...item, image };
    });
  } catch (error) {
    console.error('获取用户闲置失败:', error);
    items.value = [];
  } finally {
    itemsLoading.value = false;
  }
}

onMounted(async () => {
  await loadUserProfile();
  await Promise.all([loadUserPosts(), loadUserItems()]);
});
</script>

<style scoped>
.user-profile-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-top: var(--nav-height);
}

.profile-loading,
.profile-not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: var(--text-secondary);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border-light);
  border-top-color: var(--color-primary-500);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: var(--space-3);
}

.loading-spinner.small {
  width: 24px;
  height: 24px;
  border-width: 2px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 头部信息 */
.profile-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6) var(--page-padding);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
}

.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary-600), var(--color-primary-800));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-semibold);
  color: white;
  overflow: hidden;
  flex-shrink: 0;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-nickname {
  font-size: var(--text-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.profile-meta {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.profile-gender,
.profile-school {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  padding: 2px var(--space-2);
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-sm);
}

.profile-join-time {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

/* 个人简介 */
.profile-bio {
  padding: var(--space-4) var(--page-padding);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
}

.profile-bio p {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--line-height-relaxed);
  margin: 0;
}

/* 分类导航 */
.profile-tabs {
  display: flex;
  gap: var(--space-2);
  padding: 0 var(--page-padding);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  position: relative;
  position: sticky;
  top: var(--nav-height);
  z-index: var(--z-sticky);
}

.profile-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) 0;
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-secondary);
  background: none;
  border: none;
  cursor: pointer;
  transition: color var(--transition-fast);
  position: relative;
  z-index: 1;
}

.profile-tab:hover {
  color: var(--text-primary);
}

.profile-tab--active {
  color: var(--color-primary-700);
}

.tab-text {
  font-weight: var(--font-weight-medium);
}

.tab-count {
  font-size: var(--text-xs);
  padding: 2px 6px;
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-full);
  color: var(--text-tertiary);
}

.profile-tab--active .tab-count {
  background-color: var(--color-primary-100);
  color: var(--color-primary-700);
}

/* 底部下划线指示器 */
.tab-underline {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 40px;
  height: 2px;
  background-color: var(--color-primary-700);
  border-radius: 1px;
  transform: translateX(-50%) scaleX(0);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.profile-tab--active .tab-underline {
  transform: translateX(-50%) scaleX(1);
}

/* 内容区域 */
.profile-content {
  padding: var(--space-3);
}

.profile-section-loading,
.profile-section-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) 0;
  color: var(--text-tertiary);
}

/* 帖子卡片 - 仿ForumList风格 */
.posts-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.post-card {
  padding: var(--space-4);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.post-card:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

.post-card-header {
  margin-bottom: var(--space-2);
}

.post-card-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  line-height: var(--line-height-tight);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-card-preview {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--line-height-normal);
  margin: 0 0 var(--space-3) 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.post-card-likes {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.post-card-icon {
  width: 14px;
  height: 14px;
}

/* 闲置瀑布流 - 仿TradeList风格 */
.items-waterfall {
  column-count: 2;
  column-gap: var(--space-3);
}

.item-card {
  break-inside: avoid;
  margin-bottom: var(--space-3);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
}

.item-card:active {
  transform: scale(0.98);
}

.item-card-image {
  position: relative;
  width: 100%;
  aspect-ratio: 4/3;
  background-color: var(--bg-tertiary);
}

.item-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-card-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-secondary);
  color: var(--text-tertiary);
  font-size: var(--text-sm);
}

.item-card-type {
  position: absolute;
  top: var(--space-2);
  left: var(--space-2);
  padding: 2px var(--space-2);
  font-size: 10px;
  font-weight: var(--font-weight-semibold);
  background-color: var(--color-success-600);
  color: white;
  border-radius: var(--radius-sm);
}

.item-card-type--sell {
  background-color: var(--color-primary-700);
}

.item-card-content {
  padding: var(--space-3);
}

.item-card-title {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
  line-height: var(--line-height-normal);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.item-card-price {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-error-600);
}

@media (min-width: 768px) {
  .items-waterfall {
    column-count: 3;
    column-gap: var(--space-4);
    padding: var(--space-2);
  }

  .item-card {
    margin-bottom: var(--space-4);
  }
}
</style>
