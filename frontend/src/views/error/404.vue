<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="flex items-center justify-center h-screen bg-gray-100">
    <div class="text-center">
      <h1 class="text-9xl font-bold text-gray-300">404</h1>
      <p class="text-2xl text-gray-500 mt-4">页面未找到</p>
      <el-button type="primary" class="mt-6" @click="goHome">
        返回首页
      </el-button>
    </div>
  </div>
</template>
