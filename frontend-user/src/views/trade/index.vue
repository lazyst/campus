<template>
  <div class="trade-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">闲置市场</h1>
      <p class="page-subtitle">发现好物</p>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <div 
        v-for="tab in tabs" 
        :key="tab.key"
        class="filter-tab"
        :class="{ active: activeTab === tab.key }"
        @click="onTabChange(tab.key)"
      >
        {{ tab.label }}
      </div>
    </div>

    <ResponsiveContainer size="full">
      <!-- 加载中 -->
      <div v-if="loading && products.length === 0" class="loading-state">
        <div class="loading-spinner"></div>
        <p class="loading-text">加载中...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="error-state">
        <p class="error-text">{{ error }}</p>
        <button class="error-btn" @click="loadItems">重试</button>
      </div>

      <!-- 空状态 -->
      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4Z" stroke="currentColor" stroke-width="1.5"/>
            <path d="M12 11L8 7M12 11L16 7M12 11L12 15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="empty-text">暂无闲置物品</p>
        <p class="empty-hint">快去发布一个吧~</p>
      </div>

      <!-- 瀑布流布局 -->
      <div v-else class="waterfall-container">
        <div 
          v-for="product in products" 
          :key="product.id"
          class="waterfall-item"
          @click="goToProductDetail(product.id)"
        >
        <!-- 商品图片 -->
        <div class="waterfall-image">
          <img v-if="product.image" :src="product.image" :alt="product.title" />
          <div v-else class="image-placeholder">
            <span>📦</span>
          </div>
          <!-- 状态标签 -->
          <span 
            v-if="product.status !== 'normal'" 
            class="item-status"
            :class="product.status"
          >
            {{ statusText[product.status] }}
          </span>
          <!-- 收藏按钮 -->
          <button 
            v-if="product.showCollect"
            class="collect-btn"
            :class="{ 'collect-btn--collected': product.isCollected }"
            @click.stop="handleToggleCollect(product.id)"
          >
            {{ product.isCollected ? '已收藏' : '收藏' }}
          </button>
        </div>

        <!-- 商品信息 -->
        <div class="waterfall-content">
          <!-- 用户信息 -->
          <div class="user-info" v-if="product.userNickname">
            <div class="user-avatar">
              <img
                v-if="getProductAvatar(product)"
                :src="getProductAvatar(product)"
                alt="头像"
              />
              <span v-else>{{ (product.userNickname || '匿名').charAt(0) }}</span>
            </div>
            <span class="user-name">{{ product.userNickname }}</span>
          </div>

          <!-- 商品标题 -->
          <h3 class="item-title">{{ product.title }}</h3>

          <!-- 标签 -->
          <div class="item-tags" v-if="product.tags && product.tags.length">
            <span 
              v-for="tag in product.tags" 
              :key="tag"
              class="item-tag"
            >
              {{ tag }}
            </span>
          </div>

          <!-- 价格 -->
          <div class="item-footer">
            <span class="item-price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="item-original">
              ¥{{ product.originalPrice }}
            </span>
          </div>
        </div>
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
import { getItems } from '@/api/modules/item';
import { checkItemCollected, toggleItemCollect } from '@/api/modules/itemCollect';
import { showToast } from '@/services/toastService';
import BottomNav from '@/components/BottomNav.vue';
import ResponsiveContainer from '@/components/layout/ResponsiveContainer.vue';
import { useUserStore } from '@/stores/user';
import { getImageUrl } from '@/utils/imageUrl';

interface Item {
  id: number;
  title: string;
  price: number;
  images: string | null;
  type: number;
  status: number;
  userId?: number;
  userNickname?: string;
  userAvatar?: string;
}

interface Product {
  id: number;
  image: string;
  title: string;
  price: number;
  originalPrice?: number;
  tags: string[];
  status: 'normal' | 'sold' | 'reserved';
  isCollected?: boolean;
  showCollect?: boolean;
  userNickname?: string;
  userAvatar?: string;
  sellerAvatar?: string;
}

const router = useRouter();
const userStore = useUserStore();

