<script setup lang="ts">
import { computed } from 'vue'
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

// 从 PermissionStore 获取菜单
const menuItems = computed(() => permissionStore.menus)

// 侧边栏折叠状态
const collapsed = computed(() => appStore.sidebarCollapsed)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 打开的子菜单
const defaultOpeneds = computed(() => {
  const opens: string[] = []
  menuItems.value.forEach((menu) => {
    if (menu.menuType === '1' && menu.children && menu.children.length > 0) {
      opens.push(`menu-${menu.id}`)
    }
  })
  return opens
})

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
  // index 格式为 "menu-{id}"，需要找到对应菜单并跳转
  const menu = findMenuById(menuItems.value, parseInt(index.replace('menu-', '')))
  if (menu && menu.menuType === '2') {
    const path = buildMenuPath(menu)
    router.push(path)
  }
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
      :default-active="activeMenu"
      :default-openeds="defaultOpeneds"
      :collapse="collapsed"
      :collapse-transition="false"
      background-color="transparent"
      text-color="rgba(255, 255, 255, 0.85)"
      active-text-color="#409eff"
      class="sidebar-menu border-none flex-1"
      @select="handleSelect"
    >
      <template
        v-for="menu in menuItems"
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
  background-color: var(--color-bg-sidebar);
  border-right: 1px solid var(--color-border-sidebar);
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
