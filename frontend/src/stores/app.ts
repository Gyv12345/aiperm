import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'system'
export type ResolvedTheme = 'light' | 'dark'

export const useAppStore = defineStore(
  'app',
  () => {
    const sidebarCollapsed = ref(false)
    const loading = ref(false)
    const theme = ref<ThemeMode>('system')

    // 获取系统主题偏好
    function getSystemTheme(): ResolvedTheme {
      if (typeof window !== 'undefined' && window.matchMedia) {
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
      }
      return 'light'
    }

    // 计算实际应用的主题
    const resolvedTheme = computed<ResolvedTheme>(() => {
      if (theme.value === 'system') {
        return getSystemTheme()
      }
      return theme.value
    })

    // 是否为深色主题
    const isDark = computed(() => resolvedTheme.value === 'dark')

    // 切换侧边栏
    function toggleSidebar() {
      sidebarCollapsed.value = !sidebarCollapsed.value
    }

    // 设置加载状态
    function setLoading(value: boolean) {
      loading.value = value
    }

    // 设置主题
    function setTheme(mode: ThemeMode) {
      theme.value = mode
    }

    // 更新 HTML class
    function updateThemeClass() {
      const html = document.documentElement
      html.classList.remove('light', 'dark')
      html.classList.add(resolvedTheme.value)

      // 同时更新 Element Plus 的暗色模式
      if (resolvedTheme.value === 'dark') {
        html.setAttribute('data-theme', 'dark')
      }
      else {
        html.removeAttribute('data-theme')
      }
    }

    // 监听系统主题变化
    function watchSystemTheme() {
      if (typeof window !== 'undefined' && window.matchMedia) {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
        const handler = () => {
          if (theme.value === 'system') {
            updateThemeClass()
          }
        }
        mediaQuery.addEventListener('change', handler)
        return () => mediaQuery.removeEventListener('change', handler)
      }
      return () => {}
    }

    // 监听主题变化
    watch(theme, () => {
      updateThemeClass()
    }, { immediate: false })

    return {
      // 状态
      sidebarCollapsed,
      loading,
      theme,
      resolvedTheme,
      // 计算属性
      isDark,
      // 方法
      toggleSidebar,
      setLoading,
      setTheme,
      updateThemeClass,
      watchSystemTheme,
      getSystemTheme,
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
