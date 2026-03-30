<script setup lang="ts">
import {computed} from 'vue'
import {useAppStore} from '@/stores/app'
import {useUserStore} from '@/stores/user'

const appStore = useAppStore()
const userStore = useUserStore()

const watermarkContent = computed(() => {
  return userStore.userInfo?.nickname || userStore.username || 'AIPerm'
})

const watermarkFont = computed(() => {
  return {
    color: appStore.isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(15, 23, 42, 0.08)',
    fontSize: 15,
  }
})
</script>

<template>
  <div
    v-if="appStore.showWatermark"
    class="app-watermark"
  >
    <ElWatermark
      class="h-full w-full"
      :content="watermarkContent"
      :font="watermarkFont"
      :rotate="-20"
      :gap="[120, 120]"
      :offset="[40, 40]"
    >
      <div class="h-full w-full" />
    </ElWatermark>
  </div>
</template>

<style scoped>
.app-watermark {
  position: absolute;
  inset: 0;
  z-index: 15;
  overflow: hidden;
  pointer-events: none;
}
</style>
