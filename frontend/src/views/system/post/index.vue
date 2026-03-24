<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {postApi, type PostDTO, type PostVO} from '@/api/system/post'
import type {PageResult, TableColumn} from '@/types'
import {useDict} from '@/composables/useDict'

// 字典
const dictData = useDict('sys_status')
const sys_status = dictData.sys_status!

// 表格列配置
const columns = ref<TableColumn[]>([
  { key: 'id', label: '岗位ID', visible: true, fixed: 'left' },
  { key: 'postName', label: '岗位名称', visible: true },
  { key: 'postCode', label: '岗位编码', visible: true },
  { key: 'sort', label: '排序', visible: true },
  { key: 'createTime', label: '创建时间', visible: true },
  { key: 'remark', label: '备注', visible: true },
])

const visibleColumns = computed(() => columns.value.filter(c => c.visible))

// 表格引用（用于 clearSelection）
const tableRef = ref()

// 多选
const selectedRows = ref<PostVO[]>([])
function handleSelectionChange(rows: PostVO[]) {
  selectedRows.value = rows
}

// 批量删除
async function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的岗位')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个岗位吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    const ids = selectedRows.value.map(row => row.id!)
    await postApi.deleteBatch(ids)
    ElMessage.success('批量删除成功')
    tableRef.value?.clearSelection()
    fetchPostList()
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

// 表格数据
const tableData = ref<PostVO[]>([])

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

// 当前编辑的岗位ID
const currentId = ref<number>(0)

// 表单数据
const formData = reactive<PostDTO>({
  postName: '',
  postCode: '',
  sort: 0,
  status: 0,
  remark: '',
})

// 查询表单
const queryForm = reactive({
  postName: '',
  postCode: '',
  status: undefined as number | undefined,
})

// 表单验证规则
const rules = computed<FormRules>(() => ({
  postName: [
    { required: true, message: '请输入岗位名称', trigger: 'blur' },
    { min: 2, max: 50, message: '岗位名称长度为 2-50 个字符', trigger: 'blur' },
  ],
  postCode: [
    { required: true, message: '请输入岗位编码', trigger: 'blur' },
    { min: 2, max: 50, message: '岗位编码长度为 2-50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '岗位编码必须以字母开头，只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  sort: [
    { type: 'number', message: '排序必须为数字', trigger: 'blur' },
  ],
}))

// 获取岗位列表
async function fetchPostList() {
  loading.value = true
  try {
    const params: PostDTO = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      postName: queryForm.postName || undefined,
      postCode: queryForm.postCode || undefined,
      status: queryForm.status,
    }
    const result = await postApi.list(params) as PageResult<PostVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取岗位列表失败:', error)
    ElMessage.error('获取岗位列表失败')
  }
  finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchPostList()
}

// 重置搜索
function handleReset() {
  queryForm.postName = ''
  queryForm.postCode = ''
  queryForm.status = undefined
  pagination.page = 1
  fetchPostList()
}

// 新增岗位
function handleCreate() {
  dialogType.value = 'create'
  currentId.value = 0
  Object.assign(formData, {
    postName: '',
    postCode: '',
    sort: 0,
    status: 0,
    remark: '',
  })
  dialogVisible.value = true
}

// 编辑岗位
function handleUpdate(row: PostVO) {
  dialogType.value = 'update'
  currentId.value = row.id || 0
  Object.assign(formData, {
    postName: row.postName || '',
    postCode: row.postCode || '',
    sort: row.sort || 0,
    status: row.status || 0,
    remark: row.remark || '',
  })
  dialogVisible.value = true
}

// 删除岗位
async function handleDelete(row: PostVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除岗位「${row.postName}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await postApi.delete(row.id!)
    ElMessage.success('删除成功')
    fetchPostList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除岗位失败:', error)
      ElMessage.error('删除岗位失败')
    }
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    const submitData: PostDTO = {
      postName: formData.postName,
      postCode: formData.postCode,
      sort: formData.sort,
      status: formData.status,
      remark: formData.remark || undefined,
    }

    if (dialogType.value === 'create') {
      await postApi.create(submitData)
      ElMessage.success('创建成功')
      // 新增后回到第一页，避免当前页不是第一页时看不到最新数据
      pagination.page = 1
    }
    else {
      await postApi.update(currentId.value, submitData)
      ElMessage.success('更新成功')
    }

    dialogVisible.value = false
    fetchPostList()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存岗位失败:', error)
      ElMessage.error('保存岗位失败')
    }
  }
}

// 关闭对话框
function handleDialogClose() {
  formRef.value?.resetFields()
}

// 分页改变
function handlePageChange(page: number) {
  pagination.page = page
  fetchPostList()
}

// 每页条数改变
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchPostList()
}

// 页面加载
onMounted(() => {
  fetchPostList()
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
            <el-form-item label="岗位名称">
              <el-input
                v-model="queryForm.postName"
                placeholder="请输入岗位名称"
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
            <el-form-item label="岗位编码">
              <el-input
                v-model="queryForm.postCode"
                placeholder="请输入岗位编码"
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
              <DictSelect
                v-model="queryForm.status"
                dict-type="sys_status"
                class="filter-control"
                clearable
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
            新增岗位
          </el-button>
        </template>
        <template #tools>
          <el-button
            :icon="Refresh"
            circle
            @click="fetchPostList"
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
            v-else-if="col.key === 'postName'"
            prop="postName"
            :label="col.label"
            min-width="120"
            show-overflow-tooltip
          />
          <el-table-column
            v-else-if="col.key === 'postCode'"
            prop="postCode"
            :label="col.label"
            min-width="120"
            show-overflow-tooltip
          />
          <el-table-column
            v-else-if="col.key === 'sort'"
            prop="sort"
            :label="col.label"
            width="80"
            align="center"
          />
          <el-table-column
            v-else-if="col.key === 'createTime'"
            prop="createTime"
            :label="col.label"
            width="180"
          />
          <el-table-column
            v-else-if="col.key === 'remark'"
            prop="remark"
            :label="col.label"
            min-width="150"
            show-overflow-tooltip
          />
        </template>

        <!-- 状态列 -->
        <el-table-column
          prop="status"
          label="状态"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <DictTag
              :options="sys_status"
              :value="row.status"
            />
          </template>
        </el-table-column>

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
      :title="dialogType === 'create' ? '新增岗位' : '编辑岗位'"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="岗位名称"
          prop="postName"
        >
          <el-input
            v-model="formData.postName"
            placeholder="请输入岗位名称"
          />
        </el-form-item>
        <el-form-item
          label="岗位编码"
          prop="postCode"
        >
          <el-input
            v-model="formData.postCode"
            placeholder="请输入岗位编码"
            :disabled="dialogType === 'update'"
          />
        </el-form-item>
        <el-form-item
          label="显示顺序"
          prop="sort"
        >
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :max="999"
          />
        </el-form-item>
        <el-form-item
          label="状态"
          prop="status"
        >
          <DictRadio
            v-model="formData.status"
            dict-type="sys_status"
          />
        </el-form-item>
        <el-form-item
          label="备注"
          prop="remark"
        >
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
