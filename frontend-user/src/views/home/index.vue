<template>
  <div class="home-page">
    <!-- 状态栏 - 移动端显示 -->
    <div class="status-bar status-bar--mobile">
      <span class="time">9:41</span>
      <div class="status-icons">
        <span class="status-icon">信号</span>
        <span class="status-icon">4G</span>
        <span class="status-icon">电量</span>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <SearchBar 
        v-model="searchQuery"
        @search="handleSearch"
      />
      <div class="message-btn" @click="goToMessages">
        <span class="message-icon">消息</span>
      </div>
    </div>

    <ResponsiveContainer>
      <!-- 分类导航 -->
      <div class="categories-section">
        <CategoryCard 
          v-for="category in categories" 
          :key="category.name"
          :name="category.name"
          :color="category.color"
          size="medium"
          @click="handleCategoryClick(category.name)"
        />
      </div>

      <!-- 帖子列表 -->
      <div ref="listRef" class="posts-section">
        <div class="section-header">
          <h2>{{ isSearching ? '搜索结果' : '最新帖子' }}</h2>
          <span v-if="searchQuery" class="clear-search-btn" @click="clearSearch">清除 ›</span>
        </div>

        <!-- 搜索中状态 -->
        <div v-if="loading && posts.length === 0" class="posts-loading">
          <div class="loading-spinner"></div>
          <p class="loading-text">加载中...</p>
        </div>

        <!-- 无结果状态 -->
        <div v-else-if="posts.length === 0" class="posts-empty">
          <p class="empty-text">{{ searchQuery ? '未找到相关帖子' : '暂无帖子' }}</p>
        </div>

        <!-- 帖子列表 -->
        <div v-else class="posts-list">
          <PostCard
            v-for="post in posts"
            :key="post.id"
            :id="post.id"
            :author="post.author"
            :avatar="post.avatar"
            :title="post.title"
            :excerpt="post.excerpt"
            :time="post.time"
            :tags="post.tags"
            :likes="post.likes"
            :comments="post.comments"
            :views="post.views"
            @click="goToPostDetail"
          />
        </div>

        <!-- 加载中 -->
        <div v-if="loading && posts.length > 0" class="posts-loading-more">
          <span class="loading-text">加载中...</span>
        </div>

        <!-- 已加载完 -->
        <div v-if="finished && posts.length > 0" class="posts-finished">
          <span class="finished-text">没有更多了</span>
        </div>

        <!-- 无限滚动 sentinel -->
        <div ref="sentinelRef" class="scroll-sentinel"></div>
      </div>
    </ResponsiveContainer>

    <!-- 底部导航 -->
    <BottomNav 
      :active="activeNav"
      @change="handleNavChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useInfiniteScroll } from '@vueuse/core';
import { getPosts, searchPosts } from '@/api/modules/post';
import { showToast } from '@/services/toastService';
import BottomNav from '@/components/BottomNav.vue';
import SearchBar from '@/components/SearchBar.vue';
import CategoryCard from '@/components/CategoryCard.vue';
import PostCard from '@/components/PostCard.vue';
import ResponsiveContainer from '@/components/layout/ResponsiveContainer.vue';

const router = useRouter();

const searchQuery = ref('');
const activeNav = ref('home');
const loading = ref(false);
const isSearching = ref(false);

// 分页相关
const page = ref(1);
const finished = ref(false);
const listRef = ref<HTMLElement | null>(null);
const sentinelRef = ref<HTMLElement | null>(null);

// 分类数据（UI配置，可保留）
const categories = [
  { name: '交流', color: 'purple' },
  { name: '学习', color: 'amber' },
  { name: '搭子', color: 'pink' },
  { name: '闲置', color: 'green' },
];

interface Post {
  id: number;
  author: string;
  avatar: string;
  title: string;
  excerpt: string;
  time: Date;
  tags: string[];
  likes: number;
  comments: number;
  views: number;
}

const posts = ref<Post[]>([]);

async function loadPosts(isRefresh = false) {
  if (isRefresh) {
    page.value = 1;
    finished.value = false;
  }

  if (loading.value || finished.value) return;

  loading.value = true;

  try {
    const response = await getPosts({ page: page.value, size: 10 });
    const records = response?.records || [];

    if (isRefresh) {
      posts.value = records;
    } else {
      posts.value.push(...records);
    }

    page.value++;

    if (records.length < 10) {
      finished.value = true;
    }
  } catch (error: any) {
    console.error('获取帖子列表失败:', error);
    if (isRefresh) {
      showToast(error.message || '加载失败');
    }
  } finally {
    loading.value = false;
  }
}

