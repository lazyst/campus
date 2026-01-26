<template>
  <div class="notification-messages min-h-screen bg-gray-100 pt-2.5">
    <NavBar title="消息通知" :left-arrow="true" @click-left="onClickLeft">
      <template #right>
        <BaseButton type="primary" size="small" @click="onMarkAllRead">
          全部已读
        </BaseButton>
      </template>
    </NavBar>

    <div class="p-2.5">
      <!-- Loading State -->
      <div v-if="loading && notifications.length === 0" class="text-center py-8">
        <span class="text-gray-400">加载中...</span>
      </div>

      <!-- Empty State -->
      <div v-else-if="notifications.length === 0" class="text-center py-8">
        <span class="text-gray-400">暂无通知</span>
      </div>

      <!-- Notification List -->
      <div
        v-for="notif in notifications"
        :key="notif.id"
        class="notify-item flex bg-white rounded-lg p-3 mb-2.5 cursor-pointer"
        :class="{ 'bg-blue-50': !notif.read }"
        @click="onNotificationClick(notif)"
      >
        <div class="notify-icon w-10 h-10 bg-primary text-white rounded-full flex items-center justify-center mr-3 text-xs">
          {{ notif.type === 'system' ? '系统' : '通知' }}
        </div>
        <div class="notify-content flex-1">
          <div class="notify-title font-bold mb-1">{{ notif.title }}</div>
          <div class="notify-desc text-sm text-gray-500 mb-1">{{ notif.content }}</div>
          <div class="notify-time text-xs text-gray-400">{{ formatDate(notif.createdAt) }}</div>
        </div>
      </div>

      <!-- Loading More -->
      <div v-if="loading && notifications.length > 0" class="text-center py-4">
        <span class="text-gray-400">加载中...</span>
      </div>

      <!-- Finished -->
      <div v-if="finished && notifications.length > 0" class="text-center py-4">
        <span class="text-gray-400 text-sm">没有更多了</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markAsRead, markAllAsRead } from '@/api/modules'
import NavBar from '@/components/navigation/NavBar.vue'
import BaseButton from '@/components/base/Button.vue'

const router = useRouter()
const notifications = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

async function loadNotifications() {
  if (loading.value || finished.value) return

  try {
    loading.value = true
    const newNotifications = await getNotifications({
      page: page.value,
      size: 10
    })

    if (Array.isArray(newNotifications)) {
      notifications.value.push(...newNotifications)
      if (newNotifications.length < 10) {
        finished.value = true
      }
    } else if (newNotifications.records) {
      notifications.value.push(...newNotifications.records)
      if (notifications.value.length >= newNotifications.total) {
        finished.value = true
      }
    }

    page.value++
  } catch (error) {
    console.error('获取通知失败:', error)
  } finally {
    loading.value = false
  }
}

async function onNotificationClick(notification) {
  if (!notification.read) {
    try {
      await markAsRead(notification.id)
      notification.read = true
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  // 可选：跳转到相关页面
}

async function onMarkAllRead() {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.read = true)
  } catch (error) {
    console.error('全部标记已读失败:', error)
  }
}

function onClickLeft() {
  router.back()
}

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  loadNotifications()
})
</script>
