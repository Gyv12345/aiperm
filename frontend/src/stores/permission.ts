import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { authApi } from '@/api/auth'
import type { MenuVO } from '@/api/system/menu'

// 组件映射表
const componentModules = import.meta.glob('@/views/**/*.vue')

function loadComponent(component: string | undefined) {
  if (!component) {
    return undefined
  }
  const path = `/src/views/${component}.vue`
  return componentModules[path]
}

interface MenuItem extends MenuVO {
  children?: MenuItem[]
}

export const usePermissionStore = defineStore('permission', () => {
  const menus = ref<MenuItem[]>([])
  const routes = ref<RouteRecordRaw[]>([])
  const isRoutesLoaded = ref(false)

  // 获取用户菜单
  async function fetchMenus() {
    const data = await authApi.menus()
    menus.value = data as MenuItem[]
    return menus.value
  }

  // 根据菜单生成路由
  function generateRoutes() {
    const dynamicRoutes: RouteRecordRaw[] = []

    // 遍历菜单生成路由
    menus.value.forEach((menu) => {
      const route = generateRoute(menu)
      if (route) {
        dynamicRoutes.push(route)
      }
    })

    // 根路由
    const rootRoute: RouteRecordRaw = {
      path: '/',
      name: 'Root',
      component: () => import('@/components/layout/MainLayout.vue'),
      redirect: '/dashboard',
      children: dynamicRoutes,
    }

    routes.value = [rootRoute]
    isRoutesLoaded.value = true

    return routes.value
  }

  // 生成单个路由
  function generateRoute(menu: MenuItem): RouteRecordRaw | null {
    // 只处理菜单类型（menuType = '2'）
    if (menu.menuType !== '2') {
      return null
    }

    const component = loadComponent(menu.component)
    if (!component) {
      console.warn(`Component not found: ${menu.component}`)
      return null
    }

    // 构建完整路径
    const fullPath = buildFullPath(menu)

    return {
      path: fullPath,
      name: `menu-${menu.id}`,
      component,
      meta: {
        title: menu.menuName,
        icon: menu.icon,
        requiresAuth: true,
      },
    }
  }

  // 构建完整路径
  function buildFullPath(menu: MenuItem): string {
    // 查找父菜单
    const parent = findParent(menus.value, menu.parentId)
    if (parent && parent.path) {
      // 父菜单路径是 /system，子菜单路径是 user，完整路径是 /system/user
      const parentPath = parent.path.startsWith('/') ? parent.path : `/${parent.path}`
      return `${parentPath}/${menu.path}`
    }
    // 没有父菜单或父菜单没有路径，直接使用自己的路径
    return menu.path?.startsWith('/') ? menu.path : `/${menu.path}`
  }

  // 查找父菜单
  function findParent(menus: MenuItem[], parentId: number | null): MenuItem | null {
    if (!parentId || parentId === 0) {
      return null
    }
    for (const menu of menus) {
      if (menu.id === parentId) {
        return menu
      }
      if (menu.children) {
        const found = findParent(menu.children, parentId)
        if (found) {
          return found
        }
      }
    }
    return null
  }

  // 重置权限
  function resetPermission() {
    menus.value = []
    routes.value = []
    isRoutesLoaded.value = false
  }

  return {
    menus,
    routes,
    isRoutesLoaded,
    fetchMenus,
    generateRoutes,
    resetPermission,
  }
})
