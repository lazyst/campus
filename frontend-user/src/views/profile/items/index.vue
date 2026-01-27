<template>
  <div class="profile-items-page">
    <NavBar title="我的闲置" :left-arrow="true" @click-left="onClickLeft" />

    <div class="profile-items-container">
      <!-- Loading State -->
      <div v-if="loading && items.length === 0" class="profile-items__state">
        <span class="profile-items__state-text">加载中...</span>
      </div>

      <!-- Empty State -->
      <div v-else-if="items.length === 0" class="profile-items__state">
        <span class="profile-items__state-text">暂无闲置物品</span>
      </div>

      <!-- Item List -->
      <div v-else class="profile-items__list">
        <div
          v-for="item in items"
          :key="item.id"
          class="profile-items__item"
          @click="onItemClick(item)"
        >
          <div class="profile-items__item-image">
            {{ item.title.charAt(0) }}
          </div>
          <div class="profile-items__item-info">
            <div class="profile-items__item-title">{{ item.title }}</div>
            <div class="profile-items__item-price">¥{{ item.price }}</div>
            <div class="profile-items__item-status">
              <span
                class="profile-items__status-badge"
                :class="getStatusClass(item.status)"
              >
                {{ getStatusText(item.status) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Loading More -->
        <div v-if="loading && items.length > 0" class="profile-items__state">
          <span class="profile-items__state-text">加载中...</span>
        </div>

        <!-- Finished -->
        <div v-if="finished && items.length > 0" class="profile-items__state">
          <span class="profile-items__state-text profile-items__state-text--muted">没有更多了</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyItems } from '@/api/modules'
import NavBar from '@/components/navigation/NavBar.vue'

interface Item {
  id: number
  title: string
  price: number
  status: number
}

const router = useRouter()
const items = ref<Item[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

async function loadItems() {
  if (loading.value || finished.value) return

  try {
    loading.value = true
    const response = await getMyItems({
      page: page.value,
      size: 10
    }) as { records: Item[], total: number }
    items.value.push(...response.records)
    page.value++

    if (items.value.length >= response.total) {
      finished.value = true
    }
  } catch (error) {
    console.error('获取物品列表失败:', error)
  } finally {
    loading.value = false
  }
}

function onItemClick(item: Item) {
  router.push(`/trade/${item.id}`)
}

function onClickLeft() {
  router.back()
}

function getStatusText(status: number): string {
  const statusMap: Record<number, string> = {
    0: '在售',
    1: '已下架',
    2: '已完成'
  }
  return statusMap[status] || '未知'
}

function getStatusClass(status: number): string {
  const classMap: Record<number, string> = {
    0: 'profile-items__status-badge--active',
    1: 'profile-items__status-badge--inactive',
    2: 'profile-items__status-badge--completed'
  }
  return classMap[status] || 'profile-items__status-badge--inactive'
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

.profile-items-container {
  padding: var(--page-padding);
}

.profile-items__state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) 0;
}

.profile-items__state-text {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.profile-items__state-text--muted {
  font-size: var(--text-xs);
}

.profile-items__list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.profile-items__item {
  display: flex;
  padding: var(--space-4);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.profile-items__item:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

.profile-items__item-image {
  width: 80px;
  height: 80px;
  background-color: var(--color-primary-700);
  color: #FFFFFF;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-xl);
  font-weight: var(--font-weight-semibold);
  margin-right: var(--space-3);
  flex-shrink: 0;
}

.profile-items__item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.profile-items__item-title {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-items__item-price {
  font-size: var(--text-lg);
  color: var(--color-error-500);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-2);
}

.profile-items__item-status {
  display: flex;
  align-items: center;
}

.profile-items__status-badge {
  display: inline-block;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  font-size: var(--text-xs);
}

.profile-items__status-badge--active {
  background-color: var(--color-success-100);
  color: var(--color-success-600);
}

.profile-items__status-badge--inactive {
  background-color: var(--color-warning-100);
  color: var(--color-warning-600);
}

.profile-items__status-badge--completed {
  background-color: var(--bg-tertiary);
  color: var(--text-secondary);
}
</style>
