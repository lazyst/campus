<template>
  <div class="profile-messages-page">
    <NavBar title="消息通知" :left-arrow="true" @click-left="onClickLeft">
      <template #right>
        <button class="profile-messages__mark-all" @click="onMarkAllRead">
          全部已读
        </button>
      </template>
    </NavBar>

    <div class="profile-messages-container">
      <!-- Loading State -->
      <div v-if="loading && notifications.length === 0" class="profile-messages__state">
        <span class="profile-messages__state-text">加载中...</span>
      </div>

      <!-- Empty State -->
      <div v-else-if="notifications.length === 0" class="profile-messages__state">
        <span class="profile-messages__state-text">暂无通知</span>
      </div>

      <!-- Notification List -->
      <div v-else class="profile-messages__list">
        <div
          v-for="notif in notifications"
          :key="notif.id"
          class="profile-messages__item"
          :class="{ 'profile-messages__item--unread': !notif.read }"
          @click="onNotificationClick(notif)"
        >
          <div class="profile-messages__item-icon">
            {{ notif.type === 'system' ? '系' : '通' }}
          </div>
          <div class="profile-messages__item-content">
            <div class="profile-messages__item-title">{{ notif.title }}</div>
            <div class="profile-messages__item-desc">{{ notif.content }}</div>
            <div class="profile-messages__item-time">{{ formatDate(notif.createdAt) }}</div>
          </div>
          <div v-if="!notif.read" class="profile-messages__unread-dot"></div>
        </div>

        <!-- Loading More -->
        <div v-if="loading && notifications.length > 0" class="profile-messages__state">
          <span class="profile-messages__state-text">加载中...</span>
        </div>

        <!-- Finished -->
        <div v-if="finished && notifications.length > 0" class="profile-messages__state">
          <span class="profile-messages__state-text profile-messages__state-text--muted">没有更多了</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markAsRead, markAllAsRead } from '@/api/modules'
import NavBar from '@/components/navigation/NavBar.vue'

interface Notification {
  id: number
  title: string
  content: string
  type: string
  read: boolean
  createdAt: string
}

const router = useRouter()
const notifications = ref<Notification[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

async function loadNotifications() {
  if (loading.value || finished.value) return

  try {
    loading.value = true
    const response = await getNotifications({
      page: page.value,
      size: 10
    })

    let newNotifications: Notification[] = []
    if (Array.isArray(response)) {
      newNotifications = response as Notification[]
      if (newNotifications.length < 10) {
        finished.value = true
      }
    } else if ((response as { records?: Notification[] }).records) {
      newNotifications = (response as { records: Notification[], total: number }).records
      if (notifications.value.length >= (response as { total: number }).total) {
        finished.value = true
      }
    }

    notifications.value.push(...newNotifications)
    page.value++
  } catch (error) {
    console.error('获取通知失败:', error)
  } finally {
    loading.value = false
  }
}

async function onNotificationClick(notification: Notification) {
  if (!notification.read) {
    try {
      await markAsRead(notification.id)
      notification.read = true
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
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

function formatDate(dateString: string): string {
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

<style scoped>
.profile-messages-page {
  min-height: 100vh;
  background-color: var(--bg-page);
}

.profile-messages-container {
  padding: var(--page-padding);
}

.profile-messages__mark-all {
  font-size: var(--text-sm);
  color: var(--color-primary-700);
  background: none;
  border: none;
  cursor: pointer;
  padding: var(--space-1) var(--space-2);
}

.profile-messages__mark-all:active {
  opacity: 0.7;
}

.profile-messages__state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) 0;
}

.profile-messages__state-text {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.profile-messages__state-text--muted {
  font-size: var(--text-xs);
}

.profile-messages__list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.profile-messages__item {
  display: flex;
  align-items: flex-start;
  padding: var(--space-4);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
}

.profile-messages__item--unread {
  background-color: var(--color-primary-50);
}

.profile-messages__item:active {
  transform: scale(0.98);
}

.profile-messages__item-icon {
  width: 40px;
  height: 40px;
  background-color: var(--color-primary-700);
  color: #FFFFFF;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  margin-right: var(--space-3);
  flex-shrink: 0;
}

.profile-messages__item-content {
  flex: 1;
  min-width: 0;
}

.profile-messages__item-title {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.profile-messages__item-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-1);
  line-height: var(--line-height-normal);
}

.profile-messages__item-time {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.profile-messages__unread-dot {
  width: 8px;
  height: 8px;
  background-color: var(--color-error-500);
  border-radius: var(--radius-full);
  flex-shrink: 0;
  margin-left: var(--space-2);
}
</style>
