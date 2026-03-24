<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search, VideoPause, VideoPlay} from '@element-plus/icons-vue'
import {jobApi, type JobDTO, type JobVO} from '@/api/enterprise/job'
import type {PageResult, TableColumn} from '@/types'

// 表格列配置
const columns = ref<TableColumn[]>([
  { key: 'id', label: '任务ID', visible: true, fixed: 'left' },
  { key: 'jobName', label: '任务名称', visible: true },
  { key: 'jobGroup', label: '任务分组', visible: true },
  { key: 'cronExpression', label: 'Cron表达式', visible: true },
  { key: 'invokeTarget', label: '执行目标', visible: true },
  { key: 'remark', label: '备注', visible: true },
])

const visibleColumns = computed(() => columns.value.filter(c => c.visible))

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)
const formLoading = ref(false)

// 表格数据
const tableData = ref<JobVO[]>([])

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 对话框显示状态
const dialogVisible = ref(false)

// 对话框类型：create / update
const dialogType = ref<'create' | 'update'>('create')

// 当前编辑的任务ID
const currentId = ref<number>(0)

// 表单数据
const formData = reactive<JobDTO>({
  jobName: '',
  jobGroup: '',
  cronExpression: '',
  invokeTarget: '',
  misfirePolicy: 1,
  concurrent: 1,
  status: 1,
  remark: '',
})

// 查询表单
const queryForm = reactive({
  jobName: '',
  jobGroup: '',
  status: undefined as number | undefined,
})

// 状态选项
const statusOptions = [
  { value: 0, label: '暂停' },
  { value: 1, label: '运行' },
]

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
  invokeTarget: [
    { required: true, message: '请输入执行目标', trigger: 'blur' },
    { max: 200, message: '执行目标不能超过200个字符', trigger: 'blur' },
  ],
}))

// 获取任务列表
async function fetchJobList() {
  loading.value = true
  try {
    const params: JobDTO = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      jobName: queryForm.jobName || undefined,
      jobGroup: queryForm.jobGroup || undefined,
      status: queryForm.status,
    }
    const result = await jobApi.list(params) as PageResult<JobVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取定时任务列表失败:', error)
    ElMessage.error('获取定时任务列表失败')
  }
  finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchJobList()
}

// 重置搜索
function handleReset() {
  queryForm.jobName = ''
  queryForm.jobGroup = ''
  queryForm.status = undefined
  pagination.page = 1
  fetchJobList()
}

// 新增任务
function handleCreate() {
  dialogType.value = 'create'
  currentId.value = 0
  Object.assign(formData, {
    jobName: '',
    jobGroup: '',
    cronExpression: '',
    invokeTarget: '',
    misfirePolicy: 1,
    concurrent: 1,
    status: 1,
    remark: '',
  })
  dialogVisible.value = true
}

// 编辑任务
function handleUpdate(row: JobVO) {
  dialogType.value = 'update'
  currentId.value = row.id || 0
  Object.assign(formData, {
    jobName: row.jobName || '',
    jobGroup: row.jobGroup || '',
    cronExpression: row.cronExpression || '',
    invokeTarget: row.invokeTarget || '',
    misfirePolicy: row.misfirePolicy || 1,
    concurrent: row.concurrent || 1,
    status: row.status || 1,
    remark: row.remark || '',
  })
  dialogVisible.value = true
}

