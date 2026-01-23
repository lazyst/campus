<template>
  <div class="forum-page">
    <van-nav-bar title="论坛" left-arrow @click-left="onClickLeft" />
    <ForumList />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import ForumList from './components/ForumList.vue'

const router = useRouter()

function onClickLeft() {
  router.back()
}
</script>

<style scoped>
.forum-page {
  min-height: 100vh;
  background: #f5f5f5;
}
</style>
