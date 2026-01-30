<script setup lang="ts">
import { ref } from 'vue'
import ImagePreview from '@/components/ImagePreview.vue'

// 图片列表
const images = ref<string[]>([
  '/images/photo1.jpg',
  '/images/photo2.jpg',
  '/images/photo3.jpg'
])

// ImageInfo 格式（支持对齐方式）
import type { ImageInfo } from '@/components/ImagePreview.types'

const imagesWithAlign: (string | ImageInfo)[] = [
  { url: '/images/photo1.jpg', align: 'center' },
  { url: '/images/photo2.jpg', align: 'start' },
  { url: '/images/photo3.jpg', align: 'end' }
]

// 控制显示
const visible = ref(false)
const currentIndex = ref(0)

// 打开预览
function openPreview(index: number = 0) {
  currentIndex.value = index
  visible.value = true
}

// 关闭事件
function handleClose(context: { trigger: string; index: number }) {
  console.log('关闭原因:', context.trigger)
  console.log('当前索引:', context.index)
  visible.value = false
}

// 删除事件
function handleDelete(index: number) {
  console.log('删除图片索引:', index)
  images.value.splice(index, 1)
  
  if (images.value.length === 0) {
    visible.value = false
  }
}

// 索引变更事件
function handleIndexChange(index: number, context: { trigger: 'prev' | 'next' }) {
  console.log('切换到索引:', index)
  console.log('触发方式:', context.trigger)
}
</script>

<template>
  <div class="page">
    <h2>图片预览示例</h2>
    
    <div class="image-grid">
      <img 
        v-for="(img, index) in images" 
        :key="index"
        :src="getImageUrl(img)"
        @click="openPreview(index)"
      />
    </div>
    
    <button @click="openPreview(0)">打开预览</button>
    
    <!-- 基础用法 -->
    <ImagePreview
      v-model:visible="visible"
      v-model:index="currentIndex"
      :images="images"
      @close="handleClose"
      @indexChange="handleIndexChange"
    />
    
    <!-- 带删除功能 -->
    <ImagePreview
      v-model:visible="visible"
      v-model:index="currentIndex"
      :images="images"
      :closeBtn="true"
      :deleteBtn="true"
      :showIndex="true"
      :maxZoom="3"
      @close="handleClose"
      @delete="handleDelete"
      @indexChange="handleIndexChange"
    />
    
    <!-- 自定义配置 -->
    <ImagePreview
      v-model:visible="visible"
      v-model:index="currentIndex"
      :images="imagesWithAlign"
      :closeBtn="true"
      :deleteBtn="false"
      :showIndex="true"
      :maxZoom="2"
      @close="handleClose"
      @indexChange="handleIndexChange"
    />
  </div>
</template>
