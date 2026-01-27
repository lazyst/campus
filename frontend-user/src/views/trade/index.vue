<template>
  <div class="trade-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>闲置市场</h1>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <div 
        v-for="tab in tabs" 
        :key="tab.key"
        class="filter-tab"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 商品网格 -->
    <div class="product-grid">
      <ProductCard 
        v-for="product in products" 
        :key="product.id"
        :id="product.id"
        :image="product.image"
        :title="product.title"
        :price="product.price"
        :original-price="product.originalPrice"
        :tags="product.tags"
        :status="product.status"
        @click="goToProductDetail"
      />
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
import ProductCard from '@/components/ProductCard.vue';
import BottomNav from '@/components/BottomNav.vue';

const router = useRouter();

const activeTab = ref('all');
const activeNav = ref('trade');

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'sell', label: '出售' },
  { key: 'buy', label: '收购' },
];

// 模拟商品数据
const products = ref([
  {
    id: 1,
    image: '',
    title: 'MacBook Pro 13寸 2020款 M1芯片',
    price: 128,
    originalPrice: 9999,
    tags: ['电脑数码'],
    status: 'normal',
  },
  {
    id: 2,
    image: '',
    title: '高等数学辅导',
    price: 50,
    tags: ['学习'],
    status: 'normal',
  },
  {
    id: 3,
    image: '',
    title: '考研数学资料全套',
    price: 80,
    originalPrice: 200,
    tags: ['学习资料'],
    status: 'normal',
  },
  {
    id: 4,
    image: '',
    title: '自行车转让',
    price: 200,
    originalPrice: 500,
    tags: ['出行工具'],
    status: 'sold',
  },
  {
    id: 5,
    image: '',
    title: 'Switch游戏卡带',
    price: 150,
    originalPrice: 280,
    tags: ['游戏'],
    status: 'normal',
  },
  {
    id: 6,
    image: '',
    title: '二手iPad Air 4',
    price: 2800,
    originalPrice: 4399,
    tags: ['电脑数码'],
    status: 'reserved',
  },
]);

function goToProductDetail(id: number | string) {
  router.push(`/trade/${id}`);
}

function handleNavChange(name: string) {
  activeNav.value = name;
  // TODO: 处理导航切换
}
</script>

<style scoped>
.trade-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding-bottom: 80px;
}

.page-header {
  padding: 16px;
  background: white;
}

.page-header h1 {
  font-size: 20px;
  font-weight: 700;
  color: #1E293B;
  margin: 0;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  padding: 16px;
  background: white;
  overflow-x: auto;
}

.filter-tab {
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.filter-tab:not(.active) {
  background: #F1F5F9;
  color: #64748B;
}

.filter-tab.active {
  background: #6366F1;
  color: white;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 16px;
}
</style>
