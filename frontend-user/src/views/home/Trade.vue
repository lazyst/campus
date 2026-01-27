<template>
  <div class="trade-page">
    <!-- Navigation Bar -->
    <NavBar title="闲置交易" />
    
    <!-- Trade List -->
    <TradeList />
  </div>
</template>

<script setup lang="ts">
import TradeList from '@/views/trade/components/TradeList.vue'
import NavBar from '@/components/navigation/NavBar.vue'
</script>

<style scoped>
.trade-page {
  min-height: 100vh;
  background-color: var(--bg-page);
}
</style>
