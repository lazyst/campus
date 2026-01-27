<template>
  <div class="profile-posts-page">
    <!-- 状态栏 -->
    <div class="posts-status-bar">
      <button class="posts-back-btn" @click="goBack">‹</button>
      <span class="posts-page-title">我的帖子</span>
    </div>

    <!-- 标签页 -->
    <div class="posts-tabs">
      <button 
        v-for="(tab, index) in tabs" 
        :key="tab.key"
        class="posts-tab-item"
        :class="{ 'posts-tab-item--active': activeTab === index }"
        @click="activeTab = index"
      >
        <span class="posts-tab-label">{{ tab.label }}</span>
        <span v-if="activeTab === index" class="posts-tab-indicator"></span>
      </button>
    </div>

    <!-- 内容列表 -->
    <div class="posts-content-list">
      <div 
        v-for="item in items" 
        :key="item.id"
        class="posts-list-item"
        @click="goToDetail(item.id)"
      >
        <div class="posts-item-image">
          <span v-if="!item.image">图片</span>
        </div>
        <div class="posts-item-info">
          <h3 class="posts-item-title">{{ item.title }}</h3>
          <p class="posts-item-desc">{{ item.description }}</p>
          <div class="posts-item-footer">
            <div class="posts-item-tags">
              <span 
                v-for="tag in item.tags" 
                :key="tag"
                class="posts-item-tag"
              >
                {{ tag }}
              </span>
            </div>
            <span v-if="item.status" class="posts-item-status" :class="item.status">
              {{ statusText[item.status] }}
            </span>
            <span class="posts-item-price">¥{{ item.price }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="posts-bottom-actions">
      <button class="posts-action-btn posts-action-btn--primary" @click="goToCreate">
        发布新闲置
      </button>
      <button class="posts-action-btn posts-action-btn--secondary" @click="goToManage">
        批量管理
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const activeTab = ref(0);

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'published', label: '已发布' },
  { key: 'sold', label: '已售出' },
  { key: 'draft', label: '草稿' },
];

const statusText: Record<string, string> = {
  sold: '已售',
  reserved: '已预定',
};

interface Item {
  id: number;
  title: string;
  description: string;
  price: number;
  tags: string[];
  status?: 'sold' | 'reserved';
  image?: string;
}

const items = ref<Item[]>([
  {
    id: 1,
    title: 'MacBook Pro 13寸 2020款',
    description: '95新，无划痕，配件齐全',
    price: 128,
    tags: ['电脑数码'],
    status: 'sold',
  },
  {
    id: 2,
    title: '高等数学辅导',
    description: '期末考试辅导，高数上/下都可以',
    price: 50,
    tags: ['学习'],
  },
  {
    id: 3,
    title: '考研搭子组队',
    description: '计算机专业研友互相监督学习',
    price: 0,
    tags: ['考研搭子'],
  },
]);

function goBack() {
  router.back();
}

function goToDetail(id: number) {
  router.push(`/trade/${id}`);
}

function goToCreate() {
  router.push('/trade/create');
}

function goToManage() {
  console.log('批量管理');
}
</script>

<style scoped>
.profile-posts-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
  padding-bottom: 80px;
}

.posts-status-bar {
  display: flex;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
}

.posts-back-btn {
  font-size: 28px;
  color: var(--text-primary);
  background: none;
  border: none;
  cursor: pointer;
}

.posts-back-btn:active {
  opacity: 0.7;
}

.posts-page-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-left: var(--space-8);
}

.posts-tabs {
  display: flex;
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
}

.posts-tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-3) 0;
  background: none;
  border: none;
  cursor: pointer;
  position: relative;
}

.posts-tab-label {
  font-size: var(--text-base);
  color: var(--text-secondary);
}

.posts-tab-item--active .posts-tab-label {
  color: var(--color-primary-700);
  font-weight: var(--font-weight-medium);
}

.posts-tab-indicator {
  position: absolute;
  bottom: 0;
  width: 24px;
  height: 3px;
  background-color: var(--color-primary-700);
  border-radius: var(--radius-full);
}

.posts-content-list {
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.posts-list-item {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
}

.posts-list-item:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

.posts-item-image {
  width: 80px;
  height: 80px;
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  flex-shrink: 0;
}

.posts-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.posts-item-title {
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.posts-item-desc {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.posts-item-footer {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-1);
}

.posts-item-tags {
  display: flex;
  gap: var(--space-1);
}

.posts-item-tag {
  padding: 2px var(--space-2);
  font-size: 10px;
  background-color: var(--color-primary-50);
  color: var(--color-primary-700);
  border-radius: var(--radius-sm);
}

.posts-item-status {
  padding: 2px var(--space-2);
  font-size: 10px;
  font-weight: var(--font-weight-medium);
  border-radius: var(--radius-sm);
}

.posts-item-status.sold {
  background-color: var(--color-success-50);
  color: var(--color-success-600);
}

.posts-item-status.reserved {
  background-color: var(--color-warning-50);
  color: var(--color-warning-600);
}

.posts-item-price {
  margin-left: auto;
  font-size: var(--text-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-error-600);
}

.posts-bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4);
  padding-bottom: calc(var(--space-4) + env(safe-area-inset-bottom));
  background-color: var(--bg-card);
  border-top: 1px solid var(--border-light);
  z-index: var(--z-fixed);
}

.posts-action-btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: var(--radius-full);
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.posts-action-btn--primary {
  color: var(--text-inverse);
  background-color: var(--color-primary-700);
}

.posts-action-btn--primary:active {
  background-color: var(--color-primary-800);
}

.posts-action-btn--secondary {
  color: var(--text-secondary);
  background-color: var(--bg-tertiary);
}

.posts-action-btn--secondary:active {
  background-color: var(--color-gray-200);
}
</style>
