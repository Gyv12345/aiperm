<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

// 应用初始化时恢复主题
onMounted(() => {
  appStore.toggleTheme() // 先切换确保生效
})
</script>

<template>
  <RouterView />
</template>

<style>
#app {
  width: 100%;
  height: 100vh;
}
</style>
