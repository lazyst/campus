<template>
  <div class="trade-detail-page">
    <!-- 状态栏 -->
    <div class="detail-status-bar">
      <button class="detail-back-btn" @click="goBack">‹</button>
      <div class="detail-action-btns">
        <button class="detail-more-btn">⋯</button>
      </div>
    </div>

    <!-- 商品图片 -->
    <div class="detail-product-images">
      <div class="detail-image-placeholder">
        <span>商品图片</span>
      </div>
      <div class="detail-image-indicator">
        <span class="detail-dot detail-dot--active"></span>
        <span class="detail-dot"></span>
        <span class="detail-dot"></span>
      </div>
    </div>

    <!-- 商品信息 -->
    <div class="detail-product-info">
      <div class="detail-price-row">
        <span class="detail-price">¥{{ product.price }}</span>
        <span class="detail-time">发布于 2天前</span>
      </div>
      
      <h1 class="detail-product-title">{{ product.title }}</h1>
      
      <div class="detail-product-tags">
        <span 
          v-for="tag in product.tags" 
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
        <span>{{ product.seller.charAt(0) }}</span>
      </div>
      <div class="detail-seller-info">
        <span class="detail-seller-name">{{ product.seller }}</span>
        <span class="detail-seller-meta">发帖 23 · 好评率 98%</span>
      </div>
      <button class="detail-contact-btn" @click="contactSeller">联系TA</button>
    </div>

    <!-- 底部操作栏 -->
    <div class="detail-bottom-actions">
      <button class="detail-action-btn" @click="toggleFavorite">
        <span class="detail-action-label">{{ isFavorite ? '已收藏' : '收藏' }}</span>
      </button>
      <button class="detail-action-btn" @click="goToChat">
        <span class="detail-action-label">私信</span>
      </button>
      <button class="detail-buy-btn" @click="buyNow">立即购买</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

const isFavorite = ref(false);

// 模拟商品数据
const product = ref({
  id: 1,
  title: 'MacBook Pro 13寸 2020款 M1芯片 几乎全新',
  price: 128,
  description: `自用MacBook Pro，2020款M1芯片，8+256GB。买来主要用于写论文，现在毕业了用不到。外观无划痕，电池健康度98%，配件齐全。诚心要的可以小刀~`,
  seller: '小明学长',
  tags: ['电脑数码', '95新'],
});

function goBack() {
  router.back();
}

function toggleFavorite() {
  isFavorite.value = !isFavorite.value;
}

function contactSeller() {
  router.push(`/messages/chat/${product.value.id}`);
}

function goToChat() {
  router.push(`/messages/chat/${product.value.id}`);
}

function buyNow() {
  console.log('立即购买');
}
</script>

<style scoped>
.trade-detail-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
  padding-bottom: 70px;
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
}

.detail-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: var(--text-tertiary);
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
</style>
