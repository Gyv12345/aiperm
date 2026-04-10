<script setup lang="ts">
import {computed} from 'vue'
import {ElMessageBox} from 'element-plus'
import {APP_THEME_COLOR_PRESETS, type ThemeMode, useAppStore} from '@/stores/app'

const appStore = useAppStore()

const themeModeOptions: {label: string, value: ThemeMode}[] = [
  { label: '浅色', value: 'light' },
  { label: '深色', value: 'dark' },
  { label: '跟随系统', value: 'system' },
]

const drawerVisible = computed({
  get: () => appStore.settingsPanelVisible,
  set: (value: boolean) => {
    appStore.settingsPanelVisible = value
  },
})

function isActiveColor(color: string) {
  return appStore.themeColor.toLowerCase() === color.toLowerCase()
}

async function handleReset() {
  try {
    await ElMessageBox.confirm('确定要恢复默认配置吗？', '提示', {
      confirmButtonText: '恢复默认',
      cancelButtonText: '取消',
      type: 'warning',
    })

    appStore.resetSettings()
  }
  catch {
    // 用户取消
  }
}
</script>

<template>
  <el-drawer
    v-model="drawerVisible"
    direction="rtl"
    size="300px"
    :with-header="false"
    :z-index="2100"
    :append-to-body="true"
    :lock-scroll="true"
    class="settings-panel-drawer"
  >
    <div class="settings-panel">
      <div class="settings-panel__header">
        <span class="text-base font-semibold">
          系统设置
        </span>
        <button
          type="button"
          class="settings-close flex items-center justify-center"
          @click="drawerVisible = false"
        >
          <el-icon :size="18">
            <Close />
          </el-icon>
        </button>
      </div>

      <div class="settings-panel__body">
        <section class="settings-section">
          <div class="settings-section-title mb-3">
            主题模式
          </div>
          <div class="grid grid-cols-3 gap-2">
            <el-button
              v-for="option in themeModeOptions"
              :key="option.value"
              class="theme-mode-button !ml-0"
              :type="appStore.theme === option.value ? 'primary' : 'default'"
              plain
              @click="appStore.setTheme(option.value)"
            >
              {{ option.label }}
            </el-button>
          </div>
        </section>

        <section class="settings-section">
          <div class="settings-section-title mb-3">
            主题色
          </div>
          <div class="flex items-center gap-3">
            <el-tooltip
              v-for="color in APP_THEME_COLOR_PRESETS"
              :key="color.value"
              :content="color.label"
              placement="top"
            >
              <button
                type="button"
                class="theme-color-swatch"
                :class="{ 'is-active': isActiveColor(color.value) }"
                :style="{ backgroundColor: color.value, '--swatch-color': color.value }"
                @click="appStore.setThemeColor(color.value)"
              />
            </el-tooltip>
          </div>
        </section>

        <section class="settings-section">
          <div class="settings-section-title mb-3">
            侧边栏
          </div>
          <div class="setting-row flex items-center justify-between gap-3">
            <span>折叠侧边栏</span>
            <el-switch v-model="appStore.sidebarCollapsed" />
          </div>
        </section>

        <section class="settings-section">
          <div class="settings-section-title mb-3">
            界面显示
          </div>
          <div class="setting-list flex flex-col gap-3">
            <div class="setting-row flex items-center justify-between gap-3">
              <span>显示面包屑</span>
              <el-switch v-model="appStore.showBreadcrumb" />
            </div>
            <div class="setting-row flex items-center justify-between gap-3">
              <span>显示标签页</span>
              <el-switch v-model="appStore.showTabs" />
            </div>
            <div class="setting-row flex items-center justify-between gap-3">
              <span>显示水印</span>
              <el-switch v-model="appStore.showWatermark" />
            </div>
          </div>
        </section>
      </div>

      <div class="settings-panel__footer">
        <el-button
          link
          class="reset-button !px-0"
          @click="handleReset"
        >
          恢复默认配置
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<style scoped>
.settings-panel-drawer :deep(.el-drawer) {
  background-color: transparent;
}

.settings-panel-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.settings-panel {
  display: flex;
  height: 100%;
  flex-direction: column;
  padding: 14px;
  color: var(--color-text-primary);
}

.settings-panel__header,
.settings-panel__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 6px 14px;
}

.settings-panel__body {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.settings-section {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.2);
  box-shadow: inset 0 0 0 1px var(--color-border);
}

.settings-panel-drawer :deep(.el-button.is-plain) {
  --el-button-hover-text-color: var(--color-primary);
  --el-button-hover-border-color: var(--color-primary);
}

.settings-section-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.theme-mode-button {
  width: 100%;
}

.setting-row {
  color: var(--color-text-primary);
  font-size: 14px;
}

.settings-close {
  width: 32px;
  height: 32px;
  color: var(--color-text-secondary);
  background-color: transparent;
  border: none;
  border-radius: 8px;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.settings-close:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-primary);
}

.theme-color-swatch {
  width: 24px;
  height: 24px;
  border: 2px solid transparent;
  border-radius: 9999px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.theme-color-swatch:hover {
  transform: scale(1.08);
}

.theme-color-swatch.is-active {
  transform: scale(1.1);
  box-shadow: 0 0 0 2px var(--color-bg-card), 0 0 0 4px var(--swatch-color);
}

.reset-button {
  color: var(--color-primary);
}
</style>
