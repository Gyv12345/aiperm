<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {type LoginLogVO, profileApi} from '@/api/profile'
import type {PageResult} from '@/types'

const loading = ref(false)
const tableData = ref<LoginLogVO[]>([])
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

// 获取登录日志
const fetchLogs = async () => {
  loading.value = true
  try {
    const data: PageResult<LoginLogVO> = await profileApi.getLogs(
      pagination.value.pageNum,
      pagination.value.pageSize
    )
    tableData.value = data.list || []
    pagination.value.total = data.total || 0
  } catch (error) {
    console.error('获取登录日志失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.value.pageNum = page
  fetchLogs()
}

const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  fetchLogs()
}

onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <div class="profile-logs p-6">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center gap-2">
          <el-icon class="text-green-500">
            <Document />
          </el-icon>
          <span class="font-semibold">登录日志</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
      >
        <el-table-column
          prop="ip"
          label="登录IP"
          width="140"
        />
        <el-table-column
          prop="location"
          label="登录地点"
          min-width="120"
        >
          <template #default="{ row }">
            {{ row.location || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="browser"
          label="浏览器"
          width="120"
        >
          <template #default="{ row }">
            {{ row.browser || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="os"
          label="操作系统"
          width="120"
        >
          <template #default="{ row }">
            {{ row.os || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="80"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.status === 0 ? 'success' : 'danger'"
              size="small"
            >
              {{ row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="msg"
          label="提示消息"
          min-width="120"
        >
          <template #default="{ row }">
            {{ row.msg || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="loginTime"
          label="登录时间"
          width="180"
        />
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.profile-logs {
  background-color: var(--color-bg-page);
}
</style>
