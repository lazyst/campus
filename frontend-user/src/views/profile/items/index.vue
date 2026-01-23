<template>
  <div class="my-items">
    <van-nav-bar title="我的闲置" left-arrow @click-left="onClickLeft" />
    
    <van-list finished-text="没有更多了">
      <div v-for="i in 5" :key="i" class="item-row">
        <div class="item-image">物</div>
        <div class="item-info">
          <div class="item-title">闲置物品 {{ i }}</div>
          <div class="item-price">¥{{ i * 100 }}</div>
          <div class="item-status">
            <van-tag :type="i % 2 === 0 ? 'success' : 'warning'">
              {{ i % 2 === 0 ? '已上架' : '已下架' }}
            </van-tag>
          </div>
        </div>
      </div>
    </van-list>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function onClickLeft() {
  router.back()
}
</script>

<style scoped>
.my-items { min-height: 100vh; background: #f5f5f5; padding: 10px; }
.item-row { display: flex; background: #fff; border-radius: 8px; padding: 12px; margin-bottom: 10px; }
.item-image { width: 80px; height: 80px; background: #1989fa; color: #fff; border-radius: 8px; display: flex; align-items: center; justify-content: center; margin-right: 12px; }
.item-info { flex: 1; }
.item-title { font-size: 16px; font-weight: bold; margin-bottom: 8px; }
.item-price { font-size: 18px; color: #f44; margin-bottom: 8px; }
</style>
