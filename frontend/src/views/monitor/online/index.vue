<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Download, Refresh, Search} from '@element-plus/icons-vue'
import {onlineMonitorApi, type OnlineUserVO} from '@/api/monitor'
import type {PageResult} from '@/types'

const loading = ref(false)
const selectedRows = ref<OnlineUserVO[]>([])
const tableData = ref<OnlineUserVO[]>([])
const tableRef = ref()

const queryForm = reactive({
  username: '',
  ip: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

function formatRemain(seconds?: number) {
  const value = seconds ?? 0
  if (value <= 0) {
    return '已过期'
  }
  if (value < 60) {
    return `${value}s`
  }
  if (value < 3600) {
    return `${Math.floor(value / 60)}m ${value % 60}s`
  }
  const hours = Math.floor(value / 3600)
  const minutes = Math.floor((value % 3600) / 60)
  return `${hours}h ${minutes}m`
}

async function fetchList() {
  loading.value = true
  try {
    const result = await onlineMonitorApi.list({
      page: pagination.page,
      pageSize: pagination.pageSize,
      username: queryForm.username || undefined,
      ip: queryForm.ip || undefined,
    }) as PageResult<OnlineUserVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取在线用户失败:', error)
    ElMessage.error('获取在线用户失败')
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
  pagination.page = 1
  fetchList()
}

function handleSelectionChange(rows: OnlineUserVO[]) {
  selectedRows.value = rows
}

async function handleExport() {
  try {
    await onlineMonitorApi.export({
      username: queryForm.username || undefined,
      ip: queryForm.ip || undefined,
    })
  }
  catch (error) {
    console.error('导出在线用户失败:', error)
  }
}

async function handleForceLogout(row: OnlineUserVO) {
  if (row.currentSession) {
    ElMessage.warning('不能强退当前登录会话')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要强退用户「${row.username}」吗？`,
      '强退确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await onlineMonitorApi.forceLogout(row.id)
    ElMessage.success('已强退该会话')
    fetchList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('强退在线用户失败:', error)
      ElMessage.error('强退在线用户失败')
    }
  }
}

async function handleBatchForceLogout() {
  const validRows = selectedRows.value.filter(item => !item.currentSession)
  if (validRows.length === 0) {
    ElMessage.warning('请选择可强退的会话')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要强退选中的 ${validRows.length} 个会话吗？`,
      '批量强退确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await onlineMonitorApi.forceLogoutBatch(validRows.map(item => item.id))
    ElMessage.success('批量强退成功')
    fetchList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('批量强退失败:', error)
      ElMessage.error('批量强退失败')
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
            :sm="24"
            :md="8"
            :lg="12"
          >
            <el-form-item class="filter-actions">
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
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card>
      <div class="mb-4 flex items-center justify-between gap-3">
        <div class="text-sm text-gray-500">
          在线会话数 {{ pagination.total }}
        </div>
        <div class="flex items-center gap-2">
          <el-button
            :icon="Download"
            v-permission="'monitor:online:export'"
            @click="handleExport"
          >
            导出
          </el-button>
          <el-button
            :icon="Refresh"
            circle
            @click="fetchList"
          />
        </div>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
          fixed="left"
        />
        <el-table-column
          label="用户"
          min-width="180"
        >
          <template #default="{ row }">
            <div class="flex flex-col">
              <span class="font-medium">{{ row.username }}</span>
              <span class="text-xs text-gray-500">{{ row.nickname || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="deptName"
          label="部门"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="roleNames"
          label="角色"
          min-width="160"
          show-overflow-tooltip
        />
        <el-table-column
          prop="ip"
          label="登录 IP"
          min-width="140"
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
          label="会话状态"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.currentSession ? 'success' : 'info'"
              size="small"
            >
              {{ row.currentSession ? '当前会话' : '其他会话' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="剩余时效"
          width="110"
          align="center"
        >
          <template #default="{ row }">
            {{ formatRemain(row.tokenTimeout) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="loginTime"
          label="登录时间"
          min-width="168"
        />
        <el-table-column
          prop="lastAccessTime"
          label="最后活跃"
          min-width="168"
        />
        <el-table-column
          label="操作"
          width="110"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              type="danger"
              link
              :icon="Delete"
              :disabled="row.currentSession"
              v-permission="'monitor:online:forceLogout'"
              @click="handleForceLogout(row)"
            >
              强退
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

    <SelectionBar
      :count="selectedRows.filter(item => !item.currentSession).length"
      @clear="tableRef?.clearSelection()"
    >
      <el-button
        type="danger"
        size="small"
        :icon="Delete"
        v-permission="'monitor:online:forceLogout'"
        @click="handleBatchForceLogout"
      >
        批量强退
      </el-button>
    </SelectionBar>
  </div>
</template>
