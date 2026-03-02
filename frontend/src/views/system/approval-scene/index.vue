<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { PageResult } from '@/types'
import { approvalSceneApi, type ApprovalSceneDTO, type ApprovalSceneVO } from '@/api/system/approvalScene'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dialogType = ref<'create' | 'update'>('create')
const currentId = ref<number>(0)
const tableData = ref<ApprovalSceneVO[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const queryForm = reactive<ApprovalSceneDTO>({ sceneCode: '', sceneName: '', platform: '', enabled: undefined })
const formData = reactive<ApprovalSceneDTO>({
  sceneCode: '',
  sceneName: '',
  platform: 'WEWORK',
  templateId: '',
  enabled: 1,
  handlerClass: 'defaultApprovalHandler',
  timeoutHours: 72,
  timeoutAction: 'NOTIFY',
})

async function fetchList() {
  loading.value = true
  try {
    const result = await approvalSceneApi.list({
      ...queryForm,
      page: pagination.page,
      pageSize: pagination.pageSize,
    }) as PageResult<ApprovalSceneVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  dialogType.value = 'create'
  currentId.value = 0
  Object.assign(formData, {
    sceneCode: '',
    sceneName: '',
    platform: 'WEWORK',
    templateId: '',
    enabled: 1,
    handlerClass: 'defaultApprovalHandler',
    timeoutHours: 72,
    timeoutAction: 'NOTIFY',
  })
  dialogVisible.value = true
}

function handleEdit(row: ApprovalSceneVO) {
  dialogType.value = 'update'
  currentId.value = row.id || 0
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(id?: number) {
  if (!id) return
  await approvalSceneApi.delete(id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleSubmit() {
  saving.value = true
  try {
    if (dialogType.value === 'create') {
      await approvalSceneApi.create(formData)
    } else {
      await approvalSceneApi.update(currentId.value, formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
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
            <el-form-item label="场景编码">
              <el-input v-model="queryForm.sceneCode" class="filter-control" clearable />
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
          <el-col :xs="24" :sm="12" :md="24" :lg="6">
            <el-form-item class="filter-actions">
              <el-button type="primary" @click="fetchList">搜索</el-button>
              <el-button @click="handleCreate">新增场景</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="sceneCode" label="场景编码" min-width="140" />
        <el-table-column prop="sceneName" label="场景名称" min-width="140" />
        <el-table-column prop="platform" label="平台" width="110" />
        <el-table-column prop="handlerClass" label="处理器" min-width="180" />
        <el-table-column label="启用" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'create' ? '新增审批场景' : '编辑审批场景'" width="560px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="场景编码"><el-input v-model="formData.sceneCode" /></el-form-item>
        <el-form-item label="场景名称"><el-input v-model="formData.sceneName" /></el-form-item>
        <el-form-item label="平台">
          <el-select v-model="formData.platform" style="width: 100%">
            <el-option label="企业微信" value="WEWORK" />
            <el-option label="钉钉" value="DINGTALK" />
            <el-option label="飞书" value="FEISHU" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板ID"><el-input v-model="formData.templateId" /></el-form-item>
        <el-form-item label="处理器"><el-input v-model="formData.handlerClass" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="formData.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
