<template>
  <div class="profile-page">
    <!-- 用户信息头部 -->
    <div class="profile-header">
      <div class="profile-user-info">
        <div class="profile-avatar">
          <img v-if="userStore.userInfo?.avatar" :src="getImageUrl(userStore.userInfo.avatar)" alt="头像" />
          <span v-else>{{ userName.charAt(0) }}</span>
        </div>
        <div class="profile-user-details">
          <h2 class="profile-name">{{ userName }}</h2>
          <p class="profile-phone">{{ userPhone }}</p>
        </div>
        
        <!-- 未登录时显示登录按钮 -->
        <div v-if="!userStore.token" class="profile-login-btn" @click="goToLogin">
          去登录
        </div>
      </div>
    </div>

    <!-- 菜单列表 -->
    <div class="profile-menu" :class="{ 'profile-menu--disabled': !userStore.token }">
      <div class="profile-menu-group">
        <div class="profile-menu-title">账号管理</div>
        <div 
          v-for="(item, index) in mainMenuItems" 
          :key="index"
          class="profile-menu-item"
          @click="handleMenuClick(item.route)"
        >
          <span class="profile-menu-label">{{ item.label }}</span>
          <span class="profile-menu-arrow">›</span>
        </div>
      </div>

      <div class="profile-menu-group">
        <div class="profile-menu-title">我的内容</div>
        <div 
          v-for="(item, index) in contentMenuItems" 
          :key="index"
          class="profile-menu-item"
          @click="handleMenuClick(item.route)"
        >
          <span class="profile-menu-label">{{ item.label }}</span>
          <span class="profile-menu-arrow">›</span>
        </div>
      </div>

      <div class="profile-menu-group">
        <div class="profile-menu-title">其他</div>
        <div 
          v-for="(item, index) in otherMenuItems" 
          :key="index"
          class="profile-menu-item"
          @click="handleMenuClick(item.route)"
        >
          <span class="profile-menu-label">{{ item.label }}</span>
          <span class="profile-menu-arrow">›</span>
        </div>
      </div>

      <!-- 退出登录按钮 -->
      <div v-if="userStore.token" class="profile-logout" @click="handleLogout">
        退出登录
      </div>

      <!-- 退出确认弹窗 -->
      <Dialog
        :visible="dialogVisible"
        title="退出登录"
        message="确定要退出登录吗？"
        confirmText="退出"
        cancelText="取消"
        @confirm="confirmLogout"
        @update:visible="dialogVisible = $event"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import Dialog from '@/components/interactive/Dialog.vue';
import { getImageUrl } from '@/utils/imageUrl';

const router = useRouter();
const userStore = useUserStore();

// 确保userStore已初始化
onMounted(async () => {
  if (!userStore.isInitialized) {
    await userStore.initialize();
  }
})

const userName = computed(() => userStore.userInfo?.nickname || '校园小助手');
const userPhone = computed(() => {
  if (userStore.userInfo?.phone) {
    return userStore.userInfo.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
  }
  return '未登录';
});

const dialogVisible = ref(false);

const mainMenuItems = [
  { label: '编辑资料', route: '/profile/edit' },
];

const contentMenuItems = [
  { label: '我的帖子', route: '/profile/posts' },
  { label: '我的闲置', route: '/profile/items' },
  { label: '我的收藏', route: '/profile/collections' },
];

const otherMenuItems = [
  { label: '消息通知', route: '/profile/messages' },
  { label: '设置', route: '/settings' },
];

function handleMenuClick(route: string) {
  if (!userStore.token) {
    router.push('/login');
    return;
  }
  router.push(route);
}

function goToLogin() {
  router.push('/login');
}

function handleLogout() {
  dialogVisible.value = true;
}

function confirmLogout() {
  dialogVisible.value = false;
  userStore.logout();
}

function cancelLogout() {
  dialogVisible.value = false;
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

.profile-header {
  padding: var(--space-6) var(--space-4) var(--space-4);
  background: linear-gradient(180deg, var(--color-primary-50) 0%, var(--bg-card) 100%);
}

.profile-user-info {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-full);
  background-color: var(--color-primary-100);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-700);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.profile-name {
  font-size: var(--text-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0;
}

.profile-phone {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
}

.profile-login-btn {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-primary-700);
  background-color: var(--color-primary-50);
  border: 1px solid var(--color-primary-200);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.profile-login-btn:active {
  background-color: var(--color-primary-100);
  transform: scale(0.98);
}

.profile-menu {
  padding: var(--space-4);
}

.profile-menu--disabled {
  opacity: 0.5;
  pointer-events: none;
}

.profile-menu-group {
  margin-bottom: var(--space-4);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

.profile-menu-title {
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-xs);
  font-weight: var(--font-weight-medium);
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background-color: var(--bg-secondary);
  border-bottom: 1px solid var(--border-light);
}

.profile-menu-item {
  display: flex;
  align-items: center;
  padding: var(--space-4);
  font-size: var(--text-base);
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.profile-menu-item:last-child {
  border-bottom: none;
}

.profile-menu-item:active {
  background-color: var(--bg-secondary);
}

.profile-menu-label {
  flex: 1;
}

.profile-menu-arrow {
  font-size: var(--text-lg);
  color: var(--text-tertiary);
}

/* 退出登录按钮 */
.profile-logout {
  margin-top: var(--space-6);
  padding: var(--space-4);
  text-align: center;
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-error-500);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-error-200);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.profile-logout:active {
  background-color: var(--color-error-50);
  transform: scale(0.98);
}
</style>
