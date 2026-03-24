<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {configApi, type ConfigDTO, type ConfigVO} from '@/api/enterprise/config'
import type {PageResult, TableColumn} from '@/types'

// 表格列配置
const columns = ref<TableColumn[]>([
  { key: 'id', label: '配置ID', visible: true, fixed: 'left' },
  { key: 'configKey', label: '配置键', visible: true },
  { key: 'configValue', label: '配置值', visible: true },
  { key: 'configType', label: '配置类型', visible: true },
  { key: 'remark', label: '备注', visible: true },
  { key: 'createTime', label: '创建时间', visible: true },
])

const visibleColumns = computed(() => columns.value.filter(c => c.visible))

// 表格引用（用于 clearSelection）
const tableRef = ref()

// 多选
const selectedRows = ref<ConfigVO[]>([])
function handleSelectionChange(rows: ConfigVO[]) {
  selectedRows.value = rows
}

// 批量删除
async function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的配置')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个配置吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    const ids = selectedRows.value.map(row => row.id!)
    await configApi.deleteBatch(ids)
    ElMessage.success('批量删除成功')
    tableRef.value?.clearSelection()
    fetchConfigList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)
const formLoading = ref(false)

// 表格数据
const tableData = ref<ConfigVO[]>([])

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

// 当前编辑的配置ID
const currentId = ref<number>(0)

// 表单数据
const formData = reactive<ConfigDTO>({
  configKey: '',
  configValue: '',
  configType: undefined,
  remark: '',
})

// 查询表单
const queryForm = reactive({
  configKey: '',
  configType: '',
})

// 表单验证规则
const rules = computed<FormRules>(() => ({
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' },
    { max: 100, message: '配置键不能超过100个字符', trigger: 'blur' },
  ],
}))

// 获取配置列表
async function fetchConfigList() {
  loading.value = true
  try {
    const params: ConfigDTO = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      configKey: queryForm.configKey || undefined,
      configType: queryForm.configType ? Number(queryForm.configType) : undefined,
    }
    const result = await configApi.list(params) as PageResult<ConfigVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取系统配置列表失败:', error)
    ElMessage.error('获取系统配置列表失败')
  }
  finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchConfigList()
}

// 重置搜索
function handleReset() {
  queryForm.configKey = ''
  queryForm.configType = ''
  pagination.page = 1
  fetchConfigList()
}

// 新增配置
function handleCreate() {
  dialogType.value = 'create'
  currentId.value = 0
  Object.assign(formData, {
    configKey: '',
    configValue: '',
    configType: undefined,
    remark: '',
  })
  dialogVisible.value = true
}

// 编辑配置
function handleUpdate(row: ConfigVO) {
  dialogType.value = 'update'
  currentId.value = row.id || 0
  Object.assign(formData, {
    configKey: row.configKey || '',
    configValue: row.configValue || '',
    configType: row.configType,
    remark: row.remark || '',
  })
  dialogVisible.value = true
}

// 删除配置
async function handleDelete(row: ConfigVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除配置「${row.configKey}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await configApi.delete(row.id!)
    ElMessage.success('删除成功')
    fetchConfigList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除系统配置失败:', error)
      ElMessage.error('删除系统配置失败')
    }
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData: ConfigDTO = {
      configKey: formData.configKey,
      configValue: formData.configValue,
      configType: formData.configType,
      remark: formData.remark || undefined,
    }

    if (dialogType.value === 'create') {
      await configApi.create(submitData)
      ElMessage.success('创建成功')
    }
    else {
      await configApi.update(currentId.value, submitData)
      ElMessage.success('更新成功')
    }

    dialogVisible.value = false
    fetchConfigList()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存系统配置失败:', error)
      ElMessage.error('保存系统配置失败')
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
  fetchConfigList()
}

// 每页条数改变
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchConfigList()
}

// 页面加载
onMounted(() => {
  fetchConfigList()
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
            <el-form-item label="配置键">
              <el-input
                v-model="queryForm.configKey"
                placeholder="请输入配置键"
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
            <el-form-item label="配置类型">
              <el-input
                v-model="queryForm.configType"
                placeholder="请输入配置类型"
                clearable
                class="filter-control"
                @keyup.enter="handleSearch"
              />
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
            新增配置
          </el-button>
        </template>
        <template #tools>
          <el-button
            :icon="Refresh"
            circle
            @click="fetchConfigList"
          />
          <ColumnSetting v-model="columns" />
        </template>
      </TableToolbar>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        @selection-change="handleSelectionChange"
      >
        <!-- 多选列 -->
        <el-table-column
          type="selection"
          width="55"
          fixed="left"
        />

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
            v-else-if="col.key === 'configKey'"
            prop="configKey"
            :label="col.label"
            min-width="150"
            show-overflow-tooltip
          />
          <el-table-column
            v-else-if="col.key === 'configValue'"
            prop="configValue"
            :label="col.label"
            min-width="200"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              <el-tooltip
                v-if="row.configValue && row.configValue.length > 50"
                :content="row.configValue"
                placement="top"
              >
                <span>{{ row.configValue.substring(0, 50) + '...' }}</span>
              </el-tooltip>
              <span v-else>{{ row.configValue }}</span>
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'configType'"
            prop="configType"
            :label="col.label"
            min-width="100"
            show-overflow-tooltip
          />
          <el-table-column
            v-else-if="col.key === 'remark'"
            prop="remark"
            :label="col.label"
            min-width="150"
            show-overflow-tooltip
          />
          <el-table-column
            v-else-if="col.key === 'createTime'"
            prop="createTime"
            :label="col.label"
            width="180"
          />
        </template>

        <!-- 操作列 -->
        <el-table-column
          label="操作"
          width="150"
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

    <!-- 多选操作条 -->
    <SelectionBar
      :count="selectedRows.length"
      @clear="tableRef?.clearSelection()"
    >
      <el-button
        type="danger"
        size="small"
        :icon="Delete"
        @click="handleBatchDelete"
      >
        批量删除
      </el-button>
    </SelectionBar>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '新增系统配置' : '编辑系统配置'"
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
          label="配置键"
          prop="configKey"
        >
          <el-input
            v-model="formData.configKey"
            placeholder="请输入配置键"
            :disabled="dialogType === 'update'"
          />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input
            v-model="formData.configValue"
            type="textarea"
            :rows="4"
            placeholder="请输入配置值"
          />
        </el-form-item>
        <el-form-item label="配置类型">
          <el-input
            v-model="formData.configType"
            placeholder="请输入配置类型，如：system、email等"
          />
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
