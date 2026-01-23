<template>
  <div class="notification-messages">
    <van-nav-bar title="消息通知" left-arrow @click-left="onClickLeft" />
    
    <van-list finished-text="没有更多了">
      <div v-for="i in 5" :key="i" class="notify-item">
        <div class="notify-icon">系统</div>
        <div class="notify-content">
          <div class="notify-title">系统通知 {{ i }}</div>
          <div class="notify-desc">您有新的消息，请注意查收...</div>
          <div class="notify-time">2026-01-2{{ i }}</div>
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
.notification-messages { min-height: 100vh; background: #f5f5f5; padding: 10px; }
.notify-item { display: flex; background: #fff; border-radius: 8px; padding: 12px; margin-bottom: 10px; }
.notify-icon { width: 40px; height: 40px; background: #1989fa; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-right: 12px; font-size: 12px; }
.notify-content { flex: 1; }
.notify-title { font-weight: bold; margin-bottom: 4px; }
.notify-desc { font-size: 14px; color: #666; margin-bottom: 4px; }
.notify-time { font-size: 12px; color: #999; }
</style>
