<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {Refresh, Search} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {approvalInstanceApi, type ApprovalInstanceDTO, type ApprovalInstanceVO} from '@/api/approval'
import type {PageResult} from '@/types'

const loading = ref(false)
const tableData = ref<ApprovalInstanceVO[]>([])

const platformOptions = ['FEISHU', 'WEWORK', 'DINGTALK']
const statusOptions = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELED']

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const queryForm = reactive({
  sceneCode: '',
  businessType: '',
  platform: '',
  status: '',
})

async function fetchInstances() {
  loading.value = true
  try {
    const params: ApprovalInstanceDTO = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      sceneCode: queryForm.sceneCode || undefined,
      businessType: queryForm.businessType || undefined,
      platform: queryForm.platform || undefined,
      status: queryForm.status || undefined,
    }
    const result = await approvalInstanceApi.list(params) as PageResult<ApprovalInstanceVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取我的审批失败:', error)
    ElMessage.error('获取我的审批失败')
  }
  finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchInstances()
}

function handleReset() {
  queryForm.sceneCode = ''
  queryForm.businessType = ''
  queryForm.platform = ''
  queryForm.status = ''
  pagination.page = 1
  fetchInstances()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchInstances()
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchInstances()
}

function statusTagType(status: string) {
  switch (status) {
    case 'APPROVED':
      return 'success'
    case 'REJECTED':
      return 'danger'
    case 'CANCELED':
      return 'warning'
    default:
      return 'info'
  }
}

onMounted(() => {
  fetchInstances()
})
</script>

<template>
  <div class="p-4 space-y-4">
    <el-card shadow="never">
      <el-form
        :model="queryForm"
        label-width="72px"
      >
        <el-row :gutter="12">
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <el-form-item label="场景编码">
              <el-input
                v-model="queryForm.sceneCode"
                placeholder="请输入场景编码"
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
            <el-form-item label="业务类型">
              <el-input
                v-model="queryForm.businessType"
                placeholder="请输入业务类型"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="5"
          >
            <el-form-item label="平台">
              <el-select
                v-model="queryForm.platform"
                placeholder="全部平台"
                clearable
              >
                <el-option
                  v-for="item in platformOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="5"
          >
            <el-form-item label="状态">
              <el-select
                v-model="queryForm.status"
                placeholder="全部状态"
                clearable
              >
                <el-option
                  v-for="item in statusOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="24"
            :md="24"
            :lg="2"
            class="flex items-center justify-end"
          >
            <div class="flex gap-2">
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
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="text-base font-semibold">
          我的审批
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
      >
        <el-table-column
          prop="sceneName"
          label="场景"
          min-width="160"
        />
        <el-table-column
          prop="sceneCode"
          label="场景编码"
          min-width="160"
        />
        <el-table-column
          prop="businessType"
          label="业务类型"
          width="120"
        />
        <el-table-column
          prop="businessId"
          label="业务ID"
          width="120"
        />
        <el-table-column
          prop="platform"
          label="平台"
          width="110"
        />
        <el-table-column
          label="状态"
          width="110"
        >
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="platformInstanceId"
          label="平台实例ID"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column
          prop="createTime"
          label="发起时间"
          min-width="180"
        />
        <el-table-column
          prop="resultTime"
          label="结果时间"
          min-width="180"
        />
        <el-table-column
          prop="errorMessage"
          label="处理备注"
          min-width="220"
          show-overflow-tooltip
        />
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :current-page="pagination.page"
          :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>
