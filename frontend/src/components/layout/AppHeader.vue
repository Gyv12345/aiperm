<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAppStore, type ThemeMode } from '@/stores/app'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 用户信息
const username = computed(() => userStore.username || '未登录')

// 主题选项
const themeOptions: { value: ThemeMode, label: string, icon: string }[] = [
  { value: 'system', label: '跟随系统', icon: 'Monitor' },
  { value: 'light', label: '浅色', icon: 'Sunny' },
  { value: 'dark', label: '深色', icon: 'Moon' },
]

// 当前主题标签
const currentThemeLabel = computed(() => {
  const option = themeOptions.find(opt => opt.value === appStore.theme)
  return option?.label || '跟随系统'
})

// 设置主题
function handleThemeChange(mode: ThemeMode) {
  appStore.setTheme(mode)
}

// 处理登出
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await userStore.logout()
    router.push('/login')
  }
  catch {
    // 用户取消
  }
}

// 跳转到个人中心
function goToProfile() {
  router.push('/profile')
}

// 初始化主题
onMounted(() => {
  appStore.watchSystemTheme()
  appStore.updateThemeClass()
})
</script>

<template>
  <header class="app-header h-16 flex items-center justify-between px-6">
    <!-- 左侧标题 -->
    <div class="flex items-center">
      <h2 class="text-lg font-semibold header-title">
        <slot name="title">
          仪表板
        </slot>
      </h2>
    </div>

    <!-- 右侧操作区 -->
    <div class="flex items-center space-x-4">
      <!-- 主题切换 -->
      <el-dropdown
        trigger="click"
        @command="handleThemeChange"
      >
        <div class="theme-dropdown flex items-center cursor-pointer">
          <el-icon :size="18">
            <Sunny v-if="appStore.resolvedTheme === 'light'" />
            <Moon v-else />
          </el-icon>
          <span class="ml-1 text-sm">{{ currentThemeLabel }}</span>
          <el-icon class="ml-1">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="option in themeOptions"
              :key="option.value"
              :command="option.value"
              :class="{ 'is-active': appStore.theme === option.value }"
            >
              <el-icon>
                <component :is="option.icon" />
              </el-icon>
              <span class="ml-2">{{ option.label }}</span>
              <el-icon
                v-if="appStore.theme === option.value"
                class="ml-auto"
              >
                <Check />
              </el-icon>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 用户下拉菜单 -->
      <el-dropdown trigger="click">
        <div class="user-dropdown flex items-center cursor-pointer">
          <el-avatar
            :size="32"
            class="mr-2"
          >
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="username">{{ username }}</span>
          <el-icon class="ml-1">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="goToProfile">
              <el-icon><User /></el-icon>
              <span class="ml-2">个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item
              divided
              @click="handleLogout"
            >
              <el-icon><SwitchButton /></el-icon>
              <span class="ml-2">退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  background-color: var(--color-bg-header);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.header-title {
  color: var(--color-text-primary);
}

.theme-dropdown {
  color: var(--color-text-primary);
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.theme-dropdown:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-primary);
}

.user-dropdown {
  color: var(--color-text-primary);
}

.user-dropdown:hover {
  color: var(--color-primary);
}

.username {
  color: var(--color-text-primary);
}

/* 主题选项激活状态 */
:deep(.el-dropdown-menu__item.is-active) {
  color: var(--el-color-primary);
  background-color: var(--el-fill-color-light);
}
</style>
