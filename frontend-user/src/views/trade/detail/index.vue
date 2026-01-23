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
        <div class="user-detail">
          <div class="user-name font-bold">{{ item.nickname }}</div>
          <div class="user-time text-xs text-gray-500">发布于 {{ item.time }}</div>
        </div>
      </div>
      <div class="contact-action fixed bottom-0 left-0 right-0 p-2.5 bg-white border-t border-gray-100 pb-safe">
        <BaseButton type="primary" block @click="onContact">
          联系卖家
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'
import BaseButton from '@/components/base/Button.vue'

interface Item {
  title: string
  price: number
  type: number
  description: string
  nickname: string
  time: string
  viewCount: number
  contactCount: number
}

const route = useRoute()
const router = useRouter()
const item = ref<Item | null>(null)

onMounted(() => {
  setTimeout(() => {
    item.value = {
      title: `闲置物品详情 - ID: ${route.params.id}`,
      price: 500,
      type: 2,
      description: '这是一款闲置物品的详细描述，包含物品的现状、新旧程度等信息。',
      nickname: '测试用户',
      time: '2026-01-22 10:00',
      viewCount: 45,
      contactCount: 12
    }
  }, 500)
})

function onClickLeft() {
  router.back()
}

function onContact() {
  // TODO: Implement contact logic
  alert('联系卖家功能开发中')
}
</script>
