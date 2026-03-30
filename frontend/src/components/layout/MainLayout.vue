<script setup lang="ts">
import {computed} from 'vue'
import {useRoute} from 'vue-router'
import AppHeader from './AppHeader.vue'
import AppTabs from './AppTabs.vue'
import AppWatermark from './AppWatermark.vue'
import SettingsPanel from './SettingsPanel.vue'
import AppSidebar from './AppSidebar.vue'
import FormPageAgent from '@/components/agent/FormPageAgent.vue'

const route = useRoute()

// 页面标题
const pageTitle = computed(() => {
  return route.meta?.title as string || '仪表板'
})
</script>

<template>
  <div class="main-layout flex h-screen">
    <!-- 侧边栏 -->
    <AppSidebar />

    <!-- 主内容区 -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- 顶部导航 -->
      <AppHeader>
        <template #title>
          {{ pageTitle }}
        </template>
      </AppHeader>

      <div class="content-shell relative flex flex-1 flex-col overflow-hidden">
        <AppWatermark />
        <AppTabs />

        <!-- 内容区域 -->
        <main class="main-content flex-1 overflow-auto p-4">
          <router-view />
        </main>
      </div>

      <SettingsPanel />
    </div>

    <FormPageAgent />
  </div>
</template>

<style scoped>
.main-layout {
  width: 100%;
  height: 100vh;
}

.main-content {
  position: relative;
  z-index: 10;
  background-color: var(--color-bg-page);
  transition: background-color 0.3s ease;
}
</style>
