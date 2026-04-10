<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Download, Refresh, Search, View} from '@element-plus/icons-vue'
import {operLogMonitorApi, type OperLogVO} from '@/api/monitor'
import type {PageResult} from '@/types'

const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const detailData = ref<OperLogVO | null>(null)
const tableData = ref<OperLogVO[]>([])
const dateRange = ref<string[]>([])

const queryForm = reactive({
  title: '',
  operUser: '',
  operIp: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const statusOptions = [
  { value: 0, label: '成功' },
  { value: 1, label: '失败' },
]

async function fetchList() {
  loading.value = true
  try {
    const result = await operLogMonitorApi.list({
      page: pagination.page,
      pageSize: pagination.pageSize,
      title: queryForm.title || undefined,
      operUser: queryForm.operUser || undefined,
      operIp: queryForm.operIp || undefined,
      status: queryForm.status,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
    }) as PageResult<OperLogVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取操作日志失败:', error)
    ElMessage.error('获取操作日志失败')
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
  queryForm.title = ''
  queryForm.operUser = ''
  queryForm.operIp = ''
  queryForm.status = undefined
  dateRange.value = []
  pagination.page = 1
  fetchList()
}

async function handleExport() {
  try {
    await operLogMonitorApi.export({
      title: queryForm.title || undefined,
      operUser: queryForm.operUser || undefined,
      operIp: queryForm.operIp || undefined,
      status: queryForm.status,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
    })
  }
  catch (error) {
    console.error('导出操作日志失败:', error)
  }
}

async function handleDelete(row: OperLogVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除操作日志「${row.title}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await operLogMonitorApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除操作日志失败:', error)
      ElMessage.error('删除操作日志失败')
    }
  }
}

async function handleClean() {
  try {
    await ElMessageBox.confirm(
      '确定清空全部操作日志吗？此操作不可恢复。',
      '清空确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await operLogMonitorApi.clean()
    ElMessage.success('已清空操作日志')
    fetchList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('清空操作日志失败:', error)
      ElMessage.error('清空操作日志失败')
    }
  }
}

async function handleView(row: OperLogVO) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailData.value = await operLogMonitorApi.getById(row.id)
  }
  catch (error) {
    console.error('获取操作日志详情失败:', error)
    ElMessage.error('获取日志详情失败')
  }
  finally {
    detailLoading.value = false
  }
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
            <el-form-item label="模块名称">
              <el-input
                v-model="queryForm.title"
                placeholder="请输入模块名称"
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
            <el-form-item label="操作人">
              <el-input
                v-model="queryForm.operUser"
                placeholder="请输入操作人"
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
            <el-form-item label="操作 IP">
              <el-input
                v-model="queryForm.operIp"
                placeholder="请输入操作 IP"
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
            <el-form-item label="状态">
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
            :lg="8"
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
          共 {{ pagination.total }} 条操作日志
        </div>
        <div class="flex items-center gap-2">
          <el-button
            :icon="Download"
            v-permission="'log:oper:export'"
            @click="handleExport"
          >
            导出
          </el-button>
          <el-button
            type="danger"
            v-permission="'log:oper:delete'"
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
          prop="title"
          label="模块名称"
          min-width="140"
        />
        <el-table-column
          prop="operUser"
          label="操作人"
          min-width="110"
        />
        <el-table-column
          prop="requestMethod"
          label="请求方法"
          width="110"
          align="center"
        />
        <el-table-column
          prop="operUrl"
          label="请求地址"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column
          prop="operIp"
          label="操作 IP"
          min-width="140"
        />
        <el-table-column
          label="状态"
          width="90"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="costTime"
          label="耗时(ms)"
          width="100"
          align="center"
        />
        <el-table-column
          prop="createTime"
          label="操作时间"
          min-width="168"
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
              v-permission="'log:oper:delete'"
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
      title="操作日志详情"
      width="860px"
    >
      <div v-loading="detailLoading">
        <el-descriptions
          v-if="detailData"
          :column="2"
          border
        >
          <el-descriptions-item label="模块名称">
            {{ detailData.title }}
          </el-descriptions-item>
          <el-descriptions-item label="操作人">
            {{ detailData.operUser || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            {{ detailData.requestMethod || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ detailData.status === 0 ? '成功' : '失败' }}
          </el-descriptions-item>
          <el-descriptions-item label="请求地址" :span="2">
            {{ detailData.operUrl || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="错误信息" :span="2">
            {{ detailData.errorMsg || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <div
          v-if="detailData"
          class="mt-4 grid gap-4"
        >
          <el-card>
            <template #header>
              <div class="font-medium">
                请求参数
              </div>
            </template>
            <pre class="whitespace-pre-wrap break-all text-xs">{{ detailData.operParam || '-' }}</pre>
          </el-card>
          <el-card>
            <template #header>
              <div class="font-medium">
                返回结果
              </div>
            </template>
            <pre class="whitespace-pre-wrap break-all text-xs">{{ detailData.jsonResult || '-' }}</pre>
          </el-card>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
