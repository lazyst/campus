<template>
  <div ref="listRef" class="trade-list">
    <!-- 页面标题 -->
    <div class="trade-header">
      <h1 class="trade-title">闲置市场</h1>
      <p class="trade-subtitle">发现好物</p>
    </div>

    <!-- Category Tabs -->
    <div class="trade-tabs">
      <button
        class="trade-tab"
        :class="{ 'trade-tab--active': activeTab === 0 }"
        @click="onTabChange(0)"
      >
        全部
        <div class="tab-underline"></div>
      </button>
      <button
        class="trade-tab"
        :class="{ 'trade-tab--active': activeTab === 1 }"
        @click="onTabChange(1)"
      >
        出售
        <div class="tab-underline"></div>
      </button>
      <button
        class="trade-tab"
        :class="{ 'trade-tab--active': activeTab === 2 }"
        @click="onTabChange(2)"
      >
        求购
        <div class="tab-underline"></div>
      </button>
      <div class="trade-sort">
        <button
          class="trade-sort-btn"
          :class="{ 'trade-sort-btn--active': sortBy === 'newest' }"
          @click="onSortChange('newest')"
        >
          最新
        </button>
        <span class="trade-sort-divider">|</span>
        <button
          class="trade-sort-btn"
          :class="{ 'trade-sort-btn--active': sortBy === 'price_asc' }"
          @click="onSortChange('price_asc')"
        >
          价格↑
        </button>
        <button
          class="trade-sort-btn"
          :class="{ 'trade-sort-btn--active': sortBy === 'price_desc' }"
          @click="onSortChange('price_desc')"
        >
          价格↓
        </button>
      </div>
    </div>
    
    <!-- 加载中 -->
    <div v-if="loading && list.length === 0" class="trade-loading">
      <div class="loading-spinner"></div>
      <p class="loading-text">加载中...</p>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="list.length === 0" class="trade-empty">
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
        v-for="item in list" 
        :key="item.id" 
        class="waterfall-item"
        @click="onItemClick(item)"
      >
        <!-- 商品图片 -->
        <div class="waterfall-image">
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
            class="item-type"
            :class="{ 'item-type--buy': item.type === 1 }"
          >
            {{ item.type === 1 ? '求购' : '出售' }}
          </span>
          <!-- 状态标签 -->
          <span 
            v-if="item.status && item.status !== 1" 
            class="item-status"
            :class="getStatusClass(item.status)"
          >
            {{ getStatusText(item.status) }}
          </span>
        </div>

        <!-- 商品信息 -->
        <div class="waterfall-content">
          <!-- 用户信息 -->
          <div class="user-info" v-if="item.userNickname">
            <div class="user-avatar">
              <img
                v-if="item.userAvatar"
                :src="getImageUrl(item.userAvatar)"
                alt="头像"
                class="user-avatar-img"
              />
              <span v-else>{{ (item.userNickname || '匿名').charAt(0) }}</span>
            </div>
            <span class="user-name">{{ item.userNickname }}</span>
          </div>

          <!-- 商品标题 -->
          <h3 class="item-title">{{ item.title }}</h3>

          <!-- 价格 -->
          <div class="item-footer">
            <span class="item-price">¥{{ item.price }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Loading -->
    <div v-if="loading && list.length > 0" class="trade-loading-more">
      <span class="loading-text">加载中...</span>
    </div>
    
    <!-- Finished -->
    <div v-if="finished && list.length > 0" class="trade-finished">
      <span class="finished-text">没有更多了</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getItems, searchItems } from '@/api/modules/item'
import { getImageUrl } from '@/utils/imageUrl'

interface TradeItem {
  id: number
  type: number // 1: 收购, 2: 出售
  title: string
  price: number
  description: string
  images: string | null
  status: number
  viewCount: number
  userNickname?: string
  userAvatar?: string
}

interface Props {
  keyword?: string
}

const props = withDefaults(defineProps<Props>(), {
  keyword: ''
})

