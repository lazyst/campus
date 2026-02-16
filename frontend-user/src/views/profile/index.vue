<template>
  <div class="profile-page">
    <!-- PC端分栏布局 -->
    <div class="profile-header">
      <ResponsiveContainer>
        <div class="profile-user-info">
          <div class="profile-avatar">
            <img v-if="userStore.userInfo?.avatar" :src="getImageUrl(userStore.userInfo.avatar)" alt="头像" />
            <span v-else>{{ userName.charAt(0) }}</span>
          </div>
          <h2 class="profile-name">{{ userName }}</h2>
          <p class="profile-phone">{{ userPhone }}</p>
          
          <!-- 未登录时显示登录按钮 -->
          <div v-if="!userStore.token" class="profile-login-btn" @click="goToLogin">
            去登录
          </div>
        </div>
      </ResponsiveContainer>
    </div>

    <div class="profile-content">
      <!-- 左侧用户信息 -->
      <div class="profile-sidebar">
        <div class="profile-user-info">
          <div class="profile-avatar">
            <img v-if="userStore.userInfo?.avatar" :src="getImageUrl(userStore.userInfo.avatar)" alt="头像" />
            <span v-else>{{ userName.charAt(0) }}</span>
          </div>
          <h2 class="profile-name">{{ userName }}</h2>
          <p class="profile-phone">{{ userPhone }}</p>
          
          <!-- 未登录时显示登录按钮 -->
          <div v-if="!userStore.token" class="profile-login-btn" @click="goToLogin">
            去登录
          </div>
        </div>
      </div>

      <!-- 右侧菜单 -->
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
            <span v-if="item.route === '/profile/messages' && unreadNotificationCount > 0" class="profile-menu-badge">
              {{ unreadNotificationCount > 99 ? '99+' : unreadNotificationCount }}
            </span>
            <span class="profile-menu-arrow">›</span>
          </div>
        </div>

        <!-- 退出登录按钮 -->
        <div v-if="userStore.token" class="profile-logout" @click="handleLogout">
          退出登录
        </div>

        <!-- 注销账号按钮 -->
        <div v-if="userStore.token" class="profile-deactivate" @click="handleDeactivate">
          注销账号
        </div>
      </div>
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

    <!-- 注销账号确认弹窗 -->
    <Dialog
      :visible="deactivateDialogVisible"
      title="注销账号"
      message="注销后将清除所有数据，且该手机号需30天后才可重新注册。确定要注销吗？"
      confirmText="确定注销"
      cancelText="取消"
      @confirm="confirmDeactivate"
      @update:visible="deactivateDialogVisible = $event"
    />
  </div>
</template>

