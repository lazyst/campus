<template>
  <div class="main-layout">
    <router-view />
    
    <!-- 浮动发布按钮 -->
    <van-floating-bubble 
      v-if="showFloatingButton"
      icon="plus" 
      @click="onCreate" 
    />
    
    <van-tabbar :model-value="activeTab" @update:model-value="onTabChange" v-show="showTabbar">
      <van-tabbar-item name="Forum" icon="comment-o">论坛</van-tabbar-item>
      <van-tabbar-item name="Trade" icon="shop-o">闲置</van-tabbar-item>
      <van-tabbar-item name="Messages" icon="chat-o">消息</van-tabbar-item>
      <van-tabbar-item name="MyProfile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 路由名称到Tab名称的映射
const routeToTabMap: Record<string, string> = {
  'Forum': 'Forum',
  'Trade': 'Trade', 
  'Messages': 'Messages',
  'MyProfile': 'MyProfile'
}

// 不显示tabbar的路由列表
const hiddenTabbarRoutes = ['Login', 'Register', 'Profile', 'ForumDetail', 'TradeDetail', 'TradeCreate', 'ChatDetail', 'ForumCreate', 'ProfileEdit', 'ProfilePosts', 'ProfileItems', 'ProfileCollections', 'ProfileNotifications']

// 根据当前路由计算激活的tab
const activeTab = computed(() => {
  const routeName = route.name as string || ''
  
  // 直接匹配
  if (routeToTabMap[routeName]) {
    return routeToTabMap[routeName]
  }
  
  // 根据路径前缀匹配
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
  
  return 'Forum' // 默认值
})

// 计算是否显示tabbar
const showTabbar = computed(() => {
  const routeName = route.name as string || ''
  return !hiddenTabbarRoutes.includes(routeName)
})

// 计算是否显示浮动按钮
const showFloatingButton = computed(() => {
  const routeName = route.name as string || ''
  const path = route.path
  
  // 只在论坛和闲置页面显示
  if (routeName === 'Forum' || path === '/' || path.startsWith('/?')) {
    return true
  }
  if (routeName === 'Trade' || path.startsWith('/trade')) {
    return true
  }
  return false
})

function onTabChange(name: string) {
  const routeMap: Record<string, string> = {
    'Forum': '/',
    'Trade': '/trade',
    'Messages': '/messages',
    'MyProfile': '/profile'
  }
  
  const targetPath = routeMap[name] || '/'
  
  // 只有当目标路径与当前路径不同时才导航
  if (route.path !== targetPath) {
    router.push(targetPath)
  }
}

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

<style scoped>
.main-layout {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 50px;
}

/* 覆盖Vant浮动按钮的内联样式，避免与底部导航栏重叠 */
:deep(.van-floating-bubble) {
  bottom: 70px !important;
  top: auto !important;
  transform: none !important;
  right: 16px !important;
  left: auto !important;
}
</style>
