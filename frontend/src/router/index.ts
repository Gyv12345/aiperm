import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
  type Router,
} from 'vue-router'
import type { App } from 'vue'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'

// 静态路由（不需要权限）
export const constantRoutes: RouteRecordRaw[] = [
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
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '404',
    },
  },
  {
    path: '/',
    name: 'Root',
    component: () => import('@/components/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '首页',
          icon: 'HomeFilled',
          requiresAuth: true,
        },
      },
      {
        path: 'profile',
        name: 'Profile',
        redirect: '/profile/info',
        meta: {
          title: '个人中心',
          icon: 'User',
          requiresAuth: true,
        },
        children: [
          {
            path: 'info',
            name: 'ProfileInfo',
            component: () => import('@/views/profile/Info.vue'),
            meta: {
              title: '基本信息',
              requiresAuth: true,
            },
          },
          {
            path: 'password',
            name: 'ProfilePassword',
            component: () => import('@/views/profile/Password.vue'),
            meta: {
              title: '修改密码',
              requiresAuth: true,
            },
          },
          {
            path: 'logs',
            name: 'ProfileLogs',
            component: () => import('@/views/profile/Logs.vue'),
            meta: {
              title: '登录日志',
              requiresAuth: true,
            },
          },
        ],
      },
    ],
  },
]

// 创建路由实例
const router: Router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 }),
})

// 白名单路由
const whiteList = ['/login', '/404']

// 设置路由守卫
function setupRouterGuard(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()
    const token = userStore.token

    // 设置页面标题
    document.title = `${to.meta.title ?? 'AIPerm'} - 权限管理系统`

    // 未登录
    if (!token) {
      if (whiteList.includes(to.path)) {
        next()
      }
      else {
        next(`/login?redirect=${to.path}`)
      }
      return
    }

    // 已登录访问登录页
    if (to.path === '/login') {
      next('/')
      return
    }

    // 路由已加载
    if (permissionStore.isRoutesLoaded) {
      next()
      return
    }

    // 动态加载路由
    try {
      // 1. 获取用户信息
      await userStore.fetchUserInfo()

      // 2. 获取菜单
      await permissionStore.fetchMenus()

      // 3. 生成路由
      const routes = permissionStore.generateRoutes()

      // 4. 动态添加路由（添加为 Root 路由的子路由）
      routes.forEach((route) => {
        router.addRoute('Root', route)
      })

      // 5. 添加 404 兜底路由（必须最后添加）
      router.addRoute({
        path: '/:pathMatch(.*)*',
        redirect: '/404',
      })

      // 6. 重新导航
      next({ ...to, replace: true })
    }
    catch (error) {
      console.error('Failed to load routes:', error)
      // 加载失败，清除 token 并跳转登录
      userStore.logout()
      next(`/login?redirect=${to.path}`)
    }
  })

  router.afterEach(() => {
    // 路由切换后处理
  })
}

// 初始化路由
export function setupRouter(app: App) {
  setupRouterGuard(router)
  app.use(router)
}

export default router
