<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function handleLogout() {
  localStorage.removeItem('access_token')
  router.push('/login')
}
</script>

<template>
  <div class="dashboard-container flex h-screen">
    <!-- 侧边栏 -->
    <aside class="w-64 bg-gray-800 text-white flex flex-col">
      <div class="h-16 flex items-center justify-center border-b border-gray-700">
        <h1 class="text-xl font-bold">AIPerm</h1>
      </div>

      <nav class="flex-1 p-4">
        <ul class="space-y-2">
          <li>
            <a href="#" class="block px-4 py-2 rounded hover:bg-gray-700">
              仪表板
            </a>
          </li>
          <li>
            <a href="#" class="block px-4 py-2 rounded hover:bg-gray-700">
              用户管理
            </a>
          </li>
          <li>
            <a href="#" class="block px-4 py-2 rounded hover:bg-gray-700">
              角色管理
            </a>
          </li>
          <li>
            <a href="#" class="block px-4 py-2 rounded hover:bg-gray-700">
              权限管理
            </a>
          </li>
        </ul>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <main class="flex-1 flex flex-col">
      <!-- 顶部导航 -->
      <header class="h-16 bg-white shadow flex items-center justify-between px-6">
        <h2 class="text-lg font-semibold">仪表板</h2>
        <el-button @click="handleLogout">
          退出登录
        </el-button>
      </header>

      <!-- 内容区 -->
      <div class="flex-1 p-6 bg-gray-50">
        <el-card>
          <template #header>
            <span class="text-lg font-semibold">欢迎使用 AIPerm RBAC 系统</span>
          </template>
          <p class="text-gray-600">
            这是一个基于 Vue3 + TypeScript + Element Plus 的 RBAC 权限管理系统。
          </p>
        </el-card>
      </div>
    </main>
  </div>
</template>

<style scoped>
a {
  text-decoration: none;
  color: inherit;
}
</style>
