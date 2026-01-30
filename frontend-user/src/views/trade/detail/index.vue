<template>
  <div class="trade-detail-page">
    <!-- 导航栏 -->
    <NavBar title="商品详情" :left-arrow="true" @click-left="goBack" />

    <!-- 加载中 -->
    <div v-if="loading" class="detail-loading">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <!-- 商品内容 -->
    <template v-else-if="product">
      <!-- 商品图片 -->
      <div class="detail-product-images">
        <template v-if="productImages && productImages.length > 0">
          <img :src="productImages[0]" :alt="product.title" class="detail-main-image" />
        </template>
        <template v-else>
          <div class="detail-image-placeholder">
            <div class="detail-placeholder-corner detail-placeholder-corner--tl"></div>
            <div class="detail-placeholder-corner detail-placeholder-corner--tr"></div>
            <div class="detail-placeholder-corner detail-placeholder-corner--bl"></div>
            <div class="detail-placeholder-corner detail-placeholder-corner--br"></div>
            <div class="detail-placeholder-tape"></div>
            <div class="detail-placeholder-content">
              <div class="detail-placeholder-stamp">
                <span class="detail-placeholder-stamp-text">物品介绍</span>
              </div>
              <p class="detail-placeholder-description">{{ product.description || '暂无描述' }}</p>
            </div>
          </div>
        </template>
        <div class="detail-image-indicator" v-if="productImages && productImages.length > 0">
          <span class="detail-dot" :class="{ 'detail-dot--active': true }"></span>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="detail-product-info">
        <div class="detail-price-row">
          <span class="detail-price">¥{{ product.price }}</span>
          <span class="detail-time">{{ formatTime(product.createdAt) }}</span>
        </div>
        
        <h1 class="detail-product-title">{{ product.title }}</h1>
        
        <div class="detail-product-tags">
          <span 
            v-for="tag in itemTags" 
            :key="tag"
            class="detail-tag"
          >
            {{ tag }}
          </span>
        </div>

        <p class="detail-product-description">{{ product.description }}</p>
      </div>

      <!-- 卖家信息 -->
      <div class="detail-seller-card">
        <div class="detail-seller-avatar">
          <img v-if="product.userAvatar" :src="getImageUrl(product.userAvatar)" alt="头像" />
          <span v-else>{{ sellerInitial }}</span>
        </div>
        <div class="detail-seller-info">
          <span class="detail-seller-name">{{ product.userNickname || '匿名用户' }}</span>
          <span class="detail-seller-meta">发帖 {{ product.sellerPostCount || 0 }} · 好评率 {{ product.sellerGoodRate || 100 }}%</span>
        </div>
        <button class="detail-contact-btn" @click="contactSeller">联系TA</button>
      </div>
    </template>

    <!-- 空状态 -->
    <div v-else class="detail-empty">
      <div class="detail-empty-icon">📦</div>
      <div class="detail-empty-text">商品不存在或已被删除</div>
    </div>

    <!-- 底部操作栏 -->
    <div class="detail-bottom-actions" v-if="product">
      <button class="detail-action-btn" @click="handleCollect">
        <span class="detail-action-label">{{ isCollected ? '已收藏' : '收藏' }}</span>
      </button>
      <button class="detail-action-btn" @click="goToChat">
        <span class="detail-action-label">私信</span>
      </button>
      <button class="detail-buy-btn" @click="buyNow">立即购买</button>
    </div>

    <!-- 登录确认弹窗 -->
    <Dialog
      v-model:visible="dialogVisible"
      title="提示"
      message="该操作需要登录，是否前往登录？"
      theme="warning"
      confirmText="去登录"
      cancelText="取消"
      @confirm="goToLogin"
      @cancel="dialogVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getItemById, contactSeller as contactSellerApi } from '@/api/modules/item';
import { toggleItemCollect, checkItemCollected } from '@/api/modules/itemCollect';
import { useUserStore } from '@/stores/user';
import NavBar from '@/components/navigation/NavBar.vue';
import Dialog from '@/components/interactive/Dialog.vue';
import { showToast } from '@/services/toastService';
import { getImageUrl } from '@/utils/imageUrl';

interface Item {
  id: number;
  title: string;
  price: number;
  description: string;
  images: string | null;
  type: number;
  status: number;
  userId: number;
  userNickname?: string;
  userAvatar?: string;
  sellerName?: string;
  sellerAvatar?: string;
  sellerPostCount?: number;
  sellerGoodRate?: number;
  createdAt: string;
  updatedAt: string;
}

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const loading = ref(true);
const product = ref<Item | null>(null);
const isCollected = ref(false);
const dialogVisible = ref(false);

const itemId = computed(() => Number(route.params.id));

