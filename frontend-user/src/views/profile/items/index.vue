<template>
  <div class="profile-items-page">
    <NavBar title="我的闲置" :left-arrow="true" @click-left="onClickLeft" />

    <!-- Loading -->
    <div v-if="loading && items.length === 0" class="items-state">
      <span class="items-state-text">加载中...</span>
    </div>

    <!-- Empty -->
    <div v-else-if="items.length === 0" class="empty-state">
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
        v-for="item in items" 
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
            {{ item.type === 1 ? '收购' : '出售' }}
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
              <span>{{ (item.userNickname || '匿名').charAt(0) }}</span>
            </div>
            <span class="user-name">{{ item.userNickname }}</span>
          </div>

          <!-- 商品标题 -->
          <h3 class="item-title">{{ item.title }}</h3>

          <!-- 价格和操作按钮 -->
          <div class="item-footer">
            <span class="item-price">¥{{ item.price }}</span>
            <div class="item-actions">
              <!-- 完成按钮 -->
              <button
                v-if="item.status === 1"
                class="item-action-btn item-action-btn--complete"
                @click.stop="showCompleteDialog(item)"
                title="标记为已卖出"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 6L9 17l-5-5"></path>
                </svg>
              </button>
              <!-- 删除按钮 -->
              <button
                v-if="item.status === 1 || item.status === 2"
                class="item-action-btn item-action-btn--delete"
                @click.stop="showDeleteDialog(item)"
                title="删除"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading More -->
    <div v-if="loading && items.length > 0" class="items-state">
      <span class="items-state-text">加载中...</span>
    </div>

    <!-- Finished -->
    <div v-if="finished && items.length > 0" class="items-state">
      <span class="items-state-text items-state-text--muted">没有更多了</span>
    </div>

    <!-- 删除确认弹窗 -->
    <Dialog
      v-model:visible="deleteDialogVisible"
      title="删除闲置"
      message="确定要删除这个闲置物品吗？"
      theme="danger"
      confirm-text="删除"
      :loading="deleteLoading"
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />

    <!-- 完成确认弹窗 -->
    <Dialog
      v-model:visible="completeDialogVisible"
      title="标记已完成"
      message="确定要将此物品标记为已卖出吗？"
      theme="success"
      confirm-text="确定"
      :loading="completeLoading"
      @confirm="confirmComplete"
      @cancel="cancelComplete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyItems, deleteItem, completeItem } from '@/api/modules/item'
import { showToast } from '@/services/toastService'
import NavBar from '@/components/navigation/NavBar.vue'
import Dialog from '@/components/interactive/Dialog.vue'
import { getImageUrl } from '@/utils/imageUrl'

interface Item {
  id: number
  type: number
  title: string
  price: number
  description: string
  images: string | null
  status: number
  image?: string
  userNickname?: string
  userAvatar?: string
}

