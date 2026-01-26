<template>
  <div class="item-detail min-h-screen bg-gray-100 pb-16">
    <NavBar title="物品详情" :left-arrow="true" @click-left="onClickLeft" />

    <div v-if="item" class="item-content">
      <div class="item-image-placeholder h-72 bg-primary text-white flex items-center justify-center text-xl">
        物品图片
      </div>
      <div class="item-info p-4 bg-white mb-2.5">
        <div class="item-price text-2xl text-red-500 font-bold mb-2">¥{{ item.price }}</div>
        <div class="item-title text-lg font-bold mb-2">{{ item.title }}</div>
        <div class="item-meta flex gap-4 text-sm text-gray-500">
          <span>{{ item.type === 1 ? '收购' : '出售' }}</span>
          <span>{{ item.viewCount }} 浏览</span>
          <span>{{ item.contactCount }} 联系</span>
        </div>
      </div>
      <div class="item-description p-4 bg-white mb-2.5">
        <div class="section-title font-bold mb-2">物品描述</div>
        <div class="section-content text-sm leading-relaxed">{{ item.description }}</div>
      </div>
      <div class="user-info flex items-center p-4 bg-white mb-2.5">
        <div class="user-avatar w-12 h-12 bg-primary text-white rounded-full flex items-center justify-center mr-3">
          用
        </div>
        <div class="user-detail flex-1">
          <div class="user-name font-bold">{{ item.userNickname }}</div>
          <div class="user-time text-xs text-gray-500">发布于 {{ formatDate(item.createdAt) }}</div>
        </div>
        <BaseButton
          :type="isCollected ? 'primary' : 'default'"
          size="small"
          @click="onToggleCollect"
        >
          {{ isCollected ? '已收藏' : '收藏' }}
        </BaseButton>
      </div>
      <div class="contact-action fixed bottom-0 left-0 right-0 p-2.5 bg-white border-t border-gray-100 pb-safe">
        <BaseButton type="primary" block @click="onContact">
          联系卖家
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItemById, contactSeller, toggleItemCollect, checkItemCollected } from '@/api/modules'
import NavBar from '@/components/navigation/NavBar.vue'
import BaseButton from '@/components/base/Button.vue'

const route = useRoute()
const router = useRouter()
const item = ref(null)
const isCollected = ref(false)

async function loadItemDetail() {
  try {
    const itemId = route.params.id
    item.value = await getItemById(itemId)

    // 检查收藏状态
    isCollected.value = await checkItemCollected(itemId)
  } catch (error) {
    console.error('获取物品详情失败:', error)
  }
}

async function onContact() {
  try {
    await contactSeller(route.params.id)
    // 跳转到消息页面
    router.push('/messages')
  } catch (error) {
    console.error('联系卖家失败:', error)
  }
}

async function onToggleCollect() {
  try {
    const result = await toggleItemCollect(route.params.id)
    isCollected.value = result.isCollected
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

function onClickLeft() {
  router.back()
}

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  loadItemDetail()
})
</script>
