<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { usePermissionStore } from '@/stores/permission'
import type { MenuVO } from '@/api/system/menu'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

interface MenuItem extends MenuVO {
  children?: MenuItem[]
}

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const permissionStore = usePermissionStore()

// 从 PermissionStore 获取动态菜单
const dynamicMenus = computed(() => permissionStore.menus)

// 合并静态菜单和动态菜单
const menuItems = computed(() => {
  return [...dynamicMenus.value] as MenuItem[]
})

// 侧边栏折叠状态
const collapsed = computed(() => appStore.sidebarCollapsed)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 当前打开的子菜单（只保持一个打开）
const openedMenu = ref<string>('')

// 根据当前路由自动展开包含当前页面的父菜单
watch(
  () => route.path,
  (path) => {
    // 查找当前路由对应的父菜单
    const parentMenu = findParentMenuByPath(menuItems.value, path)
    if (parentMenu) {
      openedMenu.value = `menu-${parentMenu.id}`
    }
  },
  { immediate: true }
)

// 根据路径查找父菜单
function findParentMenuByPath(menus: MenuItem[], targetPath: string): MenuItem | null {
  for (const menu of menus) {
    if (menu.menuType === '1' && menu.children) {
      for (const child of menu.children) {
        const childPath = child.path?.startsWith('/') ? child.path : `/${child.path}`
        const fullPath = menu.path ? `${menu.path}/${child.path}` : childPath
        if (fullPath === targetPath || childPath === targetPath) {
          return menu
        }
      }
    }
  }
  return null
}

// 切换侧边栏
function toggleSidebar() {
  appStore.toggleSidebar()
}

// 构建菜单路径
function buildMenuPath(menu: MenuItem): string {
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

// 选择菜单
function handleSelect(index: string) {
  // 处理首页
  if (index === 'dashboard') {
    router.push('/dashboard')
    return
  }

  // 处理动态菜单
  const menu = findMenuById(menuItems.value, parseInt(index.replace('menu-', '')))
  if (menu && menu.menuType === '2') {
    const path = buildMenuPath(menu)
    router.push(path)
  }
}

// 处理子菜单展开
function handleMenuOpen(index: string) {
  openedMenu.value = index
}

// 获取当前激活菜单的索引
function getActiveMenuIndex(): string {
  // 首页特殊处理
  if (route.path === '/dashboard') {
    return 'dashboard'
  }
  const menu = findMenuByPath(dynamicMenus.value as MenuItem[], route.path)
  return menu ? `menu-${menu.id}` : route.path
}

// 根据路径查找菜单
function findMenuByPath(menus: MenuItem[], targetPath: string): MenuItem | null {
  for (const menu of menus) {
    // 检查子菜单
    if (menu.children) {
      for (const child of menu.children) {
        const childPath = child.path?.startsWith('/') ? child.path : `/${child.path}`
        if (childPath === targetPath) {
          return child
        }
      }
    }
    // 检查当前菜单
    const menuPath = menu.path?.startsWith('/') ? menu.path : `/${menu.path}`
    if (menuPath === targetPath) {
      return menu
    }
  }
  return null
}

// 根据 ID 查找菜单
function findMenuById(menus: MenuItem[], id: number): MenuItem | null {
  for (const menu of menus) {
    if (menu.id === id) return menu
    if (menu.children) {
      const found = findMenuById(menu.children as MenuItem[], id)
      if (found) return found
    }
  }
  return null
}

// 获取图标组件
function getIconComponent(iconName: string | null | undefined) {
  if (!iconName) return null
  return (ElementPlusIconsVue as Record<string, any>)[iconName]
}
</script>

<template>
  <aside
    class="sidebar flex flex-col text-white transition-all duration-300"
    :class="collapsed ? 'w-16' : 'w-64'"
  >
    <!-- Logo 区域 -->
    <div class="sidebar-header h-16 flex items-center justify-center border-b">
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
    <el-menu
      :default-active="getActiveMenuIndex()"
      :default-openeds="openedMenu ? [openedMenu] : []"
      :collapse="collapsed"
      :collapse-transition="false"
      :unique-opened="true"
      background-color="transparent"
      text-color="rgba(255, 255, 255, 0.85)"
      active-text-color="#409eff"
      class="sidebar-menu border-none flex-1"
      @select="handleSelect"
      @open="handleMenuOpen"
    >
      <!-- 首页 -->
      <el-menu-item index="dashboard">
        <el-icon>
          <HomeFilled />
        </el-icon>
        <span>首页</span>
      </el-menu-item>

      <!-- 动态菜单 -->
      <template
        v-for="menu in dynamicMenus"
        :key="menu.id"
      >
        <!-- 目录类型：有子菜单 -->
        <el-sub-menu
          v-if="menu.menuType === '1' && menu.children && menu.children.length > 0"
          :index="`menu-${menu.id}`"
        >
          <template #title>
            <el-icon>
              <component :is="getIconComponent(menu.icon) || Document" />
            </el-icon>
            <span>{{ menu.menuName }}</span>
          </template>
          <!-- 子菜单 -->
          <el-menu-item
            v-for="child in menu.children"
            :key="child.id"
            :index="`menu-${child.id}`"
          >
            <el-icon>
              <component :is="getIconComponent(child.icon) || Document" />
            </el-icon>
            <span>{{ child.menuName }}</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 菜单类型：无子菜单 -->
        <el-menu-item
          v-else-if="menu.menuType === '2'"
          :index="`menu-${menu.id}`"
        >
          <el-icon>
            <component :is="getIconComponent(menu.icon) || Document" />
          </el-icon>
          <span>{{ menu.menuName }}</span>
        </el-menu-item>
      </template>
    </el-menu>

    <!-- 折叠按钮 -->
    <div class="sidebar-footer p-2 border-t">
      <div
        class="flex items-center justify-center py-2 rounded cursor-pointer hover:bg-white/10"
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
  background-color: #1f2937; /* 默认深色背景 */
  background-color: var(--color-bg-sidebar, #1f2937);
  border-right: 1px solid #374151;
  border-right-color: var(--color-border-sidebar, #374151);
}

.sidebar-header {
  border-color: var(--color-border-sidebar);
}

.sidebar-footer {
  border-color: var(--color-border-sidebar);
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 256px;
}

:deep(.el-menu) {
  border-right: none;
  background-color: transparent !important;
}

:deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
}

:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.05) !important;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.05) !important;
}

:deep(.el-menu-item.is-active) {
  background-color: rgba(64, 158, 255, 0.15) !important;
}
</style>
