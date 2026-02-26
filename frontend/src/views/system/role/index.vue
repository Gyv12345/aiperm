<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import { roleApi, type RoleVO, type RoleDTO } from '@/api/system/role'
import { menuApi, type MenuVO } from '@/api/system/menu'
import type { PageResult } from '@/types'

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref<RoleVO[]>([])

// 菜单树数据
const menuTree = ref<MenuVO[]>([])

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 对话框显示状态
const dialogVisible = ref(false)
const assignMenuDialogVisible = ref(false)

// 对话框类型：create / update
const dialogType = ref<'create' | 'update'>('create')

// 当前编辑的角色ID
const currentId = ref<number>(0)

// 表单数据
const formData = reactive<RoleDTO>({
  roleName: '',
  roleCode: '',
  sort: 0,
  status: 0,
  remark: '',
})

// 分配菜单表单
const assignMenuForm = reactive({
  roleId: 0,
  menuIds: [] as number[],
})

// 菜单树选中的节点
const menuTreeRef = ref()
const checkedMenuIds = ref<number[]>([])

// 查询表单
const queryForm = reactive({
  roleName: '',
  roleCode: '',
  status: undefined as number | undefined,
})

// 状态选项
const statusOptions = [
  { value: 0, label: '正常' },
  { value: 1, label: '停用' },
]

// 表单验证规则
const rules = computed<FormRules>(() => ({
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 100, message: '角色名称长度为 2-100 个字符', trigger: 'blur' },
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 2, max: 100, message: '角色编码长度为 2-100 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '角色编码必须以字母开头，只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  sort: [
    { type: 'number', message: '排序必须为数字', trigger: 'blur' },
  ],
}))

// 获取角色列表
async function fetchRoleList() {
  loading.value = true
  try {
    const params: RoleDTO = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      roleName: queryForm.roleName || undefined,
      roleCode: queryForm.roleCode || undefined,
      status: queryForm.status,
    }
    const result = await roleApi.list(params) as PageResult<RoleVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  }
  finally {
    loading.value = false
  }
}

// 获取菜单树
async function fetchMenuTree() {
  try {
    menuTree.value = await menuApi.tree()
  }
  catch (error) {
    console.error('获取菜单树失败:', error)
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchRoleList()
}

// 重置搜索
function handleReset() {
  queryForm.roleName = ''
  queryForm.roleCode = ''
  queryForm.status = undefined
  pagination.page = 1
  fetchRoleList()
}

// 新增角色
function handleCreate() {
  dialogType.value = 'create'
  currentId.value = 0
  Object.assign(formData, {
    roleName: '',
    roleCode: '',
    sort: 0,
    status: 0,
    remark: '',
  })
  dialogVisible.value = true
}

// 编辑角色
async function handleUpdate(row: RoleVO) {
  dialogType.value = 'update'
  currentId.value = row.id || 0
  formLoading.value = true
  dialogVisible.value = true

  try {
    const role = await roleApi.getById(row.id!)
    Object.assign(formData, {
      roleName: role.roleName || '',
      roleCode: role.roleCode || '',
      sort: role.sort || 0,
      status: role.status || 0,
      remark: role.remark || '',
    })
  }
  catch (error) {
    console.error('获取角色详情失败:', error)
    ElMessage.error('获取角色详情失败')
    dialogVisible.value = false
  }
  finally {
    formLoading.value = false
  }
}

// 删除角色
async function handleDelete(row: RoleVO) {
  // 检查是否为内置角色
  if ((row as any).isBuiltin === 1) {
    ElMessage.warning('内置角色不能删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除角色「${row.roleName}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await roleApi.delete(row.id!)
    ElMessage.success('删除成功')
    fetchRoleList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除角色失败:', error)
      ElMessage.error('删除角色失败')
    }
  }
}

// 打开分配菜单弹窗
async function handleAssignMenu(row: RoleVO) {
  assignMenuForm.roleId = row.id || 0
  assignMenuDialogVisible.value = true

  try {
    // 获取角色当前菜单
    checkedMenuIds.value = await roleApi.getRoleMenus(row.id!)
    // 设置树形控件选中状态
    if (menuTreeRef.value) {
      menuTreeRef.value.setCheckedKeys(checkedMenuIds.value)
    }
  }
  catch (error) {
    console.error('获取角色菜单失败:', error)
    ElMessage.error('获取角色菜单失败')
  }
}

// 提交分配菜单
async function submitAssignMenu() {
  try {
    // 获取选中的菜单ID（包括半选中的父节点）
    const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
    const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
    const allMenuIds = [...checkedKeys, ...halfCheckedKeys].map((id: number | string) => Number(id))

    await roleApi.assignMenus(assignMenuForm.roleId, allMenuIds)
    ElMessage.success('分配菜单成功')
    assignMenuDialogVisible.value = false
  }
  catch (error) {
    console.error('分配菜单失败:', error)
    ElMessage.error('分配菜单失败')
  }
}

// 表单加载状态
const formLoading = ref(false)

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    const submitData: RoleDTO = {
      roleName: formData.roleName,
      roleCode: formData.roleCode,
      sort: formData.sort,
      status: formData.status,
      remark: formData.remark || undefined,
    }

    if (dialogType.value === 'create') {
      await roleApi.create(submitData)
      ElMessage.success('创建成功')
    }
    else {
      await roleApi.update(currentId.value, submitData)
      ElMessage.success('更新成功')
    }

    dialogVisible.value = false
    fetchRoleList()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存角色失败:', error)
      ElMessage.error('保存角色失败')
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
  fetchRoleList()
}

// 每页条数改变
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchRoleList()
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
  fetchRoleList()
  fetchMenuTree()
})
</script>

<template>
  <div class="p-4">
    <!-- 搜索区域 -->
    <el-card class="mb-4">
      <el-form
        :inline="true"
        :model="queryForm"
      >
        <el-form-item label="角色名称">
          <el-input
            v-model="queryForm.roleName"
            placeholder="请输入角色名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input
            v-model="queryForm.roleCode"
            placeholder="请输入角色编码"
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
          <span class="font-semibold">角色列表</span>
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleCreate"
          >
            新增角色
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
          label="角色ID"
          width="80"
          align="center"
        />
        <el-table-column
          prop="roleName"
          label="角色名称"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="roleCode"
          label="角色编码"
          min-width="150"
          show-overflow-tooltip
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
          width="240"
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
              type="success"
              link
              @click="handleAssignMenu(row)"
            >
              分配菜单
            </el-button>
            <el-button
              type="danger"
              link
              :icon="Delete"
              :disabled="(row as any).isBuiltin === 1"
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
      :title="dialogType === 'create' ? '新增角色' : '编辑角色'"
      width="500px"
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
          label="角色名称"
          prop="roleName"
        >
          <el-input
            v-model="formData.roleName"
            placeholder="请输入角色名称"
          />
        </el-form-item>
        <el-form-item
          label="角色编码"
          prop="roleCode"
        >
          <el-input
            v-model="formData.roleCode"
            placeholder="请输入角色编码"
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
            <el-radio
              v-for="item in statusOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
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

    <!-- 分配菜单对话框 -->
    <el-dialog
      v-model="assignMenuDialogVisible"
      title="分配菜单"
      width="500px"
    >
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{
          label: 'menuName',
          children: 'children',
        }"
        node-key="id"
        show-checkbox
        default-expand-all
        highlight-current
      />
      <template #footer>
        <el-button @click="assignMenuDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="submitAssignMenu"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
