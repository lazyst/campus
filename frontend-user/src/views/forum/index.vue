<template>
  <div class="forum-page min-h-screen bg-gray-100">
    <NavBar title="论坛" :left-arrow="true" @click-left="onClickLeft" />
    <ForumList class="forum-page__list" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'
import ForumList from './components/ForumList.vue'

const router = useRouter()

function onClickLeft() {
  router.back()
}
</script>

<style scoped>
.forum-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  height: 100%;
  background: var(--bg-page);
}

.forum-page__list {
  flex: 1;
  /* 确保列表底部不被 TabBar 遮挡 - 使用 padding 替代 margin */
  padding-bottom: calc(var(--tabbar-height) + var(--page-safe-bottom, 16px));
  overflow-y: auto;
}
</style>
