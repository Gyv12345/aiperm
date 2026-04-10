<script setup lang="ts">
import {computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch} from 'vue'
import {useRouter} from 'vue-router'
import {useAppStore} from '@/stores/app'
import {dashboardApi} from '@/api/dashboard'
import {noticeApi, type NoticeVO} from '@/api/enterprise/notice'

const router = useRouter()
const appStore = useAppStore()

const stats = shallowRef([
  {
    title: '用户总数',
    eyebrow: 'Identity footprint',
    value: 0,
    icon: 'User',
    key: 'userCount',
    accent: '#0060a9',
    tone: 'linear-gradient(180deg, rgba(0, 96, 169, 0.14) 0%, rgba(0, 96, 169, 0) 100%)',
    soft: 'rgba(0, 96, 169, 0.12)',
  },
  {
    title: '角色数量',
    eyebrow: 'Authorization fabric',
    value: 0,
    icon: 'UserFilled',
    key: 'roleCount',
    accent: '#1f8f6a',
    tone: 'linear-gradient(180deg, rgba(31, 143, 106, 0.14) 0%, rgba(31, 143, 106, 0) 100%)',
    soft: 'rgba(31, 143, 106, 0.12)',
  },
  {
    title: '菜单数量',
    eyebrow: 'Navigation topology',
    value: 0,
    icon: 'Menu',
    key: 'menuCount',
    accent: '#c77720',
    tone: 'linear-gradient(180deg, rgba(199, 119, 32, 0.14) 0%, rgba(199, 119, 32, 0) 100%)',
    soft: 'rgba(199, 119, 32, 0.12)',
  },
  {
    title: '在线用户',
    eyebrow: 'Realtime presence',
    value: 0,
    icon: 'Connection',
    key: 'onlineCount',
    accent: '#6a57cf',
    tone: 'linear-gradient(180deg, rgba(106, 87, 207, 0.16) 0%, rgba(106, 87, 207, 0) 100%)',
    soft: 'rgba(106, 87, 207, 0.12)',
  },
])

const systemInfo = shallowRef([
  { label: '系统版本', value: 'v1.0.0', icon: 'Document', accent: '#0060a9', soft: 'rgba(0, 96, 169, 0.12)' },
  { label: '数据库', value: 'MySQL 8.0', icon: 'Coin', accent: '#1f8f6a', soft: 'rgba(31, 143, 106, 0.12)' },
  { label: '缓存服务', value: 'Redis 7.x', icon: 'Timer', accent: '#c77720', soft: 'rgba(199, 119, 32, 0.12)' },
  { label: '认证框架', value: 'Sa-Token', icon: 'Key', accent: '#6a57cf', soft: 'rgba(106, 87, 207, 0.12)' },
])

const backendSecurity = shallowRef([
  {
    name: 'Sa-Token 认证',
    desc: 'Token 存储在 Redis，支持分布式部署',
    icon: 'Key',
    accent: '#0060a9',
    soft: 'rgba(0, 96, 169, 0.12)',
  },
  {
    name: 'BCrypt 密码加密',
    desc: '单向哈希加密，降低凭据泄露风险',
    icon: 'Lock',
    accent: '#1f8f6a',
    soft: 'rgba(31, 143, 106, 0.12)',
  },
  {
    name: '图形验证码',
    desc: '防止暴力破解，5 分钟有效',
    icon: 'Picture',
    accent: '#c77720',
    soft: 'rgba(199, 119, 32, 0.12)',
  },
  {
    name: '接口权限控制',
    desc: '@SaCheckPermission 细粒度收口',
    icon: 'Shield',
    accent: '#6a57cf',
    soft: 'rgba(106, 87, 207, 0.12)',
  },
  {
    name: '数据权限隔离',
    desc: '部门与个人范围自动过滤',
    icon: 'Filter',
    accent: '#c45d84',
    soft: 'rgba(196, 93, 132, 0.12)',
  },
  {
    name: '双因素认证',
    desc: 'TOTP 动态验证码，支持 Google Authenticator',
    icon: 'Iphone',
    accent: '#b66d1a',
    soft: 'rgba(182, 109, 26, 0.12)',
  },
])

