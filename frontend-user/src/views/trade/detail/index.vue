<template>
  <div class="item-detail">
    <van-nav-bar title="物品详情" left-arrow @click-left="onClickLeft" />
    
    <div v-if="item" class="item-content">
      <div class="item-image-placeholder">物品图片</div>
      <div class="item-info">
        <div class="item-price">¥{{ item.price }}</div>
        <div class="item-title">{{ item.title }}</div>
        <div class="item-meta">
          <span>{{ item.type === 1 ? '收购' : '出售' }}</span>
          <span>{{ item.viewCount }} 浏览</span>
          <span>{{ item.contactCount }} 联系</span>
        </div>
      </div>
      <div class="item-description">
        <div class="section-title">物品描述</div>
        <div class="section-content">{{ item.description }}</div>
      </div>
      <div class="user-info">
        <div class="user-avatar">用</div>
        <div class="user-detail">
          <div class="user-name">{{ item.nickname }}</div>
          <div class="user-time">发布于 {{ item.time }}</div>
        </div>
      </div>
      <div class="contact-action">
        <van-button type="primary" block @click="onContact">联系卖家</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

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
}
</script>

<style scoped>
.item-detail { min-height: 100vh; background: #f5f5f5; padding-bottom: 60px; }
.item-image-placeholder { height: 300px; background: #1989fa; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.item-info { padding: 16px; background: #fff; margin-bottom: 10px; }
.item-price { font-size: 24px; color: #f44; font-weight: bold; margin-bottom: 8px; }
.item-title { font-size: 18px; font-weight: bold; margin-bottom: 8px; }
.item-meta { display: flex; gap: 16px; font-size: 14px; color: #999; }
.item-description { padding: 16px; background: #fff; margin-bottom: 10px; }
.section-title { font-weight: bold; margin-bottom: 8px; }
.section-content { font-size: 14px; line-height: 1.6; }
.user-info { display: flex; align-items: center; padding: 16px; background: #fff; margin-bottom: 10px; }
.user-avatar { width: 50px; height: 50px; background: #1989fa; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-right: 12px; }
.user-name { font-weight: bold; }
.user-time { font-size: 12px; color: #999; }
.contact-action { position: fixed; bottom: 0; left: 0; right: 0; padding: 10px; background: #fff; }
</style>
