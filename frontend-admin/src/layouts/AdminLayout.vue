<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="logo">
        <div class="logo-icon">校</div>
        <span class="logo-text">校园互助</span>
      </div>

      <!-- Element Plus Menu -->
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        background-color="#FFFFFF"
        text-color="#6B7280"
        active-text-color="#1E3A8A"
        router
      >
        <el-menu-item index="/">
          <span>数据概览</span>
        </el-menu-item>

        <el-sub-menu index="content">
          <template #title>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/boards">板块管理</el-menu-item>
          <el-menu-item index="/posts">帖子管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/items">
          <span>闲置管理</span>
        </el-menu-item>

        <el-menu-item index="/users">
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/storage">
          <span>文件管理</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout">
          <span>退出登录</span>
        </button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-wrapper">
      <header class="header">
        <div class="header-title">
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="header-user">
          <div class="user-avatar">管</div>
          <span class="user-name">管理员</span>
        </div>
      </header>

      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logout } from '@/api/admin/auth'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/': '数据概览',
    '/users': '用户管理',
    '/boards': '板块管理',
    '/posts': '帖子管理',
    '/items': '闲置管理',
    '/storage': '文件管理'
  }
  return titles[route.path] || '管理后台'
})

const handleLogout = async () => {
  try {
    await logout()
  } catch (e) {
    // 忽略API调用错误
  }
  localStorage.removeItem('admin_token')
  router.push('/login')
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  background: #F5F7FA;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

// 侧边栏
.sidebar {
  width: 240px;
  height: 100%;
  background: #FFFFFF;
  border-right: 1px solid #E5E7EB;
  display: flex;
  flex-direction: column;
  padding: 24px 0;
  position: relative;
  z-index: 1;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 24px;
  margin-bottom: 24px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1E3A8A;
  border-radius: 8px;
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  letter-spacing: 1px;
}

// Element Plus Menu 样式覆盖
.admin-menu {
  flex: 1;
  border-right: none;

  // 菜单项
  :deep(.el-menu-item) {
    height: 44px;
    line-height: 44px;
    margin: 4px 12px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;

    &:hover {
      background-color: #F3F4F6 !important;
      color: #1E3A8A !important;
    }

    &.is-active {
      background-color: #EEF2FF !important;
      color: #1E3A8A !important;
      font-weight: 600;
    }
  }

  // 子菜单标题
  :deep(.el-sub-menu__title) {
    height: 44px;
    line-height: 44px;
    margin: 4px 12px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;

    &:hover {
      background-color: #F3F4F6 !important;
      color: #1E3A8A !important;
    }
  }

  // 子菜单展开时的标题
  :deep(.el-sub-menu.is-active .el-sub-menu__title) {
    color: #1E3A8A !important;
    font-weight: 600;
  }

  // 子菜单项
  :deep(.el-sub-menu .el-menu-item) {
    height: 40px;
    line-height: 40px;
    margin: 2px 12px 2px 24px;
    font-size: 13px;

    &.is-active {
      background-color: #EEF2FF !important;
    }
  }

  // 移除默认的左边框
  :deep(.el-menu-item.is-active) {
    border-left: none;
  }
}

// 侧边栏底部
.sidebar-footer {
  padding: 16px 24px 0;
  border-top: 1px solid #E5E7EB;
  margin-top: auto;
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  color: #6B7280;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #FEE2E2;
    border-color: #FECACA;
    color: #DC2626;
  }
}

// 主内容区
.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
  overflow: hidden;
}

.header {
  height: 64px;
  padding: 0 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #E5E7EB;
  background: #FFFFFF;
}

.header-title h1 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 24px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: #1E3A8A;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.main-content {
  flex: 1;
  padding: 24px 32px;
  overflow-y: auto;
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

// 响应式
@media (max-width: 768px) {
  .sidebar {
    width: 72px;
    padding: 16px 0;
  }

  .logo {
    padding: 0 8px;
    justify-content: center;
  }

  .logo-text {
    display: none;
  }

  .logout-btn span {
    display: none;
  }

  // Element Plus Menu 响应式
  :deep(.el-menu-item span),
  :deep(.el-sub-menu__title span) {
    display: none;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    justify-content: center;
    padding: 0 !important;
  }

  :deep(.el-sub-menu .el-menu-item) {
    display: none;
  }
}
</style>
