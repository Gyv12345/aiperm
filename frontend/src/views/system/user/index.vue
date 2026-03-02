<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh, Key, User, MoreFilled, Female, Male, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { userApi, type UserVO, type UserDTO } from '@/api/system/user'
import { roleApi, type RoleVO } from '@/api/system/role'
import { deptApi, type DeptVO } from '@/api/system/dept'
import { postApi, type PostVO } from '@/api/system/post'
import type { PageResult, TableColumn } from '@/types'
import { useDict } from '@/composables/useDict'

// 字典
const dictData = useDict('sys_status')
const sys_status = dictData.sys_status!

// 搜索区域折叠状态
const searchCollapsed = ref(false)

// 表格列配置
const columns = ref<TableColumn[]>([
  { key: 'id',         label: '用户ID',   visible: true, fixed: 'left' },
  { key: 'username',   label: '用户名',   visible: true },
  { key: 'nickname',   label: '昵称',     visible: true },
  { key: 'deptName',   label: '部门',     visible: true },
  { key: 'postNames',  label: '岗位',     visible: true },
  { key: 'roleNames',  label: '角色',     visible: true },
  { key: 'email',      label: '邮箱',     visible: true },
  { key: 'phone',      label: '手机号',   visible: true },
  { key: 'gender',     label: '性别',     visible: true },
  { key: 'createTime', label: '创建时间', visible: true },
])

// 表格引用（用于 clearSelection）
const tableRef = ref()

// 多选
const selectedRows = ref<UserVO[]>([])
function handleSelectionChange(rows: UserVO[]) {
  selectedRows.value = rows
}

// 批量删除
async function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的用户')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个用户吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    const ids = selectedRows.value.map(row => row.id!)
    await userApi.deleteBatch(ids)
    ElMessage.success('批量删除成功')
    tableRef.value?.clearSelection()
    fetchUserList()
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
const resetPasswordFormRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref<UserVO[]>([])

// 角色列表（用于分配角色）
const roleList = ref<RoleVO[]>([])

// 岗位列表（用于选择岗位）
const postList = ref<PostVO[]>([])

// 部门树（用于选择部门）
const deptTree = ref<DeptVO[]>([])

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 对话框显示状态
const dialogVisible = ref(false)
const resetPasswordDialogVisible = ref(false)
const assignRoleDialogVisible = ref(false)

// 对话框类型：create / update
const dialogType = ref<'create' | 'update'>('create')

// 当前编辑的用户ID
const currentId = ref<number>(0)

// 表单数据
const formData = reactive<UserDTO>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  avatar: '',
  deptId: undefined,
  postIds: undefined,
  roleIds: [],
  status: 1,
  remark: '',
})

// 重置密码表单
const resetPasswordForm = reactive({
  newPassword: '',
  confirmPassword: '',
})

// 分配角色表单
const assignRoleForm = reactive({
  userId: 0,
  roleIds: [] as number[],
})

// 查询表单
const queryForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  status: undefined as number | undefined,
})

// 表单验证规则
const rules = computed<FormRules>(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度为 2-50 个字符', trigger: 'blur' },
  ],
  password: [
    {
      required: dialogType.value === 'create',
      message: '请输入密码',
      trigger: 'blur',
    },
    { min: 6, max: 100, message: '密码长度为 6-100 个字符', trigger: 'blur' },
  ],
  nickname: [
    { max: 50, message: '昵称不能超过50个字符', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' },
  ],
}))

// 重置密码表单验证规则
const resetPasswordRules = computed<FormRules>(() => ({
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度为 6-100 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== resetPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        }
        else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}))

// 获取用户列表
async function fetchUserList() {
  loading.value = true
  try {
    const params: UserDTO = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      username: queryForm.username || undefined,
      nickname: queryForm.nickname || undefined,
      phone: queryForm.phone || undefined,
      status: queryForm.status,
    }
    const result = await userApi.list(params) as PageResult<UserVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
  finally {
    loading.value = false
  }
}

