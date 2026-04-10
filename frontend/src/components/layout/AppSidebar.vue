<script setup lang="ts">
import {computed, ref, watch, type Component} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useAppStore} from '@/stores/app'
import {usePermissionStore} from '@/stores/permission'
import type {MenuVO} from '@/api/system/menu'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import {Document} from '@element-plus/icons-vue'

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

// 当前打开的子菜单（只保持一个打开）
const openedMenu = ref<string>('')

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

// 根据当前路由自动展开包含当前页面的父菜单
watch(
  () => route.path,
  (path) => {
    const currentMenu = findMenuByPath(menuItems.value, path)
    const parentMenu = currentMenu ? findParent(menuItems.value, currentMenu.parentId) : null
    if (parentMenu) {
      openedMenu.value = `menu-${parentMenu.id}`
    }
  },
  { immediate: true }
)

// 切换侧边栏
function toggleSidebar() {
  appStore.toggleSidebar()
}

// 构建菜单路径
function buildMenuPath(menu: MenuItem): string {
  const parent = findParent(menuItems.value, menu.parentId)
  return buildMenuFullPath(parent ? buildMenuPath(parent) : '', menu.path)
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
  if (route.path === '/dashboard') {
    return 'dashboard'
  }
  const menu = findMenuByPath(dynamicMenus.value as MenuItem[], route.path)
  return menu ? `menu-${menu.id}` : route.path
}

// 根据路径查找菜单
function findMenuByPath(menus: MenuItem[], targetPath: string, parentPath = ''): MenuItem | null {
  for (const menu of menus) {
    const fullPath = buildMenuFullPath(parentPath, menu.path)
    if (menu.menuType === '2' && fullPath === targetPath) {
      return menu
    }
    if (menu.children) {
      const found = findMenuByPath(menu.children as MenuItem[], targetPath, fullPath)
      if (found) {
        return found
      }
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
  return (ElementPlusIconsVue as Record<string, Component>)[iconName]
}
</script>

<template>
  <aside
    class="sidebar"
    :class="{ 'sidebar--collapsed': collapsed }"
  >
    <div class="sidebar-inner">
      <div class="sidebar-header">
        <div class="brand-mark">
          <span>AI</span>
        </div>
        <div
          v-if="!collapsed"
          class="brand-copy"
        >
          <p class="brand-copy__eyebrow">
            Architectural Ledger
          </p>
          <h1 class="brand-copy__title">
            AIPerm
          </h1>
          <p class="brand-copy__subtitle">
            RBAC workspace
          </p>
        </div>
      </div>

      <p
        v-if="!collapsed"
        class="sidebar-section-label"
      >
        Navigation
      </p>

      <el-menu
        :default-active="getActiveMenuIndex()"
        :default-openeds="openedMenu ? [openedMenu] : []"
        :collapse="collapsed"
        :collapse-transition="false"
        :unique-opened="true"
        background-color="transparent"
        text-color="rgba(255, 255, 255, 0.85)"
        active-text-color="var(--color-primary-container)"
        class="sidebar-menu"
        @select="handleSelect"
        @open="handleMenuOpen"
      >
        <el-menu-item index="dashboard">
          <el-icon>
            <HomeFilled />
          </el-icon>
          <span>首页</span>
        </el-menu-item>

        <template
          v-for="menu in dynamicMenus"
          :key="menu.id"
        >
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

      <div class="sidebar-footer">
        <button
          type="button"
          class="sidebar-toggle"
          @click="toggleSidebar"
        >
          <el-icon class="text-lg">
            <Expand v-if="collapsed" />
            <Fold v-else />
          </el-icon>
          <span v-if="!collapsed">收起导航</span>
        </button>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 288px;
  min-height: 100vh;
  padding: 18px 0 18px 18px;
  color: var(--color-text-sidebar);
  transition: width 0.3s ease, padding 0.3s ease;
}

.sidebar--collapsed {
  width: 104px;
}

.sidebar-inner {
  display: flex;
  height: 100%;
  flex-direction: column;
  padding: 18px 12px 12px;
  border-radius: 30px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.06), transparent 28%),
    var(--color-bg-sidebar);
  box-shadow:
    inset 0 0 0 1px var(--color-border-sidebar),
    0 26px 56px rgba(7, 10, 14, 0.18);
}

.sidebar-header {
  display: flex;
  min-height: 76px;
  align-items: center;
  gap: 14px;
  padding: 0 8px 18px;
}

.brand-mark {
  display: inline-flex;
  height: 44px;
  width: 44px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.06);
}

.brand-copy {
  min-width: 0;
}

.brand-copy__eyebrow {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text-sidebar-muted);
}

.brand-copy__title {
  font-size: 1.125rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text-sidebar);
}

.brand-copy__subtitle {
  font-size: 12px;
  color: var(--color-text-sidebar-muted);
}

.sidebar-section-label {
  padding: 0 12px 10px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--color-text-sidebar-muted);
}

.sidebar-menu {
  flex: 1;
  padding-top: 4px;
}

.sidebar-footer {
  padding-top: 10px;
}

.sidebar-toggle {
  display: inline-flex;
  width: 100%;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.05);
  color: var(--color-text-sidebar);
  min-height: 44px;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

:deep(.el-menu) {
  background-color: transparent !important;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  position: relative;
  margin: 4px 0;
  height: 46px;
  border-radius: 16px;
  color: rgba(245, 247, 251, 0.86) !important;
  line-height: 46px;
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  font-size: 16px;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
  color: #ffffff !important;
}

:deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: var(--color-primary-container) !important;
}

:deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  inset: 10px auto 10px 0;
  width: 4px;
  border-radius: 999px;
  background: var(--color-primary-container);
}

:deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #ffffff !important;
}

:deep(.el-sub-menu .el-menu-item) {
  margin-left: 10px;
  height: 42px;
  line-height: 42px;
}

:deep(.el-menu--collapse .el-menu-item),
:deep(.el-menu--collapse .el-sub-menu__title) {
  justify-content: center;
}

:deep(.el-menu--collapse .el-sub-menu__title .el-sub-menu__icon-arrow) {
  display: none;
}

@media (max-width: 960px) {
  .sidebar {
    width: 272px;
    padding: 14px 0 14px 14px;
  }

  .sidebar--collapsed {
    width: 92px;
  }

  .sidebar-inner {
    border-radius: 24px;
  }
}
</style>
