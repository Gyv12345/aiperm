<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Download, Refresh, Search} from '@element-plus/icons-vue'
import {loginLogApi, type LoginLogVO} from '@/api/monitor'
import type {PageResult} from '@/types'

const loading = ref(false)
const tableData = ref<LoginLogVO[]>([])
const dateRange = ref<string[]>([])

const queryForm = reactive({
  username: '',
  ip: '',
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
    const result = await loginLogApi.list({
      page: pagination.page,
      pageSize: pagination.pageSize,
      username: queryForm.username || undefined,
      ip: queryForm.ip || undefined,
      status: queryForm.status,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
    }) as PageResult<LoginLogVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取登录日志失败:', error)
    ElMessage.error('获取登录日志失败')
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
  queryForm.username = ''
  queryForm.ip = ''
  queryForm.status = undefined
  dateRange.value = []
  pagination.page = 1
  fetchList()
}

async function handleExport() {
  try {
    await loginLogApi.export({
      username: queryForm.username || undefined,
      ip: queryForm.ip || undefined,
      status: queryForm.status,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
    })
  }
  catch (error) {
    console.error('导出登录日志失败:', error)
  }
}

async function handleDelete(row: LoginLogVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除登录日志「${row.username} / ${row.loginTime}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await loginLogApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除登录日志失败:', error)
      ElMessage.error('删除登录日志失败')
    }
  }
}

async function handleClean() {
  try {
    await ElMessageBox.confirm(
      '确定清空全部登录日志吗？此操作不可恢复。',
      '清空确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await loginLogApi.clean()
    ElMessage.success('已清空登录日志')
    fetchList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('清空登录日志失败:', error)
      ElMessage.error('清空登录日志失败')
    }
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
            <el-form-item label="用户名">
              <el-input
                v-model="queryForm.username"
                placeholder="请输入用户名"
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
            <el-form-item label="登录 IP">
              <el-input
                v-model="queryForm.ip"
                placeholder="请输入登录 IP"
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
          共 {{ pagination.total }} 条登录日志
        </div>
        <div class="flex items-center gap-2">
          <el-button
            :icon="Download"
            v-permission="'monitor:login-log:export'"
            @click="handleExport"
          >
            导出
          </el-button>
          <el-button
            type="danger"
            v-permission="'monitor:login-log:delete'"
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
          prop="username"
          label="用户名"
          min-width="120"
        />
        <el-table-column
          prop="ip"
          label="登录 IP"
          min-width="140"
        />
        <el-table-column
          prop="location"
          label="登录地点"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column
          prop="browser"
          label="浏览器"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="os"
          label="操作系统"
          min-width="120"
          show-overflow-tooltip
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
          prop="msg"
          label="说明"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column
          prop="loginTime"
          label="登录时间"
          min-width="168"
        />
        <el-table-column
          label="操作"
          width="100"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              type="danger"
              link
              :icon="Delete"
              v-permission="'monitor:login-log:delete'"
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
  </div>
</template>
