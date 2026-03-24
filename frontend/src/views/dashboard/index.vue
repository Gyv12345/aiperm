<script setup lang="ts">
import {computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch} from 'vue'
import {dashboardApi} from '@/api/dashboard'
import {noticeApi, type NoticeVO} from '@/api/enterprise/notice'

// 统计数据（从后端获取）
const stats = shallowRef([
  {
    title: '用户总数',
    value: 0,
    icon: 'User',
    gradient: 'from-blue-500 to-blue-600',
    key: 'userCount'
  },
  {
    title: '角色数量',
    value: 0,
    icon: 'UserFilled',
    gradient: 'from-emerald-500 to-emerald-600',
    key: 'roleCount'
  },
  {
    title: '菜单数量',
    value: 0,
    icon: 'Menu',
    gradient: 'from-orange-500 to-orange-600',
    key: 'menuCount'
  },
  {
    title: '在线用户',
    value: 0,
    icon: 'Connection',
    gradient: 'from-violet-500 to-violet-600',
    key: 'onlineCount'
  },
])

// 加载状态
const loading = ref(false)
const announcements = ref<NoticeVO[]>([])
const announcementIndex = ref(0)
const noticeViewportRef = ref<HTMLElement>()
const noticeMeasureRef = ref<HTMLElement>()
const shouldMarqueeCurrentLine = ref(false)

const announcementLines = computed(() =>
  announcements.value
    .map((item) => {
      const title = item.title?.trim() || ''
      const content = item.content || ''
      if (!title) return content
      if (!content || content === title) return title
      return `${title}：${content}`
    })
    .filter(Boolean)
)
const currentAnnouncementLine = computed(() => {
  if (announcementLines.value.length === 0) return ''
  return announcementLines.value[announcementIndex.value] || announcementLines.value[0]
})
let announcementTimer: number | undefined
let announcementRotateTimer: number | undefined

const detectAnnouncementOverflow = () => {
  const viewport = noticeViewportRef.value
  const measure = noticeMeasureRef.value
  if (!viewport || !measure || !currentAnnouncementLine.value) {
    shouldMarqueeCurrentLine.value = false
    return
  }

  shouldMarqueeCurrentLine.value = measure.scrollWidth > viewport.clientWidth + 8
}

const startAnnouncementRotate = () => {
  if (announcementRotateTimer) {
    window.clearInterval(announcementRotateTimer)
    announcementRotateTimer = undefined
  }

  if (announcementLines.value.length <= 1) {
    announcementIndex.value = 0
    return
  }

  announcementRotateTimer = window.setInterval(() => {
    announcementIndex.value = (announcementIndex.value + 1) % announcementLines.value.length
  }, 5000)
}

// 系统信息
const systemInfo = shallowRef([
  { label: '系统版本', value: 'v1.0.0', icon: 'Document' },
  { label: '数据库', value: 'MySQL 8.0', icon: 'Coin' },
  { label: '缓存服务', value: 'Redis 7.x', icon: 'Timer' },
  { label: '认证框架', value: 'Sa-Token', icon: 'Key' },
])

// 后端安全特性
const backendSecurity = shallowRef([
  {
    name: 'Sa-Token 认证',
    desc: 'Token 存储在 Redis，支持分布式部署',
    icon: 'Key',
    color: 'red'
  },
  {
    name: 'BCrypt 密码加密',
    desc: '单向哈希加密，防止密码泄露',
    icon: 'Lock',
    color: 'amber'
  },
  {
    name: '图形验证码',
    desc: '防止暴力破解，5分钟有效期',
    icon: 'Picture',
    color: 'blue'
  },
  {
    name: '接口权限控制',
    desc: '@SaCheckPermission 细粒度权限',
    icon: 'Shield',
    color: 'emerald'
  },
  {
    name: '数据权限隔离',
    desc: '部门/个人数据权限自动过滤',
    icon: 'Filter',
    color: 'violet'
  },
  {
    name: '双因素认证 (2FA)',
    desc: 'TOTP 动态验证码，支持 Google Authenticator',
    icon: 'Iphone',
    color: 'orange'
  },
  {
    name: '操作日志审计',
    desc: '@Log 注解自动记录操作轨迹',
    icon: 'Document',
    color: 'sky'
  },
])

