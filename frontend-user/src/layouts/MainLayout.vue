<template>
  <div class="min-h-screen bg-gray-50 pb-14" :class="{ 'pt-11': showTopPadding }">
    <router-view />
    
    <!-- Floating Action Button -->
    <div 
      v-if="showFloatingButton"
      class="fixed right-4 bg-primary text-white rounded-full w-12 h-12 flex items-center justify-center shadow-lg cursor-pointer z-40"
      style="bottom: 70px;"
      @click="onCreate"
    >
      <span class="text-2xl font-medium">+</span>
    </div>
    
    <!-- Custom TabBar -->
    <TabBar
      :model-value="activeTab"
      :items="tabItems"
      @change="onTabChange"
      v-show="showTabbar"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import TabBar from '../components/navigation/TabBar.vue'

interface TabItem {
  name: string
  label: string
}

const router = useRouter()
const route = useRoute()

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
  const routeName = route.name as string || ''
  const path = route.path
  
  if (routeName === 'Trade' || path.startsWith('/trade')) {
    router.push('/trade/create')
  } else {
    router.push('/forum/create')
  }
}
</script>
