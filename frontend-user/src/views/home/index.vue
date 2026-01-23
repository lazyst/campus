<template>
  <div class="home-redirect min-h-screen flex items-center justify-center bg-gray-100">
    <!-- Custom loading spinner -->
    <div class="flex flex-col items-center">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-primary mb-4"></div>
      <span class="text-gray-500">加载中...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  // Redirect to forum page
  router.replace({ name: 'Forum' })
})
</script>
