<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

// 菜单配置
const menuItems = [
  { path: '/dashboard', icon: 'Odometer', title: '仪表板' },
  { path: '/system/user', icon: 'User', title: '用户管理' },
  { path: '/system/role', icon: 'UserFilled', title: '角色管理' },
  { path: '/system/permission', icon: 'Lock', title: '权限管理' },
  { path: '/system/menu', icon: 'Menu', title: '菜单管理' },
  { path: '/system/dept', icon: 'OfficeBuilding', title: '部门管理' },
  { path: '/system/dict', icon: 'Collection', title: '字典管理' },
] as const

// 侧边栏折叠状态
const collapsed = computed(() => appStore.sidebarCollapsed)

// 切换侧边栏
function toggleSidebar() {
  appStore.toggleSidebar()
}

// 导航到指定路径
function navigateTo(path: string) {
  router.push(path)
}

// 检查是否激活
function isActive(path: string): boolean {
  return route.path === path || route.path.startsWith(path + '/')
}
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
          v-for="item in menuItems"
          :key="item.path"
        >
          <div
            class="menu-item flex items-center px-3 py-2 rounded cursor-pointer transition-colors"
            :class="isActive(item.path) ? 'bg-blue-600 text-white' : 'hover:bg-gray-700'"
            @click="navigateTo(item.path)"
          >
            <el-icon class="text-lg">
              <component :is="item.icon" />
            </el-icon>
            <span
              v-if="!collapsed"
              class="ml-3"
            >
              {{ item.title }}
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
