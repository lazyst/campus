<template>
  <div class="min-h-screen bg-gray-100">
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
