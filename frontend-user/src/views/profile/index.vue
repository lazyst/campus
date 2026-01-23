<template>
  <div class="profile-page min-h-screen bg-gray-100">
    <div class="profile-header bg-white p-6 flex items-center">
      <div class="avatar-section mr-5">
        <div class="w-20 h-20 rounded-full bg-primary text-white flex items-center justify-center text-2xl">
          {{ userInfo?.nickname?.charAt(0) || '未' }}
        </div>
      </div>
      <div class="info-section">
        <h2 class="text-xl font-bold mb-1">{{ userInfo?.nickname || '未登录' }}</h2>
        <p class="text-gray-500 text-sm">{{ userInfo?.phone || '请先登录' }}</p>
      </div>
    </div>

    <div class="profile-menu mt-5">
      <div class="bg-white">
        <div 
          class="flex justify-between items-center p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50"
          @click="router.push('/profile/edit')"
        >
          <span>编辑资料</span>
          <span class="text-gray-400">›</span>
        </div>
        <div 
          class="flex justify-between items-center p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50"
          @click="router.push('/profile/posts')"
        >
          <span>我的帖子</span>
          <span class="text-gray-400">›</span>
        </div>
        <div 
          class="flex justify-between items-center p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50"
          @click="router.push('/profile/items')"
        >
          <span>我的闲置</span>
          <span class="text-gray-400">›</span>
        </div>
        <div 
          class="flex justify-between items-center p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50"
          @click="router.push('/profile/collections')"
        >
          <span>我的收藏</span>
          <span class="text-gray-400">›</span>
        </div>
        <div 
          class="flex justify-between items-center p-4 cursor-pointer hover:bg-gray-50"
          @click="router.push('/profile/messages')"
        >
          <span>消息通知</span>
          <span class="text-gray-400">›</span>
        </div>
      </div>
    </div>

    <div class="logout-section mt-10 px-5">
      <BaseButton type="primary" block @click="handleLogout">
        退出登录
      </BaseButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import BaseButton from '@/components/base/Button.vue'

const userStore = useUserStore()
const router = useRouter()

const userInfo = ref(userStore.userInfo)

onMounted(async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  await userStore.fetchUserInfo()
  userInfo.value = userStore.userInfo
})

async function handleLogout() {
  try {
    await userStore.logout()
    alert('已退出登录')
    router.push('/login')
  } catch (error) {
    alert('退出失败')
  }
}
</script>