const frontendSecurity = shallowRef([
  {
    name: '路由守卫',
    desc: 'beforeEach 阻断未授权访问',
    icon: 'Guide',
    accent: '#0060a9',
    soft: 'rgba(0, 96, 169, 0.12)',
  },
  {
    name: 'Token 管理',
    desc: '自动注入 Token，401 自动回退登录',
    icon: 'Ticket',
    accent: '#1f8f6a',
    soft: 'rgba(31, 143, 106, 0.12)',
  },
  {
    name: '权限指令',
    desc: 'v-permission 控制按钮与入口级操作',
    icon: 'Aim',
    accent: '#c77720',
    soft: 'rgba(199, 119, 32, 0.12)',
  },
  {
    name: '动态路由',
    desc: '根据菜单权限构造运行时导航图',
    icon: 'Route',
    accent: '#6a57cf',
    soft: 'rgba(106, 87, 207, 0.12)',
  },
])

const frontendStack = shallowRef([
  { name: 'Vue 3.5', desc: '渐进式 UI 运行时', accent: '#1f8f6a', soft: 'rgba(31, 143, 106, 0.12)' },
  { name: 'TypeScript 5.9', desc: '类型系统与编辑期约束', accent: '#0060a9', soft: 'rgba(0, 96, 169, 0.12)' },
  { name: 'Vite 7', desc: '轻量构建与极速反馈', accent: '#6a57cf', soft: 'rgba(106, 87, 207, 0.12)' },
  { name: 'Element Plus', desc: '组件基础层', accent: '#c77720', soft: 'rgba(199, 119, 32, 0.12)' },
  { name: 'Pinia', desc: '界面状态容器', accent: '#c45d84', soft: 'rgba(196, 93, 132, 0.12)' },
  { name: 'UnoCSS', desc: '原子化视觉编排', accent: '#3a86b4', soft: 'rgba(58, 134, 180, 0.12)' },
])

const backendStack = shallowRef([
  { name: 'Spring Boot 4.0', desc: '企业服务编排框架', accent: '#1f8f6a', soft: 'rgba(31, 143, 106, 0.12)' },
  { name: 'Java 25', desc: '平台运行时', accent: '#c77720', soft: 'rgba(199, 119, 32, 0.12)' },
  { name: 'Sa-Token', desc: '认证与权限基座', accent: '#0060a9', soft: 'rgba(0, 96, 169, 0.12)' },
  { name: 'Spring Data JPA', desc: '持久层与数据抽象', accent: '#6a57cf', soft: 'rgba(106, 87, 207, 0.12)' },
  { name: 'MySQL 8.0', desc: '关系型数据核心', accent: '#3a86b4', soft: 'rgba(58, 134, 180, 0.12)' },
  { name: 'Redis 7.x', desc: '缓存与会话加速', accent: '#c45d84', soft: 'rgba(196, 93, 132, 0.12)' },
])

const loading = ref(false)
const announcements = ref<NoticeVO[]>([])
const announcementIndex = ref(0)
const noticeViewportRef = ref<HTMLElement>()
const noticeMeasureRef = ref<HTMLElement>()
const shouldMarqueeCurrentLine = ref(false)
const isLoaded = ref(false)

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

const todayLabel = computed(() =>
  new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  }).format(new Date())
)

const heroMetrics = computed(() => [
  { label: '账户资产', value: stats.value[0]?.value ?? 0 },
  { label: '角色矩阵', value: stats.value[1]?.value ?? 0 },
  { label: '在线会话', value: stats.value[3]?.value ?? 0 },
])

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

