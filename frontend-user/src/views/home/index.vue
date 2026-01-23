<template>
  <div class="home-redirect">
    <van-loading type="spinner" />
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

<style scoped>
.home-redirect {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
}
</style>
