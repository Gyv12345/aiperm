<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { PageResult } from '@/types'
import { approvalApi, type ApprovalInstanceVO, type ApprovalSubmitDTO } from '@/api/approval'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref<ApprovalInstanceVO[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const queryForm = reactive({ sceneCode: '', status: '' })
const submitForm = reactive<ApprovalSubmitDTO>({
  sceneCode: '',
  businessType: '',
  businessId: 0,
  formData: {},
})

async function fetchList() {
  loading.value = true
  try {
    const result = await approvalApi.my({
      ...queryForm,
      page: pagination.page,
      pageSize: pagination.pageSize,
    }) as PageResult<ApprovalInstanceVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  } finally {
    loading.value = false
  }
}

function openSubmitDialog() {
  Object.assign(submitForm, { sceneCode: '', businessType: '', businessId: 0, formData: {} })
  dialogVisible.value = true
}

async function submitApproval() {
  submitting.value = true
  try {
    await approvalApi.submit(submitForm)
    ElMessage.success('审批已提交')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="p-4">
    <el-card class="mb-4">
      <el-form
        :model="queryForm"
        class="grid-filter-form"
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
                class="filter-control"
                clearable
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
                class="filter-control"
                clearable
              >
                <el-option
                  label="待审批"
                  value="PENDING"
                />
                <el-option
                  label="已通过"
                  value="APPROVED"
                />
                <el-option
                  label="已拒绝"
                  value="REJECTED"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="12"
            :md="24"
            :lg="6"
          >
            <el-form-item class="filter-actions">
              <el-button
                type="primary"
                @click="fetchList"
              >
                搜索
              </el-button>
              <el-button @click="openSubmitDialog">
                发起审批
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card>
      <el-table
        v-loading="loading"
        :data="tableData"
        border
      >
        <el-table-column
          prop="sceneCode"
          label="场景编码"
          min-width="140"
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
          width="100"
        />
        <el-table-column
          prop="status"
          label="状态"
          width="100"
        />
        <el-table-column
          prop="createTime"
          label="发起时间"
          width="180"
        />
        <el-table-column
          prop="resultTime"
          label="结果时间"
          width="180"
        />
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      title="发起审批"
      width="520px"
    >
      <el-form
        :model="submitForm"
        label-width="100px"
      >
        <el-form-item label="场景编码">
          <el-input v-model="submitForm.sceneCode" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-input v-model="submitForm.businessType" />
        </el-form-item>
        <el-form-item label="业务ID">
          <el-input-number
            v-model="submitForm.businessId"
            :min="1"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="submitApproval"
        >
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
