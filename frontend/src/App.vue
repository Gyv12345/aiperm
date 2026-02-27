<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterView } from 'vue-router'
import { useAppStore } from '@/stores/app'
import MfaVerifyDialog from '@/components/mfa/MfaVerifyDialog.vue'

const appStore = useAppStore()
const mfaDialogVisible = ref(false)

// 处理 2FA 验证请求
function handleMfaRequired() {
  mfaDialogVisible.value = true
}

// 应用初始化时恢复主题
onMounted(() => {
  // 恢复保存的主题状态
  appStore.updateThemeClass()
  // 监听 2FA 验证请求事件
  window.addEventListener('mfa-required', handleMfaRequired)
})

onUnmounted(() => {
  window.removeEventListener('mfa-required', handleMfaRequired)
})
</script>

<template>
  <RouterView />
  <!-- 全局 2FA 验证弹窗 -->
  <MfaVerifyDialog
    v-model:visible="mfaDialogVisible"
    @success="mfaDialogVisible = false"
  />
</template>

<style>
#app {
  width: 100%;
  height: 100vh;
}
</style>