const fetchStats = async () => {
  loading.value = true
  try {
    const data = await dashboardApi.getStats()
    stats.value = stats.value.map(stat => ({
      ...stat,
      value: data[stat.key as keyof typeof data] || 0,
    }))
  }
  catch (error) {
    console.error('获取统计数据失败:', error)
  }
  finally {
    loading.value = false
  }
}

const fetchAnnouncements = async () => {
  try {
    announcements.value = await noticeApi.feed(2, 10)
    if (announcementIndex.value >= announcementLines.value.length) {
      announcementIndex.value = 0
    }
    startAnnouncementRotate()
    await nextTick()
    detectAnnouncementOverflow()
  }
  catch (error) {
    console.error('获取公告失败:', error)
  }
}

function goToUserCenter() {
  router.push('/system/user')
}

function goToNoticeCenter() {
  router.push('/enterprise/notice')
}

watch(currentAnnouncementLine, async () => {
  await nextTick()
  detectAnnouncementOverflow()
})

onMounted(() => {
  fetchStats()
  fetchAnnouncements()
  announcementTimer = window.setInterval(fetchAnnouncements, 60_000)
  window.addEventListener('resize', detectAnnouncementOverflow)
  window.setTimeout(() => {
    isLoaded.value = true
  }, 120)
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
  <div
    class="dashboard-page"
    :class="{ 'dashboard-page--dark': appStore.isDark }"
  >
    <div class="page-shell dashboard-shell">
      <section
        class="dashboard-hero"
        :class="{ 'is-loaded': isLoaded }"
      >
        <div class="dashboard-hero__copy">
          <p class="page-kicker">
            Architectural Ledger
          </p>
          <h1 class="page-title dashboard-hero__title">
            权限结构总览
          </h1>
          <p class="page-subtitle dashboard-hero__subtitle">
            以更安静的层次查看用户、角色、菜单与在线态，让权限系统像一张可读的建筑图纸。
          </p>
          <div class="dashboard-hero__actions">
            <el-button
              type="primary"
              @click="goToUserCenter"
            >
              进入用户管理
            </el-button>
            <el-button @click="goToNoticeCenter">
              查看公告中心
            </el-button>
          </div>
        </div>

        <div class="dashboard-hero__panel">
          <span class="dashboard-hero__chip">{{ todayLabel }}</span>
          <div class="dashboard-hero__metrics">
            <div
              v-for="metric in heroMetrics"
              :key="metric.label"
              class="dashboard-hero__metric"
            >
              <span class="dashboard-hero__metric-label">{{ metric.label }}</span>
              <span class="dashboard-hero__metric-value">{{ metric.value }}</span>
            </div>
          </div>
          <p class="dashboard-hero__footnote">
            结构越清晰，权限边界越不容易失真。
          </p>
        </div>
      </section>

      <section
        v-if="currentAnnouncementLine"
        class="dashboard-notice"
        :class="{ 'is-loaded': isLoaded }"
      >
        <div class="dashboard-notice__label">
          <el-icon><Bell /></el-icon>
          <span>公告带</span>
        </div>
        <div
          ref="noticeViewportRef"
          class="dashboard-notice__viewport"
        >
          <transition
            name="notice-slide-up"
            mode="out-in"
          >
            <div
              :key="`${announcementIndex}-${currentAnnouncementLine}`"
              class="dashboard-notice__line-wrapper"
            >
              <template v-if="shouldMarqueeCurrentLine">
                <div class="dashboard-notice__marquee-track">
                  <span class="dashboard-notice__line dashboard-notice__line--nowrap">{{ currentAnnouncementLine }}</span>
                  <span class="dashboard-notice__marquee-gap" />
                  <span class="dashboard-notice__line dashboard-notice__line--nowrap">{{ currentAnnouncementLine }}</span>
                  <span class="dashboard-notice__marquee-gap" />
                </div>
              </template>
              <template v-else>
                <div class="dashboard-notice__line">
                  {{ currentAnnouncementLine }}
                </div>
              </template>
            </div>
          </transition>
          <span
            ref="noticeMeasureRef"
            class="dashboard-notice__line dashboard-notice__line--measure"
          >
            {{ currentAnnouncementLine }}
          </span>
        </div>
      </section>

      <section class="dashboard-stat-grid">
        <article
          v-for="(stat, index) in stats"
          :key="stat.title"
          class="stat-panel"
          :class="{ 'is-loaded': isLoaded }"
          :style="{ transitionDelay: `${index * 80}ms`, '--accent': stat.accent, '--soft': stat.soft, '--tone': stat.tone }"
        >
          <div class="stat-panel__tone" />
          <div class="stat-panel__header">
            <div class="stat-panel__icon">
              <el-icon>
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <span class="stat-panel__label">{{ stat.title }}</span>
          </div>
          <p class="stat-panel__eyebrow">
            {{ stat.eyebrow }}
          </p>
          <p class="stat-panel__value">
            {{ stat.value }}
          </p>
        </article>
      </section>

      <section class="dashboard-columns">
        <article
          class="dashboard-panel"
          :class="{ 'is-loaded': isLoaded }"
        >
          <header class="dashboard-panel__header">
            <div>
              <p class="page-kicker">
                Security Matrix
              </p>
              <h2 class="dashboard-panel__title">
                安全矩阵
              </h2>
            </div>
            <span class="dashboard-panel__caption">Backend & Frontend</span>
          </header>

          <div class="security-grid">
            <div class="security-column">
              <div class="security-column__title">
                后端防线
              </div>
              <div class="security-list">
                <article
                  v-for="item in backendSecurity"
                  :key="item.name"
                  class="security-entry"
                >
                  <div
                    class="security-entry__icon"
                    :style="{ color: item.accent, background: item.soft }"
                  >
                    <el-icon>
                      <component :is="item.icon" />
                    </el-icon>
                  </div>
                  <div class="security-entry__body">
                    <h3>{{ item.name }}</h3>
                    <p>{{ item.desc }}</p>
                  </div>
                </article>
              </div>
            </div>

            <div class="security-column">
              <div class="security-column__title">
                前端防线
              </div>
              <div class="security-list">
                <article
                  v-for="item in frontendSecurity"
                  :key="item.name"
                  class="security-entry"
                >
                  <div
                    class="security-entry__icon"
                    :style="{ color: item.accent, background: item.soft }"
                  >
                    <el-icon>
                      <component :is="item.icon" />
                    </el-icon>
                  </div>
                  <div class="security-entry__body">
                    <h3>{{ item.name }}</h3>
                    <p>{{ item.desc }}</p>
                  </div>
                </article>
              </div>
            </div>
          </div>
        </article>

        <article
          class="dashboard-panel"
          :class="{ 'is-loaded': isLoaded }"
        >
          <header class="dashboard-panel__header">
            <div>
              <p class="page-kicker">
                Platform Ledger
              </p>
              <h2 class="dashboard-panel__title">
                平台资产
              </h2>
            </div>
            <span class="dashboard-panel__caption">Topology & Stack</span>
          </header>

          <div class="info-grid">
            <article
              v-for="item in systemInfo"
              :key="item.label"
              class="info-card"
            >
              <div
                class="info-card__icon"
                :style="{ color: item.accent, background: item.soft }"
              >
                <el-icon>
                  <component :is="item.icon" />
                </el-icon>
              </div>
              <div>
                <p class="info-card__label">
                  {{ item.label }}
                </p>
                <p class="info-card__value">
                  {{ item.value }}
                </p>
              </div>
            </article>
          </div>

          <div class="stack-grid">
            <section class="stack-section">
              <div class="stack-section__title">
                Frontend
              </div>
              <div class="stack-list">
                <article
                  v-for="tech in frontendStack"
                  :key="tech.name"
                  class="stack-entry"
                >
                  <span
                    class="stack-entry__dot"
                    :style="{ background: tech.accent }"
                  />
                  <div class="stack-entry__body">
                    <h3>{{ tech.name }}</h3>
                    <p>{{ tech.desc }}</p>
                  </div>
                </article>
              </div>
            </section>

            <section class="stack-section">
              <div class="stack-section__title">
                Backend
              </div>
              <div class="stack-list">
                <article
                  v-for="tech in backendStack"
                  :key="tech.name"
                  class="stack-entry"
                >
                  <span
                    class="stack-entry__dot"
                    :style="{ background: tech.accent }"
                  />
                  <div class="stack-entry__body">
                    <h3>{{ tech.name }}</h3>
                    <p>{{ tech.desc }}</p>
                  </div>
                </article>
              </div>
            </section>
          </div>
        </article>
      </section>

      <footer
        class="dashboard-footer"
        :class="{ 'is-loaded': isLoaded }"
      >
        Powered by <span>AIPerm</span> · Structural silence for administrative clarity
      </footer>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  min-height: 100%;
}

.dashboard-shell {
  gap: 20px;
  padding: 8px;
}

.dashboard-hero,
.dashboard-notice,
.stat-panel,
.dashboard-panel,
.dashboard-footer {
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.55s ease, transform 0.55s ease;
}

.dashboard-hero.is-loaded,
.dashboard-notice.is-loaded,
.stat-panel.is-loaded,
.dashboard-panel.is-loaded {
  opacity: 1;
  transform: translateY(0);
}

.dashboard-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.9fr);
  gap: 22px;
  padding: 28px;
  border-radius: 32px;
  background:
    radial-gradient(circle at top right, rgba(64, 158, 255, 0.12), transparent 34%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.52), rgba(255, 255, 255, 0.22)),
    var(--color-surface-container-lowest);
  box-shadow: var(--shadow-md);
}

