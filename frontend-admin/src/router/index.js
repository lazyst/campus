import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
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

export default router