const itemTags = computed(() => {
  if (!product.value) return [];
  const tags = [];
  // 根据type显示
  if (product.value.type === 1) tags.push('求购');
  else if (product.value.type === 2) tags.push('出售');
  // 根据status显示
  if (product.value.status === 2) tags.push('已售');
  else if (product.value.status === 3) tags.push('已预定');
  return tags;
});

const productImages = computed(() => {
  if (!product.value?.images) return [];
  
  try {
    const images = JSON.parse(product.value.images);
    const imageArray = Array.isArray(images) ? images : [product.value.images];
    return imageArray.map(img => img ? getImageUrl(img) : '');
  } catch {
    const img = product.value.images;
    return img ? [getImageUrl(img)] : [];
  }
});

const sellerInitial = computed(() => {
  const name = product.value?.userNickname || product.value?.sellerName || '匿名';
  return name.charAt(0).toUpperCase();
});

onMounted(async () => {
  await loadItem();
  if (userStore.token) {
    await checkCollectStatus();
  }
});

async function loadItem() {
  loading.value = true;
  try {
    const data = await getItemById(itemId.value);
    if (data) {
      product.value = data as Item;
    }
  } catch (error) {
    console.error('获取商品详情失败:', error);
    showToast('获取商品详情失败', 'error');
  } finally {
    loading.value = false;
  }
}

async function checkCollectStatus() {
  try {
    const collected = await checkItemCollected(itemId.value);
    isCollected.value = collected;
  } catch (error) {
    console.error('检查收藏状态失败:', error);
  }
}

function goBack() {
  router.back();
}

async function handleCollect() {
  if (!userStore.token) {
    dialogVisible.value = true;
    return;
  }

  // 乐观更新
  const previousState = isCollected.value;
  isCollected.value = !isCollected.value;

  try {
    await toggleItemCollect(itemId.value);
    showToast(isCollected.value ? '已收藏' : '取消收藏');
  } catch (error) {
    // 回滚
    isCollected.value = previousState;
    console.error('收藏操作失败:', error);
    showToast('操作失败，请重试', 'error');
  }
}

async function contactSeller() {
  if (!userStore.token) {
    dialogVisible.value = true;
    return;
  }

  try {
    const response = await contactSellerApi(itemId.value);
    console.log('联系API响应:', response);
    // 后端返回 { sellerId, sellerName }，sellerId 在 data 中
    const sellerId = response?.data?.sellerId || product.value?.userId;
    if (sellerId) {
      window.location.href = `/messages/${sellerId}`;
    } else {
      showToast('获取卖家信息失败', 'error');
    }
  } catch (error) {
    console.error('联系卖家失败:', error);
    // 即使API失败，也尝试直接跳转
    if (product.value?.userId) {
      window.location.href = `/messages/${product.value.userId}`;
    } else {
      showToast('联系卖家失败，请稍后重试', 'error');
    }
  }
}

function goToChat() {
  if (!userStore.token) {
    dialogVisible.value = true;
    return;
  }
  window.location.href = `/messages/${product.value?.userId}`;
}

function buyNow() {
  showToast('购买功能开发中', 'info');
}

function goToLogin() {
  dialogVisible.value = false;
  router.push('/login');
}

function formatTime(time: string) {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚发布';
  if (minutes < 60) return `${minutes}分钟前发布`;
  if (hours < 24) return `${hours}小时前发布`;
  if (days < 7) return `${days}天前发布`;
  
  return date.toLocaleDateString('zh-CN');
}
</script>

<style scoped>
.trade-detail-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
  padding-bottom: 70px;
  padding-top: var(--nav-height);
}

.detail-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  background: transparent;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-10);
}

.detail-back-btn {
  font-size: 28px;
  color: var(--text-primary);
  cursor: pointer;
  background-color: rgba(255, 255, 255, 0.8);
  border: none;
  border-radius: var(--radius-full);
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-back-btn:active {
  background-color: var(--bg-secondary);
}

.detail-action-btns {
  display: flex;
  gap: var(--space-2);
}

.detail-more-btn {
  font-size: 22px;
  color: var(--text-primary);
  cursor: pointer;
  background-color: rgba(255, 255, 255, 0.8);
  border: none;
  border-radius: var(--radius-full);
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-more-btn:active {
  background-color: var(--bg-secondary);
}

.detail-product-images {
  height: 375px;
  background-color: var(--bg-tertiary);
  position: relative;
  overflow: hidden;
}

.detail-main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 复古手作风格 */
.detail-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #FDF8F0;
  position: relative;
  overflow: hidden;
  padding: var(--space-6);
}

/* 复古纸张纹理 */
.detail-image-placeholder::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.08'/%3E%3C/svg%3E");
  pointer-events: none;
  z-index: 0;
}

