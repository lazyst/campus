<template>
  <div class="trade-page">
    <van-nav-bar title="闲置交易" />
    <TradeList />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import TradeList from '@/views/trade/components/TradeList.vue'

const router = useRouter()
</script>

<style scoped>
.trade-page {
  min-height: 100vh;
  background: #f5f5f5;
}
</style>
