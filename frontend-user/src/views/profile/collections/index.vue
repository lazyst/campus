<template>
  <div class="my-collections">
    <van-nav-bar title="我的收藏" left-arrow @click-left="onClickLeft" />
    
    <van-empty description="暂无收藏" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function onClickLeft() {
  router.back()
}
</script>

<style scoped>
.my-collections { min-height: 100vh; background: #f5f5f5; }
</style>
