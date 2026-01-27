<template>
  <div class="post-card" @click="handleClick">
    <!-- 作者信息 -->
    <div class="post-header">
      <div class="author-avatar">
        <img v-if="avatar" :src="avatar" :alt="author" />
        <span v-else class="avatar-placeholder">{{ author.charAt(0) }}</span>
      </div>
      <div class="author-info">
        <span class="author-name">{{ author }}</span>
        <span class="post-time">{{ formattedTime }}</span>
      </div>
    </div>

    <!-- 帖子内容 -->
    <div class="post-content">
      <h3 class="post-title">{{ title }}</h3>
      <p class="post-excerpt" v-if="excerpt">{{ excerpt }}</p>
    </div>

    <!-- 帖子标签 -->
    <div class="post-tags" v-if="tags && tags.length">
      <span 
        v-for="tag in tags" 
        :key="tag" 
        class="tag"
        :style="getTagStyle(tag)"
      >
        {{ tag }}
      </span>
    </div>

    <!-- 互动数据 -->
    <div class="post-stats">
      <div class="stat-item">
        <span class="stat-icon">❤️</span>
        <span class="stat-value">{{ likes }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-icon">💬</span>
        <span class="stat-value">{{ comments }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-icon">👁️</span>
        <span class="stat-value">{{ views }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { categoryColors } from '@/styles/theme';

interface Props {
  id: number | string;
  author: string;
  avatar?: string;
  title: string;
  excerpt?: string;
  time: string | Date;
  tags?: string[];
  likes?: number;
  comments?: number;
  views?: number;
}

const props = withDefaults(defineProps<Props>(), {
  likes: 0,
  comments: 0,
  views: 0,
  tags: () => [],
});

const emit = defineEmits<{
  click: [id: number | string];
}>();

const formattedTime = computed(() => {
  const date = new Date(props.time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  
  return date.toLocaleDateString('zh-CN');
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
.post-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.08);
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.12);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #E2E8F0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 16px;
  color: #64748B;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #1E293B;
}

.post-time {
  font-size: 12px;
  color: #94A3B8;
}

.post-content {
  margin-bottom: 12px;
}

.post-title {
  font-size: 16px;
  font-weight: 500;
  color: #1E293B;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.post-excerpt {
  font-size: 14px;
  color: #64748B;
  margin: 0;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.post-stats {
  display: flex;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid #F1F5F9;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  font-size: 16px;
}

.stat-value {
  font-size: 13px;
  color: #64748B;
}
</style>
