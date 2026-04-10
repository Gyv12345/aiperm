<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Refresh} from '@element-plus/icons-vue'
import {serverMonitorApi, type ServerMonitorVO} from '@/api/monitor'

const loading = ref(false)
const overview = ref<ServerMonitorVO | null>(null)

function formatPercent(value?: number) {
  return `${Math.round((value ?? 0) * 100)}%`
}

function toPercent(value?: number) {
  return Math.min(100, Math.max(0, Math.round((value ?? 0) * 100)))
}

function formatBytes(value?: number) {
  const size = value ?? 0
  if (size <= 0) {
    return '0 B'
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let current = size
  let index = 0
  while (current >= 1024 && index < units.length - 1) {
    current /= 1024
    index++
  }
  return `${current.toFixed(current >= 10 || index === 0 ? 0 : 1)} ${units[index]}`
}

function formatUptime(value?: number) {
  const totalSeconds = Math.floor((value ?? 0) / 1000)
  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor((totalSeconds % 86400) / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  if (days > 0) {
    return `${days}天 ${hours}小时 ${minutes}分钟`
  }
  if (hours > 0) {
    return `${hours}小时 ${minutes}分钟`
  }
  return `${minutes}分钟`
}

const heapUsage = computed(() => {
  if (!overview.value?.heapMax) {
    return 0
  }
  return (overview.value.heapUsed / overview.value.heapMax) * 100
})

const nonHeapUsage = computed(() => {
  if (!overview.value?.nonHeapMax) {
    return 0
  }
  return (overview.value.nonHeapUsed / overview.value.nonHeapMax) * 100
})

const diskUsage = computed(() => {
  if (!overview.value?.diskTotal) {
    return 0
  }
  return ((overview.value.diskTotal - overview.value.diskUsable) / overview.value.diskTotal) * 100
})

async function fetchOverview() {
  loading.value = true
  try {
    overview.value = await serverMonitorApi.getOverview()
  }
  catch (error) {
    console.error('获取服务监控失败:', error)
    ElMessage.error('获取服务监控失败')
  }
  finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOverview()
})
</script>

<template>
  <div class="p-4">
    <div class="mb-4 flex items-center justify-between">
      <div>
        <div class="text-lg font-semibold">
          服务监控
        </div>
        <div class="text-sm text-gray-500">
          查看应用运行状态、资源占用和健康检查明细
        </div>
      </div>
      <el-button
        :icon="Refresh"
        v-permission="'monitor:server:list'"
        @click="fetchOverview"
      >
        刷新
      </el-button>
    </div>

    <div
      v-loading="loading"
      class="grid gap-4"
    >
      <el-row :gutter="16">
        <el-col
          :xs="24"
          :sm="12"
          :lg="6"
        >
          <el-card>
            <div class="text-sm text-gray-500">
              应用状态
            </div>
            <div class="mt-3 flex items-center gap-2">
              <el-tag :type="overview?.status === 'UP' ? 'success' : 'danger'">
                {{ overview?.status || '-' }}
              </el-tag>
              <span class="text-sm">{{ overview?.appName || 'AIPerm' }}</span>
            </div>
          </el-card>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :lg="6"
        >
          <el-card>
            <div class="text-sm text-gray-500">
              运行时长
            </div>
            <div class="mt-3 text-xl font-semibold">
              {{ formatUptime(overview?.uptime) }}
            </div>
          </el-card>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :lg="6"
        >
          <el-card>
            <div class="text-sm text-gray-500">
              系统 CPU
            </div>
            <div class="mt-3 text-xl font-semibold">
              {{ formatPercent(overview?.systemCpuUsage) }}
            </div>
            <el-progress
              class="mt-3"
              :percentage="toPercent(overview?.systemCpuUsage)"
              :stroke-width="8"
            />
          </el-card>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :lg="6"
        >
          <el-card>
            <div class="text-sm text-gray-500">
              进程 CPU
            </div>
            <div class="mt-3 text-xl font-semibold">
              {{ formatPercent(overview?.processCpuUsage) }}
            </div>
            <el-progress
              class="mt-3"
              :percentage="toPercent(overview?.processCpuUsage)"
              :stroke-width="8"
              status="success"
            />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col
          :xs="24"
          :lg="14"
        >
          <el-card>
            <template #header>
              <div class="font-medium">
                资源使用
              </div>
            </template>
            <div class="grid gap-4">
              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span>堆内存</span>
                  <span>{{ formatBytes(overview?.heapUsed) }} / {{ formatBytes(overview?.heapMax) }}</span>
                </div>
                <el-progress
                  :percentage="Math.round(heapUsage)"
                  :stroke-width="10"
                />
              </div>
              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span>非堆内存</span>
                  <span>{{ formatBytes(overview?.nonHeapUsed) }} / {{ formatBytes(overview?.nonHeapMax) }}</span>
                </div>
                <el-progress
                  :percentage="Math.round(nonHeapUsage)"
                  :stroke-width="10"
                  status="success"
                />
              </div>
              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span>磁盘使用</span>
                  <span>{{ formatBytes((overview?.diskTotal ?? 0) - (overview?.diskUsable ?? 0)) }} / {{ formatBytes(overview?.diskTotal) }}</span>
                </div>
                <el-progress
                  :percentage="Math.round(diskUsage)"
                  :stroke-width="10"
                  status="warning"
                />
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col
          :xs="24"
          :lg="10"
        >
          <el-card>
            <template #header>
              <div class="font-medium">
                运行环境
              </div>
            </template>
            <el-descriptions
              :column="1"
              border
            >
              <el-descriptions-item label="环境">
                {{ overview?.activeProfiles?.join(', ') || 'default' }}
              </el-descriptions-item>
              <el-descriptions-item label="Java">
                {{ overview?.javaVersion || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="操作系统">
                {{ overview?.osName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="CPU 核数">
                {{ overview?.processors ?? 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="线程">
                活动 {{ overview?.liveThreads ?? 0 }} / 守护 {{ overview?.daemonThreads ?? 0 }} / 峰值 {{ overview?.peakThreads ?? 0 }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-card>
        <template #header>
          <div class="font-medium">
            健康检查
          </div>
        </template>
        <el-table
          :data="overview?.healthComponents || []"
          empty-text="暂无健康检查数据"
        >
          <el-table-column
            prop="name"
            label="组件"
            min-width="160"
          />
          <el-table-column
            label="状态"
            width="120"
            align="center"
          >
            <template #default="{ row }">
              <el-tag :type="row.status === 'UP' ? 'success' : 'danger'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="details"
            label="详情"
            min-width="260"
            show-overflow-tooltip
          />
        </el-table>
      </el-card>
    </div>
  </div>
</template>
