<template>
  <div class="messages-page min-h-screen bg-gray-100">
    <NavBar title="消息" :left-arrow="true" @click-left="onClickLeft" />
    <MessageList />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'
import MessageList from './components/MessageList.vue'

const router = useRouter()

function onClickLeft() {
  router.back()
}
</script>
