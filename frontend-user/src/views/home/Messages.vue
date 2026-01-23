<template>
  <div class="messages-page">
    <van-nav-bar title="消息" />
    <MessageList />
  </div>
</template>

<script setup lang="ts">
import MessageList from '@/views/messages/components/MessageList.vue'
</script>

<style scoped>
.messages-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 50px;
}
</style>