const router = useRouter()
const items = ref<Item[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

// 删除弹窗相关
const deleteDialogVisible = ref(false)
const deleteLoading = ref(false)
const itemToDelete = ref<Item | null>(null)

// 完成弹窗相关
const completeDialogVisible = ref(false)
const completeLoading = ref(false)
const itemToComplete = ref<Item | null>(null)

function getStatusText(status: number): string {
  const statusMap: Record<number, string> = {
    1: '在售',
    2: '已完成',
    3: '已下架'
  }
  return statusMap[status] || '未知'
}

function getStatusClass(status: number): string {
  const classMap: Record<number, string> = {
    1: 'status-normal',
    2: 'status-sold',
    3: 'status-reserved'
  }
  return classMap[status] || 'status-normal'
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

async function loadItems() {
  if (loading.value || finished.value) return

  try {
    loading.value = true
    const response = await getMyItems({
      page: page.value,
      size: 20
    })

    // 兼容两种格式：直接数组 或 Page 对象
    let records: any[] = []
    if (Array.isArray(response)) {
      records = response
    } else if (response && typeof response === 'object') {
      records = (response as { records?: any[] }).records || []
    }

    const newList = records.map(transformItem)
    items.value.push(...newList)
    page.value++

    if (newList.length < 20) {
      finished.value = true
    }
  } catch (error) {
    // 忽略错误
  } finally {
    loading.value = false
  }
}

function showDeleteDialog(item: Item) {
  itemToDelete.value = item
  deleteDialogVisible.value = true
}

function cancelDelete() {
  itemToDelete.value = null
  deleteDialogVisible.value = false
}

async function confirmDelete() {
  if (!itemToDelete.value) return

  try {
    deleteLoading.value = true
    await deleteItem(itemToDelete.value.id)
    items.value = items.value.filter(i => i.id !== itemToDelete.value!.id)
    showToast('删除成功', 'success')
  } catch (error) {
    // 忽略错误
    showToast('删除失败，请重试', 'error')
  } finally {
    deleteLoading.value = false
    itemToDelete.value = null
    deleteDialogVisible.value = false
  }
}

function showCompleteDialog(item: Item) {
  itemToComplete.value = item
  completeDialogVisible.value = true
}

function cancelComplete() {
  itemToComplete.value = null
  completeDialogVisible.value = false
}

async function confirmComplete() {
  if (!itemToComplete.value) return

  try {
    completeLoading.value = true
    await completeItem(itemToComplete.value.id)
    // 更新本地状态
    const item = items.value.find(i => i.id === itemToComplete.value!.id)
    if (item) {
      item.status = 3 // 3 = 已完成
    }
    showToast('操作成功，物品已标记为已卖出', 'success')
  } catch (error) {
    // 忽略错误
    showToast('操作失败，请重试', 'error')
  } finally {
    completeLoading.value = false
    itemToComplete.value = null
    completeDialogVisible.value = false
  }
}

function onItemClick(item: Item) {
  router.push(`/trade/${item.id}`)
}

function onClickLeft() {
  router.back()
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.profile-items-page {
  min-height: 100vh;
  background-color: var(--bg-page);
}

.items-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: calc(var(--nav-height) + var(--space-8)) var(--space-4) var(--space-8);
  min-height: 50vh;
}

.items-state-text {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.items-state-text--muted {
  font-size: var(--text-xs);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: calc(var(--nav-height) + var(--space-8)) var(--space-4) var(--space-8);
  min-height: 50vh;
}

.empty-icon {
  color: var(--text-tertiary);
  margin-bottom: var(--space-4);
}

.empty-icon svg {
  stroke: currentColor;
}

.empty-text {
  font-family: var(--font-family-sans);
  font-size: var(--text-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.empty-hint {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
}

/* 瀑布流容器 */
.waterfall-container {
  padding: calc(var(--nav-height) + var(--space-3)) var(--space-3) var(--space-3);
  column-count: 2;
  column-gap: var(--space-3);
}

/* 瀑布流项 */
.waterfall-item {
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

/* 操作按钮组 */
.item-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

/* 操作按钮 */
.item-action-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.item-action-btn--complete {
  background-color: var(--color-success-100);
  border: 1px solid var(--color-success-300);
  color: var(--color-success-600);
}

.item-action-btn--complete:hover {
  background-color: var(--color-success-200);
  border-color: var(--color-success-400);
  color: var(--color-success-700);
}

.item-action-btn--delete {
  background-color: var(--color-error-100);
  border: 1px solid var(--color-error-300);
  color: var(--color-error-600);
}

.item-action-btn--delete:hover {
  background-color: var(--color-error-200);
  border-color: var(--color-error-400);
  color: var(--color-error-700);
}

.item-action-btn:active {
  transform: scale(0.9);
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
  z-index: 2;
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
  z-index: 2;
}

.item-status.status-normal {
  background-color: var(--color-success-500);
  color: white;
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--space-2);
  border-top: 1px solid var(--border-light);
  margin-top: var(--space-2);
}

.item-price {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-error-600);
}

/* 响应式 */
@media (min-width: 768px) {
  .waterfall-container {
    column-count: 3;
    column-gap: var(--space-4);
    padding: var(--space-4);
  }
  
  .waterfall-item {
    margin-bottom: var(--space-4);
  }
}

@media (min-width: 1024px) {
  .waterfall-container {
    column-count: 4;
    column-gap: var(--space-4);
    padding: var(--space-4);
  }
}
</style>
