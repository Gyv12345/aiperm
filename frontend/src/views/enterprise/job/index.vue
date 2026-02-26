<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh, VideoPause, VideoPlay } from '@element-plus/icons-vue'
import request from '@/utils/request'
import type { PageResult } from '@/types'

interface JobVO {
  id: number
  jobName: string
  jobGroup: string
  cronExpression: string
  beanClass: string
  status: number
  remark: string
  nextTime: string
  createTime: string
}

interface JobDTO {
  page?: number
  pageSize?: number
  jobName?: string
  jobGroup?: string
  status?: number
}

// 定时任务列表
const jobList = ref<JobVO[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 搜索条件
const searchForm = reactive({
  jobName: '',
  jobGroup: '',
  status: undefined as number | undefined,
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const formLoading = ref(false)

// 状态选项
const statusOptions = [
  { value: 0, label: '暂停' },
  { value: 1, label: '运行' },
]

// 任务表单
const form = reactive({
  id: undefined as number | undefined,
  jobName: '',
  jobGroup: '',
  cronExpression: '',
  beanClass: '',
  status: 1,
  remark: '',
})

// 表单验证规则
const rules = computed<FormRules>(() => ({
  jobName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { max: 100, message: '任务名称不能超过100个字符', trigger: 'blur' },
  ],
  cronExpression: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' },
    { max: 100, message: 'Cron表达式不能超过100个字符', trigger: 'blur' },
  ],
  beanClass: [
    { required: true, message: '请输入执行类', trigger: 'blur' },
    { max: 200, message: '执行类不能超过200个字符', trigger: 'blur' },
  ],
}))

// 获取任务列表
async function fetchJobList() {
  loading.value = true
  try {
    const params: JobDTO = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.jobName) params.jobName = searchForm.jobName
    if (searchForm.jobGroup) params.jobGroup = searchForm.jobGroup
    if (searchForm.status !== undefined) params.status = searchForm.status

    const data = await request.get<PageResult<JobVO>>('/enterprise/job', { params })
    if (data) {
      jobList.value = data.list || []
      pagination.total = data.total || 0
    }
  }
  catch (error) {
    console.error('获取定时任务列表失败:', error)
    ElMessage.error('获取定时任务列表失败')
  }
  finally {
    loading.value = false
  }
}

// 重置表单
function resetForm() {
  Object.assign(form, {
    id: undefined,
    jobName: '',
    jobGroup: '',
    cronExpression: '',
    beanClass: '',
    status: 1,
    remark: '',
  })
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchJobList()
}

// 重置搜索
function handleResetSearch() {
  searchForm.jobName = ''
  searchForm.jobGroup = ''
  searchForm.status = undefined
  pagination.page = 1
  fetchJobList()
}

// 新增
function handleAdd() {
  resetForm()
  dialogTitle.value = '新增定时任务'
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: JobVO) {
  resetForm()
  dialogTitle.value = '编辑定时任务'
  Object.assign(form, {
    id: row.id,
    jobName: row.jobName,
    jobGroup: row.jobGroup,
    cronExpression: row.cronExpression,
    beanClass: row.beanClass,
    status: row.status,
    remark: row.remark,
  })
  dialogVisible.value = true
}

// 删除
async function handleDelete(row: JobVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除定时任务「${row.jobName}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await request.delete(`/enterprise/job/${row.id}`)
    ElMessage.success('删除成功')
    fetchJobList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除定时任务失败:', error)
      ElMessage.error('删除定时任务失败')
    }
  }
}

// 暂停任务
async function handlePause(row: JobVO) {
  try {
    await request.put(`/enterprise/job/${row.id}/pause`)
    ElMessage.success('暂停成功')
    fetchJobList()
  }
  catch (error) {
    console.error('暂停定时任务失败:', error)
    ElMessage.error('暂停定时任务失败')
  }
}

// 恢复任务
async function handleResume(row: JobVO) {
  try {
    await request.put(`/enterprise/job/${row.id}/resume`)
    ElMessage.success('恢复成功')
    fetchJobList()
  }
  catch (error) {
    console.error('恢复定时任务失败:', error)
    ElMessage.error('恢复定时任务失败')
  }
}

// 提交表单
async function submitForm() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      jobName: form.jobName,
      jobGroup: form.jobGroup,
      cronExpression: form.cronExpression,
      beanClass: form.beanClass,
      status: form.status,
      remark: form.remark || undefined,
    }

    if (form.id) {
      await request.put(`/enterprise/job/${form.id}`, submitData)
      ElMessage.success('修改成功')
    }
    else {
      await request.post('/enterprise/job', submitData)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    fetchJobList()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存定时任务失败:', error)
      ElMessage.error('保存定时任务失败')
    }
  }
  finally {
    formLoading.value = false
  }
}

// 分页变化
function handlePageChange(page: number) {
  pagination.page = page
  fetchJobList()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchJobList()
}

// 初始化
onMounted(() => {
  fetchJobList()
})
</script>

<template>
  <div class="p-4 h-full">
    <el-card>
      <!-- 搜索区域 -->
      <div class="mb-4">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="任务名称">
            <el-input
              v-model="searchForm.jobName"
              placeholder="请输入任务名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="任务分组">
            <el-input
              v-model="searchForm.jobGroup"
              placeholder="请输入任务分组"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">
              搜索
            </el-button>
            <el-button :icon="Refresh" @click="handleResetSearch">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 工具栏 -->
      <div class="mb-4">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="jobList" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="jobName" label="任务名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="jobGroup" label="任务分组" min-width="100" show-overflow-tooltip />
        <el-table-column prop="cronExpression" label="Cron表达式" min-width="120" show-overflow-tooltip />
        <el-table-column prop="beanClass" label="执行类" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ statusOptions.find(o => o.value === row.status)?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              size="small"
              :icon="VideoPause"
              @click="handlePause(row)"
            >
              暂停
            </el-button>
            <el-button
              v-else
              type="success"
              link
              size="small"
              :icon="VideoPlay"
              @click="handleResume(row)"
            >
              恢复
            </el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="form.jobName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务分组" prop="jobGroup">
          <el-input v-model="form.jobGroup" placeholder="请输入任务分组" />
        </el-form-item>
        <el-form-item label="Cron表达式" prop="cronExpression">
          <el-input v-model="form.cronExpression" placeholder="请输入Cron表达式，如：0 0/5 * * * ?" />
        </el-form-item>
        <el-form-item label="执行类" prop="beanClass">
          <el-input v-model="form.beanClass" placeholder="请输入执行类的完整类名" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="item in statusOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="formLoading" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
