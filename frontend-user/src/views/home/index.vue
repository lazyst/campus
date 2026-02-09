<template>
  <div class="home-page">
    <!-- 状态栏 -->
    <div class="status-bar">
      <span class="time">9:41</span>
      <div class="status-icons">
        <span>📶</span>
        <span>📡</span>
        <span>🔋</span>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <SearchBar 
        v-model="searchQuery"
        @search="handleSearch"
      />
      <div class="message-btn" @click="goToMessages">
        <span>💬</span>
      </div>
    </div>

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
    <div class="posts-section">
      <div class="section-header">
        <h2>{{ isSearching ? '搜索结果' : '最新帖子' }}</h2>
        <span v-if="searchQuery" class="clear-search-btn" @click="clearSearch">清除 ›</span>
      </div>

      <!-- 搜索中状态 -->
      <div v-if="loading" class="posts-loading">
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
    </div>

    <!-- 底部导航 -->
    <BottomNav 
      :active="activeNav"
      @change="handleNavChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getPosts, searchPosts } from '@/api/modules/post';
import { showToast } from '@/services/toastService';
import BottomNav from '@/components/BottomNav.vue';
import SearchBar from '@/components/SearchBar.vue';
import CategoryCard from '@/components/CategoryCard.vue';
import PostCard from '@/components/PostCard.vue';

const router = useRouter();

const searchQuery = ref('');
const activeNav = ref('home');
const loading = ref(false);
const isSearching = ref(false);

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

async function loadPosts() {
  loading.value = true;

  try {
    const response = await getPosts({ size: 20 });
    posts.value = response.records || [];
  } catch (error: any) {
    console.error('获取帖子列表失败:', error);
    showToast(error.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

async function handleSearch(query: string) {
  if (!query.trim()) {
    // 空搜索词时加载默认帖子
    await loadPosts();
    return;
  }

  isSearching.value = true;
  loading.value = true;

  try {
    const response = await searchPosts(query, { size: 20 });
    posts.value = response.records || [];
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
  loadPosts();
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

onMounted(() => {
  loadPosts();
});
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #F8FAFC;
  /* 底部导航栏高度 + 安全区域 + 额外空间 */
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

.status-icons {
  display: flex;
  gap: 4px;
  font-size: 12px;
}

.search-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: white;
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

.categories-section {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: white;
  overflow-x: auto;
}

.posts-section {
  padding: 16px;
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
</style>
