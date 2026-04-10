<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Download, Refresh, Search, View} from '@element-plus/icons-vue'
import {jobLogApi, type JobLogVO} from '@/api/monitor'
import type {PageResult} from '@/types'

const loading = ref(false)
const detailVisible = ref(false)
const detailData = ref<JobLogVO | null>(null)
const tableData = ref<JobLogVO[]>([])
const dateRange = ref<string[]>([])

const queryForm = reactive({
  jobName: '',
  triggerSource: undefined as string | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const statusOptions = [
  { value: 1, label: '成功' },
  { value: 0, label: '失败' },
]

const triggerOptions = [
  { value: 'MANUAL', label: '手动执行' },
  { value: 'SCHEDULE', label: '定时触发' },
]

function formatTrigger(triggerSource?: string) {
  return triggerOptions.find(item => item.value === triggerSource)?.label || triggerSource || '-'
}

async function fetchList() {
  loading.value = true
  try {
    const result = await jobLogApi.list({
      page: pagination.page,
      pageSize: pagination.pageSize,
      jobName: queryForm.jobName || undefined,
      triggerSource: queryForm.triggerSource,
      status: queryForm.status,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
    }) as PageResult<JobLogVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取任务日志失败:', error)
    ElMessage.error('获取任务日志失败')
  }
  finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchList()
}

function handleReset() {
  queryForm.jobName = ''
  queryForm.triggerSource = undefined
  queryForm.status = undefined
  dateRange.value = []
  pagination.page = 1
  fetchList()
}

async function handleExport() {
  try {
    await jobLogApi.export({
      jobName: queryForm.jobName || undefined,
      triggerSource: queryForm.triggerSource,
      status: queryForm.status,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
    })
  }
  catch (error) {
    console.error('导出任务日志失败:', error)
  }
}

async function handleDelete(row: JobLogVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除任务日志「${row.jobName} / ${row.startTime}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await jobLogApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除任务日志失败:', error)
      ElMessage.error('删除任务日志失败')
    }
  }
}

async function handleClean() {
  try {
    await ElMessageBox.confirm(
      '确定清空全部任务日志吗？此操作不可恢复。',
      '清空确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await jobLogApi.clean()
    ElMessage.success('已清空任务日志')
    fetchList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('清空任务日志失败:', error)
      ElMessage.error('清空任务日志失败')
    }
  }
}

function handleView(row: JobLogVO) {
  detailData.value = row
  detailVisible.value = true
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchList()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="p-4">
    <el-card class="mb-4">
      <el-form
        :model="queryForm"
        label-width="72px"
        class="grid-filter-form"
      >
        <el-row :gutter="12">
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <el-form-item label="任务名称">
              <el-input
                v-model="queryForm.jobName"
                placeholder="请输入任务名称"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <el-form-item label="触发方式">
              <el-select
                v-model="queryForm.triggerSource"
                placeholder="请选择触发方式"
                clearable
              >
                <el-option
                  v-for="item in triggerOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <el-form-item label="执行状态">
              <el-select
                v-model="queryForm.status"
                placeholder="请选择状态"
                clearable
              >
                <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="24"
            :md="12"
            :lg="6"
          >
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                value-format="YYYY-MM-DD"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                range-separator="至"
                unlink-panels
              />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="flex justify-end gap-2">
          <el-button
            type="primary"
            :icon="Search"
            @click="handleSearch"
          >
            搜索
          </el-button>
          <el-button
            :icon="Refresh"
            @click="handleReset"
          >
            重置
          </el-button>
        </div>
      </el-form>
    </el-card>

    <el-card>
      <div class="mb-4 flex items-center justify-between">
        <div class="text-sm text-gray-500">
          共 {{ pagination.total }} 条任务日志
        </div>
        <div class="flex items-center gap-2">
          <el-button
            :icon="Download"
            v-permission="'monitor:job-log:export'"
            @click="handleExport"
          >
            导出
          </el-button>
          <el-button
            type="danger"
            v-permission="'monitor:job-log:delete'"
            @click="handleClean"
          >
            清空
          </el-button>
          <el-button
            :icon="Refresh"
            circle
            @click="fetchList"
          />
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
      >
        <el-table-column
          prop="jobName"
          label="任务名称"
          min-width="140"
        />
        <el-table-column
          prop="jobGroup"
          label="任务分组"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          label="触发方式"
          width="110"
          align="center"
        >
          <template #default="{ row }">
            {{ formatTrigger(row.triggerSource) }}
          </template>
        </el-table-column>
        <el-table-column
          label="状态"
          width="90"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="message"
          label="执行结果"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column
          prop="startTime"
          label="开始时间"
          min-width="168"
        />
        <el-table-column
          prop="endTime"
          label="结束时间"
          min-width="168"
        />
        <el-table-column
          prop="costTime"
          label="耗时(ms)"
          width="100"
          align="center"
        />
        <el-table-column
          label="操作"
          width="150"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="View"
              @click="handleView(row)"
            >
              详情
            </el-button>
            <el-button
              type="danger"
              link
              :icon="Delete"
              v-permission="'monitor:job-log:delete'"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailVisible"
      title="任务日志详情"
      width="860px"
    >
      <div v-if="detailData">
        <el-descriptions
          :column="2"
          border
        >
          <el-descriptions-item label="任务名称">
            {{ detailData.jobName }}
          </el-descriptions-item>
          <el-descriptions-item label="任务分组">
            {{ detailData.jobGroup || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="执行目标" :span="2">
            {{ detailData.beanClass || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="触发方式">
            {{ formatTrigger(detailData.triggerSource) }}
          </el-descriptions-item>
          <el-descriptions-item label="执行状态">
            {{ detailData.status === 1 ? '成功' : '失败' }}
          </el-descriptions-item>
          <el-descriptions-item label="执行结果" :span="2">
            {{ detailData.message || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-card class="mt-4">
          <template #header>
            <div class="font-medium">
              异常详情
            </div>
          </template>
          <pre class="whitespace-pre-wrap break-all text-xs">{{ detailData.exceptionInfo || '-' }}</pre>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>
