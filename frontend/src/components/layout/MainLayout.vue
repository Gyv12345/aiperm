<script setup lang="ts">
import {computed} from 'vue'
import {useRoute} from 'vue-router'
import AppHeader from './AppHeader.vue'
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

      <!-- 内容区域 -->
      <main class="main-content flex-1 overflow-auto p-4">
        <router-view />
      </main>
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
  background-color: var(--color-bg-page);
  transition: background-color 0.3s ease;
}
</style>
