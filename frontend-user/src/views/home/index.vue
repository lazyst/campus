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
        <h2>最新帖子</h2>
        <span class="more-btn">查看更多 ›</span>
      </div>
      
      <div class="posts-list">
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getPosts } from '@/api/modules/post';
import { showToast } from '@/services/toastService';
import BottomNav from '@/components/BottomNav.vue';

const router = useRouter();

const searchQuery = ref('');
const activeNav = ref('home');
const loading = ref(false);

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

function handleSearch(query: string) {
  // TODO: 实现搜索功能
  console.log('搜索:', query);
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

onMounted(() => {
  loadPosts();
});
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding-bottom: 80px;
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
</style>