const router = useRouter()
const activeTab = ref(0)
const sortBy = ref('newest')
const list = ref<TradeItem[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const listRef = ref<HTMLElement | null>(null)

const statusTexts: Record<number, string> = {
  2: '已售',
  3: '已下架',
}

function getStatusText(status: number): string {
  return statusTexts[status] || ''
}

function getStatusClass(status: number): string {
  if (status === 2) return 'status-sold'
  if (status === 3) return 'status-reserved'
  return ''
}

function transformItem(item: any): TradeItem {
  // 处理图片字段
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

async function onLoad() {
  if (loading.value || finished.value) return

  loading.value = true

  try {
    let type: number | undefined
    if (activeTab.value === 1) type = 2  // 出售
    else if (activeTab.value === 2) type = 1  // 收购

    let response
    if (props.keyword && props.keyword.trim()) {
      // 搜索模式
      response = await searchItems(props.keyword, { type, page: page.value, size: 10, sortBy: sortBy.value })
    } else {
      // 普通模式
      response = await getItems({ type, page: page.value, size: 10, sortBy: sortBy.value })
    }

    // 处理分页响应 - 支持 Page 对象和直接数组两种格式
    let records = []
    if (Array.isArray(response)) {
      records = response
    } else if (response && typeof response === 'object') {
      records = response.records || response.items || []
    }

    const newList = records.map(transformItem)

    list.value.push(...newList)
    page.value++

    // 获取总数 - 支持多种字段名
    const total = response?.total || response?.totalCount || list.value.length

    if (newList.length < 10 || list.value.length >= total) {
      finished.value = true
    }
  } catch (error) {
    console.error('获取物品列表失败:', error)
  } finally {
    loading.value = false
  }
}

function onTabChange(tab: number) {
  activeTab.value = tab
  list.value = []
  page.value = 1
  finished.value = false
  onLoad()
}

function onSortChange(sort: string) {
  sortBy.value = sort
  list.value = []
  page.value = 1
  finished.value = false
  onLoad()
}

function onItemClick(item: TradeItem) {
  router.push(`/trade/${item.id}`)
}

// 监听关键词变化，重置列表并重新加载
watch(() => props.keyword, () => {
  list.value = []
  page.value = 1
  finished.value = false
  onLoad()
})

// Infinite scroll using scroll event
let scrollHandler: (() => void) | undefined

onMounted(() => {
  onLoad()

  // Setup scroll event listener for infinite scroll
  scrollHandler = () => {
    if (loading.value || finished.value) return

    const scrollHeight = document.documentElement.scrollHeight
    const scrollTop = window.scrollY || document.documentElement.scrollTop
    const clientHeight = window.innerHeight

    // 当距离底部 100px 时触发加载
    if (scrollHeight - scrollTop - clientHeight < 100) {
      onLoad()
    }
  }

  window.addEventListener('scroll', scrollHandler, { passive: true })
})

onUnmounted(() => {
  if (scrollHandler) {
    window.removeEventListener('scroll', scrollHandler)
  }
})
</script>

<style scoped>
/* 页面基础样式 */
.trade-list {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding-bottom: calc(var(--tabbar-height) + var(--space-4));
}

/* 页面标题区域 */
.trade-header {
  padding: var(--space-6) var(--page-padding);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
}

.trade-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-1) 0;
  line-height: var(--line-height-tight);
}

.trade-subtitle {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

/* 筛选标签 */
.trade-tabs {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-3) var(--page-padding);
  padding-bottom: 0;
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: var(--nav-height);
  z-index: var(--z-sticky);
}

.trade-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-3) 0;
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: color var(--transition-fast);
  border: none;
  background: transparent;
  color: var(--text-secondary);
  position: relative;
  z-index: 1;
}

.trade-tab:hover {
  color: var(--text-primary);
}

.trade-tab--active {
  color: var(--color-primary-700);
}

.tab-underline {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 32px;
  height: 2px;
  background-color: var(--color-primary-700);
  border-radius: 1px;
  transform: translateX(-50%) scaleX(0);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.trade-tab--active .tab-underline {
  transform: translateX(-50%) scaleX(1);
}

/* 排序按钮 */
.trade-sort {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-left: auto;
  padding-left: var(--space-2);
}

.trade-sort-btn {
  padding: var(--space-1) var(--space-2);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: color var(--transition-fast);
}

.trade-sort-btn:hover {
  color: var(--text-primary);
}

.trade-sort-btn--active {
  color: var(--color-primary-700);
  font-weight: var(--font-weight-medium);
}

.trade-sort-divider {
  color: var(--border-light);
}

/* 加载状态 */
.trade-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) 0;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--border-light);
  border-top-color: var(--color-primary-600);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

