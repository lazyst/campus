<template>
  <div class="trade-page">
    <van-nav-bar title="闲置交易" left-arrow @click-left="onClickLeft" />
    <TradeList />
    <van-floating-bubble icon="plus" @click="onAdd" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import TradeList from './components/TradeList.vue'

const router = useRouter()

function onClickLeft() {
  router.back()
}

function onAdd() {
  router.push('/trade/create')
}
</script>

<style scoped>
.trade-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 60px;
}
</style>