.dashboard-hero__copy {
  display: flex;
  flex-direction: column;
  gap: 14px;
  justify-content: center;
}

.dashboard-hero__title {
  max-width: 12ch;
}

.dashboard-hero__subtitle {
  max-width: 52ch;
}

.dashboard-hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding-top: 6px;
}

.dashboard-hero__panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 22px;
  border-radius: 26px;
  background: var(--color-surface-container-low);
}

.dashboard-hero__chip {
  display: inline-flex;
  align-self: flex-start;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.64);
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 600;
}

.dashboard-hero__metrics {
  display: grid;
  gap: 12px;
}

.dashboard-hero__metric {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.dashboard-hero__metric-label {
  color: var(--color-text-secondary);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.dashboard-hero__metric-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.dashboard-hero__footnote {
  margin-top: auto;
  color: var(--color-text-muted);
  font-size: 13px;
}

.dashboard-page--dark .dashboard-hero__panel {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.03), transparent 42%),
    rgba(14, 20, 26, 0.86);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.06);
}

.dashboard-page--dark .dashboard-hero__chip {
  background: rgba(255, 255, 255, 0.12);
  color: rgba(243, 246, 250, 0.92);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.08);
}

.dashboard-page--dark .dashboard-hero__metric-label {
  color: rgba(243, 246, 250, 0.82);
}

