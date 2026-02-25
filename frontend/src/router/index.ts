import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
  type Router,
} from 'vue-router'
import type { App } from 'vue'
import { useUserStore } from '@/stores/user'

// 路由配置
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    redirect: '/dashboard',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
    },
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: {
      title: '仪表板',
      requiresAuth: true,
    },
  },
  {
    path: '/system/dept',
    name: 'Dept',
    component: () => import('@/views/system/dept/index.vue'),
    meta: {
      title: '部门管理',
      requiresAuth: true,
    },
  },
  {
    path: '/system/post',
    name: 'Post',
    component: () => import('@/views/system/post/index.vue'),
    meta: {
      title: '岗位管理',
      requiresAuth: true,
    },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '404',
    },
  },
]

// 创建路由实例
const router: Router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 }),
})

// 白名单路由（不需要登录）
const whiteList = ['/login', '/404']

// 设置路由守卫
function setupRouterGuard(router: Router) {
  router.beforeEach((to, _from, next) => {
    // 获取用户 store（必须在路由守卫内部获取，确保 Pinia 已初始化）
    const userStore = useUserStore()
    const token = userStore.token
    const requiresAuth = to.meta.requiresAuth !== false

    // 设置页面标题
    document.title = `${to.meta.title ?? 'AIPerm'} - 权限管理系统`

    if (token) {
      // 已登录
      if (to.path === '/login') {
        // 登录页跳转到首页
        next('/dashboard')
      }
      else {
        next()
      }
    }
    else {
      // 未登录
      if (whiteList.includes(to.path) || !requiresAuth) {
        // 白名单或不需要认证的页面
        next()
      }
      else {
        // 跳转到登录页
        next(`/login?redirect=${to.path}`)
      }
    }
  })

  router.afterEach(() => {
    // 路由切换后可以做一些处理，如关闭 loading
  })
}

// 初始化路由
export function setupRouter(app: App) {
  setupRouterGuard(router)
  app.use(router)
}

export default router
