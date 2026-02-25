<script setup lang="ts">
import { shallowRef } from 'vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppHeader from '@/components/layout/AppHeader.vue'

// 统计数据（使用 shallowRef 优化性能）
const stats = shallowRef([
  { title: '用户总数', value: 128, icon: 'User', color: 'bg-blue-500' },
  { title: '角色数量', value: 8, icon: 'UserFilled', color: 'bg-green-500' },
  { title: '权限数量', value: 56, icon: 'Lock', color: 'bg-orange-500' },
  { title: '在线用户', value: 23, icon: 'Connection', color: 'bg-purple-500' },
])
</script>

<template>
  <div class="dashboard-container flex h-screen">
    <!-- 侧边栏 -->
    <AppSidebar />

    <!-- 主内容区 -->
    <main class="flex-1 flex flex-col overflow-hidden">
      <!-- 顶部导航 -->
      <AppHeader>
        <template #title>
          仪表板
        </template>
      </AppHeader>

      <!-- 内容区 -->
      <div class="flex-1 p-6 bg-gray-50 overflow-y-auto">
        <!-- 统计卡片 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
          <el-card
            v-for="stat in stats"
            :key="stat.title"
            class="stat-card"
            shadow="hover"
          >
            <div class="flex items-center">
              <div
                class="stat-icon flex items-center justify-center w-12 h-12 rounded-lg text-white"
                :class="stat.color"
              >
                <el-icon class="text-2xl">
                  <component :is="stat.icon" />
                </el-icon>
              </div>
              <div class="ml-4">
                <p class="text-sm text-gray-500">
                  {{ stat.title }}
                </p>
                <p class="text-2xl font-bold text-gray-800">
                  {{ stat.value }}
                </p>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 欢迎卡片 -->
        <el-card class="mb-6">
          <template #header>
            <span class="text-lg font-semibold">欢迎使用 AIPerm RBAC 系统</span>
          </template>
          <p class="text-gray-600">
            这是一个基于 Vue 3 + TypeScript + Element Plus 的 RBAC 权限管理系统。
          </p>
          <div class="mt-4 flex flex-wrap gap-2">
            <el-tag type="primary">Vue 3.5</el-tag>
            <el-tag type="success">TypeScript 5.9</el-tag>
            <el-tag type="warning">Element Plus</el-tag>
            <el-tag type="info">Pinia</el-tag>
            <el-tag>Vite 7</el-tag>
          </div>
        </el-card>

        <!-- 功能模块 -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <el-card>
            <template #header>
              <span class="font-semibold">快速开始</span>
            </template>
            <el-space
              wrap
              :size="12"
            >
              <el-button type="primary">
                <el-icon class="mr-1"><Plus /></el-icon>
                新增用户
              </el-button>
              <el-button type="success">
                <el-icon class="mr-1"><Plus /></el-icon>
                新增角色
              </el-button>
              <el-button type="warning">
                <el-icon class="mr-1"><Setting /></el-icon>
                系统设置
              </el-button>
            </el-space>
          </el-card>

          <el-card>
            <template #header>
              <span class="font-semibold">系统信息</span>
            </template>
            <el-descriptions
              :column="1"
              border
            >
              <el-descriptions-item label="系统版本">
                v1.0.0
              </el-descriptions-item>
              <el-descriptions-item label="后端框架">
                Spring Boot 3.5
              </el-descriptions-item>
              <el-descriptions-item label="数据库">
                MySQL 8.0
              </el-descriptions-item>
              <el-descriptions-item label="缓存">
                Redis
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.stat-card {
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  flex-shrink: 0;
}
</style>
