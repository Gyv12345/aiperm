<script setup lang="ts">
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessageBox} from 'element-plus'
import {useUserStore} from '@/stores/user'
import {useAppStore} from '@/stores/app'
import {noticeApi, type NoticeVO} from '@/api/enterprise/notice'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
let noticeTimer: number | undefined
let stopSystemThemeWatch: (() => void) | undefined

// 用户信息
const username = computed(() => userStore.username || '未登录')
const notifications = ref<NoticeVO[]>([])
const noticeLoading = ref(false)
const noticeCount = computed(() => notifications.value.length)
const currentDateLabel = computed(() =>
  new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  }).format(new Date())
)
const themeLabel = computed(() => appStore.resolvedTheme === 'dark' ? 'Dark mode' : 'Light mode')

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

function handleNoticeOpen() {
  notifications.value = []
}

// 初始化主题
onMounted(() => {
  stopSystemThemeWatch = appStore.watchSystemTheme()
  appStore.updateThemeClass()
  fetchNotifications()
  noticeTimer = window.setInterval(fetchNotifications, 60_000)
})

onUnmounted(() => {
  stopSystemThemeWatch?.()
  if (noticeTimer) {
    window.clearInterval(noticeTimer)
  }
})
</script>

<template>
  <header
    class="app-header"
    :class="{ 'app-header--dark': appStore.isDark }"
  >
    <div class="app-header__title-block">
      <p class="app-header__eyebrow">
        Architectural Ledger
      </p>
      <h2 class="header-title">
        <slot name="title">
          仪表板
        </slot>
      </h2>
    </div>

    <div class="app-header__actions">
      <div class="app-header__meta">
        <span>{{ currentDateLabel }}</span>
        <span class="app-header__meta-dot" />
        <span>{{ themeLabel }}</span>
      </div>

      <el-dropdown
        trigger="click"
        placement="bottom-end"
        @command="handleNoticeCommand"
      >
        <div
          class="icon-trigger"
          @click="handleNoticeOpen"
        >
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

      <div
        class="icon-trigger"
        @click="appStore.toggleSettingsPanel"
      >
        <el-icon :size="18">
          <Setting />
        </el-icon>
      </div>

      <el-dropdown trigger="click">
        <div class="user-dropdown">
          <el-avatar
            :size="32"
            class="user-dropdown__avatar"
          >
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="user-dropdown__content">
            <span class="user-dropdown__eyebrow">Operator</span>
            <span class="username">{{ username }}</span>
          </div>
          <el-icon class="user-dropdown__arrow">
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
  display: flex;
  min-height: 78px;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 0 30px;
  border-radius: 26px;
  background: var(--color-bg-header);
  box-shadow: inset 0 0 0 1px var(--color-border);
  backdrop-filter: blur(14px);
}

.header-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.app-header__title-block {
  min-width: 0;
}

.app-header__eyebrow {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.app-header__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-header__meta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  min-height: 38px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.42);
  color: var(--color-text-secondary);
  font-size: 12px;
  box-shadow: inset 0 0 0 1px var(--color-border);
}

.app-header__meta-dot {
  width: 4px;
  height: 4px;
  border-radius: 999px;
  background: var(--color-primary);
}

.icon-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.42);
  color: var(--color-text-primary);
  cursor: pointer;
  box-shadow: inset 0 0 0 1px var(--color-border);
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.icon-trigger:hover {
  transform: translateY(-1px);
  background-color: var(--color-bg-hover);
  color: var(--color-primary);
}

.user-dropdown {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 48px;
  padding: 6px 10px 6px 6px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.42);
  color: var(--color-text-primary);
  box-shadow: inset 0 0 0 1px var(--color-border);
}

.user-dropdown__avatar {
  background: var(--gradient-primary);
  color: #ffffff;
}

.user-dropdown__content {
  display: flex;
  min-width: 0;
  flex-direction: column;
  line-height: 1.2;
}

.user-dropdown__eyebrow {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.user-dropdown__arrow {
  color: var(--color-text-muted);
}

.app-header--dark .app-header__meta,
.app-header--dark .icon-trigger,
.app-header--dark .user-dropdown {
  background: rgba(255, 255, 255, 0.08);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
}

.app-header--dark .app-header__meta {
  color: rgba(243, 246, 250, 0.88);
}

.app-header--dark .icon-trigger {
  color: rgba(243, 246, 250, 0.88);
}

.app-header--dark .icon-trigger:hover,
.app-header--dark .user-dropdown:hover {
  background: rgba(77, 160, 239, 0.16);
  color: var(--color-primary-container);
}

.app-header--dark .user-dropdown__eyebrow,
.app-header--dark .user-dropdown__arrow {
  color: rgba(243, 246, 250, 0.62);
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

.user-dropdown:hover {
  color: var(--color-primary);
}

.username {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

:deep(.notice-menu .el-dropdown-menu__item) {
  align-items: flex-start;
}

@media (max-width: 960px) {
  .app-header {
    padding: 0 18px;
  }

  .app-header__meta {
    display: none;
  }

  .user-dropdown__content {
    display: none;
  }
}
</style>
