<template>
  <div class="my-collections min-h-screen bg-gray-100">
    <NavBar title="我的收藏" :left-arrow="true" @click-left="onClickLeft" />
    
    <div class="flex flex-col items-center justify-center py-20">
      <div class="text-4xl mb-4">暂无收藏</div>
      <div class="text-gray-400">快去收藏一些内容吧</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'

const router = useRouter()

function onClickLeft() {
  router.back()
}
</script>