// 前端安全特性
const frontendSecurity = shallowRef([
  {
    name: '路由守卫',
    desc: 'beforeEach 拦截未授权访问',
    icon: 'Guide',
    color: 'blue'
  },
  {
    name: 'Token 管理',
    desc: '自动携带 Token，401 自动跳转登录',
    icon: 'Ticket',
    color: 'emerald'
  },
  {
    name: '权限指令',
    desc: 'v-permission 按钮级别权限控制',
    icon: 'Aim',
    color: 'orange'
  },
  {
    name: '动态路由',
    desc: '根据用户菜单权限动态生成路由',
    icon: 'Route',
    color: 'violet'
  },
])

// 前端技术栈
const frontendStack = shallowRef([
  { name: 'Vue 3.5', desc: '渐进式 JavaScript 框架', color: 'emerald' },
  { name: 'TypeScript 5.9', desc: '类型安全的 JavaScript', color: 'blue' },
  { name: 'Vite 7', desc: '下一代前端构建工具', color: 'purple' },
  { name: 'Element Plus', desc: 'Vue 3 组件库', color: 'sky' },
  { name: 'Pinia', desc: 'Vue 状态管理', color: 'amber' },
  { name: 'UnoCSS', desc: '原子化 CSS 引擎', color: 'rose' },
])

// 后端技术栈
const backendStack = shallowRef([
  { name: 'Spring Boot 4.0', desc: 'Java 微服务框架', color: 'green' },
  { name: 'Java 25', desc: '最新平台版本', color: 'orange' },
  { name: 'Sa-Token', desc: '轻量级权限认证框架', color: 'red' },
  { name: 'Spring Data JPA', desc: 'ORM 数据库操作', color: 'teal' },
  { name: 'MySQL 8.0', desc: '关系型数据库', color: 'blue' },
  { name: 'Redis 7.x', desc: '高性能缓存服务', color: 'red' },
])

// 动画状态
const isLoaded = ref(false)

