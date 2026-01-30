<template>
  <div class="collections-page">
    <NavBar title="我的收藏" :left-arrow="true" @click-left="onClickLeft" />

    <!-- 分类标签 -->
    <div class="collections-tabs">
      <div 
        class="collections-tab" 
        :class="{ 'collections-tab--active': activeTab === 'posts' }"
        @click="activeTab = 'posts'"
      >
        帖子收藏
      </div>
      <div 
        class="collections-tab" 
        :class="{ 'collections-tab--active': activeTab === 'items' }"
        @click="activeTab = 'items'"
      >
        闲置收藏
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="collections-loading">
      <div class="collections-spinner"></div>
      <span>加载中...</span>
    </div>

    <!-- 帖子收藏列表 -->
    <div v-else-if="activeTab === 'posts'">
      <div v-if="collections.length > 0" class="collections-posts-list">
        <div 
          v-for="post in collections" 
          :key="post.id"
          class="collections-post-card"
          @click="goToPostDetail(post.id)"
        >
          <!-- 用户信息 -->
          <div class="collections-post-card-user">
            <div class="collections-post-card-avatar">
              <span>{{ (post.userNickname || '匿名').charAt(0) }}</span>
            </div>
            <span class="collections-post-card-username">{{ post.userNickname || '匿名用户' }}</span>
          </div>

          <!-- 帖子标题 -->
          <h3 class="collections-post-card-title">{{ post.title }}</h3>

          <!-- 帖子内容预览 -->
          <p class="collections-post-card-preview">{{ post.content }}</p>

          <!-- 帖子图片 -->
          <div v-if="post.images && post.images.length > 0" class="collections-post-card-images">
            <div 
              v-for="(img, index) in post.images.slice(0, 3)" 
              :key="index"
              class="collections-post-card-image"
            >
              <img :src="getImageUrl(img)" :alt="'图片' + (index + 1)" />
            </div>
            <div v-if="post.images.length > 3" class="collections-post-card-image-more">
              +{{ post.images.length - 3 }}
            </div>
          </div>

          <!-- 帖子底部 -->
          <div class="collections-post-card-footer">
            <span class="collections-post-card-time">{{ formatTime(post.createdAt) }}</span>
            <div class="collections-post-card-stats">
              <span class="collections-post-stat">点赞 {{ post.likeCount || 0 }}</span>
              <span class="collections-post-stat">评论 {{ post.commentCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="collections-empty">
        <div class="collections-empty-icon">📝</div>
        <div class="collections-empty-text">暂无帖子收藏</div>
        <div class="collections-empty-hint">快去收藏一些有趣的帖子吧</div>
      </div>
    </div>

    <!-- 闲置收藏列表 -->
    <div v-else-if="activeTab === 'items'">
      <div v-if="items.length > 0" class="collections-waterfall">
        <div 
          v-for="item in items" 
          :key="item.id"
          class="collections-waterfall-item"
          @click="goToItemDetail(item.id)"
        >
          <!-- 商品图片 -->
          <div class="collections-waterfall-image">
            <img v-if="item.image" :src="item.image" :alt="item.title" />
            <div v-else class="image-placeholder">
              <div class="image-placeholder-content">
                <div class="image-placeholder-corner image-placeholder-corner--tl"></div>
                <div class="image-placeholder-corner image-placeholder-corner--tr"></div>
                <div class="image-placeholder-corner image-placeholder-corner--bl"></div>
                <div class="image-placeholder-corner image-placeholder-corner--br"></div>
                <div class="image-placeholder-stamp">
                  <span class="image-placeholder-stamp-text">物品介绍</span>
                </div>
                <p class="image-placeholder-description">
                  {{ item.description || '暂无描述' }}
                </p>
              </div>
            </div>
            <!-- 类型标签 -->
            <span
              v-if="item.type"
              class="collections-item-type"
              :class="{ 'collections-item-type--buy': item.type === 1 }"
            >
              {{ item.type === 1 ? '收购' : '出售' }}
            </span>
          </div>

          <!-- 商品信息 -->
          <div class="collections-waterfall-content">
            <!-- 用户信息 -->
            <div class="collections-item-user" v-if="item.userNickname">
              <div class="collections-item-avatar">
                <span>{{ (item.userNickname || '匿名').charAt(0) }}</span>
              </div>
              <span class="collections-item-username">{{ item.userNickname }}</span>
            </div>

            <!-- 商品标题 -->
            <h3 class="collections-item-title">{{ item.title }}</h3>

            <!-- 价格 -->
            <div class="collections-item-footer">
              <span class="collections-item-price">¥{{ item.price || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="collections-empty">
        <div class="collections-empty-icon">🛍️</div>
        <div class="collections-empty-text">暂无闲置收藏</div>
        <div class="collections-empty-hint">快去收藏一些实惠的闲置物品吧</div>
      </div>
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
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyCollections } from '@/api/modules/post'
import { getCollectedItems } from '@/api/modules/itemCollect'
import NavBar from '@/components/navigation/NavBar.vue'
import Dialog from '@/components/interactive/Dialog.vue'
import { getImageUrl } from '@/utils/imageUrl'

interface Post {
  id: number
  title: string
  createdAt: string
  likeCount: number
  commentCount: number
  collectCount: number
}

interface Item {
  id: number
  title: string
  price: number
  images: string | null
  image?: string
  type?: number
  description?: string
  userNickname?: string
  userAvatar?: string
}

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref<'posts' | 'items'>('posts')
const loading = ref(true)
const collections = ref<Post[]>([])
const items = ref<Item[]>([])
const dialogVisible = ref(false)

onMounted(async () => {
  if (!userStore.token) {
    dialogVisible.value = true
    loading.value = false
    return
  }

  await loadData()
})

watch(activeTab, async () => {
  if (!userStore.token) {
    dialogVisible.value = true
    return
  }
  
  loading.value = true
  await loadData()
  loading.value = false
})

async function loadData() {
  try {
    if (activeTab.value === 'posts') {
      const data = await getMyCollections()
      collections.value = data || []
    } else if (activeTab.value === 'items') {
      const result = await getCollectedItems()
      items.value = (result || []).map(transformItem)
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    if (activeTab.value === 'posts') {
      collections.value = []
    } else {
      items.value = []
    }
  } finally {
    loading.value = false
  }
}

function onClickLeft() {
  router.back()
}

function goToLogin() {
  dialogVisible.value = false
  router.push('/login')
}

function goToPostDetail(postId: number) {
  router.push(`/forum/detail/${postId}`)
}

function goToItemDetail(itemId: number) {
  router.push(`/trade/${itemId}`)
}

function transformItem(item: any): Item {
  let image = ''
  if (item.images) {
    try {
      const images = JSON.parse(item.images)
      image = Array.isArray(images) && images.length > 0 ? getImageUrl(images[0]) : ''
    } catch {
      image = getImageUrl(item.images)
    }
  }

  return {
    ...item,
    image,
  }
}

function formatTime(time: string) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

</script>

<style scoped>
/* 页面基础样式 */
.collections-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-top: calc(var(--nav-height) + var(--space-4));
}

/* 分类标签 */
.collections-tabs {
  display: flex;
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: 10;
}

.collections-tab {
  flex: 1;
  padding: var(--space-3) 0;
  text-align: center;
  font-size: var(--text-base);
  color: var(--text-secondary);
  cursor: pointer;
  position: relative;
  transition: color var(--transition-fast);
  border: none;
  background: transparent;
}

.collections-tab--active {
  color: var(--color-primary-700);
  font-weight: var(--font-weight-medium);
}

.collections-tab--active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 2px;
  background-color: var(--color-primary-700);
  border-radius: 1px;
}

/* 加载状态 */
.collections-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
  color: var(--text-secondary);
}

.collections-spinner {
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

/* ==================== 帖子收藏列表样式 ==================== */

/* 帖子列表容器 */
.collections-posts-list {
  padding: var(--space-3);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

/* 帖子卡片 */
.collections-post-card {
  padding: var(--space-4);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.collections-post-card:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

/* 帖子卡片 - 用户信息 */
.collections-post-card-user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.collections-post-card-avatar {
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

.collections-post-card-username {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
}

/* 帖子卡片 - 标题 */
.collections-post-card-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
  line-height: var(--line-height-tight);
}

/* 帖子卡片 - 内容预览 */
.collections-post-card-preview {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-3);
  line-height: var(--line-height-normal);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 帖子卡片 - 图片 */
.collections-post-card-images {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.collections-post-card-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  overflow: hidden;
  flex-shrink: 0;
}

.collections-post-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.collections-post-card-image-more {
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

/* 帖子卡片 - 底部 */
.collections-post-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.collections-post-card-stats {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.collections-post-stat {
  color: var(--text-tertiary);
}

.collections-post-card-time {
  color: var(--text-tertiary);
}

/* ==================== 闲置收藏瀑布流样式 ==================== */

/* 瀑布流容器 */
.collections-waterfall {
  padding: var(--space-3);
  column-count: 2;
  column-gap: var(--space-3);
}

/* 瀑布流项 */
.collections-waterfall-item {
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

.collections-waterfall-item:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

/* 商品图片 */
.collections-waterfall-image {
  position: relative;
  width: 100%;
  aspect-ratio: 4/3;
  background-color: var(--bg-tertiary);
  overflow: hidden;
}

.collections-waterfall-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.collections-waterfall-item:active .collections-waterfall-image img {
  transform: scale(0.95);
}

/* 无图片时的占位符 */
.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #FDF8F0;
  position: relative;
  overflow: hidden;
}

/* 复古纸张纹理 */
.image-placeholder::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.08'/%3E%3C/svg%3E");
  pointer-events: none;
  z-index: 0;
}

.image-placeholder-content {
  width: 100%;
  height: 100%;
  padding: var(--space-3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
}

/* 复古照片角落折痕效果 */
.image-placeholder-corner {
  position: absolute;
  width: 24px;
  height: 24px;
  border: 2px solid rgba(139, 115, 85, 0.3);
  z-index: 2;
}

.image-placeholder-corner--tl {
  top: 8px;
  left: 8px;
  border-right: none;
  border-bottom: none;
  border-top-left-radius: 8px;
}

.image-placeholder-corner--tr {
  top: 8px;
  right: 8px;
  border-left: none;
  border-bottom: none;
  border-top-right-radius: 8px;
}

.image-placeholder-corner--bl {
  bottom: 8px;
  left: 8px;
  border-right: none;
  border-top: none;
  border-bottom-left-radius: 8px;
}

.image-placeholder-corner--br {
  bottom: 8px;
  right: 8px;
  border-left: none;
  border-top: none;
  border-bottom-right-radius: 8px;
}

/* 复古印章效果 */
.image-placeholder-stamp {
  width: 48px;
  height: 48px;
  border: 2px dashed #C45C5C;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
  transform: rotate(-8deg);
  opacity: 0.9;
}

.image-placeholder-stamp-text {
  font-size: 10px;
  font-weight: bold;
  color: #C45C5C;
  letter-spacing: 1px;
  text-transform: uppercase;
  font-family: 'Georgia', serif;
  transform: rotate(-4deg);
}

.image-placeholder-description {
  font-size: 12px;
  color: #5D4E3C;
  line-height: 1.6;
  text-align: center;
  font-family: 'Georgia', 'Times New Roman', serif;
  font-style: italic;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
}

.collections-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: var(--color-gray-400);
}

/* 类型标签 */
.collections-item-type {
  position: absolute;
  top: var(--space-2);
  left: var(--space-2);
  padding: 4px var(--space-2);
  font-size: 10px;
  font-weight: var(--font-weight-semibold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background-color: var(--color-primary-700);
  color: white;
  border-radius: var(--radius-sm);
}

.collections-item-type--buy {
  background-color: var(--color-success-600);
}

/* 商品内容 */
.collections-waterfall-content {
  padding: var(--space-3);
}

/* 用户信息 */
.collections-item-user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.collections-item-avatar {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary-600), var(--color-primary-800));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: var(--font-weight-semibold);
  color: white;
}

.collections-item-username {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  font-weight: var(--font-weight-medium);
}

/* 商品标题 */
.collections-item-title {
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

/* 商品底部 */
.collections-item-footer {
  padding-top: var(--space-2);
  border-top: 1px solid var(--border-light);
  margin-top: var(--space-2);
}

.collections-item-price {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-error-600);
}

/* ==================== 空状态样式 ==================== */

.collections-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
}

.collections-empty-icon {
  width: 80px;
  height: 80px;
  background-color: var(--color-primary-700);
  color: #FFFFFF;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-4);
}

.collections-empty-text {
  font-size: var(--text-lg);
  color: var(--text-primary);
  font-weight: var(--font-weight-medium);
  margin-bottom: var(--space-2);
}

.collections-empty-hint {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

/* ==================== 响应式布局 ==================== */

@media (min-width: 768px) {
  .collections-waterfall {
    column-count: 3;
    column-gap: var(--space-4);
    padding: var(--space-4);
  }
  
  .collections-waterfall-item {
    margin-bottom: var(--space-4);
  }
}

@media (min-width: 1024px) {
  .collections-waterfall {
    column-count: 4;
    column-gap: var(--space-4);
    padding: var(--space-4);
  }
}
</style>