async function handleSearch(query: string) {
  if (!query.trim()) {
    // 空搜索词时加载默认帖子
    await loadPosts(true);
    return;
  }

  isSearching.value = true;
  finished.value = false;
  page.value = 1;
  loading.value = true;

  try {
    const response = await searchPosts(query, { page: page.value, size: 10 });
    const records = response?.records || [];
    posts.value = records;
    page.value++;

    if (records.length < 10) {
      finished.value = true;
    }

    if (posts.value.length === 0) {
      showToast('未找到相关帖子');
    }
  } catch (error: any) {
    console.error('搜索帖子失败:', error);
    showToast(error.message || '搜索失败');
  } finally {
    loading.value = false;
    isSearching.value = false;
  }
}

function clearSearch() {
  searchQuery.value = '';
  loadPosts(true);
}

function handleCategoryClick(category: string) {
  console.log('点击分类:', category);
  // TODO: 跳转到分类页面
}

function goToMessages() {
  router.push('/messages');
}

function goToPostDetail(id: number | string) {
  router.push(`/forum/detail/${id}`);
}

function handleNavChange(name: string) {
  activeNav.value = name;
  // TODO: 处理导航切换
}

// 监听搜索词变化（防抖）
let searchTimeout: number;
watch(searchQuery, (newQuery) => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    handleSearch(newQuery);
  }, 300);
});

// 无限滚动 - 使用 Intersection Observer
let observer: IntersectionObserver | undefined;

onMounted(() => {
  loadPosts(true);

  // 使用 Intersection Observer 监听 sentinel 元素
  observer = new IntersectionObserver(
    (entries) => {
      const entry = entries[0];
      if (entry.isIntersecting && !loading.value && !finished.value && !isSearching.value) {
        loadPosts();
      }
    },
    { rootMargin: '100px' }
  );

  if (sentinelRef.value) {
    observer.observe(sentinelRef.value);
  }
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding-bottom: calc(var(--tabbar-height) + var(--page-safe-bottom, 16px));
}

.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  font-size: 15px;
  font-weight: 600;
  color: #1E293B;
}

.status-bar--mobile {
  display: flex;
}

@media (min-width: 1024px) {
  .status-bar--mobile {
    display: none;
  }

  .home-page {
    padding-bottom: var(--space-6);
  }
}

.status-icons {
  display: flex;
  gap: 4px;
  font-size: 12px;
}

.status-icon {
  display: inline-block;
}

.search-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: white;
}

@media (min-width: 1024px) {
  .search-section {
    max-width: 900px;
    margin: 0 auto;
    padding: 16px 24px;
    border-radius: 0 0 16px 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  }
}

.search-section .search-bar {
  flex: 1;
}

.message-btn {
  width: 36px;
  height: 36px;
  background: #6366F1;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  cursor: pointer;
}

@media (min-width: 1024px) {
  .message-btn {
    width: 44px;
    height: 44px;
    font-size: 20px;
  }
}

.message-icon {
  display: inline-block;
}

.categories-section {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: white;
  overflow-x: auto;
}

@media (min-width: 768px) {
  .categories-section {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (min-width: 1024px) {
  .categories-section {
    max-width: 900px;
    margin: 0 auto;
    padding: 20px 24px;
    gap: 16px;
  }
}

.posts-section {
  padding: 16px;
}

@media (min-width: 1024px) {
  .posts-section {
    max-width: 900px;
    margin: 0 auto;
    padding: 24px;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

@media (min-width: 1024px) {
  .section-header h2 {
    font-size: 20px;
  }
}

.more-btn {
  font-size: 14px;
  color: #94A3B8;
  cursor: pointer;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

@media (min-width: 1024px) {
  .posts-list {
    gap: 16px;
  }
}

.posts-loading,
.posts-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 16px;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #E2E8F0;
  border-top-color: #6366F1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 12px;
  font-size: 14px;
  color: #94A3B8;
}

.empty-text {
  font-size: 14px;
  color: #94A3B8;
}

.clear-search-btn {
  font-size: 14px;
  color: #6366F1;
  cursor: pointer;
}

.posts-loading-more,
.posts-finished {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4) 0;
}

.finished-text {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.scroll-sentinel {
  height: 20px;
  width: 100%;
}
</style>