/* 复古照片角落折痕 */
.detail-placeholder-corner {
  position: absolute;
  width: 60px;
  height: 60px;
  border: 2px solid rgba(139, 115, 85, 0.25);
  z-index: 2;
}

.detail-placeholder-corner--tl {
  top: 16px;
  left: 16px;
  border-right: none;
  border-bottom: none;
  border-top-left-radius: 16px;
}

.detail-placeholder-corner--tr {
  top: 16px;
  right: 16px;
  border-left: none;
  border-bottom: none;
  border-top-right-radius: 16px;
}

.detail-placeholder-corner--bl {
  bottom: 16px;
  left: 16px;
  border-right: none;
  border-top: none;
  border-bottom-left-radius: 16px;
}

.detail-placeholder-corner--br {
  bottom: 16px;
  right: 16px;
  border-left: none;
  border-top: none;
  border-bottom-right-radius: 16px;
}

/* 胶带装饰 */
.detail-placeholder-tape {
  position: absolute;
  top: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 24px;
  background: linear-gradient(
    to bottom,
    rgba(245, 240, 225, 0.9) 0%,
    rgba(245, 240, 225, 0.8) 50%,
    rgba(235, 225, 205, 0.85) 100%
  );
  opacity: 0.8;
  transform: translateX(-50%) rotate(-2deg);
  z-index: 3;
}

/* 复古印章 */
.detail-placeholder-stamp {
  width: 72px;
  height: 72px;
  border: 3px dashed #C45C5C;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-4);
  transform: rotate(-12deg);
  opacity: 0.95;
  background: rgba(196, 92, 92, 0.05);
}

.detail-placeholder-stamp-text {
  font-size: 11px;
  font-weight: bold;
  color: #C45C5C;
  letter-spacing: 2px;
  text-transform: uppercase;
  font-family: 'Georgia', serif;
  transform: rotate(-6deg);
}

.detail-placeholder-content {
  width: 100%;
  max-width: 450px;
  text-align: center;
  position: relative;
  z-index: 1;
}

.detail-placeholder-description {
  font-size: var(--text-lg);
  color: #5D4E3C;
  line-height: 1.8;
  font-family: 'Georgia', 'Times New Roman', serif;
  font-style: italic;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.6);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 8;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.detail-image-indicator {
  position: absolute;
  bottom: var(--space-4);
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: var(--space-2);
}

.detail-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background-color: var(--border-default);
}

.detail-dot--active {
  background-color: var(--color-primary-600);
}

.detail-product-info {
  padding: var(--space-4);
  background-color: var(--bg-card);
}

.detail-price-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: var(--space-3);
}

.detail-price {
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-error-600);
}

.detail-time {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.detail-product-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-3) 0;
  line-height: var(--line-height-tight);
}

.detail-product-tags {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.detail-tag {
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-xs);
  background-color: var(--color-primary-50);
  color: var(--color-primary-700);
  border-radius: var(--radius-sm);
}

.detail-product-description {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--line-height-relaxed);
  margin: 0;
}

.detail-seller-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background-color: var(--bg-card);
  margin-top: var(--space-2);
}

.detail-seller-avatar {
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

.detail-seller-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-seller-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-seller-name {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.detail-seller-meta {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.detail-contact-btn {
  width: 80px;
  height: 36px;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-inverse);
  background-color: var(--color-primary-700);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
}

.detail-contact-btn:active {
  background-color: var(--color-primary-800);
}

.detail-bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  padding-bottom: calc(var(--space-4) + env(safe-area-inset-bottom));
  background-color: var(--bg-card);
  border-top: 1px solid var(--border-light);
  z-index: var(--z-fixed);
}

.detail-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-2) var(--space-3);
  background: none;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-full);
  cursor: pointer;
}

.detail-action-btn:active {
  background-color: var(--bg-secondary);
}

.detail-action-label {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.detail-buy-btn {
  flex: 1;
  height: 44px;
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-inverse);
  background-color: var(--color-error-500);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
}

.detail-buy-btn:active {
  background-color: var(--color-error-600);
}

/* 加载状态 */
.detail-loading {
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

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.detail-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.detail-empty-icon {
  font-size: 64px;
  margin-bottom: var(--space-4);
}

.detail-empty-text {
  font-size: var(--text-base);
  color: var(--text-secondary);
}

/* 收藏按钮样式 */
.detail-action-btn:active {
  background-color: var(--bg-secondary);
}

.detail-action-btn:has(.detail-action-label:contains("已收藏")) {
  background-color: var(--color-primary-50);
  border-color: var(--color-primary-200);
}

.detail-action-btn:has(.detail-action-label:contains("已收藏")) .detail-action-label {
  color: var(--color-primary-700);
}
</style>
