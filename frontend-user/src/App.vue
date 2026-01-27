<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 应用启动时初始化用户状态
onMounted(async () => {
  await userStore.initialize()
})
</script>

<style scoped>
</style>
