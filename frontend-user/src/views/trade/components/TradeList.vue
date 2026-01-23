<template>
  <div class="trade-list">
    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="出售"></van-tab>
      <van-tab title="收购"></van-tab>
    </van-tabs>
    
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
    >
      <div v-for="item in list" :key="item.id" class="trade-item" @click="onItemClick(item)">
        <div class="trade-image">{{ item.type === 1 ? '收' : '售' }}</div>
        <div class="trade-content">
          <div class="trade-title">{{ item.title }}</div>
          <div class="trade-price">¥{{ item.price }}</div>
          <div class="trade-footer">
            <span>{{ item.nickname }}</span>
            <span>{{ item.viewCount }} 浏览</span>
          </div>
        </div>
      </div>
    </van-list>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

interface TradeItem {
  id: number
  type: number // 1: 收购, 2: 出售
  title: string
  price: number
  nickname: string
  viewCount: number
}

const router = useRouter()
const activeTab = ref(0)
const list = ref<TradeItem[]>([])
const loading = ref(false)
const finished = ref(false)

function onLoad() {
  setTimeout(() => {
    for (let i = 0; i < 5; i++) {
      list.value.push({
        id: list.value.length + 1,
        type: activeTab.value + 1,
        title: `物品 ${list.value.length + 1}`,
        price: Math.floor(Math.random() * 1000) + 100,
        nickname: '用户' + (list.value.length + 1),
        viewCount: Math.floor(Math.random() * 200)
      })
    }
    loading.value = false
    if (list.value.length >= 20) {
      finished.value = true
    }
  }, 500)
}

function onTabChange() {
  list.value = []
  finished.value = false
  onLoad()
}

function onItemClick(item: TradeItem) {
  router.push(`/trade/${item.id}`)
}
</script>

<style scoped>
.trade-list {
  padding: 10px;
}
.trade-item {
  display: flex;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
}
.trade-image {
  width: 80px;
  height: 80px;
  background: #1989fa;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  border-radius: 8px;
  margin-right: 12px;
}
.trade-content {
  flex: 1;
}
.trade-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
}
.trade-price {
  font-size: 18px;
  color: #f44;
  font-weight: bold;
  margin-bottom: 8px;
}
.trade-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}
</style>
