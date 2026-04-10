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
  <div class="main-layout">
    <AppSidebar />

    <div class="layout-main">
      <AppHeader>
        <template #title>
          {{ pageTitle }}
        </template>
      </AppHeader>

      <div class="content-shell">
        <AppWatermark />
        <AppTabs />

        <main class="main-content">
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
  display: flex;
  width: 100%;
  height: 100vh;
  background: transparent;
}

.layout-main {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px 18px 18px 0;
}

.content-shell {
  position: relative;
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border-radius: 32px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.18), transparent 24%),
    var(--color-surface-container-low);
  box-shadow: inset 0 0 0 1px var(--color-border);
}

.main-content {
  position: relative;
  z-index: 10;
  flex: 1;
  overflow: auto;
  padding: 12px 12px 28px;
}

@media (max-width: 960px) {
  .layout-main {
    padding: 14px 14px 14px 0;
  }

  .content-shell {
    border-radius: 24px;
  }

  .main-content {
    padding: 10px 10px 22px;
  }
}
</style>
