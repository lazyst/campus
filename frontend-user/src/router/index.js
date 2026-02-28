import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import { showLoginConfirm } from '@/stores/loginConfirm'

const authRoutes = ['ForumCreate', 'TradeCreate']

function isAuthenticated() {
  const token = localStorage.getItem('token')
  if (!token) return false

  const parts = token.split('.')
  return parts.length === 3
}

const routes = [
  // 主布局页面（显示底部导航和侧边栏）
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
        path: 'messages/:id',
        name: 'ChatDetail',
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
  if (authRoutes.includes(to.name) && !isAuthenticated()) {
    showLoginConfirm(() => {
      window.location.href = `/login?redirect=${encodeURIComponent(to.fullPath)}`
    })
    next(false)
    return
  }
  next()
})

export default router
