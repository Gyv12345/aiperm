<script setup lang="ts">
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessageBox} from 'element-plus'
import {useUserStore} from '@/stores/user'
import {type ThemeMode, useAppStore} from '@/stores/app'
import {noticeApi, type NoticeVO} from '@/api/enterprise/notice'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
let noticeTimer: number | undefined

// 用户信息
const username = computed(() => userStore.username || '未登录')
const notifications = ref<NoticeVO[]>([])
const noticeLoading = ref(false)
const noticeCount = computed(() => notifications.value.length)

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

function goToNoticeCenter() {
  router.push('/enterprise/notice')
}

async function fetchNotifications() {
  noticeLoading.value = true
  try {
    notifications.value = await noticeApi.feed(1, 8)
  }
  catch (error) {
    console.error('获取通知失败:', error)
  }
  finally {
    noticeLoading.value = false
  }
}

function handleNoticeCommand(command: string) {
  if (command === 'refresh') {
    fetchNotifications()
    return
  }
  if (command === 'all' || command.startsWith('notice:')) {
    goToNoticeCenter()
  }
}

function formatNoticeTime(time?: string) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

// 初始化主题
onMounted(() => {
  appStore.watchSystemTheme()
  appStore.updateThemeClass()
  fetchNotifications()
  noticeTimer = window.setInterval(fetchNotifications, 60_000)
})

onUnmounted(() => {
  if (noticeTimer) {
    window.clearInterval(noticeTimer)
  }
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
      <!-- 通知提醒 -->
      <el-dropdown
        trigger="click"
        placement="bottom-end"
        @command="handleNoticeCommand"
      >
        <div class="notice-trigger flex items-center justify-center cursor-pointer">
          <el-badge
            :value="noticeCount"
            :max="99"
            :hidden="noticeCount === 0"
          >
            <el-icon :size="18">
              <Bell />
            </el-icon>
          </el-badge>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="notice-menu">
            <el-dropdown-item
              command="refresh"
              :disabled="noticeLoading"
            >
              <el-icon><Refresh /></el-icon>
              <span class="ml-2">{{ noticeLoading ? '刷新中...' : '刷新通知' }}</span>
            </el-dropdown-item>
            <el-dropdown-item
              v-for="item in notifications"
              :key="item.id"
              :command="`notice:${item.id}`"
              class="notice-item"
            >
              <div class="notice-item-content">
                <p class="notice-item-title">
                  {{ item.title }}
                </p>
                <p class="notice-item-time">
                  {{ formatNoticeTime(item.publishTime || item.createTime) }}
                </p>
              </div>
            </el-dropdown-item>
            <el-dropdown-item
              v-if="notifications.length === 0"
              disabled
            >
              暂无通知
            </el-dropdown-item>
            <el-dropdown-item
              divided
              command="all"
            >
              <el-icon><List /></el-icon>
              <span class="ml-2">查看全部公告通知</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

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

.notice-trigger {
  color: var(--color-text-primary);
  padding: 6px 8px;
  border-radius: 6px;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.notice-trigger:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-primary);
}

.notice-item-content {
  max-width: 280px;
}

.notice-item-title {
  margin: 0;
  font-size: 13px;
  line-height: 1.4;
  color: var(--color-text-primary);
  white-space: normal;
}

.notice-item-time {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--color-text-secondary);
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

:deep(.notice-menu .el-dropdown-menu__item) {
  align-items: flex-start;
}
</style>