const activeTab = ref('all');
const activeNav = ref('trade');
const loading = ref(false);
const error = ref<string | null>(null);

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'sell', label: '出售' },
  { key: 'buy', label: '收购' },
];

const statusText: Record<string, string> = {
  sold: '已售',
  reserved: '已预定',
};

const products = ref<Product[]>([]);

// 计算商品头像URL
function getProductAvatar(product: Product): string {
  const avatar = product.userAvatar || product.sellerAvatar || '';
  if (!avatar) return '';
  return getImageUrl(avatar);
}

function transformItem(item: Item): Product {
  // 根据type显示不同标签
  const typeTags: Record<number, string[]> = {
    1: ['求购'],
    2: ['出售'],
  };

  // 根据status显示不同状态
  let status: 'normal' | 'sold' | 'reserved' = 'normal';
  if (item.status === 2) status = 'sold';
  else if (item.status === 3) status = 'reserved';

  // 处理图片字段（后端返回的是JSON字符串或null）
  let image = '';
  if (item.images) {
    try {
      const images = JSON.parse(item.images);
      image = Array.isArray(images) && images.length > 0 ? getImageUrl(images[0]) : '';
    } catch {
      image = getImageUrl(item.images);
    }
  }

  // 计算头像URL
  const avatarUrl = item.userAvatar || item.sellerAvatar || '';
  const processedAvatar = avatarUrl ? getImageUrl(avatarUrl) : '';

  return {
    id: item.id,
    image,
    title: item.title,
    price: item.price,
    tags: typeTags[item.type] || [],
    status,
    showCollect: true,
    userNickname: item.userNickname,
    userAvatar: processedAvatar,
    sellerAvatar: '',
  };
}

async function loadItems() {
  loading.value = true;
  error.value = null;
  
  try {
    const type = activeTab.value === 'sell' ? 2 : activeTab.value === 'buy' ? 1 : undefined;

    const response = await getItems({ type, page: 1, size: 20 });

    // 处理分页响应 - 支持多种数据格式
    let records = []

    if (Array.isArray(response)) {
      // 直接返回数组格式
      records = response
    } else if (response && typeof response === 'object') {
      // Page 对象格式
      records = response.records || response.items || response.list || []
    } else {
      records = [];
    }

    // 确保 records 是数组
    if (!Array.isArray(records)) {
      records = [];
    }

    // 转换数据格式
    const transformedProducts = records.map(transformItem);
    
    // 检查收藏状态
    if (userStore.token) {
      for (const product of transformedProducts) {
        try {
          const collected = await checkItemCollected(product.id);
          product.isCollected = collected;
        } catch {
          product.isCollected = false;
        }
      }
    }
    
    products.value = transformedProducts;
  } catch (err: any) {
    console.error('获取商品列表失败:', err);
    error.value = err.message || '加载失败';
    products.value = [];
  } finally {
    loading.value = false;
  }
}

async function handleToggleCollect(id: number | string) {
  if (!userStore.token) {
    showToast('请先登录', 'warning');
    router.push('/login');
    return;
  }

  // 找到商品并乐观更新
  const product = products.value.find(p => p.id === id);
  if (!product) return;

  const previousState = product.isCollected || false;
  product.isCollected = !previousState;

  try {
    await toggleItemCollect(id as number);
    showToast(product.isCollected ? '已收藏' : '取消收藏');
  } catch (error) {
    // 回滚
    product.isCollected = previousState;
    console.error('收藏操作失败:', error);
    showToast('操作失败，请重试', 'error');
  }
}

function goToProductDetail(id: number | string) {
  router.push(`/trade/${id}`);
}

function handleNavChange(name: string) {
  activeNav.value = name;
  if (name === 'trade') {
    loadItems();
  }
}

function onTabChange(tab: string) {
  activeTab.value = tab;
  loadItems();
}

onMounted(() => {
  loadItems();
});
</script>

<style scoped>
/* 页面基础样式 */
.trade-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #F8FAFC 0%, #F1F5F9 100%);
  padding-bottom: 100px;
}

