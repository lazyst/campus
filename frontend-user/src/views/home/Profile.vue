<template>
  <div class="min-h-screen bg-gray-100 pb-safe">
    <NavBar title="我的" />
    
    <div class="p-4">
      <!-- Profile Header -->
      <div class="bg-white rounded-xl p-5 mb-4 flex items-center" @click="goToLogin">
        <div class="w-15 h-15 bg-primary text-white rounded-full flex items-center justify-center text-2xl mr-4">
          我
        </div>
        <div class="flex-1">
          <div class="text-lg font-semibold text-gray-800 mb-1">点击登录</div>
          <div class="text-sm text-gray-400">登录后查看完整信息</div>
        </div>
        <span class="text-gray-400">›</span>
      </div>
      
      <!-- Menu Section -->
      <div class="bg-white rounded-xl mb-4">
        <div class="flex items-center px-4 py-4 border-b border-gray-100" @click="goTo('/profile/edit')">
          <span class="text-primary mr-3">编辑</span>
          <span class="flex-1 text-gray-800">编辑资料</span>
          <span class="text-gray-400">›</span>
        </div>
        <div class="flex items-center px-4 py-4 border-b border-gray-100" @click="goTo('/profile/posts')">
          <span class="text-primary mr-3">帖子</span>
          <span class="flex-1 text-gray-800">我的帖子</span>
          <span class="text-gray-400">›</span>
        </div>
        <div class="flex items-center px-4 py-4 border-b border-gray-100" @click="goTo('/profile/items')">
          <span class="text-primary mr-3">闲置</span>
          <span class="flex-1 text-gray-800">我的闲置</span>
          <span class="text-gray-400">›</span>
        </div>
        <div class="flex items-center px-4 py-4 border-b border-gray-100" @click="goTo('/profile/collections')">
          <span class="text-primary mr-3">收藏</span>
          <span class="flex-1 text-gray-800">我的收藏</span>
          <span class="text-gray-400">›</span>
        </div>
        <div class="flex items-center px-4 py-4" @click="goTo('/profile/messages')">
          <span class="text-primary mr-3">消息</span>
          <span class="flex-1 text-gray-800">消息通知</span>
          <span class="text-gray-400">›</span>
        </div>
      </div>
      
      <!-- Action Section -->
      <BaseButton block @click="goToLogin">
        登录/注册
      </BaseButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'
import BaseButton from '@/components/base/Button.vue'

const router = useRouter()

function goToLogin() {
  router.push('/login')
}
</script>
