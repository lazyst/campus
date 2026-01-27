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
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import SearchBar from '@/components/SearchBar.vue';
import CategoryCard from '@/components/CategoryCard.vue';
import PostCard from '@/components/PostCard.vue';
import BottomNav from '@/components/BottomNav.vue';

const router = useRouter();

const searchQuery = ref('');
const activeNav = ref('home');

// 分类数据
const categories = [
  { name: '交流', color: 'purple' },
  { name: '学习', color: 'amber' },
  { name: '搭子', color: 'pink' },
  { name: '闲置', color: 'green' },
];

// 模拟帖子数据
const posts = ref([
  {
    id: 1,
    author: '小明学长',
    avatar: '',
    title: '出售MacBook Pro 13寸 2020款 M1芯片',
    excerpt: '自用电脑，95新，无划痕，电池健康度98%。买来主要用于写论文，现在毕业了用不到。',
    time: new Date(Date.now() - 1000 * 60 * 10), // 10分钟前
    tags: ['电脑数码'],
    likes: 42,
    comments: 12,
    views: 328,
  },
  {
    id: 2,
    author: '学习达人',
    avatar: '',
    title: '找一个一起考研的搭子，互相监督',
    excerpt: '本人计算机专业，二战上岸学长，想找志同道合的研友一起学习，互相监督进步。',
    time: new Date(Date.now() - 1000 * 60 * 60), // 1小时前
    tags: ['考研搭子'],
    likes: 128,
    comments: 45,
    views: 892,
  },
  {
    id: 3,
    author: '图书馆小助手',
    avatar: '',
    title: '高等数学辅导，期末考试辅导',
    excerpt: '本人数学专业研究生，可以辅导高数上/下，期末冲刺，考研数学辅导。',
    time: new Date(Date.now() - 1000 * 60 * 60 * 2), // 2小时前
    tags: ['学习'],
    likes: 56,
    comments: 8,
    views: 245,
  },
]);

function handleSearch(query: string) {
  console.log('搜索:', query);
  // TODO: 实现搜索功能
}

function handleCategoryClick(category: string) {
  console.log('点击分类:', category);
  // TODO: 跳转到分类页面
}

function goToMessages() {
  router.push('/messages');
}

function goToPostDetail(id: number | string) {
  router.push(`/post/${id}`);
}

function handleNavChange(name: string) {
  activeNav.value = name;
  // TODO: 处理导航切换
}
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
