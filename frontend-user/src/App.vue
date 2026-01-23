<template>
  <van-config-provider :theme-vars="themeVars">
    <router-view />
  </van-config-provider>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const themeVars = ref({
  primaryColor: '#1989fa',
  successColor: '#07c160',
  warningColor: '#ff976a',
  dangerColor: '#ee0a24'
})
</script>

<style scoped>
</style>
