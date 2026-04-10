import {defineStore} from 'pinia'
import {computed, ref, watch} from 'vue'

export type ThemeMode = 'light' | 'dark' | 'system'
export type ResolvedTheme = 'light' | 'dark'

export const APP_THEME_COLOR_PRESETS = [
  { label: '架构蓝', value: '#0060a9' },
  { label: '云青蓝', value: '#409eff' },
  { label: '绿色', value: '#67c23a' },
  { label: '橙色', value: '#e6a23c' },
  { label: '红色', value: '#f56c6c' },
] as const

const DEFAULT_SETTINGS = {
  sidebarCollapsed: false,
  theme: 'system' as ThemeMode,
  themeColor: '#0060a9',
  showBreadcrumb: true,
  showTabs: false,
  showWatermark: false,
  settingsPanelVisible: false,
}

function normalizeHexColor(color: string) {
  const raw = color.trim().replace('#', '')
  if (!/^[\da-fA-F]{3}([\da-fA-F]{3})?$/.test(raw)) {
    return null
  }

  const value = raw.length === 3
    ? raw.split('').map(char => `${char}${char}`).join('')
    : raw

  return `#${value.toLowerCase()}`
}

function mixColor(color: string, mixWith: string, weight: number) {
  const source = normalizeHexColor(color)
  const target = normalizeHexColor(mixWith)

  if (!source || !target) {
    return color
  }

  const channels = [0, 2, 4].map((offset) => {
    const start = Number.parseInt(source.slice(offset + 1, offset + 3), 16)
    const end = Number.parseInt(target.slice(offset + 1, offset + 3), 16)
    return Math.round(start + (end - start) * weight)
      .toString(16)
      .padStart(2, '0')
  })

  return `#${channels.join('')}`
}

export const useAppStore = defineStore(
  'app',
  () => {
    const sidebarCollapsed = ref(false)
    const loading = ref(false)
    const theme = ref<ThemeMode>('system')
    const themeColor = ref(DEFAULT_SETTINGS.themeColor)
    const showBreadcrumb = ref(DEFAULT_SETTINGS.showBreadcrumb)
    const showTabs = ref(DEFAULT_SETTINGS.showTabs)
    const showWatermark = ref(DEFAULT_SETTINGS.showWatermark)
    const settingsPanelVisible = ref(DEFAULT_SETTINGS.settingsPanelVisible)

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

    // 设置主题色
    function setThemeColor(color: string) {
      const normalized = normalizeHexColor(color)
      if (normalized) {
        themeColor.value = normalized
      }
    }

    // 切换设置面板
    function toggleSettingsPanel() {
      settingsPanelVisible.value = !settingsPanelVisible.value
    }

    // 重置为默认配置
    function resetSettings() {
      sidebarCollapsed.value = DEFAULT_SETTINGS.sidebarCollapsed
      theme.value = DEFAULT_SETTINGS.theme
      themeColor.value = DEFAULT_SETTINGS.themeColor
      showBreadcrumb.value = DEFAULT_SETTINGS.showBreadcrumb
      showTabs.value = DEFAULT_SETTINGS.showTabs
      showWatermark.value = DEFAULT_SETTINGS.showWatermark
      settingsPanelVisible.value = DEFAULT_SETTINGS.settingsPanelVisible
    }

    // 更新 HTML class
    function updateThemeClass() {
      if (typeof document === 'undefined') {
        return
      }

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

    // 更新主题色相关变量
    function updateThemeColor() {
      if (typeof document === 'undefined') {
        return
      }

      const html = document.documentElement
      const primary = normalizeHexColor(themeColor.value) || DEFAULT_SETTINGS.themeColor
      const primaryHover = mixColor(primary, '#ffffff', 0.12)
      const primaryContainer = mixColor(primary, '#ffffff', 0.32)
      const primaryFixed = mixColor(primary, '#ffffff', 0.86)
      const primaryFixedVariant = mixColor(primary, '#000000', 0.12)

      html.style.setProperty('--color-primary', primary)
      html.style.setProperty('--color-primary-hover', primaryHover)
      html.style.setProperty('--color-primary-container', primaryContainer)
      html.style.setProperty('--color-primary-fixed', primaryFixed)
      html.style.setProperty('--color-primary-fixed-variant', primaryFixedVariant)
      html.style.setProperty('--el-color-primary', primary)
      html.style.setProperty('--el-color-primary-light-3', mixColor(primary, '#ffffff', 0.3))
      html.style.setProperty('--el-color-primary-light-5', mixColor(primary, '#ffffff', 0.5))
      html.style.setProperty('--el-color-primary-light-7', mixColor(primary, '#ffffff', 0.7))
      html.style.setProperty('--el-color-primary-light-8', mixColor(primary, '#ffffff', 0.8))
      html.style.setProperty('--el-color-primary-light-9', mixColor(primary, '#ffffff', 0.9))
      html.style.setProperty('--el-color-primary-dark-2', mixColor(primary, '#000000', 0.2))
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
    }, { immediate: true })

    watch(themeColor, () => {
      updateThemeColor()
    }, { immediate: true })

    return {
      // 状态
      sidebarCollapsed,
      loading,
      theme,
      themeColor,
      showBreadcrumb,
      showTabs,
      showWatermark,
      settingsPanelVisible,
      resolvedTheme,
      // 计算属性
      isDark,
      // 方法
      toggleSidebar,
      setLoading,
      setTheme,
      setThemeColor,
      toggleSettingsPanel,
      resetSettings,
      updateThemeClass,
      updateThemeColor,
      watchSystemTheme,
      getSystemTheme,
    }
  },
  {
    persist: {
      key: 'aiperm-app',
      storage: localStorage,
      pick: [
        'sidebarCollapsed',
        'theme',
        'themeColor',
        'showBreadcrumb',
        'showTabs',
        'showWatermark',
        'settingsPanelVisible',
      ],
    },
  },
)
