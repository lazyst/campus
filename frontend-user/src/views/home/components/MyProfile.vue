<template>
  <div class="my-profile min-h-[calc(100vh-50px)] bg-gray-100 p-4">
    <div class="profile-header flex items-center bg-white rounded-xl p-5 mb-4 cursor-pointer" @click="goToProfile">
      <div class="avatar w-15 h-15 bg-primary text-white rounded-full flex items-center justify-center text-2xl mr-4">
        我
      </div>
      <div class="user-info flex-1">
        <div class="nickname text-lg font-bold mb-1">点击登录</div>
        <div class="phone text-sm text-gray-400">登录后查看完整信息</div>
      </div>
      <span class="text-gray-400">›</span>
    </div>
    
    <div class="menu-section bg-white rounded-xl mb-4">
      <div class="menu-item flex items-center p-4 border-b border-gray-100 cursor-pointer" @click="goTo('/profile/edit')">
        <span class="w-5 text-primary mr-3">编</span>
        <span class="flex-1">编辑资料</span>
        <span class="text-gray-400">›</span>
      </div>
      <div class="menu-item flex items-center p-4 border-b border-gray-100 cursor-pointer" @click="goTo('/profile/posts')">
        <span class="w-5 text-primary mr-3">帖</span>
        <span class="flex-1">我的帖子</span>
        <span class="text-gray-400">›</span>
      </div>
      <div class="menu-item flex items-center p-4 border-b border-gray-100 cursor-pointer" @click="goTo('/profile/items')">
        <span class="w-5 text-primary mr-3">闲</span>
        <span class="flex-1">我的闲置</span>
        <span class="text-gray-400">›</span>
      </div>
      <div class="menu-item flex items-center p-4 border-b border-gray-100 cursor-pointer" @click="goTo('/profile/collections')">
        <span class="w-5 text-primary mr-3">收</span>
        <span class="flex-1">我的收藏</span>
        <span class="text-gray-400">›</span>
      </div>
      <div class="menu-item flex items-center p-4 cursor-pointer" @click="goTo('/profile/messages')">
        <span class="w-5 text-primary mr-3">消</span>
        <span class="flex-1">消息通知</span>
        <span class="text-gray-400">›</span>
      </div>
    </div>
    
    <div class="action-section px-4">
      <BaseButton type="primary" block @click="goToLogin">
        登录/注册
      </BaseButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import BaseButton from '@/components/base/Button.vue'

const router = useRouter()

function goToProfile() {
  router.push('/profile')
}

function goTo(path: string) {
  router.push(path)
}

function goToLogin() {
  router.push('/login')
}
</script>