.dashboard-page--dark .dashboard-hero__metric-value {
  color: #f8fbff;
}

.dashboard-page--dark .dashboard-hero__footnote {
  color: rgba(243, 246, 250, 0.72);
}

.dashboard-notice {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 18px 22px;
  border-radius: 24px;
  background: var(--color-surface-container-lowest);
  box-shadow: var(--shadow-sm);
}

.dashboard-notice__label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.dashboard-notice__viewport {
  position: relative;
  flex: 1;
  overflow: hidden;
}

.dashboard-notice__line-wrapper {
  overflow: hidden;
}

.dashboard-notice__line {
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.dashboard-notice__line--nowrap {
  white-space: nowrap;
}

.dashboard-notice__line--measure {
  position: absolute;
  inset-inline-start: 0;
  inset-block-start: 0;
  visibility: hidden;
  pointer-events: none;
  white-space: nowrap;
}

.dashboard-notice__marquee-track {
  display: inline-flex;
  min-width: max-content;
  align-items: center;
  animation: notice-marquee 16s linear infinite;
}

.dashboard-notice__marquee-gap {
  width: 3rem;
  flex-shrink: 0;
}

.dashboard-stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.stat-panel {
  position: relative;
  overflow: hidden;
  padding: 22px;
  border-radius: 26px;
  background: var(--color-surface-container-lowest);
  box-shadow: var(--shadow-sm);
}

.stat-panel__tone {
  position: absolute;
  inset: 0;
  background: var(--tone);
  pointer-events: none;
}

.stat-panel__header,
.stat-panel__icon {
  position: relative;
  z-index: 1;
}

.stat-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.stat-panel__icon {
  display: inline-flex;
  width: 46px;
  height: 46px;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: var(--soft);
  color: var(--accent);
  font-size: 20px;
}

.stat-panel__label {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.stat-panel__eyebrow {
  position: relative;
  z-index: 1;
  margin-top: 18px;
  color: var(--color-text-muted);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.stat-panel__value {
  position: relative;
  z-index: 1;
  margin-top: 8px;
  color: var(--color-text-primary);
  font-size: 2rem;
  font-weight: 700;
  line-height: 1.1;
}

.dashboard-columns {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(0, 0.92fr);
  gap: 18px;
}

.dashboard-panel {
  padding: 24px;
  border-radius: 28px;
  background: var(--color-surface-container-lowest);
  box-shadow: var(--shadow-sm);
}

.dashboard-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 22px;
}

.dashboard-panel__title {
  margin-top: 4px;
  font-size: 1.25rem;
  color: var(--color-text-primary);
}

.dashboard-panel__caption {
  padding: 8px 12px;
  border-radius: 999px;
  background: var(--color-surface-container-low);
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 600;
}

.security-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.security-column {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.security-column__title,
.stack-section__title {
  color: var(--color-text-secondary);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.security-list,
.stack-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.security-entry,
.stack-entry,
.info-card {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 20px;
  background: var(--color-surface-container-low);
}

.security-entry__icon,
.info-card__icon {
  display: inline-flex;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  flex-shrink: 0;
}

.security-entry__body h3,
.stack-entry__body h3 {
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 600;
}

.security-entry__body p,
.stack-entry__body p,
.info-card__label {
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.55;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.info-card__value {
  margin-top: 4px;
  color: var(--color-text-primary);
  font-size: 15px;
  font-weight: 600;
}

.stack-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.stack-entry__dot {
  width: 10px;
  height: 10px;
  margin-top: 6px;
  border-radius: 999px;
  flex-shrink: 0;
}

.dashboard-footer {
  padding: 8px 4px 0;
  color: var(--color-text-muted);
  font-size: 13px;
  text-align: center;
}

.dashboard-footer span {
  color: var(--color-primary);
  font-weight: 700;
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

@media (max-width: 1200px) {
  .dashboard-stat-grid,
  .dashboard-columns,
  .security-grid,
  .stack-grid,
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-shell {
    gap: 16px;
    padding: 4px;
  }

  .dashboard-hero,
  .dashboard-notice,
  .dashboard-panel,
  .stat-panel {
    padding: 20px;
    border-radius: 24px;
  }

  .dashboard-stat-grid,
  .dashboard-columns,
  .security-grid,
  .stack-grid,
  .info-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-notice {
    flex-direction: column;
  }

  .dashboard-hero__panel {
    padding: 18px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .dashboard-hero,
  .dashboard-notice,
  .stat-panel,
  .dashboard-panel,
  .dashboard-footer {
    transition: none !important;
  }

  .dashboard-notice__marquee-track {
    animation: none;
  }
}
</style>
