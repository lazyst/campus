<template>
  <div
    class="main-layout"
    :class="{ 'main-layout--with-top-padding': showTopPadding }"
  >
    <!-- 桌面端侧边栏导航 -->
    <Sidebar
      v-show="showSidebar"
      :model-value="activeTab"
      :items="tabItems"
      :unread-count="totalUnreadCount"
      @change="onTabChange"
    />

    <div
      class="main-layout__content"
      :class="{ 'main-layout__content--with-sidebar': showSidebar }"
    >
      <router-view />
      
      <!-- PC端发布按钮 - 位于内容区域内部 -->
      <button
        v-if="showFloatingButton && showSidebar"
        class="fab-button-pc"
        @click="onCreate"
      >
        <span class="fab-button-pc__icon">+</span>
        <span class="fab-button-pc__text">发布</span>
      </button>
    </div>

    <!-- 移动端浮动发布按钮 -->
    <button
      v-if="showFloatingButton && !showSidebar"
      class="fab-button"
      @click="onCreate"
    >
      发布
    </button>

    <!-- 移动端底部TabBar -->
    <TabBar
      v-show="showTabbar"
      :model-value="activeTab"
      :items="tabItems"
      :unread-count="totalUnreadCount"
      @change="onTabChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, inject, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showLoginConfirm } from '@/stores/loginConfirm'
import TabBar from '../components/navigation/TabBar.vue'
import Sidebar from '../components/navigation/Sidebar.vue'

interface TabItem {
  name: string
  label: string
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式状态
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1024)

// 响应式断点
const breakpoint = 1024

// 是否显示桌面端侧边栏
const showSidebar = computed(() => windowWidth.value >= breakpoint)

// 从 App.vue 注入全局未读数
const totalUnreadCount = inject<ref<number>>('totalUnreadCount', ref(0))

// Tab bar items configuration
const tabItems: TabItem[] = [
  { name: 'Forum', label: '论坛' },
  { name: 'Trade', label: '闲置' },
  { name: 'Messages', label: '消息' },
  { name: 'MyProfile', label: '我的' }
]

// Route name to tab name mapping
const routeToTabMap: Record<string, string> = {
  'Forum': 'Forum',
  'Trade': 'Trade',
  'Messages': 'Messages',
  'MyProfile': 'MyProfile'
}

// Routes where tabbar should be hidden
const hiddenTabbarRoutes = [
  'Login',
  'Register',
  'Profile',
  'ForumDetail',
  'TradeDetail',
  'TradeCreate',
  'ChatDetail',
  'ForumCreate',
  'ProfileEdit',
  'ProfilePosts',
  'ProfileItems',
  'ProfileCollections',
  'ProfileNotifications'
]

// Compute active tab based on current route
const activeTab = computed(() => {
  const routeName = route.name as string || ''

  // Direct match
  if (routeToTabMap[routeName]) {
    return routeToTabMap[routeName]
  }

  // Match by path prefix
  const path = route.path
  if (path === '/' || path.startsWith('/?')) {
    return 'Forum'
  }
  if (path.startsWith('/trade')) {
    return 'Trade'
  }
  if (path.startsWith('/messages') && !path.includes('/chat')) {
    return 'Messages'
  }
  if (path === '/profile') {
    return 'MyProfile'
  }

  return 'Forum' // Default
})

// Compute whether to show tabbar
const showTabbar = computed(() => {
  const routeName = route.name as string || ''
  return !hiddenTabbarRoutes.includes(routeName)
})

// Compute whether to show top padding
// 一级页面（首页）没有自己的 NavBar，需要顶部 padding 为 MainLayout 的 NavBar 留出空间
const showTopPadding = computed(() => {
  const path = route.path
  // 匹配一级页面的路径模式
  if (path === '/' || path.startsWith('/?')) {
    return true // Forum (首页)
  }
  if (path === '/trade') {
    return true
  }
  if (path.startsWith('/messages') && !path.includes('/chat')) {
    return true // Messages (首页，不是 chat 详情)
  }
  if (path === '/profile') {
    return true
  }
  // 其他都是二级页，有自己的 NavBar，不需要顶部 padding
  return false
})

// Compute whether to show floating button
const showFloatingButton = computed(() => {
  const routeName = route.name as string || ''
  const path = route.path

  // Show only on forum and trade pages
  if (routeName === 'Forum' || path === '/' || path.startsWith('/?')) {
    return true
  }
  if (routeName === 'Trade' || path.startsWith('/trade')) {
    return true
  }
  return false
})

// Handle tab change
function onTabChange(name: string) {
  const routeMap: Record<string, string> = {
    'Forum': '/',
    'Trade': '/trade',
    'Messages': '/messages',
    'MyProfile': '/profile'
  }

  const targetPath = routeMap[name] || '/'

  // Only navigate if target path is different from current path
  if (route.path !== targetPath) {
    router.push(targetPath)
  }
}

// Handle create action
function onCreate() {
  // 检查是否已登录
  if (!userStore.isLoggedIn) {
    // 弹窗提示，让用户选择是否登录
    const currentPath = route.fullPath
    showLoginConfirm(() => {
      router.push({
        path: '/login',
        query: { redirect: currentPath }
      })
    })
    return
  }

  const routeName = route.name as string || ''
  const path = route.path

  if (routeName === 'Trade' || path.startsWith('/trade')) {
    router.push('/trade/create')
  } else {
    router.push('/forum/create')
  }
}

// 窗口大小变化处理
function handleResize() {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

.main-layout--with-top-padding {
  padding-top: var(--nav-height);
}

.main-layout__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  transition: padding-left var(--transition-normal);
}

.main-layout__content--with-sidebar {
  padding-left: var(--sidebar-width);
}

/* 移动端浮动发布按钮样式 */
.fab-button {
  position: fixed;
  right: var(--space-4);
  bottom: calc(var(--tabbar-height) + var(--space-6));
  min-width: 56px;
  height: 56px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-inverse);
  background-color: var(--color-primary-700);
  border: none;
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
  z-index: var(--z-fixed);
}

.fab-button:active {
  transform: scale(0.95);
  background-color: var(--color-primary-800);
  box-shadow: var(--shadow-md);
}

/* PC端发布按钮 - 位于内容区域右下角，随滚动移动 */
.fab-button-pc {
  position: sticky;
  float: right;
  margin: var(--space-6);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  min-width: 100px;
  height: 44px;
  padding: 0 var(--space-5);
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-inverse);
  background: linear-gradient(135deg, var(--color-primary-600) 0%, var(--color-primary-700) 100%);
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.3);
  cursor: pointer;
  transition: all var(--transition-normal);
  z-index: var(--z-fixed);
}

.fab-button-pc:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.4);
}

.fab-button-pc:active {
  transform: translateY(0);
}

.fab-button-pc__icon {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-bold);
  line-height: 1;
}

.fab-button-pc__text {
  letter-spacing: 0.02em;
}
</style>