// 删除任务
async function handleDelete(row: JobVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除定时任务「${row.jobName}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await jobApi.delete(row.id!)
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
    await jobApi.pause(row.id!)
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
    await jobApi.resume(row.id!)
    ElMessage.success('恢复成功')
    fetchJobList()
  }
  catch (error) {
    console.error('恢复定时任务失败:', error)
    ElMessage.error('恢复定时任务失败')
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData: JobDTO = {
      jobName: formData.jobName,
      jobGroup: formData.jobGroup,
      cronExpression: formData.cronExpression,
      invokeTarget: formData.invokeTarget,
      misfirePolicy: formData.misfirePolicy,
      concurrent: formData.concurrent,
      status: formData.status,
      remark: formData.remark || undefined,
    }

    if (dialogType.value === 'create') {
      await jobApi.create(submitData)
      ElMessage.success('创建成功')
    }
    else {
      await jobApi.update(currentId.value, submitData)
      ElMessage.success('更新成功')
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

// 关闭对话框
function handleDialogClose() {
  formRef.value?.resetFields()
}

// 分页改变
function handlePageChange(page: number) {
  pagination.page = page
  fetchJobList()
}

// 每页条数改变
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchJobList()
}

// 页面加载
onMounted(() => {
  fetchJobList()
})
</script>

<template>
  <div class="p-4">
    <!-- 搜索区域 -->
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
            <el-form-item label="任务名称">
              <el-input
                v-model="queryForm.jobName"
                placeholder="请输入任务名称"
                clearable
                class="filter-control"
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
            <el-form-item label="任务分组">
              <el-input
                v-model="queryForm.jobGroup"
                placeholder="请输入任务分组"
                clearable
                class="filter-control"
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
                class="filter-control"
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
            :sm="12"
            :md="24"
            :lg="6"
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

    <!-- 表格区域 -->
    <el-card>
      <!-- 工具栏 -->
      <TableToolbar>
        <template #actions>
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleCreate"
          >
            新增任务
          </el-button>
        </template>
        <template #tools>
          <el-button
            :icon="Refresh"
            circle
            @click="fetchJobList"
          />
          <ColumnSetting v-model="columns" />
        </template>
      </TableToolbar>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
      >
        <!-- 动态普通数据列 -->
        <template
          v-for="col in visibleColumns"
          :key="col.key"
        >
          <el-table-column
            v-if="col.key === 'id'"
            prop="id"
            :label="col.label"
            width="80"
            align="center"
            fixed="left"
          />
          <el-table-column
            v-else-if="col.key === 'jobName'"
            prop="jobName"
            :label="col.label"
            min-width="120"
            show-overflow-tooltip
          />
          <el-table-column
            v-else-if="col.key === 'jobGroup'"
            prop="jobGroup"
            :label="col.label"
            min-width="100"
            show-overflow-tooltip
          />
          <el-table-column
            v-else-if="col.key === 'cronExpression'"
            prop="cronExpression"
            :label="col.label"
            min-width="120"
            show-overflow-tooltip
          />
          <el-table-column
            v-else-if="col.key === 'invokeTarget'"
            prop="invokeTarget"
            :label="col.label"
            min-width="180"
            show-overflow-tooltip
          />
          <el-table-column
            v-else-if="col.key === 'remark'"
            prop="remark"
            :label="col.label"
            min-width="150"
            show-overflow-tooltip
          />
        </template>

        <!-- 状态列（手写，因为含义不同） -->
        <el-table-column
          prop="status"
          label="状态"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ statusOptions.find(o => o.value === row.status)?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column
          label="操作"
          width="200"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="Edit"
              @click="handleUpdate(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              :icon="VideoPause"
              @click="handlePause(row)"
            >
              暂停
            </el-button>
            <el-button
              v-else
              type="success"
              link
              :icon="VideoPlay"
              @click="handleResume(row)"
            >
              恢复
            </el-button>
            <el-button
              type="danger"
              link
              :icon="Delete"
              @click="handleDelete(row)"
            >
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '新增定时任务' : '编辑定时任务'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="任务名称"
          prop="jobName"
        >
          <el-input
            v-model="formData.jobName"
            placeholder="请输入任务名称"
          />
        </el-form-item>
        <el-form-item label="任务分组">
          <el-input
            v-model="formData.jobGroup"
            placeholder="请输入任务分组"
          />
        </el-form-item>
        <el-form-item
          label="Cron表达式"
          prop="cronExpression"
        >
          <el-input
            v-model="formData.cronExpression"
            placeholder="请输入Cron表达式，如：0 0/5 * * * ?"
          />
        </el-form-item>
        <el-form-item
          label="执行目标"
          prop="invokeTarget"
        >
          <el-input
            v-model="formData.invokeTarget"
            placeholder="请输入Spring Bean名称或完整类名"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio
              v-for="item in statusOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
