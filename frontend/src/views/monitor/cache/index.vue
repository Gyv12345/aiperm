<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Refresh} from '@element-plus/icons-vue'
import {cacheMonitorApi, type CacheMonitorVO} from '@/api/monitor'

const loading = ref(false)
const overview = ref<CacheMonitorVO | null>(null)

function toPercent(value?: number) {
  return Math.min(100, Math.max(0, Math.round((value ?? 0) * 100)))
}

function formatPercent(value?: number) {
  return `${Math.round((value ?? 0) * 100)}%`
}

function formatTtl(value?: number) {
  const seconds = value ?? 0
  if (seconds <= 0) {
    return '永久/未知'
  }
  if (seconds < 60) {
    return `${seconds}s`
  }
  if (seconds < 3600) {
    return `${Math.floor(seconds / 60)}m`
  }
  return `${Math.floor(seconds / 3600)}h`
}

async function fetchOverview() {
  loading.value = true
  try {
    overview.value = await cacheMonitorApi.getOverview()
  }
  catch (error) {
    console.error('获取缓存监控失败:', error)
    ElMessage.error('获取缓存监控失败')
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
          缓存监控
        </div>
        <div class="text-sm text-gray-500">
          聚合 Redis 运行状态与业务缓存分布
        </div>
      </div>
      <el-button
        :icon="Refresh"
        v-permission="'monitor:cache:list'"
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
              Redis 已用内存
            </div>
            <div class="mt-3 text-2xl font-semibold">
              {{ overview?.usedMemoryHuman || '-' }}
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
              客户端连接
            </div>
            <div class="mt-3 text-2xl font-semibold">
              {{ overview?.connectedClients ?? 0 }}
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
              总 Key 数
            </div>
            <div class="mt-3 text-2xl font-semibold">
              {{ overview?.totalKeys ?? 0 }}
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
              命中率
            </div>
            <div class="mt-3 text-2xl font-semibold">
              {{ formatPercent(overview?.hitRate) }}
            </div>
            <el-progress
              class="mt-3"
              :percentage="toPercent(overview?.hitRate)"
              :stroke-width="8"
              status="success"
            />
          </el-card>
        </el-col>
      </el-row>

      <el-card>
        <template #header>
          <div class="font-medium">
            读写命中统计
          </div>
        </template>
        <el-row :gutter="16">
          <el-col
            :xs="24"
            :md="12"
          >
            <div class="rounded-lg border border-green-100 bg-green-50 p-4">
              <div class="text-sm text-green-700">
                命中次数
              </div>
              <div class="mt-2 text-2xl font-semibold text-green-800">
                {{ overview?.hits ?? 0 }}
              </div>
            </div>
          </el-col>
          <el-col
            :xs="24"
            :md="12"
          >
            <div class="rounded-lg border border-orange-100 bg-orange-50 p-4">
              <div class="text-sm text-orange-700">
                未命中次数
              </div>
              <div class="mt-2 text-2xl font-semibold text-orange-800">
                {{ overview?.misses ?? 0 }}
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-card>
        <template #header>
          <div class="font-medium">
            缓存分区
          </div>
        </template>
        <el-table
          :data="overview?.entries || []"
          empty-text="暂无缓存条目"
        >
          <el-table-column
            prop="cacheName"
            label="缓存名称"
            min-width="180"
          />
          <el-table-column
            prop="keyPrefix"
            label="Key 前缀"
            min-width="220"
            show-overflow-tooltip
          />
          <el-table-column
            prop="estimatedSize"
            label="估算数量"
            width="120"
            align="center"
          />
          <el-table-column
            label="样例 TTL"
            width="140"
            align="center"
          >
            <template #default="{ row }">
              {{ formatTtl(row.sampleTtl) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>