// 获取所有角色列表
async function fetchRoleList() {
  try {
    roleList.value = await roleApi.all()
  }
  catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// 获取所有岗位列表
async function fetchPostList() {
  try {
    postList.value = await postApi.all()
  }
  catch (error) {
    console.error('获取岗位列表失败:', error)
  }
}

// 获取部门树
async function fetchDeptTree() {
  try {
    deptTree.value = await deptApi.tree()
  }
  catch (error) {
    console.error('获取部门树失败:', error)
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchUserList()
}

// 重置搜索
function handleReset() {
  queryForm.username = ''
  queryForm.nickname = ''
  queryForm.phone = ''
  queryForm.status = undefined
  pagination.page = 1
  fetchUserList()
}

// 新增用户
function handleCreate() {
  dialogType.value = 'create'
  currentId.value = 0
  Object.assign(formData, {
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    gender: 0,
    avatar: '',
    deptId: undefined,
    postIds: undefined,
    roleIds: [],
    status: 0,
    remark: '',
  })
  dialogVisible.value = true
}

// 编辑用户
function handleUpdate(row: UserVO) {
  dialogType.value = 'update'
  currentId.value = row.id || 0
  Object.assign(formData, {
    username: row.username || '',
    password: '',
    nickname: row.nickname || '',
    email: row.email || '',
    phone: row.phone || '',
    gender: row.gender || 0,
    avatar: row.avatar || '',
    deptId: row.deptId,
    postIds: row.postIds,
    roleIds: row.roleIds || [],
    status: row.status || 0,
    remark: '',
  })
  dialogVisible.value = true
}

// 删除用户
async function handleDelete(row: UserVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户「${row.username}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await userApi.delete(row.id!)
    ElMessage.success('删除成功')
    fetchUserList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }
}

// 处理下拉菜单命令
function handleCommand(command: string, row: UserVO) {
  switch (command) {
    case 'status':
      handleStatusChange(row)
      break
    case 'resetPwd':
      handleResetPassword(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

// 修改用户状态
async function handleStatusChange(row: UserVO) {
  const newStatus = row.status === 1 ? 0 : 1
  const dictItem = sys_status.value?.find(d => Number(d.dictValue) === newStatus)
  const statusText = dictItem?.dictLabel || (newStatus === 1 ? '正常' : '停用')
  try {
    await ElMessageBox.confirm(
      `确定要将用户「${row.username}」状态修改为「${statusText}」吗？`,
      '状态修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await userApi.changeStatus(row.id!, newStatus)
    ElMessage.success('状态修改成功')
    fetchUserList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('修改状态失败:', error)
      ElMessage.error('修改状态失败')
    }
  }
}

// 打开重置密码弹窗
function handleResetPassword(row: UserVO) {
  currentId.value = row.id || 0
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordDialogVisible.value = true
}

// 提交重置密码
async function submitResetPassword() {
  if (!resetPasswordFormRef.value) return

  try {
    await resetPasswordFormRef.value.validate()

    await userApi.resetPassword(currentId.value, resetPasswordForm.newPassword)
    ElMessage.success('密码重置成功')
    resetPasswordDialogVisible.value = false
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

// 提交分配角色
async function submitAssignRole() {
  try {
    ElMessage.success('分配角色成功')
    assignRoleDialogVisible.value = false
  }
  catch (error) {
    console.error('分配角色失败:', error)
    ElMessage.error('分配角色失败')
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    const submitData: UserDTO = {
      username: formData.username,
      password: formData.password || undefined,
      nickname: formData.nickname || undefined,
      email: formData.email || undefined,
      phone: formData.phone || undefined,
      gender: formData.gender,
      avatar: formData.avatar || undefined,
      deptId: formData.deptId,
      postIds: formData.postIds,
      roleIds: formData.roleIds,
      status: formData.status,
      remark: formData.remark || undefined,
    }

    if (dialogType.value === 'create') {
      await userApi.create(submitData)
      ElMessage.success('创建成功')
    }
    else {
      await userApi.update(currentId.value, submitData)
      ElMessage.success('更新成功')
    }

    dialogVisible.value = false
    fetchUserList()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存用户失败:', error)
      ElMessage.error('保存用户失败')
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
  fetchUserList()
}

// 每页条数改变
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchUserList()
}

// 页面加载
onMounted(() => {
  fetchUserList()
  fetchRoleList()
  fetchPostList()
  fetchDeptTree()
})
</script>

<template>
  <div class="user-page p-4">
    <!-- 搜索区域 -->
    <el-card class="search-card mb-4 overflow-hidden transition-all duration-300">
      <div class="flex items-center justify-between mb-3">
        <span class="text-base font-medium text-gray-700 dark:text-gray-200">搜索条件</span>
        <el-button
          text
          type="primary"
          class="collapse-btn"
          @click="searchCollapsed = !searchCollapsed"
        >
          {{ searchCollapsed ? '展开' : '收起' }}
          <el-icon class="ml-1 transition-transform duration-300" :class="{ 'rotate-180': searchCollapsed }">
            <ArrowUp />
          </el-icon>
        </el-button>
      </div>
      <el-collapse-transition>
        <div v-show="!searchCollapsed">
          <el-form
            :inline="true"
            :model="queryForm"
            class="search-form"
          >
            <el-form-item label="用户名">
              <el-input
                v-model="queryForm.username"
                placeholder="请输入用户名"
                clearable
                class="w-48"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input
                v-model="queryForm.nickname"
                placeholder="请输入昵称"
                clearable
                class="w-48"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input
                v-model="queryForm.phone"
                placeholder="请输入手机号"
                clearable
                class="w-48"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="状态">
              <DictSelect
                v-model="queryForm.status"
                dict-type="sys_status"
                clearable
                placeholder="请选择状态"
                class="w-36"
              />
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
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <!-- 工具栏 -->
      <TableToolbar>
        <template #actions>
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleCreate"
          >
            新增用户
          </el-button>
        </template>
        <template #tools>
          <el-button
            :icon="Refresh"
            circle
            @click="fetchUserList"
          />
          <ColumnSetting v-model="columns" />
        </template>
      </TableToolbar>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        class="user-table"
        header-cell-class-name="table-header-cell"
        @selection-change="handleSelectionChange"
      >
        <!-- 多选列 -->
        <el-table-column
          type="selection"
          width="55"
          fixed="left"
        />

        <!-- 用户ID -->
        <el-table-column
          v-if="columns.find(c => c.key === 'id')?.visible"
          prop="id"
          label="ID"
          width="80"
          align="center"
          fixed="left"
        />

        <!-- 用户信息（头像+用户名+昵称） -->
        <el-table-column
          label="用户信息"
          min-width="200"
        >
          <template #default="{ row }">
            <div class="user-info-cell flex items-center">
              <el-avatar
                :size="40"
                :src="row.avatar"
                class="user-avatar"
              >
                <el-icon :size="20"><User /></el-icon>
              </el-avatar>
              <div class="user-detail ml-3">
                <div class="username-row flex items-center">
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ row.username }}</span>
                  <el-tag
                    v-if="row.status === 1"
                    type="success"
                    size="small"
                    class="ml-2"
                    effect="light"
                  >
                    正常
                  </el-tag>
                  <el-tag
                    v-else
                    type="danger"
                    size="small"
                    class="ml-2"
                    effect="light"
                  >
                    停用
                  </el-tag>
                </div>
                <div class="nickname text-sm text-gray-500 dark:text-gray-400">
                  {{ row.nickname || '-' }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 部门 -->
        <el-table-column
          v-if="columns.find(c => c.key === 'deptName')?.visible"
          prop="deptName"
          label="部门"
          min-width="120"
          show-overflow-tooltip
        />

        <!-- 角色 -->
        <el-table-column
          v-if="columns.find(c => c.key === 'roleNames')?.visible"
          label="角色"
          min-width="140"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span v-if="row.roleNames" class="role-text">
              {{ row.roleNames }}
            </span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>

        <!-- 联系方式 -->
        <el-table-column
          label="联系方式"
          min-width="180"
        >
          <template #default="{ row }">
            <div class="contact-info">
              <div v-if="row.phone" class="flex items-center text-sm mb-1">
                <span class="text-gray-500 dark:text-gray-400">{{ row.phone }}</span>
              </div>
              <div v-if="row.email" class="flex items-center text-sm">
                <span class="text-gray-500 dark:text-gray-400">{{ row.email }}</span>
              </div>
              <span v-if="!row.phone && !row.email" class="text-gray-400">-</span>
            </div>
          </template>
        </el-table-column>

        <!-- 性别 -->
        <el-table-column
          v-if="columns.find(c => c.key === 'gender')?.visible"
          label="性别"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-icon v-if="row.gender === 1" class="text-blue-500" :size="18">
              <Male />
            </el-icon>
            <el-icon v-else-if="row.gender === 2" class="text-pink-500" :size="18">
              <Female />
            </el-icon>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>

        <!-- 创建时间 -->
        <el-table-column
          v-if="columns.find(c => c.key === 'createTime')?.visible"
          prop="createTime"
          label="创建时间"
          width="170"
        >
          <template #default="{ row }">
            <span class="text-gray-600 dark:text-gray-300">{{ row.createTime }}</span>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column
          label="操作"
          width="160"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <div class="action-buttons flex items-center justify-center">
              <el-button
                type="primary"
                link
                size="small"
                :icon="Edit"
                @click="handleUpdate(row)"
              >
                编辑
              </el-button>
              <el-dropdown
                trigger="click"
                @command="(cmd: string) => handleCommand(cmd, row)"
              >
                <el-button
                  type="primary"
                  link
                  size="small"
                >
                  更多
                  <el-icon class="ml-1"><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                  <el-dropdown-item :command="'status'">
                    {{ row.status === 1 ? '停用' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="'resetPwd'">
                    <el-icon><Key /></el-icon>
                    <span class="ml-1">重置密码</span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="'delete'"
                    divided
                    class="text-red-500"
                  >
                    <el-icon><Delete /></el-icon>
                    <span class="ml-1">删除</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '新增用户' : '编辑用户'"
      width="640px"
      class="user-dialog"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="80px"
        class="user-form"
      >
        <div class="form-section mb-6">
          <div class="section-title text-sm font-medium text-gray-600 dark:text-gray-300 mb-4 pb-2 border-b border-gray-200 dark:border-gray-700">
            基本信息
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item
                label="用户名"
                prop="username"
              >
                <el-input
                  v-model="formData.username"
                  placeholder="请输入用户名"
                  :disabled="dialogType === 'update'"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                label="密码"
                prop="password"
              >
                <el-input
                  v-model="formData.password"
                  type="password"
                  :placeholder="dialogType === 'create' ? '请输入密码' : '留空则不修改'"
                  show-password
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                label="昵称"
                prop="nickname"
              >
                <el-input
                  v-model="formData.nickname"
                  placeholder="请输入昵称"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别">
                <el-radio-group v-model="formData.gender">
                  <el-radio :value="1">
                    <el-icon class="text-blue-500 align-middle"><Male /></el-icon>
                    <span class="ml-1">男</span>
                  </el-radio>
                  <el-radio :value="2">
                    <el-icon class="text-pink-500 align-middle"><Female /></el-icon>
                    <span class="ml-1">女</span>
                  </el-radio>
                  <el-radio :value="0">
                    未知
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section mb-6">
          <div class="section-title text-sm font-medium text-gray-600 dark:text-gray-300 mb-4 pb-2 border-b border-gray-200 dark:border-gray-700">
            联系方式
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item
                label="手机号"
                prop="phone"
              >
                <el-input
                  v-model="formData.phone"
                  placeholder="请输入手机号"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                label="邮箱"
                prop="email"
              >
                <el-input
                  v-model="formData.email"
                  placeholder="请输入邮箱"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section mb-6">
          <div class="section-title text-sm font-medium text-gray-600 dark:text-gray-300 mb-4 pb-2 border-b border-gray-200 dark:border-gray-700">
            组织架构
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="部门">
                <el-tree-select
                  v-model="formData.deptId"
                  :data="deptTree"
                  :props="{ label: 'deptName', value: 'id', children: 'children' }"
                  placeholder="请选择部门"
                  clearable
                  check-strictly
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="岗位">
                <el-select
                  v-model="formData.postIds"
                  multiple
                  collapse-tags
                  collapse-tags-tooltip
                  placeholder="请选择岗位"
                  style="width: 100%"
                >
                  <el-option
                    v-for="post in postList"
                    :key="post.id"
                    :label="post.postName"
                    :value="post.id!"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="角色">
                <el-select
                  v-model="formData.roleIds"
                  multiple
                  collapse-tags
                  collapse-tags-tooltip
                  placeholder="请选择角色"
                  style="width: 100%"
                >
                  <el-option
                    v-for="role in roleList"
                    :key="role.id"
                    :label="role.roleName"
                    :value="role.id!"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section">
          <div class="section-title text-sm font-medium text-gray-600 dark:text-gray-300 mb-4 pb-2 border-b border-gray-200 dark:border-gray-700">
            其他设置
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="状态">
                <el-radio-group v-model="formData.status">
                  <el-radio :value="0">正常</el-radio>
                  <el-radio :value="1">停用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入备注"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer flex justify-end gap-2">
          <el-button @click="dialogVisible = false">
            取消
          </el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
          >
            {{ dialogType === 'create' ? '创建' : '保存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="420px"
      class="reset-password-dialog"
      :close-on-click-modal="false"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="80px"
      >
        <el-form-item
          label="新密码"
          prop="newPassword"
        >
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item
          label="确认密码"
          prop="confirmPassword"
        >
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer flex justify-end gap-2">
          <el-button @click="resetPasswordDialogVisible = false">
            取消
          </el-button>
          <el-button
            type="primary"
            @click="submitResetPassword"
          >
            确认重置
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog
      v-model="assignRoleDialogVisible"
      title="分配角色"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="选择角色">
          <el-select
            v-model="assignRoleForm.roleIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id!"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer flex justify-end gap-2">
          <el-button @click="assignRoleDialogVisible = false">
            取消
          </el-button>
          <el-button
            type="primary"
            @click="submitAssignRole"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

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
  </div>
</template>

<style scoped>
.user-page {
  min-height: calc(100vh - 120px);
}

/* 搜索卡片 */
.search-card {
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.search-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.collapse-btn {
  padding: 4px 8px;
  font-size: 13px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.search-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

/* 表格卡片 */
.table-card {
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 表格样式 */
.user-table :deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 10;
}

.table-header-cell {
  background-color: var(--color-bg-page) !important;
  font-weight: 600;
}

/* 用户信息单元格 */
.user-info-cell {
  padding: 8px 0;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  flex-shrink: 0;
}

.user-detail {
  min-width: 0;
}

.username-row {
  line-height: 1.4;
}

.nickname {
  line-height: 1.4;
  margin-top: 2px;
}

/* 角色文本 */
.role-text {
  color: var(--el-color-primary);
  font-size: 13px;
}

/* 操作按钮 */
.action-buttons {
  gap: 4px;
}

.action-buttons :deep(.el-button + .el-button) {
  margin-left: 4px;
}

/* 联系方式 */
.contact-info {
  line-height: 1.6;
}

/* 分页 */
.pagination-wrapper {
  padding: 16px 0 0;
}

/* 对话框 */
.user-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 16px;
  margin-bottom: 0;
}

.user-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}

.user-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.form-section:last-child {
  margin-bottom: 0 !important;
}

/* 深色模式适配 */
:root.dark .search-card,
:root.dark .table-card {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

:root.dark .section-title {
  border-color: #374151;
}

:root.dark .user-dialog :deep(.el-dialog__header) {
  border-color: #374151;
}

/* 过渡动画 */
.rotate-180 {
  transform: rotate(180deg);
}

/* 响应式 */
@media (max-width: 768px) {
  .search-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .user-form :deep(.el-col) {
    width: 100%;
    max-width: 100%;
  }
}
</style>
