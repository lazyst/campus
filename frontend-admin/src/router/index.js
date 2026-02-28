import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

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
      },
      {
        path: 'storage',
        name: 'Storage',
        component: () => import('@/views/storage/index.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory('/admin'),
  routes
})

router.beforeEach((to, from, next) => {
  const adminStore = useAdminStore()
  const isAuthenticated = adminStore.token && !adminStore.isTokenExpired()

  if (to.name === 'Login' && isAuthenticated) {
    next({ name: 'Dashboard', replace: true })
    return
  }

  if (to.meta.requiresAuth && !isAuthenticated) {
    adminStore.logout()
    next({ name: 'Login', replace: true })
    return
  }

  next()
})

export default router
