<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import { noticeApi, type NoticeVO, type NoticeDTO } from '@/api/enterprise/notice'
import type { PageResult, TableColumn } from '@/types'

// 表格列配置
const columns = ref<TableColumn[]>([
  { key: 'id', label: 'ID', visible: true, fixed: 'left' },
  { key: 'title', label: '标题', visible: true },
  { key: 'type', label: '类型', visible: true },
  { key: 'publishTime', label: '发布时间', visible: true },
  { key: 'createBy', label: '创建人', visible: true },
  { key: 'createTime', label: '创建时间', visible: true },
])

const visibleColumns = computed(() => columns.value.filter(c => c.visible))

// 表格引用
const tableRef = ref()

// 加载状态
const loading = ref(false)
const formLoading = ref(false)

// 表格数据
const tableData = ref<NoticeVO[]>([])

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 查询表单
const queryForm = reactive({
  title: '',
  type: undefined as number | undefined,
  status: undefined as number | undefined,
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const currentId = ref<number>(0)

// 表单数据
const formData = reactive<NoticeDTO>({
  title: '',
  content: '',
  type: 1,
  status: 0,
  page: 1,
  pageSize: 10,
})

// 类型选项
const typeOptions = [
  { label: '通知', value: 1 },
  { label: '公告', value: 2 },
]

// 状态选项
const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
]

// 多选
const selectedRows = ref<NoticeVO[]>([])
function handleSelectionChange(rows: NoticeVO[]) {
  selectedRows.value = rows
}

// 批量删除
async function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的公告')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条公告吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    const ids = selectedRows.value.map(row => row.id!)
    await noticeApi.deleteBatch(ids)
    ElMessage.success('批量删除成功')
    tableRef.value?.clearSelection()
    fetchNoticeList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 获取公告列表
async function fetchNoticeList() {
  loading.value = true
  try {
    const params: NoticeDTO = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      title: queryForm.title || undefined,
      type: queryForm.type,
      status: queryForm.status,
    }
    const result = await noticeApi.list(params) as PageResult<NoticeVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
  }
  finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchNoticeList()
}

// 重置
function handleReset() {
  queryForm.title = ''
  queryForm.type = undefined
  queryForm.status = undefined
  pagination.page = 1
  fetchNoticeList()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增公告'
  currentId.value = 0
  Object.assign(formData, {
    title: '',
    content: '',
    type: 1,
    status: 0,
  })
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: NoticeVO) {
  dialogTitle.value = '编辑公告'
  currentId.value = row.id || 0
  Object.assign(formData, {
    title: row.title,
    content: row.content,
    type: row.type,
    status: row.status,
  })
  dialogVisible.value = true
}

// 提交表单
async function handleSubmit() {
  if (!formData.title) {
    ElMessage.warning('请输入标题')
    return
  }

  formLoading.value = true
  try {
    if (currentId.value) {
      await noticeApi.update(currentId.value, formData)
      ElMessage.success('更新成功')
    }
    else {
      await noticeApi.create(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchNoticeList()
  }
  catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
  finally {
    formLoading.value = false
  }
}

// 发布
async function handlePublish(row: NoticeVO) {
  try {
    await ElMessageBox.confirm('确定要发布该公告吗？', '提示', {
      type: 'warning',
    })
    await noticeApi.publish(row.id!)
    ElMessage.success('发布成功')
    fetchNoticeList()
  }
  catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

// 撤回
async function handleWithdraw(row: NoticeVO) {
  try {
    await ElMessageBox.confirm('确定要撤回该公告吗？', '提示', {
      type: 'warning',
    })
    await noticeApi.withdraw(row.id!)
    ElMessage.success('撤回成功')
    fetchNoticeList()
  }
  catch (error) {
    if (error !== 'cancel') {
      console.error('撤回失败:', error)
      ElMessage.error('撤回失败')
    }
  }
}

// 删除
async function handleDelete(row: NoticeVO) {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      type: 'warning',
    })
    await noticeApi.delete(row.id!)
    ElMessage.success('删除成功')
    fetchNoticeList()
  }
  catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 查看详情
const detailVisible = ref(false)
const detailData = ref<NoticeVO | null>(null)

async function handleView(row: NoticeVO) {
  try {
    detailData.value = await noticeApi.getById(row.id!)
    detailVisible.value = true
  }
  catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 分页
function handlePageChange(page: number) {
  pagination.page = page
  fetchNoticeList()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchNoticeList()
}

// 格式化类型
function formatType(type?: number) {
  const option = typeOptions.find(item => item.value === type)
  return option?.label || '-'
}

// 格式化状态
function formatStatus(status?: number) {
  const option = statusOptions.find(item => item.value === status)
  return option?.label || '-'
}

// 格式化时间
function formatTime(time?: string) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

// 页面加载
onMounted(() => {
  fetchNoticeList()
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
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="标题">
              <el-input
                v-model="queryForm.title"
                placeholder="请输入标题"
                clearable
                class="filter-control"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="类型">
              <el-select
                v-model="queryForm.type"
                placeholder="请选择类型"
                clearable
                class="filter-control"
              >
                <el-option
                  v-for="item in typeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
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
          <el-col :xs="24" :sm="12" :md="24" :lg="6">
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
            @click="handleAdd"
          >
            新增公告
          </el-button>
        </template>
        <template #tools>
          <el-button
            :icon="Refresh"
            circle
            @click="fetchNoticeList"
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
            v-else-if="col.key === 'title'"
            prop="title"
            :label="col.label"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column
            v-else-if="col.key === 'type'"
            prop="type"
            :label="col.label"
            width="100"
            align="center"
          >
            <template #default="{ row }">
              <el-tag :type="row.type === 1 ? 'primary' : 'success'">
                {{ formatType(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'publishTime'"
            prop="publishTime"
            :label="col.label"
            width="180"
          >
            <template #default="{ row }">
              {{ formatTime(row.publishTime) }}
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'createBy'"
            prop="createBy"
            :label="col.label"
            width="120"
          />
          <el-table-column
            v-else-if="col.key === 'createTime'"
            prop="createTime"
            :label="col.label"
            width="180"
          >
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
        </template>

        <!-- 状态列 -->
        <el-table-column
          prop="status"
          label="状态"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column
          label="操作"
          width="280"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              type="primary"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              @click="handleWithdraw(row)"
            >
              撤回
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
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        v-loading="formLoading"
        :model="formData"
        label-width="80px"
      >
        <el-form-item
          label="标题"
          required
        >
          <el-input
            v-model="formData.title"
            placeholder="请输入标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select
            v-model="formData.type"
            placeholder="请选择类型"
          >
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="6"
            placeholder="请输入内容"
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="公告详情"
      width="600px"
    >
      <el-descriptions
        v-if="detailData"
        :column="1"
        border
      >
        <el-descriptions-item label="标题">
          {{ detailData.title }}
        </el-descriptions-item>
        <el-descriptions-item label="类型">
          {{ formatType(detailData.type) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ formatStatus(detailData.status) }}
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">
          {{ formatTime(detailData.publishTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建人">
          {{ detailData.createBy }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="内容">
          <div class="whitespace-pre-wrap">
            {{ detailData.content }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
