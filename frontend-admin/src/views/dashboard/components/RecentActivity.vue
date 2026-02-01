<template>
  <el-card shadow="hover" class="recent-activity-card">
    <template #header>
      <div class="activity-header">
        <span>最近活跃</span>
        <el-tabs v-model="activeTab" class="activity-tabs">
          <el-tab-pane label="帖子" name="posts" />
          <el-tab-pane label="用户" name="users" />
        </el-tabs>
      </div>
    </template>
    <div class="activity-content">
      <el-timeline v-if="activeTab === 'posts'" class="activity-timeline">
        <el-timeline-item
          v-for="post in posts"
          :key="post.id"
          placement="top"
          :timestamp="post.createdAt"
          :color="post.color || '#1E3A8A'"
        >
          <div class="timeline-content">
            <span class="timeline-title">{{ post.title }}</span>
            <span class="timeline-id">ID: {{ post.authorId }}</span>
          </div>
        </el-timeline-item>
        <el-timeline-item v-if="!posts.length" placement="top">
          <div class="empty-text">暂无帖子</div>
        </el-timeline-item>
      </el-timeline>

      <el-timeline v-else class="activity-timeline">
        <el-timeline-item
          v-for="user in users"
          :key="user.id"
          placement="top"
          :timestamp="user.createdAt"
          color="#10B981"
        >
          <div class="timeline-content user-content">
            <el-avatar :size="28" :src="user.avatar" class="user-avatar">
              {{ user.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="timeline-title">{{ user.nickname || '匿名用户' }}</span>
          </div>
        </el-timeline-item>
        <el-timeline-item v-if="!users.length" placement="top">
          <div class="empty-text">暂无用户</div>
        </el-timeline-item>
      </el-timeline>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface PostItem {
  id: number
  title: string
  authorId: number
  createdAt: string
}

interface UserItem {
  id: number
  nickname: string | null
  avatar: string | null
  createdAt: string
}

interface Props {
  data: {
    posts: PostItem[]
    users: UserItem[]
  }
}

const props = defineProps<Props>()

const activeTab = ref('posts')

const posts = computed(() => props.data.posts || [])
const users = computed(() => props.data.users || [])
</script>

<style scoped lang="scss">
.recent-activity-card {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  transition: all 0.3s;
  height: 100%;

  :deep(.el-card__header) {
    padding: 12px 20px;
    border-bottom: 1px solid #F3F4F6;
    background: #FAFAFA;
  }

  :deep(.el-card__body) {
    padding: 16px 20px;
    max-height: 360px;
    overflow-y: auto;
  }

  &:hover {
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  }
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 500;
  color: #374151;

  :deep(.el-tabs) {
    --el-tabs-header-height: 28px;
  }

  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    font-size: 13px;
    padding: 0 12px;
    height: 28px;
    line-height: 28px;
  }

  :deep(.el-tabs__active-bar) {
    height: 2px;
  }
}

.activity-content {
  padding: 4px 0;
}

.activity-timeline {
  padding-left: 0;

  :deep(.el-timeline-item__timestamp) {
    font-size: 11px;
    color: #9CA3AF;
  }

  :deep(.el-timeline-item__node) {
    width: 8px;
    height: 8px;
  }
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.timeline-title {
  font-size: 13px;
  color: #374151;
  line-height: 1.4;
}

.timeline-id {
  font-size: 11px;
  color: #9CA3AF;
}

.user-content {
  flex-direction: row;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background: #1E3A8A;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
}

.empty-text {
  font-size: 13px;
  color: #9CA3AF;
  text-align: center;
  padding: 20px 0;
}
</style>
