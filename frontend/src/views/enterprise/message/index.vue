<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {messageApi, type MessageDTO, type MessageReceiverVO, type MessageVO} from '@/api/enterprise/message'
import type {PageResult, TableColumn} from '@/types'

// 表格列配置
const columns = ref<TableColumn[]>([
  { key: 'id', label: 'ID', visible: true, fixed: 'left' },
  { key: 'title', label: '标题', visible: true },
  { key: 'senderName', label: '发送人', visible: true },
  { key: 'receiverName', label: '接收人', visible: true },
  { key: 'readTime', label: '阅读时间', visible: true },
  { key: 'createTime', label: '发送时间', visible: true },
])

// 表格引用
const tableRef = ref()

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref<MessageVO[]>([])

// 未读数量
const unreadCount = ref(0)

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 查询表单
const queryForm = reactive({
  boxType: 1 as number, // 1-收件箱 2-发件箱
  isRead: undefined as number | undefined,
})

const boxTypeOptions = [
  { label: '收件箱', value: 1 },
  { label: '发件箱', value: 2 },
]

const isOutbox = computed(() => queryForm.boxType === 2)

const visibleColumns = computed(() =>
  columns.value
    .filter(c => c.visible)
    .filter((c) => {
      if (c.key === 'senderName') return !isOutbox.value
      if (c.key === 'receiverName') return isOutbox.value
      return true
    }),
)

// 已读状态选项
const readStatusOptions = [
  { label: '未读', value: 0 },
  { label: '已读', value: 1 },
]

// 多选
const selectedRows = ref<MessageVO[]>([])
function handleSelectionChange(rows: MessageVO[]) {
  selectedRows.value = rows
}

// 批量删除
async function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的消息')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条消息吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    const ids = selectedRows.value.map(row => row.id!)
    await messageApi.deleteBatch(ids)
    ElMessage.success('批量删除成功')
    tableRef.value?.clearSelection()
    fetchUnreadCount()
    fetchMessageList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 批量标记为已读
async function handleBatchRead() {
  if (isOutbox.value) {
    return
  }
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要标记的消息')
    return
  }

  try {
    const ids = selectedRows.value.map(row => row.id!)
    await messageApi.markAsReadByIds(ids)
    ElMessage.success('批量标记成功')
    tableRef.value?.clearSelection()
    fetchUnreadCount()
    fetchMessageList()
  }
  catch (error) {
    console.error('批量标记失败:', error)
    ElMessage.error('批量标记失败')
  }
}

// 全部标记为已读
async function handleReadAll() {
  if (isOutbox.value) {
    return
  }
  try {
    await ElMessageBox.confirm('确定要将所有未读消息标记为已读吗？', '提示', {
      type: 'warning',
    })
    await messageApi.markAllAsRead()
    ElMessage.success('全部标记成功')
    tableRef.value?.clearSelection()
    fetchUnreadCount()
    fetchMessageList()
  }
  catch (error) {
    if (error !== 'cancel') {
      console.error('全部标记失败:', error)
      ElMessage.error('全部标记失败')
    }
  }
}

// 发送消息对话框
const sendDialogVisible = ref(false)
const sendFormLoading = ref(false)
const receiverLoading = ref(false)
const receiverOptions = ref<MessageReceiverVO[]>([])
const sendFormData = reactive<MessageDTO>({
  receiverId: undefined,
  title: '',
  content: '',
})

// 详情对话框
const detailVisible = ref(false)
const detailData = ref<MessageVO | null>(null)

// 获取未读数量
async function fetchUnreadCount() {
  try {
    unreadCount.value = await messageApi.unreadCount()
  }
  catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

// 获取消息列表
async function fetchMessageList() {
  loading.value = true
  try {
    const params: MessageDTO = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      boxType: queryForm.boxType,
      isRead: queryForm.isRead,
    }
    const result = await messageApi.list(params) as PageResult<MessageVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取消息列表失败:', error)
    ElMessage.error('获取消息列表失败')
  }
  finally {
    loading.value = false
  }
}