/* 空状态 */
.trade-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16) var(--page-padding);
}

.empty-icon {
  color: var(--color-gray-300);
  margin-bottom: var(--space-4);
}

.empty-text {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.empty-hint {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
}

/* 瀑布流容器 - 改为Grid布局确保显示顺序与数据一致 */
.waterfall-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-3);
  padding: var(--space-3);
}

/* 瀑布流项 */
.waterfall-item {
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
}

.waterfall-item:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

/* 商品图片 */
.waterfall-image {
  position: relative;
  width: 100%;
  aspect-ratio: 4/3;
  background-color: var(--bg-tertiary);
  overflow: hidden;
}

.waterfall-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.waterfall-item:active .waterfall-image img {
  transform: scale(0.95);
}

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

/* 悬停效果 */
.waterfall-item:active .image-placeholder-stamp {
  transform: rotate(-4deg) scale(1.05);
  transition: transform 0.2s ease;
}

.waterfall-item:active .image-placeholder-description {
  color: #3D3428;
  transition: color 0.2s ease;
}

/* 类型标签 */
.item-type {
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

.item-type--buy {
  background-color: var(--color-success-600);
}

/* 状态标签 */
.item-status {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  padding: 4px var(--space-2);
  font-size: 10px;
  font-weight: var(--font-weight-semibold);
  border-radius: var(--radius-sm);
}

.item-status.status-sold {
  background-color: var(--color-gray-800);
  color: white;
}

.item-status.status-reserved {
  background-color: var(--color-warning-600);
  color: white;
}

/* 商品内容 */
.waterfall-content {
  padding: var(--space-3);
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.user-avatar {
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
  overflow: hidden;
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  font-weight: var(--font-weight-medium);
}

/* 商品标题 */
.item-title {
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
.item-footer {
  padding-top: var(--space-2);
  border-top: 1px solid var(--border-light);
  margin-top: var(--space-2);
}

.item-price {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-error-600);
}

/* 加载更多 */
.trade-loading-more,
.trade-finished {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4) 0;
}

.loading-text,
.finished-text {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

/* 响应式 */
@media (min-width: 768px) {
  .waterfall-container {
    grid-template-columns: repeat(3, 1fr);
    gap: var(--space-4);
    padding: var(--space-4);
  }

  .trade-header {
    padding: var(--space-8) var(--space-6);
  }

  .trade-tabs {
    padding: var(--space-4) var(--space-6);
  }
}

@media (min-width: 1024px) {
  .waterfall-container {
    grid-template-columns: repeat(4, 1fr);
    gap: var(--space-5);
    padding: var(--space-6);
  }

  /* PC端卡片增强 */
  .waterfall-item {
    border-radius: var(--radius-xl);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 4px 12px rgba(0, 0, 0, 0.03);
  }

  .waterfall-item:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08), 0 16px 48px rgba(0, 0, 0, 0.04);
  }

  .waterfall-item:active {
    transform: translateY(-2px);
  }

  .waterfall-content {
    padding: var(--space-4);
  }

  .item-title {
    font-size: var(--text-base);
  }

  .trade-header {
    padding: var(--space-10) var(--space-8);
    background: linear-gradient(180deg, #FFFFFF 0%, #F8FAFC 100%);
    border-bottom: 1px solid var(--color-gray-100);
  }

  .trade-title {
    font-size: var(--text-3xl);
    margin-bottom: var(--space-2);
  }

  .trade-subtitle {
    font-size: var(--text-sm);
    letter-spacing: 0.15em;
  }

  .trade-tabs {
    padding: var(--space-5) var(--space-8);
    background: linear-gradient(180deg, #FFFFFF 0%, #FAFAFA 100%);
    border-bottom: 1px solid var(--color-gray-100);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  }

  .trade-tab {
    font-size: var(--text-lg);
    padding: var(--space-4) 0;
  }

  .trade-sort-btn {
    font-size: var(--text-sm);
    padding: var(--space-2) var(--space-3);
  }

  .tab-underline {
    height: 3px;
  }

  /* 用户信息增强 */
  .user-avatar {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }

  .user-name {
    font-size: var(--text-sm);
  }

  /* 价格增强 */
  .item-footer {
    padding-top: var(--space-3);
    margin-top: var(--space-3);
  }

  .item-price {
    font-size: var(--text-xl);
  }
}
</style>