@media (min-width: 1024px) {
  .trade-page {
    padding-bottom: var(--space-6);
  }
}

/* 页面标题区域 */
.page-header {
  padding: 48px 24px 32px;
  background: white;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.page-title {
  font-family: 'Playfair Display', 'Times New Roman', serif;
  font-size: 42px;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 8px 0;
  letter-spacing: -1px;
  line-height: 1.1;
}

.page-subtitle {
  font-family: 'Inter', -apple-system, sans-serif;
  font-size: 14px;
  color: #94A3B8;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 3px;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  gap: 12px;
  padding: 20px 24px;
  background: white;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.filter-tab {
  padding: 12px 24px;
  border-radius: 0;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
  background: #F8FAFC;
  color: #64748B;
}

.filter-tab:hover {
  background: #F1F5F9;
  color: #1E293B;
}

.filter-tab.active {
  background: #0F172A;
  color: white;
  border-color: #0F172A;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 16px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid #E2E8F0;
  border-top-color: #0F172A;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 16px;
  font-size: 14px;
  color: #94A3B8;
}

/* 错误状态 */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 16px;
}

.error-text {
  font-size: 14px;
  color: #EF4444;
  margin: 0 0 16px 0;
}

.error-btn {
  padding: 12px 32px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  background: #0F172A;
  border: none;
  border-radius: 0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.error-btn:hover {
  background: #1E293B;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 16px;
}

.empty-icon {
  color: #CBD5E1;
  margin-bottom: 24px;
}

.empty-icon svg {
  stroke: #CBD5E1;
}

.empty-text {
  font-family: 'Playfair Display', serif;
  font-size: 24px;
  color: #0F172A;
  margin: 0 0 8px 0;
}

.empty-hint {
  font-size: 14px;
  color: #94A3B8;
  margin: 0;
}

/* 瀑布流容器 */
.waterfall-container {
  padding: 24px;
  column-count: 2;
  column-gap: 20px;
}

/* 瀑布流项 */
.waterfall-item {
  break-inside: avoid;
  margin-bottom: 20px;
  background: white;
  border-radius: 0;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.waterfall-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
}

/* 商品图片 */
.waterfall-image {
  position: relative;
  width: 100%;
  aspect-ratio: 4/3;
  background: linear-gradient(135deg, #F8FAFC 0%, #E2E8F0 100%);
  overflow: hidden;
}

.waterfall-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.waterfall-item:hover .waterfall-image img {
  transform: scale(1.08);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #CBD5E1;
}

/* 状态标签 */
.item-status {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 12px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  border-radius: 0;
}

.item-status.sold {
  background: #0F172A;
  color: white;
}

.item-status.reserved {
  background: #F59E0B;
  color: white;
}

/* 收藏按钮 */
.collect-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 8px 16px;
  font-size: 12px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.95);
  color: #0F172A;
  border-radius: 0;
}

.collect-btn:hover {
  background: #0F172A;
  color: white;
}

.collect-btn--collected {
  background: #0F172A;
  color: white;
}

/* 商品内容 */
.waterfall-content {
  padding: 20px;
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0F172A 0%, #334155 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: white;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: #64748B;
}

/* 商品标题 */
.item-title {
  font-family: 'Inter', -apple-system, sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: #0F172A;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 标签 */
.item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}

.item-tag {
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 500;
  background: #F1F5F9;
  color: #64748B;
  border-radius: 0;
}

/* 商品底部 */
.item-footer {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid #F1F5F9;
}

.item-price {
  font-family: 'Playfair Display', serif;
  font-size: 24px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.5px;
}

.item-original {
  font-size: 13px;
  color: #94A3B8;
  text-decoration: line-through;
}

/* 响应式 */
@media (min-width: 768px) {
  .waterfall-container {
    column-count: 3;
    column-gap: 24px;
    padding: 32px;
  }
  
  .waterfall-item {
    margin-bottom: 24px;
  }
}

@media (min-width: 1024px) {
  .waterfall-container {
    column-count: 4;
    column-gap: 24px;
    padding: 40px;
  }
}
</style>