<script setup>
import { computed, ref, onMounted, inject, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import Dialog from '@/components/interactive/Dialog.vue';
import ResponsiveContainer from '@/components/layout/ResponsiveContainer.vue';
import { getImageUrl } from '@/utils/imageUrl';
import { getUnreadCount } from '@/api/modules/notification';
import { deactivateAccount } from '@/api/modules/user';

const router = useRouter();
const userStore = useUserStore();

// 注入通知更新事件
const notificationUpdateEvent = inject('notificationUpdateEvent');
const totalNotificationUnreadCount = inject('totalNotificationUnreadCount');

const unreadNotificationCount = ref(0);

// 监听通知更新事件，实时增加未读数
watch(() => notificationUpdateEvent.value, () => {
  unreadNotificationCount.value = (unreadNotificationCount.value || 0) + 1;
}, { immediate: false });

// 监听全局未读通知数变化（从 App.vue 提供）
watch(() => totalNotificationUnreadCount.value, (newVal) => {
  unreadNotificationCount.value = newVal || 0;
}, { immediate: true });

// 获取未读通知数量
async function fetchUnreadCount() {
  if (!userStore.token) return;
  try {
    const res = await getUnreadCount();
    unreadNotificationCount.value = res.count || 0;
  } catch (error) {
    console.error('获取未读通知数失败:', error);
  }
}

// 监听 userStore 变化，当从其他页面跳转过来时更新状态
watch(() => userStore.token, async (newToken) => {
  if (newToken && !userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  await fetchUnreadCount()
}, { immediate: true });

// 确保userStore已初始化
onMounted(async () => {
  if (!userStore.isInitialized) {
    await userStore.initialize();
  }
  await fetchUnreadCount();
})

const userName = computed(() => userStore.userInfo?.nickname || '校园小助手');
const showFullPhone = ref(false);
const userPhone = computed(() => {
  if (userStore.userInfo?.phone) {
    if (showFullPhone.value) {
      return userStore.userInfo.phone;
    }
    return userStore.userInfo.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
  }
  return '未登录';
});

function togglePhone() {
  if (userStore.userInfo?.phone) {
    showFullPhone.value = !showFullPhone.value;
  }
}

const dialogVisible = ref(false);
const deactivateDialogVisible = ref(false);

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
];

function handleMenuClick(route) {
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

function handleDeactivate() {
  deactivateDialogVisible.value = true;
}

async function confirmDeactivate() {
  deactivateDialogVisible.value = false;
  try {
    await deactivateAccount();
    userStore.logout();
  } catch (error) {
    console.error('注销账号失败:', error);
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

/* 移动端样式 - 默认 */
.profile-header {
  display: block;
}

.profile-content {
  display: block;
}

.profile-sidebar {
  display: none;
}

/* PC端样式增强 - 分栏布局 */
@media (min-width: 1024px) {
    .profile-page {
      background: #F1F5F9;
      min-height: 100vh;
    }

    .profile-header {
      display: none;
    }

    .profile-sidebar {
      display: block;
    }

    /* 分栏容器 */
    .profile-content {
      display: flex;
      justify-content: center;
      align-items: flex-start;
      width: 100%;
      max-width: 1400px;
      margin: 0 auto;
      padding: var(--space-12) var(--space-16);
      gap: var(--space-10);
      box-sizing: border-box;
    }

    /* 左侧用户信息 */
    .profile-sidebar {
      width: 380px;
      flex-shrink: 0;
    }

    .profile-sidebar .profile-user-info {
      background: #FFFFFF;
      border-radius: var(--radius-2xl);
      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
      padding: var(--space-12);
      text-align: center;
      min-height: 320px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
    }

    .profile-avatar {
      width: 120px;
      height: 120px;
      font-size: var(--text-4xl);
      margin: 0 auto var(--space-6);
    }

  .profile-name {
    font-size: var(--text-3xl);
    margin-bottom: var(--space-3);
  }

  .profile-phone {
    font-size: var(--text-lg);
    margin-bottom: var(--space-6);
  }

  .profile-login-btn {
    width: 100%;
    max-width: 200px;
    padding: var(--space-4) var(--space-8);
    font-size: var(--text-lg);
  }

  /* 右侧菜单 */
  .profile-menu {
    flex: 1;
    padding: 0;
    max-width: 600px;
    width: 100%;
  }

  .profile-menu-group {
    border-radius: var(--radius-xl);
    margin-bottom: var(--space-5);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
    border: 1px solid rgba(0, 0, 0, 0.04);
    overflow: hidden;
    background: #FFFFFF;
  }

  .profile-menu-title {
    padding: var(--space-4) var(--space-5);
    font-size: var(--text-sm);
    background: #F8FAFC;
    border-bottom: 1px solid var(--color-gray-100);
  }

  .profile-menu-item {
    padding: var(--space-5);
    position: relative;
  }

  .profile-menu-item::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: var(--space-5);
    right: var(--space-5);
    height: 1px;
    background: var(--color-gray-100);
  }

  .profile-menu-item:last-child::after {
    display: none;
  }

  .profile-menu-item:hover {
    background: linear-gradient(90deg, rgba(37, 99, 235, 0.03) 0%, transparent 100%);
  }

  .profile-menu-label {
    font-size: var(--text-base);
  }

  .profile-logout {
    max-width: none;
    margin: var(--space-5) 0 0;
    padding: var(--space-4) var(--space-5);
    border-radius: var(--radius-xl);
    background: #FFFFFF;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
    border: 1px solid rgba(0, 0, 0, 0.04);
    text-align: center;
    cursor: pointer;
    transition: all var(--transition-fast);
  }

  .profile-logout:hover {
    background: #FEF2F2;
    color: var(--color-error-600);
    border-color: var(--color-error-200);
  }

  .profile-deactivate {
    max-width: none;
    margin: var(--space-3) 0 0;
    padding: var(--space-4) var(--space-5);
    border-radius: var(--radius-xl);
    background: #FFFFFF;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
    border: 1px solid rgba(0, 0, 0, 0.04);
    text-align: center;
    cursor: pointer;
    transition: all var(--transition-fast);
  }

  .profile-deactivate:hover {
    background: #FEF2F2;
    color: var(--color-error-600);
    border-color: var(--color-error-200);
  }

  /* 禁用状态 */
  .profile-menu--disabled {
    opacity: 0.6;
  }
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

.profile-phone-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.profile-phone {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
}

.profile-phone-toggle {
  cursor: pointer;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  background-color: var(--bg-secondary);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  user-select: none;
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

.profile-menu-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background-color: #ef4444;
  border-radius: 10px;
  margin-right: 8px;
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

/* 注销账号按钮 */
.profile-deactivate {
  margin-top: var(--space-3);
  padding: var(--space-4);
  text-align: center;
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-tertiary);
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.profile-deactivate:active {
  background-color: var(--bg-secondary);
  transform: scale(0.98);
}
</style>
