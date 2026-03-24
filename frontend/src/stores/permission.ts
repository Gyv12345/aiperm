import {defineStore} from 'pinia'
import {ref} from 'vue'
import type {RouteRecordRaw} from 'vue-router'
import {authApi} from '@/api/auth'
import type {MenuVO} from '@/api/system/menu'

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

const DISABLED_ROUTE_PATHS = new Set([
  '/system/im-config',
  '/system/approval-scene',
  '/approval/my',
  '/approval/todo',
  '/enterprise/message-template',
  '/enterprise/message-log',
])

function normalizePath(path?: string | null): string {
  if (!path) {
    return '/'
  }
  const withLeadingSlash = path.startsWith('/') ? path : `/${path}`
  return withLeadingSlash.replace(/\/{2,}/g, '/')
}

function buildMenuFullPath(parentPath: string, menuPath?: string | null): string {
  const normalizedMenuPath = normalizePath(menuPath)
  if (!parentPath || parentPath === '/') {
    return normalizedMenuPath
  }
  return normalizePath(`${parentPath}/${normalizedMenuPath}`)
}

function filterDisabledMenus(menuList: MenuItem[], parentPath = ''): MenuItem[] {
  const result: MenuItem[] = []

  for (const menu of menuList) {
    const fullPath = buildMenuFullPath(parentPath, menu.path)
    const nextChildren = menu.children?.length
      ? filterDisabledMenus(menu.children as MenuItem[], fullPath)
      : undefined

    // 目录菜单：仅在有可见子菜单时保留
    if (menu.menuType === '1') {
      if (nextChildren && nextChildren.length > 0) {
        result.push({
          ...menu,
          children: nextChildren,
        })
      }
      continue
    }

    // 菜单项：命中禁用路径则删除
    if (DISABLED_ROUTE_PATHS.has(fullPath)) {
      continue
    }

    result.push({
      ...menu,
      children: nextChildren,
    })
  }

  return result
}

export const usePermissionStore = defineStore('permission', () => {
  const menus = ref<MenuItem[]>([])
  const routes = ref<RouteRecordRaw[]>([])
  const isRoutesLoaded = ref(false)

  // 获取用户菜单
  async function fetchMenus() {
    const data = await authApi.menus()
    menus.value = filterDisabledMenus(data as MenuItem[])
    return menus.value
  }

  // 根据菜单生成路由
  function generateRoutes() {
    const dynamicRoutes: RouteRecordRaw[] = []

    // 递归提取所有菜单类型的路由
    function extractRoutes(menuList: MenuItem[]) {
      menuList.forEach((menu) => {
        const route = generateRoute(menu)
        if (route) {
          dynamicRoutes.push(route)
        }
        // 递归处理子菜单
        if (menu.children && menu.children.length > 0) {
          extractRoutes(menu.children)
        }
      })
    }

    extractRoutes(menus.value)

    // 直接返回动态路由，不再创建根路由（根路由已在 constantRoutes 中定义）
    routes.value = dynamicRoutes
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
