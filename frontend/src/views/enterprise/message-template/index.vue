<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { PageResult } from '@/types'
import { messageTemplateApi, type MessageTemplateDTO, type MessageTemplateVO } from '@/api/enterprise/messageTemplate'

const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const dialogType = ref<'create' | 'update'>('create')
const currentId = ref<number>(0)
const tableData = ref<MessageTemplateVO[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const queryForm = reactive<MessageTemplateDTO>({ templateCode: '', category: '', platform: '' })
const formData = reactive<MessageTemplateDTO>({
  templateCode: '',
  templateName: '',
  category: 'APPROVAL',
  platform: '',
  title: '',
  content: '',
})

async function fetchList() {
  loading.value = true
  try {
    const result = await messageTemplateApi.list({
      ...queryForm,
      page: pagination.page,
      pageSize: pagination.pageSize,
    }) as PageResult<MessageTemplateVO>
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
    templateCode: '',
    templateName: '',
    category: 'APPROVAL',
    platform: '',
    title: '',
    content: '',
  })
  dialogVisible.value = true
}

function handleEdit(row: MessageTemplateVO) {
  dialogType.value = 'update'
  currentId.value = row.id || 0
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(id?: number) {
  if (!id) return
  await messageTemplateApi.delete(id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleSubmit() {
  saving.value = true
  try {
    if (dialogType.value === 'create') {
      await messageTemplateApi.create(formData)
    } else {
      await messageTemplateApi.update(currentId.value, formData)
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
            <el-form-item label="模板编码">
              <el-input v-model="queryForm.templateCode" class="filter-control" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="分类">
              <el-select v-model="queryForm.category" class="filter-control" clearable>
                <el-option label="审批" value="APPROVAL" />
                <el-option label="告警" value="ALERT" />
                <el-option label="业务" value="BUSINESS" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="24" :lg="6">
            <el-form-item class="filter-actions">
              <el-button type="primary" @click="fetchList">搜索</el-button>
              <el-button @click="handleCreate">新增模板</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="templateCode" label="模板编码" min-width="160" />
        <el-table-column prop="templateName" label="模板名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="platform" label="平台" width="110" />
        <el-table-column prop="title" label="标题模板" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'create' ? '新增消息模板' : '编辑消息模板'" width="620px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="模板编码"><el-input v-model="formData.templateCode" /></el-form-item>
        <el-form-item label="模板名称"><el-input v-model="formData.templateName" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="formData.category" style="width: 100%">
            <el-option label="审批" value="APPROVAL" />
            <el-option label="告警" value="ALERT" />
            <el-option label="业务" value="BUSINESS" />
          </el-select>
        </el-form-item>
        <el-form-item label="平台">
          <el-select v-model="formData.platform" style="width: 100%" clearable>
            <el-option label="企业微信" value="WEWORK" />
            <el-option label="钉钉" value="DINGTALK" />
            <el-option label="飞书" value="FEISHU" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="formData.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="formData.content" type="textarea" :rows="5" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
