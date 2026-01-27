<template>
  <div class="trade-list">
    <!-- Category Tabs -->
    <div class="trade-tabs">
      <button
        class="trade-tab"
        :class="{ 'trade-tab--active': activeTab === 0 }"
        @click="onTabChange(0)"
      >
        出售
      </button>
      <button
        class="trade-tab"
        :class="{ 'trade-tab--active': activeTab === 1 }"
        @click="onTabChange(1)"
      >
        收购
      </button>
    </div>
    
    <!-- Item List -->
    <div class="trade-content">
      <!-- Empty State -->
      <div v-if="list.length === 0 && !loading" class="trade-state">
        <span class="trade-state-text">暂无闲置物品</span>
      </div>
      
      <!-- Item Cards -->
      <div 
        v-for="item in list" 
        :key="item.id" 
        class="trade-card"
        @click="onItemClick(item)"
      >
        <!-- Item Type Badge -->
        <div 
          class="trade-card__badge"
          :class="{ 'trade-card__badge--buy': item.type === 1 }"
        >
          {{ item.type === 1 ? '收购' : '出售' }}
        </div>
        
        <!-- Item Content -->
        <div class="trade-card__content">
          <h3 class="trade-card__title">{{ item.title }}</h3>
          <p class="trade-card__price">¥{{ item.price }}</p>
          <div class="trade-card__footer">
            <span class="trade-card__author">{{ item.nickname }}</span>
            <span class="trade-card__views">浏览 {{ item.viewCount }}</span>
          </div>
        </div>
      </div>
      
      <!-- Loading -->
      <div v-if="loading" class="trade-state">
        <span class="trade-state-text">加载中...</span>
      </div>
      
      <!-- Finished -->
      <div v-if="finished && list.length > 0" class="trade-state">
        <span class="trade-state-text trade-state-text--muted">没有更多了</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

interface TradeItem {
  id: number
  type: number // 1: 收购, 2: 出售
  title: string
  price: number
  nickname: string
  viewCount: number
}

const router = useRouter()
const activeTab = ref(0)
const list = ref<TradeItem[]>([])
const loading = ref(false)
const finished = ref(false)

function onLoad() {
  setTimeout(() => {
    for (let i = 0; i < 5; i++) {
      list.value.push({
        id: list.value.length + 1,
        type: activeTab.value + 1,
        title: `物品 ${list.value.length + 1}`,
        price: Math.floor(Math.random() * 1000) + 100,
        nickname: '用户' + (list.value.length + 1),
        viewCount: Math.floor(Math.random() * 200)
      })
    }
    loading.value = false
    if (list.value.length >= 20) {
      finished.value = true
    }
  }, 500)
}

function onTabChange(tab: number) {
  activeTab.value = tab
  list.value = []
  finished.value = false
  onLoad()
}

function onItemClick(item: TradeItem) {
  router.push(`/trade/${item.id}`)
}

// Initial load
onLoad()
</script>

<style scoped>
.trade-list {
  background-color: var(--bg-page);
}

.trade-tabs {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3) var(--page-padding);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  overflow-x: auto;
  white-space: nowrap;
  -webkit-overflow-scrolling: touch;
}

.trade-tabs::-webkit-scrollbar {
  display: none;
}

.trade-tab {
  flex-shrink: 0;
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background-color: var(--bg-tertiary);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.trade-tab:active {
  background-color: var(--color-primary-100);
}

.trade-tab--active {
  color: var(--text-inverse);
  background-color: var(--color-primary-700);
}

.trade-tab--active:active {
  background-color: var(--color-primary-800);
}

.trade-content {
  padding: var(--space-3);
}

.trade-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) 0;
}

.trade-state-text {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.trade-state-text--muted {
  font-size: var(--text-xs);
}

.trade-card {
  display: flex;
  align-items: flex-start;
  padding: var(--space-3);
  margin-bottom: var(--space-3);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
}

.trade-card:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

.trade-card__badge {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-xs);
  font-weight: var(--font-weight-bold);
  color: var(--text-inverse);
  border-radius: var(--radius-md);
  flex-shrink: 0;
  margin-right: var(--space-3);
  background-color: var(--color-primary-600);
}

.trade-card__badge--buy {
  background-color: var(--color-success-500);
}

.trade-card__content {
  flex: 1;
  min-width: 0;
}

.trade-card__title {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.trade-card__price {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-error-600);
  margin-bottom: var(--space-2);
}

.trade-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.trade-card__author {
  font-weight: var(--font-weight-medium);
}

.trade-card__views {
  color: var(--text-tertiary);
}
</style>