async function fetchReceiverOptions() {
  receiverLoading.value = true
  try {
    receiverOptions.value = await messageApi.receivers()
  }
  catch (error) {
    console.error('获取接收人列表失败:', error)
    ElMessage.error('获取接收人列表失败')
  }
  finally {
    receiverLoading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchMessageList()
}

// 重置
function handleReset() {
  queryForm.boxType = 1
  queryForm.isRead = undefined
  pagination.page = 1
  fetchMessageList()
}

function handleBoxTypeChange() {
  queryForm.isRead = undefined
  pagination.page = 1
  tableRef.value?.clearSelection()
  fetchMessageList()
}

// 查看详情
async function handleView(row: MessageVO) {
  try {
    detailData.value = await messageApi.getById(row.id!)
    detailVisible.value = true
    // 如果是未读消息，标记为已读
    if (!isOutbox.value && row.isRead === 0) {
      await messageApi.markAsRead(row.id!)
      fetchUnreadCount()
      fetchMessageList()
    }
  }
  catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 标记单条为已读
async function handleMarkRead(row: MessageVO) {
  try {
    await messageApi.markAsRead(row.id!)
    ElMessage.success('已标记为已读')
    fetchUnreadCount()
    fetchMessageList()
  }
  catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除消息
async function handleDelete(row: MessageVO) {
  try {
    await ElMessageBox.confirm('确定要删除该消息吗？', '提示', {
      type: 'warning',
    })
    await messageApi.delete(row.id!)
    ElMessage.success('删除成功')
    if (!isOutbox.value && row.isRead === 0) {
      fetchUnreadCount()
    }
    fetchMessageList()
  }
  catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 打开发送对话框
async function handleOpenSend() {
  Object.assign(sendFormData, {
    receiverId: undefined,
    title: '',
    content: '',
  })
  await fetchReceiverOptions()
  sendDialogVisible.value = true
}

// 发送消息
async function handleSend() {
  if (!sendFormData.receiverId) {
    ElMessage.warning('请选择接收人')
    return
  }
  if (!sendFormData.title) {
    ElMessage.warning('请输入消息标题')
    return
  }

  sendFormLoading.value = true
  try {
    await messageApi.send(sendFormData)
    ElMessage.success('发送成功')
    sendDialogVisible.value = false
    fetchMessageList()
  }
  catch (error) {
    console.error('发送失败:', error)
    ElMessage.error('发送失败')
  }
  finally {
    sendFormLoading.value = false
  }
}

// 分页
function handlePageChange(page: number) {
  pagination.page = page
  fetchMessageList()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchMessageList()
}

// 格式化已读状态
function formatReadStatus(isRead?: number) {
  const option = readStatusOptions.find(item => item.value === isRead)
  return option?.label || '-'
}

// 格式化时间
function formatTime(time?: string) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

function formatReceiverLabel(receiver: MessageReceiverVO) {
  if (receiver.displayName && receiver.displayName !== receiver.username) {
    return `${receiver.displayName} (${receiver.username})`
  }
  return receiver.displayName || receiver.username
}

// 页面加载
onMounted(() => {
  fetchMessageList()
  fetchUnreadCount()
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
            <el-form-item label="消息箱体">
              <el-select
                v-model="queryForm.boxType"
                placeholder="请选择箱体"
                class="filter-control"
                @change="handleBoxTypeChange"
              >
                <el-option
                  v-for="item in boxTypeOptions"
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
            :md="8"
            :lg="6"
          >
            <el-form-item :label="isOutbox ? '对方阅读' : '阅读状态'">
              <el-select
                v-model="queryForm.isRead"
                placeholder="请选择状态"
                clearable
                class="filter-control"
              >
                <el-option
                  v-for="item in readStatusOptions"
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
            v-if="!isOutbox"
            type="success"
            :disabled="selectedRows.length === 0"
            @click="handleBatchRead"
          >
            批量已读
          </el-button>
          <el-button
            v-if="!isOutbox"
            type="warning"
            :disabled="unreadCount === 0"
            @click="handleReadAll"
          >
            全部已读
          </el-button>
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleOpenSend"
          >
            发送消息
          </el-button>
        </template>
        <template #tools>
          <el-button
            :icon="Refresh"
            circle
            @click="fetchMessageList"
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
          >
            <template #default="{ row }">
              <div class="flex items-center gap-2">
                <el-badge
                  v-if="row.isRead === 0"
                  is-dot
                  type="danger"
                />
                <span :class="{ 'font-bold': row.isRead === 0 }">{{ row.title }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'senderName'"
            prop="senderName"
            :label="col.label"
            width="120"
          />
          <el-table-column
            v-else-if="col.key === 'receiverName'"
            prop="receiverName"
            :label="col.label"
            width="120"
          >
            <template #default="{ row }">
              {{ row.receiverName || row.receiverId }}
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'readTime'"
            prop="readTime"
            :label="col.label"
            width="180"
          >
            <template #default="{ row }">
              {{ formatTime(row.readTime) }}
            </template>
          </el-table-column>
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
          prop="isRead"
          label="状态"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="row.isRead === 1 ? 'success' : 'danger'">
              {{ formatReadStatus(row.isRead) }}
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
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              v-if="!isOutbox && row.isRead === 0"
              type="success"
              link
              @click="handleMarkRead(row)"
            >
              标记已读
            </el-button>
            <el-button
              v-if="!isOutbox"
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
        v-if="!isOutbox"
        type="success"
        size="small"
        @click="handleBatchRead"
      >
        批量已读
      </el-button>
      <el-button
        v-if="!isOutbox"
        type="danger"
        size="small"
        :icon="Delete"
        @click="handleBatchDelete"
      >
        批量删除
      </el-button>
    </SelectionBar>

    <!-- 发送消息对话框 -->
    <el-dialog
      v-model="sendDialogVisible"
      title="发送消息"
      width="500px"
    >
      <el-form
        :model="sendFormData"
        label-width="80px"
      >
        <el-form-item
          label="接收人"
          required
        >
          <el-select
            v-model="sendFormData.receiverId"
            filterable
            clearable
            :loading="receiverLoading"
            no-data-text="暂无可选员工"
            placeholder="请选择接收人"
            style="width: 100%"
          >
            <el-option
              v-for="receiver in receiverOptions"
              :key="receiver.id"
              :label="formatReceiverLabel(receiver)"
              :value="receiver.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="标题"
          required
        >
          <el-input
            v-model="sendFormData.title"
            placeholder="请输入消息标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="sendFormData.content"
            type="textarea"
            :rows="4"
            placeholder="请输入消息内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="sendFormLoading"
          @click="handleSend"
        >
          发送
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="消息详情"
      width="500px"
    >
      <el-descriptions
        v-if="detailData"
        :column="1"
        border
      >
        <el-descriptions-item label="标题">
          {{ detailData.title }}
        </el-descriptions-item>
        <el-descriptions-item label="发送人">
          {{ detailData.senderName }}
        </el-descriptions-item>
        <el-descriptions-item label="接收人">
          {{ detailData.receiverName || detailData.receiverId }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.isRead === 1 ? 'success' : 'danger'">
            {{ formatReadStatus(detailData.isRead) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发送时间">
          {{ formatTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="阅读时间">
          {{ formatTime(detailData.readTime) }}
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
