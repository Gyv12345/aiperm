<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { usePermissionStore } from '@/stores/permission'
import type { MenuVO } from '@/api/system/menu'

interface MenuItem extends MenuVO {
  children?: MenuItem[]
}

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const permissionStore = usePermissionStore()

// 从 PermissionStore 获取菜单
const menuItems = computed(() => permissionStore.menus)

// 侧边栏折叠状态
const collapsed = computed(() => appStore.sidebarCollapsed)

// 切换侧边栏
function toggleSidebar() {
  appStore.toggleSidebar()
}

// 导航到指定路径
function navigateTo(menu: MenuItem) {
  // 构建完整路径
  const fullPath = buildMenuPath(menu)
  router.push(fullPath)
}

// 构建菜单路径
function buildMenuPath(menu: MenuItem): string {
  if (menu.menuType === '1') {
    // 目录类型，跳转到第一个子菜单
    if (menu.children && menu.children.length > 0) {
      return buildMenuPath(menu.children[0] as MenuItem)
    }
    return menu.path || '/'
  }
  // 菜单类型，查找父菜单构建完整路径
  const parent = findParent(menuItems.value, menu.parentId)
  if (parent && parent.path) {
    const parentPath = parent.path.startsWith('/') ? parent.path : `/${parent.path}`
    return `${parentPath}/${menu.path}`
  }
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
      const found = findParent(menu.children as MenuItem[], parentId)
      if (found) return found
    }
  }
  return null
}

// 检查是否激活
function isActive(menu: MenuItem): boolean {
  const fullPath = buildMenuPath(menu)
  return route.path === fullPath || route.path.startsWith(fullPath + '/')
}

// 获取需要显示的菜单（过滤掉目录，只显示一级菜单）
const displayMenus = computed(() => {
  return menuItems.value.map((menu) => {
    // 如果是目录，显示第一个子菜单的信息
    if (menu.menuType === '1' && menu.children && menu.children.length > 0) {
      return {
        ...menu,
        firstChild: menu.children[0],
      }
    }
    return menu
  })
})
</script>

<template>
  <aside
    class="sidebar flex flex-col bg-gray-800 text-white transition-all duration-300"
    :class="collapsed ? 'w-16' : 'w-64'"
  >
    <!-- Logo 区域 -->
    <div class="h-16 flex items-center justify-center border-b border-gray-700">
      <h1
        v-if="!collapsed"
        class="text-xl font-bold"
      >
        AIPerm
      </h1>
      <el-icon
        v-else
        class="text-2xl"
      >
        <Box />
      </el-icon>
    </div>

    <!-- 菜单列表 -->
    <nav class="flex-1 p-2 overflow-y-auto">
      <ul class="space-y-1">
        <li
          v-for="menu in displayMenus"
          :key="menu.id"
        >
          <div
            class="menu-item flex items-center px-3 py-2 rounded cursor-pointer transition-colors"
            :class="isActive(menu as MenuItem) ? 'bg-blue-600 text-white' : 'hover:bg-gray-700'"
            @click="navigateTo(menu as MenuItem)"
          >
            <el-icon class="text-lg">
              <component :is="menu.icon || 'Document'" />
            </el-icon>
            <span
              v-if="!collapsed"
              class="ml-3"
            >
              {{ menu.menuName }}
            </span>
          </div>
        </li>
      </ul>
    </nav>

    <!-- 折叠按钮 -->
    <div class="p-2 border-t border-gray-700">
      <div
        class="flex items-center justify-center py-2 rounded cursor-pointer hover:bg-gray-700"
        @click="toggleSidebar"
      >
        <el-icon class="text-lg">
          <Expand v-if="collapsed" />
          <Fold v-else />
        </el-icon>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  min-height: 100vh;
}

.menu-item {
  white-space: nowrap;
}
</style>
