<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import type {RouteLocationNormalizedLoaded} from 'vue-router'
import {useRoute, useRouter} from 'vue-router'
import {useAppStore} from '@/stores/app'

interface LayoutTab {
  key: string
  title: string
  fullPath: string
  closable: boolean
}

const HOME_TAB: LayoutTab = {
  key: '/dashboard',
  title: '首页',
  fullPath: '/dashboard',
  closable: false,
}

const appStore = useAppStore()
const route = useRoute()
const router = useRouter()
const tabs = ref<LayoutTab[]>([])

const activeTabKey = computed(() => route.path || HOME_TAB.key)

function resolveTabTitle(targetRoute: RouteLocationNormalizedLoaded) {
  const metaTitle = targetRoute.meta?.title
  if (typeof metaTitle === 'string' && metaTitle.trim()) {
    return metaTitle
  }

  if (typeof targetRoute.name === 'string' && targetRoute.name.trim()) {
    return targetRoute.name
  }

  return '未命名页面'
}

function buildTab(targetRoute: RouteLocationNormalizedLoaded): LayoutTab | null {
  if (!targetRoute.path) {
    return null
  }

  return {
    key: targetRoute.path,
    title: targetRoute.path === HOME_TAB.key ? HOME_TAB.title : resolveTabTitle(targetRoute),
    fullPath: targetRoute.fullPath || targetRoute.path,
    closable: targetRoute.path !== HOME_TAB.key,
  }
}

function ensureHomeTab() {
  if (!tabs.value.some(tab => tab.key === HOME_TAB.key)) {
    tabs.value.unshift({ ...HOME_TAB })
  }
}

function syncCurrentTab(targetRoute: RouteLocationNormalizedLoaded) {
  ensureHomeTab()

  const nextTab = buildTab(targetRoute)
  if (!nextTab) {
    return
  }

  const existingTab = tabs.value.find(tab => tab.key === nextTab.key)
  if (existingTab) {
    existingTab.title = nextTab.title
    existingTab.fullPath = nextTab.fullPath
    existingTab.closable = nextTab.closable
    return
  }

  tabs.value.push(nextTab)
}

function openTab(tab: LayoutTab) {
  if (tab.fullPath !== route.fullPath) {
    router.push(tab.fullPath)
  }
}

function closeTab(tabKey: string) {
  const tabIndex = tabs.value.findIndex(tab => tab.key === tabKey)
  const currentTab = tabIndex >= 0 ? tabs.value[tabIndex] : undefined

  if (!currentTab || !currentTab.closable) {
    return
  }

  const isCurrentTab = activeTabKey.value === tabKey
  tabs.value.splice(tabIndex, 1)

  if (!isCurrentTab) {
    return
  }

  const fallbackTab = tabs.value[tabIndex] || tabs.value[tabIndex - 1] || HOME_TAB
  router.push(fallbackTab.fullPath)
}

watch(
  () => route.fullPath,
  () => {
    syncCurrentTab(route)
  },
  { immediate: true }
)
</script>

<template>
  <div
    v-show="appStore.showTabs"
    class="app-tabs"
  >
    <div class="app-tabs__scroll">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="app-tab-item"
        :class="{ 'is-active': activeTabKey === tab.key }"
        @click="openTab(tab)"
      >
        <span class="app-tab-item__title">
          {{ tab.title }}
        </span>
        <button
          v-if="tab.closable"
          type="button"
          class="app-tab-item__close"
          @click.stop="closeTab(tab.key)"
        >
          <el-icon :size="12">
            <Close />
          </el-icon>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-tabs {
  position: relative;
  z-index: 20;
  flex-shrink: 0;
  padding: 0 18px 18px;
  background: transparent;
}

.app-tabs__scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 8px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.24);
  box-shadow: inset 0 0 0 1px var(--color-border);
  scrollbar-width: none;
}

.app-tabs__scroll::-webkit-scrollbar {
  display: none;
}

.app-tab-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 180px;
  padding: 0 12px;
  height: 38px;
  color: var(--color-text-secondary);
  background: var(--color-surface-container-high);
  border-radius: 16px 16px 12px 12px;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    color 0.2s ease,
    background-color 0.2s ease,
    box-shadow 0.2s ease;
}

.app-tab-item:hover {
  color: var(--color-primary);
  transform: translateY(-1px);
}

.app-tab-item.is-active {
  transform: translateY(-2px);
  color: var(--color-primary);
  background: var(--color-surface-container-lowest);
  box-shadow:
    0 12px 28px rgba(28, 41, 59, 0.08),
    inset 0 0 0 1px rgba(64, 158, 255, 0.14);
}

.app-tab-item__title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  line-height: 1;
}

.app-tab-item__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  padding: 0;
  color: currentColor;
  background: transparent;
  border: none;
  border-radius: 9999px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.app-tab-item__close:hover {
  background-color: var(--color-bg-hover);
}

@media (max-width: 960px) {
  .app-tabs {
    padding: 0 14px 14px;
  }
}
</style>
