import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue')
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/users/index.vue')
      },
      {
        path: 'boards',
        name: 'Boards',
        component: () => import('@/views/boards/index.vue')
      },
      {
        path: 'posts',
        name: 'Posts',
        component: () => import('@/views/posts/index.vue')
      },
      {
        path: 'items',
        name: 'Items',
        component: () => import('@/views/items/index.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')

  // 如果路由需要登录但没有token，跳转到登录页
  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', replace: true })
    return
  }

  // 如果已登录但访问登录页，跳转到首页
  if (to.name === 'Login' && token) {
    next({ name: 'Dashboard', replace: true })
    return
  }

  next()
})

export default router
