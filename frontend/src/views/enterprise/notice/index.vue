<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noticeApi, type NoticeVO, type NoticeDTO } from '@/api/enterprise/notice'
import type { PageResult } from '@/types'

// 列表数据
const loading = ref(false)
const tableData = ref<NoticeVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  noticeTitle: '',
  noticeType: undefined as number | undefined,
  status: undefined as number | undefined,
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formData = reactive<NoticeDTO>({
  noticeTitle: '',
  noticeContent: '',
  noticeType: 1,
  status: 0,
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

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const params: NoticeDTO = {
      page: queryParams.page,
      pageSize: queryParams.pageSize,
      noticeTitle: queryParams.noticeTitle || undefined,
      noticeType: queryParams.noticeType,
      status: queryParams.status,
    }
    const result = await noticeApi.list(params) as PageResult<NoticeVO>
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
  queryParams.noticeTitle = ''
  queryParams.noticeType = undefined
  queryParams.status = undefined
  fetchData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增公告'
  Object.assign(formData, {
    noticeTitle: '',
    noticeContent: '',
    noticeType: 1,
    status: 0,
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: NoticeVO) => {
  dialogTitle.value = '编辑公告'
  Object.assign(formData, {
    noticeTitle: row.noticeTitle,
    noticeContent: row.noticeContent,
    noticeType: row.noticeType,
    status: row.status,
  })
  dialogVisible.value = true
  // 存储编辑的 ID
  ;(formData as any).id = row.id
}

// 提交表单
const handleSubmit = async () => {
  if (!formData.noticeTitle) {
    ElMessage.warning('请输入标题')
    return
  }

  formLoading.value = true
  try {
    if ((formData as any).id) {
      // 编辑
      await noticeApi.update((formData as any).id, formData)
      ElMessage.success('更新成功')
    } else {
      // 新增
      await noticeApi.create(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('保存失败', error)
    ElMessage.error('保存失败')
  } finally {
    formLoading.value = false
  }
}

// 发布
const handlePublish = async (row: NoticeVO) => {
  try {
    await ElMessageBox.confirm('确定要发布该公告吗？', '提示', {
      type: 'warning',
    })
    await noticeApi.publish(row.id!)
    ElMessage.success('发布成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败', error)
      ElMessage.error('发布失败')
    }
  }
}

// 撤回
const handleWithdraw = async (row: NoticeVO) => {
  try {
    await ElMessageBox.confirm('确定要撤回该公告吗？', '提示', {
      type: 'warning',
    })
    await noticeApi.withdraw(row.id!)
    ElMessage.success('撤回成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回失败', error)
      ElMessage.error('撤回失败')
    }
  }
}

// 删除
const handleDelete = async (row: NoticeVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      type: 'warning',
    })
    await noticeApi.delete(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 查看详情
const detailVisible = ref(false)
const detailData = ref<NoticeVO | null>(null)

const handleView = async (row: NoticeVO) => {
  try {
    detailData.value = await noticeApi.getById(row.id!)
    detailVisible.value = true
  } catch (error) {
    console.error('获取详情失败', error)
    ElMessage.error('获取详情失败')
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

// 格式化类型
const formatType = (type?: number) => {
  const option = typeOptions.find((item) => item.value === type)
  return option?.label || '-'
}

// 格式化状态
const formatStatus = (status?: number) => {
  const option = statusOptions.find((item) => item.value === status)
  return option?.label || '-'
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="notice-content">
    <!-- 搜索区域 -->
    <el-card class="mb-4">
      <el-form
        :inline="true"
        :model="queryParams"
      >
        <el-form-item label="标题">
          <el-input
            v-model="queryParams.noticeTitle"
            placeholder="请输入标题"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select
            v-model="queryParams.noticeType"
            placeholder="请选择类型"
            clearable
          >
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
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
          <span class="font-semibold">公告列表</span>
          <el-button
            type="primary"
            @click="handleAdd"
          >
            新增公告
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
      >
        <el-table-column
          prop="id"
          label="ID"
          width="80"
        />
        <el-table-column
          prop="noticeTitle"
          label="标题"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="noticeType"
          label="类型"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.noticeType === 1 ? 'primary' : 'success'">
              {{ formatType(row.noticeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="publishTime"
          label="发布时间"
          width="180"
        >
          <template #default="{ row }">
            {{ formatTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="createBy"
          label="创建人"
          width="120"
        />
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
        >
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        :model="formData"
        label-width="80px"
      >
        <el-form-item
          label="标题"
          required
        >
          <el-input
            v-model="formData.noticeTitle"
            placeholder="请输入标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select
            v-model="formData.noticeType"
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
            v-model="formData.noticeContent"
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
          :loading="formLoading"
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
          {{ detailData.noticeTitle }}
        </el-descriptions-item>
        <el-descriptions-item label="类型">
          {{ formatType(detailData.noticeType) }}
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
            {{ detailData.noticeContent }}
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
.notice-content {
  /* content only */
}
</style>