// 获取统计数据
const fetchStats = async () => {
  loading.value = true
  try {
    const data = await dashboardApi.getStats()
    // 更新统计数据
    stats.value = stats.value.map(stat => ({
      ...stat,
      value: data[stat.key as keyof typeof data] || 0
    }))
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取首页公告（type=2）
const fetchAnnouncements = async () => {
  try {
    announcements.value = await noticeApi.feed(2, 10)
    if (announcementIndex.value >= announcementLines.value.length) {
      announcementIndex.value = 0
    }
    startAnnouncementRotate()
    await nextTick()
    detectAnnouncementOverflow()
  } catch (error) {
    console.error('获取公告失败:', error)
  }
}

watch(currentAnnouncementLine, async () => {
  await nextTick()
  detectAnnouncementOverflow()
})

onMounted(() => {
  // 获取统计数据
  fetchStats()
  fetchAnnouncements()
  announcementTimer = window.setInterval(fetchAnnouncements, 60_000)
  window.addEventListener('resize', detectAnnouncementOverflow)
  // 延迟触发动画
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})

onUnmounted(() => {
  if (announcementTimer) {
    window.clearInterval(announcementTimer)
  }
  if (announcementRotateTimer) {
    window.clearInterval(announcementRotateTimer)
  }
  window.removeEventListener('resize', detectAnnouncementOverflow)
})
</script>

<template>
  <div class="dashboard-page min-h-screen p-4 md:p-6">
    <!-- 装饰背景 -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-gradient-to-br from-blue-400/20 to-violet-400/20 dark:from-blue-600/10 dark:to-violet-600/10 rounded-full blur-3xl" />
      <div class="absolute top-1/2 -left-20 w-60 h-60 bg-gradient-to-br from-emerald-400/20 to-cyan-400/20 dark:from-emerald-600/10 dark:to-cyan-600/10 rounded-full blur-3xl" />
      <div class="absolute -bottom-20 right-1/4 w-72 h-72 bg-gradient-to-br from-orange-400/15 to-rose-400/15 dark:from-orange-600/10 dark:to-rose-600/10 rounded-full blur-3xl" />
    </div>

    <div class="relative z-10 space-y-6 max-w-7xl mx-auto">
      <!-- 欢迎区域 -->
      <div
        class="welcome-section transition-all duration-700"
        :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-4'"
      >
        <div class="flex items-center justify-between flex-wrap gap-4">
          <div>
            <h1 class="text-2xl font-bold text-slate-800 dark:text-slate-100 tracking-tight">
              AIPerm RBAC 权限管理系统
            </h1>
            <p class="mt-1 text-slate-500 dark:text-slate-400">
              专业的企业级权限管理解决方案，安全、高效、灵活
            </p>
          </div>
          <div class="flex items-center gap-2 text-sm text-slate-400 dark:text-slate-500">
            <el-icon class="text-lg">
              <Clock />
            </el-icon>
            <span>{{ new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}</span>
          </div>
        </div>
      </div>

      <div
        v-if="currentAnnouncementLine"
        class="notice-panel rounded-2xl bg-white/95 dark:bg-slate-800/95 border border-slate-100 dark:border-slate-700 px-4 py-3 flex items-start gap-3 transition-all duration-700"
        :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-2'"
        :style="{ transitionDelay: '140ms' }"
      >
        <div class="notice-badge-inline flex items-center gap-1.5 text-sm font-medium">
          <el-icon><Bell /></el-icon>
          <span>公告</span>
        </div>
        <div
          ref="noticeViewportRef"
          class="notice-content flex-1"
        >
          <transition
            name="notice-slide-up"
            mode="out-in"
          >
            <div
              :key="`${announcementIndex}-${currentAnnouncementLine}`"
              class="notice-line-wrapper"
            >
              <template v-if="shouldMarqueeCurrentLine">
                <div class="notice-marquee-track">
                  <span class="notice-line notice-line--nowrap">{{ currentAnnouncementLine }}</span>
                  <span class="notice-marquee-gap" />
                  <span class="notice-line notice-line--nowrap">{{ currentAnnouncementLine }}</span>
                  <span class="notice-marquee-gap" />
                </div>
              </template>
              <template v-else>
                <div class="notice-line">
                  {{ currentAnnouncementLine }}
                </div>
              </template>
            </div>
          </transition>
          <span
            ref="noticeMeasureRef"
            class="notice-line notice-line--measure"
          >
            {{ currentAnnouncementLine }}
          </span>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div
          v-for="(stat, index) in stats"
          :key="stat.title"
          class="stat-card group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 p-5 transition-all duration-500 cursor-pointer hover:border-slate-200 dark:hover:border-slate-600 hover:shadow-lg hover:shadow-slate-200/50 dark:hover:shadow-slate-900/50"
          :style="{ transitionDelay: `${index * 80}ms` }"
          :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'"
        >
          <!-- 悬停渐变背景 -->
          <div
            class="absolute inset-0 opacity-0 group-hover:opacity-5 dark:group-hover:opacity-10 transition-opacity duration-300"
            :class="`bg-gradient-to-br ${stat.gradient}`"
          />

          <div class="relative flex items-start justify-between">
            <div class="flex-1">
              <p class="text-sm font-medium text-slate-500 dark:text-slate-400">
                {{ stat.title }}
              </p>
              <div class="mt-2 flex items-baseline gap-2">
                <span class="text-3xl font-bold text-slate-800 dark:text-slate-100 tracking-tight">
                  {{ stat.value }}
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

      <!-- 安全特性区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 后端安全特性 -->
        <div
          class="security-card rounded-2xl bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 overflow-hidden transition-all duration-700 hover:shadow-lg hover:shadow-slate-200/50 dark:hover:shadow-slate-900/50"
          :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'"
          :style="{ transitionDelay: '400ms' }"
        >
          <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 bg-gradient-to-r from-emerald-50/50 to-teal-50/50 dark:from-slate-800/50 dark:to-slate-800/50">
            <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-100 flex items-center gap-2">
              <el-icon class="text-emerald-500">
                <Shield />
              </el-icon>
              后端安全特性
            </h2>
            <p class="text-sm text-slate-500 dark:text-slate-400 mt-0.5">
              多层次安全防护，保障系统安全
            </p>
          </div>
          <div class="p-5">
            <div class="grid grid-cols-2 gap-3">
              <div
                v-for="item in backendSecurity"
                :key="item.name"
                class="security-item group p-3 rounded-xl border border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-700/50 hover:border-emerald-200 dark:hover:border-emerald-700 hover:bg-emerald-50/30 dark:hover:bg-emerald-900/20 transition-all duration-300 cursor-default"
              >
                <div class="flex items-center gap-2 mb-1.5">
                  <div
                    class="w-7 h-7 rounded-lg flex items-center justify-center"
                    :class="`bg-${item.color}-100 dark:bg-${item.color}-900/30`"
                  >
                    <el-icon
                      class="text-sm"
                      :class="`text-${item.color}-500`"
                    >
                      <component :is="item.icon" />
                    </el-icon>
                  </div>
                  <span class="font-medium text-slate-800 dark:text-slate-100 text-sm">
                    {{ item.name }}
                  </span>
                </div>
                <p class="text-xs text-slate-500 dark:text-slate-400 pl-9">
                  {{ item.desc }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- 前端安全特性 -->
        <div
          class="security-card rounded-2xl bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 overflow-hidden transition-all duration-700 hover:shadow-lg hover:shadow-slate-200/50 dark:hover:shadow-slate-900/50"
          :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'"
          :style="{ transitionDelay: '480ms' }"
        >
          <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 bg-gradient-to-r from-blue-50/50 to-indigo-50/50 dark:from-slate-800/50 dark:to-slate-800/50">
            <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-100 flex items-center gap-2">
              <el-icon class="text-blue-500">
                <Monitor />
              </el-icon>
              前端安全特性
            </h2>
            <p class="text-sm text-slate-500 dark:text-slate-400 mt-0.5">
              前端多层次权限控制
            </p>
          </div>
          <div class="p-5">
            <div class="grid grid-cols-2 gap-3">
              <div
                v-for="item in frontendSecurity"
                :key="item.name"
                class="security-item group p-3 rounded-xl border border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-700/50 hover:border-blue-200 dark:hover:border-blue-700 hover:bg-blue-50/30 dark:hover:bg-blue-900/20 transition-all duration-300 cursor-default"
              >
                <div class="flex items-center gap-2 mb-1.5">
                  <div
                    class="w-7 h-7 rounded-lg flex items-center justify-center"
                    :class="`bg-${item.color}-100 dark:bg-${item.color}-900/30`"
                  >
                    <el-icon
                      class="text-sm"
                      :class="`text-${item.color}-500`"
                    >
                      <component :is="item.icon" />
                    </el-icon>
                  </div>
                  <span class="font-medium text-slate-800 dark:text-slate-100 text-sm">
                    {{ item.name }}
                  </span>
                </div>
                <p class="text-xs text-slate-500 dark:text-slate-400 pl-9">
                  {{ item.desc }}
                </p>
              </div>
            </div>

            <!-- 权限指令示例 -->
            <div class="mt-4 p-3 rounded-lg bg-slate-100/50 dark:bg-slate-700/50 border border-slate-200 dark:border-slate-600">
              <p class="text-xs text-slate-500 dark:text-slate-400 mb-2">
                权限指令示例：
              </p>
              <code class="text-xs text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-900/30 px-2 py-1 rounded">
                v-permission="'system:user:create'"
              </code>
            </div>
          </div>
        </div>
      </div>

      <!-- 技术栈区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 前端技术栈 -->
        <div
          class="rounded-2xl bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 overflow-hidden transition-all duration-700 hover:shadow-lg hover:shadow-slate-200/50 dark:hover:shadow-slate-900/50"
          :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'"
          :style="{ transitionDelay: '560ms' }"
        >
          <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/50">
            <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-100 flex items-center gap-2">
              <el-icon class="text-blue-500">
                <Monitor />
              </el-icon>
              前端技术栈
            </h2>
          </div>
          <div class="p-5">
            <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
              <div
                v-for="tech in frontendStack"
                :key="tech.name"
                class="tech-card group p-3 rounded-xl border border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-700/50 hover:border-slate-200 dark:hover:border-slate-600 hover:bg-white dark:hover:bg-slate-700 transition-all duration-300 cursor-default"
              >
                <div class="flex items-center gap-2 mb-1">
                  <div
                    class="w-2 h-2 rounded-full"
                    :class="`bg-${tech.color}-500`"
                  />
                  <span class="font-medium text-slate-800 dark:text-slate-100 text-sm">
                    {{ tech.name }}
                  </span>
                </div>
                <p class="text-xs text-slate-500 dark:text-slate-400 pl-4">
                  {{ tech.desc }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- 后端技术栈 -->
        <div
          class="rounded-2xl bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 overflow-hidden transition-all duration-700 hover:shadow-lg hover:shadow-slate-200/50 dark:hover:shadow-slate-900/50"
          :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'"
          :style="{ transitionDelay: '640ms' }"
        >
          <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/50">
            <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-100 flex items-center gap-2">
              <el-icon class="text-orange-500">
                <SetUp />
              </el-icon>
              后端技术栈
            </h2>
          </div>
          <div class="p-5">
            <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
              <div
                v-for="tech in backendStack"
                :key="tech.name"
                class="tech-card group p-3 rounded-xl border border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-700/50 hover:border-slate-200 dark:hover:border-slate-600 hover:bg-white dark:hover:bg-slate-700 transition-all duration-300 cursor-default"
              >
                <div class="flex items-center gap-2 mb-1">
                  <div
                    class="w-2 h-2 rounded-full"
                    :class="`bg-${tech.color}-500`"
                  />
                  <span class="font-medium text-slate-800 dark:text-slate-100 text-sm">
                    {{ tech.name }}
                  </span>
                </div>
                <p class="text-xs text-slate-500 dark:text-slate-400 pl-4">
                  {{ tech.desc }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 系统信息 -->
      <div
        class="rounded-2xl bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 overflow-hidden transition-all duration-700 hover:shadow-lg hover:shadow-slate-200/50 dark:hover:shadow-slate-900/50"
        :class="isLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'"
        :style="{ transitionDelay: '720ms' }"
      >
        <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/50">
          <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-100 flex items-center gap-2">
            <el-icon class="text-violet-500">
              <InfoFilled />
            </el-icon>
            系统信息
          </h2>
        </div>
        <div class="p-5">
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div
              v-for="info in systemInfo"
              :key="info.label"
              class="flex items-center gap-3 p-3 rounded-xl bg-slate-50/50 dark:bg-slate-700/50"
            >
              <div class="w-10 h-10 rounded-lg bg-slate-100 dark:bg-slate-600 flex items-center justify-center">
                <el-icon class="text-slate-500 dark:text-slate-300 text-lg">
                  <component :is="info.icon" />
                </el-icon>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-xs text-slate-400 dark:text-slate-500">
                  {{ info.label }}
                </p>
                <p class="font-medium text-slate-700 dark:text-slate-200">
                  {{ info.value }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部版权 -->
      <div
        class="text-center py-4 text-sm text-slate-400 dark:text-slate-500 transition-all duration-700"
        :class="isLoaded ? 'opacity-100' : 'opacity-0'"
        :style="{ transitionDelay: '800ms' }"
      >
        <p>
          Powered by
          <span class="text-blue-500 dark:text-blue-400 font-medium">AIPerm</span>
          · © 2024 河南爱编程网络科技有限公司
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  min-height: calc(100vh - 64px);
}

/* 统计卡片悬停效果 */
.stat-card {
  transform-origin: center;
}

.stat-card:hover {
  transform: translateY(-4px);
}

/* 技术卡片悬停效果 */
.tech-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px -2px rgba(0, 0, 0, 0.1);
}

/* 安全特性卡片悬停效果 */
.security-item:hover {
  transform: translateY(-1px);
}

.notice-badge-inline {
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.notice-content {
  position: relative;
  overflow: hidden;
}

.notice-line-wrapper {
  overflow: hidden;
}

.notice-line {
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.notice-line--nowrap {
  white-space: nowrap;
}

.notice-line--measure {
  position: absolute;
  inset-inline-start: 0;
  inset-block-start: 0;
  visibility: hidden;
  pointer-events: none;
  white-space: nowrap;
}

.notice-marquee-track {
  display: inline-flex;
  align-items: center;
  min-width: max-content;
  white-space: nowrap;
  animation: notice-marquee 16s linear infinite;
}

.notice-marquee-gap {
  width: 3rem;
  flex-shrink: 0;
}

@keyframes notice-marquee {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

.notice-slide-up-enter-active,
.notice-slide-up-leave-active {
  transition: all 0.35s ease;
}

.notice-slide-up-enter-from {
  opacity: 0;
  transform: translateY(18px);
}

.notice-slide-up-enter-to {
  opacity: 1;
  transform: translateY(0);
}

.notice-slide-up-leave-from {
  opacity: 1;
  transform: translateY(0);
}

.notice-slide-up-leave-to {
  opacity: 0;
  transform: translateY(-18px);
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .stat-card,
  .tech-card,
  .security-card,
  .welcome-section {
    transition: none !important;
  }

  .stat-card:hover,
  .tech-card:hover,
  .security-item:hover {
    transform: none;
  }

  .notice-marquee-track {
    animation: none;
  }

}
</style>
