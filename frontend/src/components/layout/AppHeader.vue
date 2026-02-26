<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 用户信息
const username = computed(() => userStore.username || '未登录')
const isDark = computed(() => appStore.isDark)

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

// 切换主题
function toggleTheme() {
  appStore.toggleTheme()
}

// 跳转到个人中心
function goToProfile() {
  router.push('/profile')
}
</script>

<template>
  <header class="h-16 bg-white shadow flex items-center justify-between px-6">
    <!-- 左侧标题 -->
    <div class="flex items-center">
      <h2 class="text-lg font-semibold text-gray-800">
        <slot name="title">
          仪表板
        </slot>
      </h2>
    </div>

    <!-- 右侧操作区 -->
    <div class="flex items-center space-x-4">
      <!-- 主题切换 -->
      <el-tooltip :content="isDark ? '切换亮色模式' : '切换暗色模式'">
        <el-button
          text
          circle
          @click="toggleTheme"
        >
          <el-icon class="text-xl">
            <Sunny v-if="isDark" />
            <Moon v-else />
          </el-icon>
        </el-button>
      </el-tooltip>

      <!-- 用户下拉菜单 -->
      <el-dropdown trigger="click">
        <div class="flex items-center cursor-pointer hover:text-blue-500">
          <el-avatar
            :size="32"
            class="mr-2"
          >
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="text-gray-700">{{ username }}</span>
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
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              <span class="ml-2">退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>
