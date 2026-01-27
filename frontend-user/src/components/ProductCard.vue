<template>
  <div class="product-card" @click="handleClick">
    <!-- 商品图片 -->
    <div class="product-image">
      <img v-if="image" :src="image" :alt="title" />
      <div v-else class="image-placeholder">
        <span>📦</span>
      </div>
      <span 
        v-if="status" 
        class="product-status"
        :class="status"
      >
        {{ statusText }}
      </span>
    </div>

    <!-- 商品信息 -->
    <div class="product-info">
      <h3 class="product-title">{{ title }}</h3>
      
      <div class="product-tags" v-if="tags && tags.length">
        <span 
          v-for="tag in tags" 
          :key="tag" 
          class="tag"
          :style="getTagStyle(tag)"
        >
          {{ tag }}
        </span>
      </div>

      <div class="product-footer">
        <span class="product-price">¥{{ price }}</span>
        <span v-if="originalPrice" class="product-original">
          ¥{{ originalPrice }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { categoryColors } from '@/styles/theme';

interface Props {
  id: number | string;
  image?: string;
  title: string;
  price: number;
  originalPrice?: number;
  tags?: string[];
  status?: 'normal' | 'sold' | 'reserved';
}

const props = withDefaults(defineProps<Props>(), {
  status: 'normal',
  tags: () => [],
});

const emit = defineEmits<{
  click: [id: number | string];
}>();

const statusText = computed(() => {
  const texts = {
    normal: '',
    sold: '已售',
    reserved: '已预定',
  };
  return texts[props.status];
});

function getTagStyle(tag: string) {
  const scheme = categoryColors[tag] || categoryColors['交流'];
  return {
    backgroundColor: scheme.bg,
    color: scheme.text,
  };
}

function handleClick() {
  emit('click', props.id);
}
</script>

<style scoped>
.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.08);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.12);
}

.product-image {
  position: relative;
  width: 100%;
  height: 160px;
  background: #E2E8F0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
}

.product-status {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.product-status.sold {
  background: #D1FAE5;
  color: #10B981;
}

.product-status.reserved {
  background: #FEF3C7;
  color: #F59E0B;
}

.product-info {
  padding: 12px;
}

.product-title {
  font-size: 14px;
  font-weight: 500;
  color: #1E293B;
  margin: 0 0 8px 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.tag {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
}

.product-footer {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #EF4444;
}

.product-original {
  font-size: 12px;
  color: #94A3B8;
  text-decoration: line-through;
}
</style>
