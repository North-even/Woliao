import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import Login from '@/views/Login.vue'
import Messages from '@/views/Messages.vue'
import Contacts from '@/views/Contacts.vue'
import Register from '@/views/Register.vue'
import Accounts from '@/views/Accounts.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/messages'
      },
      {
        path: 'messages',
        name: 'Messages',
        component: Messages,
        meta: { requiresAuth: true }
      },
      {
        path: 'contacts',
        name: 'Contacts',
        component: Contacts,
        meta: { requiresAuth: true }
      },
      {
        path: 'accounts',
        name: 'Accounts',
        component: Accounts,
        meta: { requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router 