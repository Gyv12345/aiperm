<script setup lang="ts">
import { shallowRef, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 统计数据（使用 shallowRef 优化性能）
const stats = shallowRef([
  {
    title: '用户总数',
    value: 128,
    icon: 'User',
    gradient: 'from-blue-500 to-blue-600',
    bgLight: 'bg-blue-50',
    trend: '+12%',
    trendUp: true
  },
  {
    title: '角色数量',
    value: 8,
    icon: 'UserFilled',
    gradient: 'from-emerald-500 to-emerald-600',
    bgLight: 'bg-emerald-50',
    trend: '+2',
    trendUp: true
  },
  {
    title: '权限数量',
    value: 56,
    icon: 'Lock',
    gradient: 'from-orange-500 to-orange-600',
    bgLight: 'bg-orange-50',
    trend: '+8',
    trendUp: true
  },
  {
    title: '在线用户',
    value: 23,
    icon: 'Connection',
    gradient: 'from-violet-500 to-violet-600',
    bgLight: 'bg-violet-50',
    trend: '实时',
    trendUp: true
  },
])

// 快捷操作
const quickActions = shallowRef([
  {
    title: '新增用户',
    description: '创建新的系统用户',
    icon: 'UserFilled',
    color: 'bg-blue-500',
    route: '/system/user'
  },
  {
    title: '新增角色',
    description: '配置角色权限',
    icon: 'Avatar',
    color: 'bg-emerald-500',
    route: '/system/role'
  },
  {
    title: '菜单管理',
    description: '配置系统菜单',
    icon: 'Menu',
    color: 'bg-orange-500',
    route: '/system/menu'
  },
  {
    title: '系统设置',
    description: '修改系统配置',
    icon: 'Setting',
    color: 'bg-violet-500',
    route: '/system/dict'
  },
])

// 系统信息
const systemInfo = shallowRef([
  { label: '系统版本', value: 'v1.0.0', icon: 'Document' },
  { label: '后端框架', value: 'Spring Boot 3.5', icon: 'SetUp' },
  { label: '数据库', value: 'MySQL 8.0', icon: 'Coin' },
  { label: '缓存服务', value: 'Redis', icon: 'Timer' },
])

// 技术栈标签
const techStack = shallowRef([
  { name: 'Vue 3.5', color: 'bg-emerald-100 text-emerald-700 border-emerald-200' },
  { name: 'TypeScript 5.9', color: 'bg-blue-100 text-blue-700 border-blue-200' },
  { name: 'Element Plus', color: 'bg-sky-100 text-sky-700 border-sky-200' },
  { name: 'Pinia', color: 'bg-amber-100 text-amber-700 border-amber-200' },
  { name: 'Vite 7', color: 'bg-purple-100 text-purple-700 border-purple-200' },
  { name: 'UnoCSS', color: 'bg-rose-100 text-rose-700 border-rose-200' },
])

// 动画状态
const isLoaded = ref(false)

onMounted(() => {
  // 延迟触发动画
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})

// 导航到指定页面
const navigateTo = (route: string) => {
  router.push(route)
}
</script>

<template>
  <div class="dashboard-page relative min-h-screen">
    <!-- 装饰背景 -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-gradient-to-br from-blue-400/20 to-violet-400/20 rounded-full blur-3xl" />
      <div class="absolute top-1/2 -left-20 w-60 h-60 bg-gradient-to-br from-emerald-400/20 to-cyan-400/20 rounded-full blur-3xl" />
      <div class="absolute -bottom-20 right-1/4 w-72 h-72 bg-gradient-to-br from-orange-400/15 to-rose-400/15 rounded-full blur-3xl" />
    </div>

    <div class="relative z-10 space-y-6">
      <!-- 欢迎区域 -->
      <div
        class="welcome-section transition-all duration-700"
        :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-4'"
      >
        <div class="flex items-center justify-between flex-wrap gap-4">
          <div>
            <h1 class="text-2xl font-bold text-slate-800 tracking-tight">
              欢迎使用 AIPerm RBAC 系统
            </h1>
            <p class="mt-1 text-slate-500">
              专业的企业级权限管理解决方案，安全、高效、灵活
            </p>
          </div>
          <div class="flex items-center gap-2 text-sm text-slate-400">
            <el-icon class="text-lg"><Clock /></el-icon>
            <span>{{ new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}</span>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div
          v-for="(stat, index) in stats"
          :key="stat.title"
          class="stat-card group relative overflow-hidden rounded-2xl bg-white border border-slate-100 p-5 transition-all duration-500 cursor-pointer hover:border-slate-200 hover:shadow-lg hover:shadow-slate-200/50"
          :style="{ transitionDelay: `${index * 80}ms` }"
          :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'"
        >
          <!-- 悬停渐变背景 -->
          <div
            class="absolute inset-0 opacity-0 group-hover:opacity-5 transition-opacity duration-300"
            :class="`bg-gradient-to-br ${stat.gradient}`"
          />

          <div class="relative flex items-start justify-between">
            <div class="flex-1">
              <p class="text-sm font-medium text-slate-500">
                {{ stat.title }}
              </p>
              <div class="mt-2 flex items-baseline gap-2">
                <span class="text-3xl font-bold text-slate-800 tracking-tight">
                  {{ stat.value }}
                </span>
                <span
                  class="inline-flex items-center text-xs font-medium px-2 py-0.5 rounded-full"
                  :class="stat.trendUp ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'"
                >
                  <el-icon class="mr-0.5 text-xs">
                    <component :is="stat.trendUp ? 'Top' : 'Bottom'" />
                  </el-icon>
                  {{ stat.trend }}
                </span>
              </div>
            </div>
            <div
              class="flex items-center justify-center w-12 h-12 rounded-xl text-white shadow-lg transition-transform duration-300 group-hover:scale-110"
              :class="`bg-gradient-to-br ${stat.gradient}`"
            >
              <el-icon class="text-xl">
                <component :is="stat.icon" />
              </el-icon>
            </div>
          </div>

          <!-- 底部进度条装饰 -->
          <div class="absolute bottom-0 left-0 right-0 h-1 overflow-hidden">
            <div
              class="h-full w-full transform -translate-x-full group-hover:translate-x-0 transition-transform duration-700 ease-out"
              :class="`bg-gradient-to-r ${stat.gradient}`"
            />
          </div>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <!-- 快捷操作 -->
        <div
          class="xl:col-span-2 rounded-2xl bg-white border border-slate-100 overflow-hidden transition-all duration-700"
          :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'"
          :style="{ transitionDelay: '400ms' }"
        >
          <div class="px-6 py-4 border-b border-slate-100 bg-slate-50/50">
            <h2 class="text-lg font-semibold text-slate-800 flex items-center gap-2">
              <el-icon class="text-blue-500"><Compass /></el-icon>
              快速开始
            </h2>
            <p class="text-sm text-slate-500 mt-0.5">常用功能快捷入口</p>
          </div>
          <div class="p-5">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div
                v-for="action in quickActions"
                :key="action.title"
                class="action-card group flex items-center gap-4 p-4 rounded-xl border border-slate-100 bg-slate-50/50 cursor-pointer transition-all duration-300 hover:border-slate-200 hover:bg-white hover:shadow-md"
                @click="navigateTo(action.route)"
              >
                <div
                  class="flex-shrink-0 w-11 h-11 rounded-xl flex items-center justify-center text-white shadow-md transition-transform duration-300 group-hover:scale-110"
                  :class="action.color"
                >
                  <el-icon class="text-xl">
                    <component :is="action.icon" />
                  </el-icon>
                </div>
                <div class="flex-1 min-w-0">
                  <h3 class="font-semibold text-slate-800 group-hover:text-blue-600 transition-colors">
                    {{ action.title }}
                  </h3>
                  <p class="text-sm text-slate-500 truncate">
                    {{ action.description }}
                  </p>
                </div>
                <el-icon class="text-slate-300 group-hover:text-blue-400 group-hover:translate-x-1 transition-all">
                  <ArrowRight />
                </el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 系统信息 -->
        <div
          class="rounded-2xl bg-white border border-slate-100 overflow-hidden transition-all duration-700"
          :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'"
          :style="{ transitionDelay: '480ms' }"
        >
          <div class="px-6 py-4 border-b border-slate-100 bg-slate-50/50">
            <h2 class="text-lg font-semibold text-slate-800 flex items-center gap-2">
              <el-icon class="text-violet-500"><Monitor /></el-icon>
              系统信息
            </h2>
            <p class="text-sm text-slate-500 mt-0.5">当前运行环境</p>
          </div>
          <div class="p-5 space-y-3">
            <div
              v-for="info in systemInfo"
              :key="info.label"
              class="flex items-center gap-3 p-3 rounded-xl bg-slate-50/50 hover:bg-slate-100/50 transition-colors"
            >
              <div class="w-9 h-9 rounded-lg bg-slate-100 flex items-center justify-center">
                <el-icon class="text-slate-500">
                  <component :is="info.icon" />
                </el-icon>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-xs text-slate-400">{{ info.label }}</p>
                <p class="font-medium text-slate-700">{{ info.value }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 技术栈展示 -->
      <div
        class="tech-stack-section rounded-2xl bg-gradient-to-r from-slate-800 to-slate-900 p-6 transition-all duration-700"
        :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'"
        :style="{ transitionDelay: '560ms' }"
      >
        <div class="flex items-center justify-between flex-wrap gap-4 mb-4">
          <div>
            <h2 class="text-lg font-semibold text-white flex items-center gap-2">
              <el-icon class="text-blue-400"><Cpu /></el-icon>
              技术栈
            </h2>
            <p class="text-sm text-slate-400 mt-0.5">采用现代化的前端技术构建</p>
          </div>
        </div>
        <div class="flex flex-wrap gap-2">
          <span
            v-for="tech in techStack"
            :key="tech.name"
            class="inline-flex items-center px-3 py-1.5 rounded-lg text-sm font-medium border transition-all duration-300 hover:scale-105 cursor-default"
            :class="tech.color"
          >
            {{ tech.name }}
          </span>
        </div>

        <!-- 底部装饰线 -->
        <div class="mt-6 pt-4 border-t border-slate-700/50 flex items-center justify-between text-sm">
          <span class="text-slate-400">
            Powered by
            <span class="text-blue-400 font-medium">AIPerm</span>
            Team
          </span>
          <span class="text-slate-500">
            © 2024 河南爱编程网络科技有限公司
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  padding: 0.5rem;
}

/* 统计卡片悬停效果 */
.stat-card {
  transform-origin: center;
}

.stat-card:hover {
  transform: translateY(-4px);
}

/* 快捷操作卡片悬停效果 */
.action-card {
  position: relative;
}

.action-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.action-card:hover::before {
  opacity: 1;
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .stat-card,
  .action-card,
  .welcome-section,
  .tech-stack-section {
    transition: none !important;
  }

  .stat-card:hover {
    transform: none;
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .stat-card {
    background: rgb(30 41 59);
    border-color: rgb(51 65 85);
  }

  .stat-card:hover {
    border-color: rgb(71 85 105);
    box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
  }
}
</style>
