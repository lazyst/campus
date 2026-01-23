<template>
  <div class="forum-page min-h-screen bg-gray-100">
    <NavBar title="论坛" :left-arrow="true" @click-left="onClickLeft" />
    <ForumList />
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
