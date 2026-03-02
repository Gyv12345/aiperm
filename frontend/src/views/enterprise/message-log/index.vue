<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { PageResult } from '@/types'
import { messageLogApi, type MessageLogQuery, type MessageLogVO } from '@/api/enterprise/messageLog'

const loading = ref(false)
const tableData = ref<MessageLogVO[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const queryForm = reactive<MessageLogQuery>({ templateCode: '', platform: '', status: '' })

async function fetchList() {
  loading.value = true
  try {
    const result = await messageLogApi.list({
      ...queryForm,
      page: pagination.page,
      pageSize: pagination.pageSize,
    }) as PageResult<MessageLogVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="p-4">
    <el-card class="mb-4">
      <el-form :model="queryForm" class="grid-filter-form" label-width="72px">
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="模板编码">
              <el-input v-model="queryForm.templateCode" class="filter-control" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="平台">
              <el-select v-model="queryForm.platform" class="filter-control" clearable>
                <el-option label="企业微信" value="WEWORK" />
                <el-option label="钉钉" value="DINGTALK" />
                <el-option label="飞书" value="FEISHU" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" class="filter-control" clearable>
                <el-option label="待发送" value="PENDING" />
                <el-option label="成功" value="SUCCESS" />
                <el-option label="失败" value="FAILED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="24" :lg="6">
            <el-form-item class="filter-actions">
              <el-button type="primary" @click="fetchList">搜索</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="templateCode" label="模板编码" min-width="140" />
        <el-table-column prop="platform" label="平台" width="100" />
        <el-table-column prop="platformUserId" label="平台用户ID" min-width="150" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="errorMsg" label="错误信息" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sendTime" label="发送时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>
