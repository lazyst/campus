import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import { showLoginConfirm } from '@/stores/loginConfirm'

// 需要登录的路由
const authRoutes = ['ForumCreate', 'TradeCreate']

// 检查是否已登录（通过 localStorage）
// 注意：这里不使用 Pinia store，因为在路由守卫中 store 可能未初始化
function isAuthenticated() {
  const token = localStorage.getItem('token')
  if (!token) return false

  // 简单检查 token 格式（JWT 格式：header.payload.signature）
  const parts = token.split('.')
  if (parts.length !== 3) return false

  return true
}

// 校验 redirect 参数，防止开放重定向
function validateRedirect(path) {
  // 只有以 / 开头的相对路径才允许跳转
  if (path && path.startsWith('/') && !path.startsWith('//')) {
    return path
  }
  return '/'
}

const routes = [
  // 主布局页面（显示底部导航）
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Forum',
        component: () => import('@/views/home/Forum.vue')
      },
      {
        path: 'trade',
        name: 'Trade',
        component: () => import('@/views/home/Trade.vue')
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/messages/index.vue')
      },
      {
        path: 'profile',
        name: 'MyProfile',
        component: () => import('@/views/profile/index.vue')
      }
    ]
  },

  // 独立页面（不显示底部导航）
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue')
  },

  // 详情页面（不显示底部导航）
  {
    path: '/forum/detail/:id',
    name: 'ForumDetail',
    component: () => import('@/views/forum/detail/index.vue')
  },
  {
    path: '/forum/create',
    name: 'ForumCreate',
    component: () => import('@/views/forum/create/index.vue')
  },
  {
    path: '/trade/:id',
    name: 'TradeDetail',
    component: () => import('@/views/trade/detail/index.vue')
  },
  {
    path: '/trade/create',
    name: 'TradeCreate',
    component: () => import('@/views/trade/create/index.vue')
  },
  {
    path: '/messages/:id',
    name: 'ChatDetail',
    component: () => import('@/views/messages/chat/index.vue')
  },

  // Profile子页面（不显示底部导航）
  {
    path: '/profile/edit',
    name: 'ProfileEdit',
    component: () => import('@/views/profile/edit/index.vue')
  },
  {
    path: '/profile/posts',
    name: 'ProfilePosts',
    component: () => import('@/views/profile/posts/index.vue')
  },
  {
    path: '/profile/items',
    name: 'ProfileItems',
    component: () => import('@/views/profile/items/index.vue')
  },
  {
    path: '/profile/collections',
    name: 'ProfileCollections',
    component: () => import('@/views/profile/collections/index.vue')
  },
  {
    path: '/profile/messages',
    name: 'ProfileNotifications',
    component: () => import('@/views/profile/messages/index.vue')
  },

  // 用户主页
  {
    path: '/profile/user/:id',
    name: 'UserProfile',
    component: () => import('@/views/profile/user/index.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查需要登录的路由
router.beforeEach((to, from, next) => {
  if (authRoutes.includes(to.name)) {
    if (!isAuthenticated()) {
      // 未登录，弹窗提示，让用户选择是否登录
      showLoginConfirm(() => {
        // 用户选择登录后，使用 next({}) 方式导航到登录页
        window.location.href = `/login?redirect=${encodeURIComponent(to.fullPath)}`
      })
      // 取消当前导航
      next(false)
      return
    }
  }
  next()
})

export default router
