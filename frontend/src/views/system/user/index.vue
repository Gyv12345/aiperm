<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh, Key } from '@element-plus/icons-vue'
import { userApi, type UserVO, type UserDTO } from '@/api/system/user'
import { roleApi, type RoleVO } from '@/api/system/role'
import { dictApi, type DictDataVO } from '@/api/system/dict'
import type { PageResult } from '@/types'

// 表单引用
const formRef = ref<FormInstance>()
const resetPasswordFormRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref<UserVO[]>([])

// 角色列表（用于分配角色）
const roleList = ref<RoleVO[]>([])

// 状态字典数据
const statusDictData = ref<DictDataVO[]>([])

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

// 性别选项
const genderOptions = [
  { value: 0, label: '未知' },
  { value: 1, label: '男' },
  { value: 2, label: '女' },
]

// 状态选项（从字典获取）
const statusOptions = computed(() =>
  statusDictData.value.map(item => ({
    value: Number(item.dictValue),
    label: item.dictLabel,
  }))
)

// 获取状态标签
function getStatusLabel(status: number): string {
  const item = statusDictData.value.find(d => Number(d.dictValue) === status)
  return item?.dictLabel || '未知'
}

// 获取状态标签类型（从字典的 listClass 获取）
function getStatusTagType(status: number): 'success' | 'danger' | 'warning' | 'info' {
  const item = statusDictData.value.find(d => Number(d.dictValue) === status)
  const listClass = item?.listClass || ''
  if (listClass.includes('success')) return 'success'
  if (listClass.includes('danger')) return 'danger'
  if (listClass.includes('warning')) return 'warning'
  if (listClass.includes('info')) return 'info'
  // 默认根据状态值判断
  return status === 1 ? 'success' : 'danger'
}

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

// 获取状态字典
async function fetchStatusDict() {
  try {
    statusDictData.value = await dictApi.dataList('sys_status')
  }
  catch (error) {
    console.error('获取状态字典失败:', error)
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
    status: 1,  // 默认正常
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

// 修改用户状态
async function handleStatusChange(row: UserVO) {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = getStatusLabel(newStatus)
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
    // TODO: 调用分配角色接口
    // 目前后端可能还没有这个接口，先提示
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

// 获取性别文本
function getGenderText(gender: number): string {
  return genderOptions.find(o => o.value === gender)?.label || '未知'
}

// 页面加载
onMounted(() => {
  fetchUserList()
  fetchRoleList()
  fetchStatusDict()
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
        <el-form-item label="用户名">
          <el-input
            v-model="queryForm.username"
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input
            v-model="queryForm.nickname"
            placeholder="请输入昵称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="queryForm.phone"
            placeholder="请输入手机号"
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
          <span class="font-semibold">用户列表</span>
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleCreate"
          >
            新增用户
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
          label="用户ID"
          width="80"
          align="center"
        />
        <el-table-column
          prop="username"
          label="用户名"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="nickname"
          label="昵称"
          min-width="100"
          show-overflow-tooltip
        />
        <el-table-column
          prop="email"
          label="邮箱"
          min-width="100"
          show- 所有工具调用均失败
          show-overflow-tooltip
        />
        <el- table-column
          prop="phone"
          label="手机号"
          width="130"
        />
        <el-table-column
          prop="gender"
          label="性别"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            {{ getGenderText(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="getStatusTagType(row.status)"
              size="small"
            >
              {{ getStatusLabel(row.status) }}
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
          width="280"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              @click="handleStatusChange(row)"
            >
              {{ getStatusLabel(row.status === 1 ? 0 : 1) }}
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
              type="warning"
              link
              :icon="Key"
              @click="handleResetPassword(row)"
            >
              重置密码
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '新增用户' : '编辑用户'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
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
                placeholder="请输入密码"
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
            <el-form-item label="性别">
              <el-select
                v-model="formData.gender"
                placeholder="请选择性别"
                style="width: 100%"
              >
                <el-option
                  v-for="item in genderOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
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

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="400px"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
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
        <el-button @click="resetPasswordDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="submitResetPassword"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog
      v-model="assignRoleDialogVisible"
      title="分配角色"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="选择角色">
          <el-select
            v-model="assignRoleForm.roleIds"
            multiple
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
        <el-button @click="assignRoleDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="submitAssignRole"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
