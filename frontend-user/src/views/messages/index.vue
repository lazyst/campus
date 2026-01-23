<template>
  <div class="messages-page">
    <van-nav-bar title="消息" left-arrow @click-left="onClickLeft" />
    <MessageList />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import MessageList from './components/MessageList.vue'

const router = useRouter()

function onClickLeft() {
  router.back()
}
</script>

<style scoped>
.messages-page {
  min-height: 100vh;
  background: #f5f5f5;
}
</style>
