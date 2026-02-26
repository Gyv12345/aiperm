<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import { deptApi, type DeptVO, type DeptDTO } from '@/api/system/dept'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppHeader from '@/components/layout/AppHeader.vue'

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 部门树数据
const deptTree = ref<DeptVO[]>([])

// 对话框显示状态
const dialogVisible = ref(false)

// 对话框类型：create / update
const dialogType = ref<'create' | 'update'>('create')

// 当前编辑的部门ID
const currentId = ref<number>(0)

// 表单数据
const formData = reactive<DeptDTO>({
  deptName: '',
  parentId: 0,
  sort: 0,
  leader: '',
  phone: '',
  email: '',
  status: 0,
  remark: '',
})

// 查询表单
const queryForm = reactive({
  deptName: '',
  status: undefined as number | undefined,
})

// 部门树选项（用于选择父部门）
const deptOptions = computed(() => {
  const options: Array<{ value: number; label: string; disabled?: boolean }> = [
    { value: 0, label: '根部门' },
  ]
  const addOptions = (depts: DeptVO[], level = 0) => {
    depts.forEach((dept) => {
      options.push({
        value: dept.id || 0,
        label: `${'　'.repeat(level)}${dept.deptName}`,
        disabled: dialogType.value === 'update' && dept.id === currentId.value,
      })
      if (dept.children && dept.children.length > 0) {
        addOptions(dept.children, level + 1)
      }
    })
  }
  addOptions(deptTree.value)
  return options
})

// 表单验证规则
const rules = computed<FormRules>(() => ({
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '部门名称长度为 2-50 个字符', trigger: 'blur' },
  ],
  parentId: [
    { required: true, message: '请选择父部门', trigger: 'change' },
  ],
  sort: [
    { type: 'number', message: '排序必须为数字', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
}))

// 获取部门树
async function fetchDeptTree() {
  loading.value = true
  try {
    deptTree.value = await deptApi.tree()
  }
  catch (error) {
    console.error('获取部门树失败:', error)
    ElMessage.error('获取部门树失败')
  }
  finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  fetchDeptTree()
}

// 重置搜索
function handleReset() {
  queryForm.deptName = ''
  queryForm.status = undefined
  fetchDeptTree()
}

// 新增部门
function handleCreate(parentId = 0) {
  dialogType.value = 'create'
  currentId.value = 0
  Object.assign(formData, {
    deptName: '',
    parentId,
    sort: 0,
    leader: '',
    phone: '',
    email: '',
    status: 0,
    remark: '',
  })
  dialogVisible.value = true
}

// 编辑部门
function handleUpdate(row: DeptVO) {
  dialogType.value = 'update'
  currentId.value = row.id || 0
  Object.assign(formData, {
    deptName: row.deptName || '',
    parentId: row.parentId || 0,
    sort: row.sort || 0,
    leader: row.leader || '',
    phone: row.phone || '',
    email: row.email || '',
    status: row.status || 0,
    remark: row.remark || '',
  })
  dialogVisible.value = true
}

// 删除部门
async function handleDelete(row: DeptVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门「${row.deptName}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await deptApi.delete(row.id!)
    ElMessage.success('删除成功')
    fetchDeptTree()
  }
  catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除部门失败:', error)
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
      await deptApi.create(formData)
      ElMessage.success('创建成功')
    }
    else {
      await deptApi.update(currentId.value, formData)
      ElMessage.success('更新成功')
    }

    dialogVisible.value = false
    fetchDeptTree()
  }
  catch (error: any) {
    if (error !== false) {
      console.error('保存部门失败:', error)
      ElMessage.error(error?.response?.data?.message || '保存失败')
    }
  }
}

// 关闭对话框
function handleDialogClose() {
  formRef.value?.resetFields()
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
  fetchDeptTree()
})
</script>

<template>
  <div class="dept-container flex h-screen">
    <!-- 侧边栏 -->
    <AppSidebar />

    <!-- 主内容区 -->
    <main class="flex-1 flex flex-col overflow-hidden">
      <!-- 顶部导航 -->
      <AppHeader>
        <template #title>
          部门管理
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
            <el-form-item label="部门名称">
              <el-input
                v-model="queryForm.deptName"
                placeholder="请输入部门名称"
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
              <span class="font-semibold">部门列表</span>
              <el-button
                type="primary"
                :icon="Plus"
                @click="handleCreate()"
              >
                新增部门
              </el-button>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="deptTree"
            row-key="id"
            border
            default-expand-all
          >
            <el-table-column
              prop="deptName"
              label="部门名称"
              min-width="200"
            />
            <el-table-column
              prop="leader"
              label="负责人"
              width="120"
            />
            <el-table-column
              prop="phone"
              label="联系电话"
              width="140"
            />
            <el-table-column
              prop="email"
              label="邮箱"
              min-width="180"
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
              label="操作"
              width="200"
              fixed="right"
            >
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  :icon="Plus"
                  @click="handleCreate(row.id)"
                >
                  新增
                </el-button>
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
        </el-card>
      </div>
    </main>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '新增部门' : '编辑部门'"
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
          label="上级部门"
          prop="parentId"
        >
          <el-select
            v-model="formData.parentId"
            placeholder="请选择上级部门"
            class="w-full"
          >
            <el-option
              v-for="item in deptOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
              :disabled="item.disabled"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="部门名称"
          prop="deptName"
        >
          <el-input
            v-model="formData.deptName"
            placeholder="请输入部门名称"
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
          label="负责人"
          prop="leader"
        >
          <el-input
            v-model="formData.leader"
            placeholder="请输入负责人"
          />
        </el-form-item>
        <el-form-item
          label="联系电话"
          prop="phone"
        >
          <el-input
            v-model="formData.phone"
            placeholder="请输入联系电话"
          />
        </el-form-item>
        <el-form-item
          label="邮箱"
          prop="email"
        >
          <el-input
            v-model="formData.email"
            placeholder="请输入邮箱"
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
.dept-container {
  background: #f5f7fa;
}
</style>
