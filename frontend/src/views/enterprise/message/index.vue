<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { messageApi, type MessageVO, type MessageDTO } from '@/api/enterprise/message'
import type { PageResult } from '@/types'

// 列表数据
const loading = ref(false)
const tableData = ref<MessageVO[]>([])
const total = ref(0)
const unreadCount = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  isRead: undefined as number | undefined,
})

// 已读状态选项
const readStatusOptions = [
  { label: '未读', value: 0 },
  { label: '已读', value: 1 },
]

// 选中的消息
const selectedIds = ref<number[]>([])

// 对话框
const sendDialogVisible = ref(false)
const sendFormLoading = ref(false)
const sendFormData = reactive<MessageDTO>({
  receiverId: 0,
  title: '',
  content: '',
})

// 详情对话框
const detailVisible = ref(false)
const detailData = ref<MessageVO | null>(null)

// 计算未读消息数量
const unreadBadge = computed(() => unreadCount.value > 99 ? '99+' : unreadCount.value)

// 获取未读数量
const fetchUnreadCount = async () => {
  try {
    unreadCount.value = await messageApi.unreadCount()
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const params: MessageDTO = {
      page: queryParams.page,
      pageSize: queryParams.pageSize,
      isRead: queryParams.isRead,
    }
    const result = await messageApi.list(params) as PageResult<MessageVO>
    tableData.value = result.list || []
    total.value = result.total || 0
  } catch (error) {
    console.error('获取列表失败', error)
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.page = 1
  queryParams.isRead = undefined
  fetchData()
}

// 选择变化
const handleSelectionChange = (selection: MessageVO[]) => {
  selectedIds.value = selection.map((item) => item.id!)
}

// 查看详情
const handleView = async (row: MessageVO) => {
  try {
    detailData.value = await messageApi.getById(row.id!)
    detailVisible.value = true
    // 如果是未读消息，标记为已读
    if (row.isRead === 0) {
      await messageApi.markAsRead(row.id!)
      fetchUnreadCount()
      fetchData()
    }
  } catch (error) {
    console.error('获取详情失败', error)
    ElMessage.error('获取详情失败')
  }
}

// 标记单条为已读
const handleMarkRead = async (row: MessageVO) => {
  try {
    await messageApi.markAsRead(row.id!)
    ElMessage.success('已标记为已读')
    fetchUnreadCount()
    fetchData()
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败')
  }
}

// 批量标记为已读
const handleBatchRead = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要标记的消息')
    return
  }

  try {
    await messageApi.markAsReadByIds(selectedIds.value)
    ElMessage.success('批量标记成功')
    selectedIds.value = []
    fetchUnreadCount()
    fetchData()
  } catch (error) {
    console.error('批量标记失败', error)
    ElMessage.error('批量标记失败')
  }
}

// 全部标记为已读
const handleReadAll = async () => {
  try {
    await ElMessageBox.confirm('确定要将所有未读消息标记为已读吗？', '提示', {
      type: 'warning',
    })
    await messageApi.markAllAsRead()
    ElMessage.success('全部标记成功')
    selectedIds.value = []
    fetchUnreadCount()
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('全部标记失败', error)
      ElMessage.error('全部标记失败')
    }
  }
}

// 删除消息
const handleDelete = async (row: MessageVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该消息吗？', '提示', {
      type: 'warning',
    })
    await messageApi.delete(row.id!)
    ElMessage.success('删除成功')
    if (row.isRead === 0) {
      fetchUnreadCount()
    }
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 打开发送对话框
const handleOpenSend = () => {
  Object.assign(sendFormData, {
    receiverId: 0,
    title: '',
    content: '',
  })
  sendDialogVisible.value = true
}

// 发送消息
const handleSend = async () => {
  if (!sendFormData.receiverId) {
    ElMessage.warning('请输入接收人ID')
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
    fetchData()
  } catch (error) {
    console.error('发送失败', error)
    ElMessage.error('发送失败')
  } finally {
    sendFormLoading.value = false
  }
}

// 分页
const handlePageChange = (page: number) => {
  queryParams.page = page
  fetchData()
}

const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  queryParams.page = 1
  fetchData()
}

// 格式化已读状态
const formatReadStatus = (isRead?: number) => {
  const option = readStatusOptions.find((item) => item.value === isRead)
  return option?.label || '-'
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  fetchData()
  fetchUnreadCount()
})
</script>

<template>
  <div class="message-content">
    <!-- 搜索区域 -->
    <el-card class="mb-4">
      <el-form
        :inline="true"
        :model="queryParams"
      >
        <el-form-item label="阅读状态">
          <el-select
            v-model="queryParams.isRead"
            placeholder="请选择状态"
            clearable
          >
            <el-option
              v-for="item in readStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="handleSearch"
          >
            搜索
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card>
      <template #header>
        <div class="flex justify-between items-center">
          <span class="font-semibold">消息列表</span>
          <div class="flex gap-2">
            <el-button
              type="success"
              :disabled="selectedIds.length === 0"
              @click="handleBatchRead"
            >
              批量已读
            </el-button>
            <el-button
              type="warning"
              :disabled="unreadCount === 0"
              @click="handleReadAll"
            >
              全部已读
            </el-button>
            <el-button
              type="primary"
              @click="handleOpenSend"
            >
              发送消息
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="50"
        />
        <el-table-column
          prop="id"
          label="ID"
          width="80"
        />
        <el-table-column
          prop="title"
          label="标题"
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
          prop="senderName"
          label="发送人"
          width="120"
        />
        <el-table-column
          prop="isRead"
          label="状态"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.isRead === 1 ? 'success' : 'danger'">
              {{ formatReadStatus(row.isRead) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="readTime"
          label="阅读时间"
          width="180"
        >
          <template #default="{ row }">
            {{ formatTime(row.readTime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="发送时间"
          width="180"
        >
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
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
              v-if="row.isRead === 0"
              type="success"
              link
              @click="handleMarkRead(row)"
            >
              标记已读
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

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
          label="接收人ID"
          required
        >
          <el-input-number
            v-model="sendFormData.receiverId"
            :min="1"
            placeholder="请输入接收人ID"
            style="width: 100%"
          />
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

<style scoped>
.message-content {
  /* content only */
}
</style>
