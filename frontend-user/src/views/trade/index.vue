<template>
  <div class="trade-page min-h-screen bg-gray-100 pb-16">
    <NavBar title="闲置交易" :left-arrow="true" @click-left="onClickLeft" />
    <TradeList />
    
    <!-- Floating Add Button -->
    <div 
      class="fixed bottom-6 right-4 w-14 h-14 bg-primary rounded-full flex items-center justify-center text-white text-3xl shadow-lg cursor-pointer hover:bg-blue-600 active:bg-blue-700 transition-colors z-40"
      @click="onAdd"
    >
      +
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'
import TradeList from './components/TradeList.vue'

const router = useRouter()

function onClickLeft() {
  router.back()
}

function onAdd() {
  router.push('/trade/create')
}
</script>
