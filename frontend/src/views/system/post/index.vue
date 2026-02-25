<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import { getAiPermRBACAPI } from '@/api/generated'
import type { SysPost, PostDTO, PageResultSysPost } from '@/models'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppHeader from '@/components/layout/AppHeader.vue'

const api = getAiPermRBACAPI()

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref<SysPost[]>([])

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
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      postName: queryForm.postName || undefined,
      postCode: queryForm.postCode || undefined,
      status: queryForm.status,
    }
    const { data } = await api.page2(params)
    if (data && data.data) {
      const result = data.data as PageResultSysPost
      tableData.value = result.records || []
      pagination.total = result.total || 0
    }
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
function handleUpdate(row: SysPost) {
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
async function handleDelete(row: SysPost) {
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

    await api.delete2(row.id!)
    ElMessage.success('删除成功')
    fetchPostList()
  }
  catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除岗位失败:', error)
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value)
    return

  try {
    await formRef.value.validate()

    if (dialogType.value === 'create') {
      await api.create2(formData)
      ElMessage.success('创建成功')
    }
    else {
      await api.update2(currentId.value, formData)
      ElMessage.success('更新成功')
    }

    dialogVisible.value = false
    fetchPostList()
  }
  catch (error: any) {
    if (error !== false) {
      console.error('保存岗位失败:', error)
      ElMessage.error(error?.response?.data?.message || '保存失败')
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

// 获取状态标签类型
function getStatusType(status: number): 'success' | 'danger' {
  return status === 0 ? 'success' : 'danger'
}

// 获取状态文本
function getStatusText(status: number): string {
  return status === 0 ? '正常' : '停用'
}

// 页面加载
onMounted(() => {
  fetchPostList()
})
</script>

<template>
  <div class="post-container flex h-screen">
    <!-- 侧边栏 -->
    <AppSidebar />

    <!-- 主内容区 -->
    <main class="flex-1 flex flex-col overflow-hidden">
      <!-- 顶部导航 -->
      <AppHeader>
        <template #title>
          岗位管理
        </template>
      </AppHeader>

      <!-- 内容区 -->
      <div class="flex-1 p-6 bg-gray-50 overflow-y-auto">
        <!-- 搜索区域 -->
        <el-card class="mb-4">
          <el-form
            :inline="true"
            :model="queryForm"
          >
            <el-form-item label="岗位名称">
              <el-input
                v-model="queryForm.postName"
                placeholder="请输入岗位名称"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="岗位编码">
              <el-input
                v-model="queryForm.postCode"
                placeholder="请输入岗位编码"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                v-model="queryForm.status"
                placeholder="请选择状态"
                clearable
              >
                <el-option
                  label="正常"
                  :value="0"
                />
                <el-option
                  label="停用"
                  :value="1"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
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
          </el-form>
        </el-card>

        <!-- 表格区域 -->
        <el-card>
          <template #header>
            <div class="flex justify-between items-center">
              <span class="font-semibold">岗位列表</span>
              <el-button
                type="primary"
                :icon="Plus"
                @click="handleCreate"
              >
                新增岗位
              </el-button>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="tableData"
            border
          >
            <el-table-column
              prop="id"
              label="岗位ID"
              width="100"
              align="center"
            />
            <el-table-column
              prop="postName"
              label="岗位名称"
              min-width="150"
            />
            <el-table-column
              prop="postCode"
              label="岗位编码"
              min-width="150"
            />
            <el-table-column
              prop="sort"
              label="排序"
              width="80"
              align="center"
            />
            <el-table-column
              prop="status"
              label="状态"
              width="100"
              align="center"
            >
              <template #default="{ row }">
                <el-tag
                  :type="getStatusType(row.status)"
                  size="small"
                >
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              prop="createTime"
              label="创建时间"
              width="180"
            />
            <el-table-column
              prop="remark"
              label="备注"
              min-width="150"
              show-overflow-tooltip
            />
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
      </div>
    </main>

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
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">
              正常
            </el-radio>
            <el-radio :value="1">
              停用
            </el-radio>
          </el-radio-group>
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

<style scoped>
.post-container {
  background: #f5f7fa;
}
</style>
