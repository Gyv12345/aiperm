import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type ThemeMode = 'light' | 'dark'

export const useAppStore = defineStore(
  'app',
  () => {
    const sidebarCollapsed = ref(false)
    const loading = ref(false)
    const theme = ref<ThemeMode>('light')

    // 计算属性
    const isDark = computed(() => theme.value === 'dark')

    // 切换侧边栏
    function toggleSidebar() {
      sidebarCollapsed.value = !sidebarCollapsed.value
    }

    // 设置加载状态
    function setLoading(value: boolean) {
      loading.value = value
    }

    // 切换主题
    function toggleTheme() {
      theme.value = theme.value === 'light' ? 'dark' : 'light'
      updateThemeClass()
    }

    // 设置主题
    function setTheme(mode: ThemeMode) {
      theme.value = mode
      updateThemeClass()
    }

    // 更新 HTML class
    function updateThemeClass() {
      const html = document.documentElement
      html.classList.remove('light', 'dark')
      html.classList.add(theme.value)
    }

    return {
      // 状态
      sidebarCollapsed,
      loading,
      theme,
      // 计算属性
      isDark,
      // 方法
      toggleSidebar,
      setLoading,
      toggleTheme,
      setTheme,
    }
  },
  {
    persist: {
      key: 'aiperm-app',
      storage: localStorage,
      pick: ['sidebarCollapsed', 'theme'],
    },
  },
)
