<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import { getAiPermRBACAPI } from '@/api/generated'
import type { SysMenu, MenuDTO } from '@/models'

const api = getAiPermRBACAPI()

// 菜单树数据（用于父级选择）
const menuTree = ref<SysMenu[]>([])
// 权限列表（按钮类型的菜单）
const permissionList = ref<SysMenu[]>([])
const loading = ref(false)

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const formLoading = ref(false)

// 权限类型选项（筛选用）
const permissionTypeOptions = [
  { value: '', label: '全部' },
  { value: 'menu', label: '菜单权限' },
  { value: 'button', label: '按钮权限' },
  { value: 'api', label: '接口权限' },
]

// 状态选项
const statusOptions = [
  { value: 0, label: '正常' },
  { value: 1, label: '停用' },
]

// 筛选条件
const filterType = ref<string>('')
const searchKeyword = ref('')

// 表单数据
const form = reactive<MenuDTO & { id?: number }>({
  menuName: '',
  parentId: 0,
  menuType: 'F', // 权限都是按钮类型
  sort: 0,
  path: '',
  component: '',
  perms: '',
  icon: '',
  isExternal: 0,
  isCache: 0,
  visible: 1,
  status: 0,
  permission: '',
  remark: '',
})

// 表单验证规则
const rules = computed<FormRules>(() => ({
  menuName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { max: 100, message: '权限名称不能超过100个字符', trigger: 'blur' },
  ],
  parentId: [
    { required: true, message: '请选择所属菜单', trigger: 'change' },
  ],
  perms: [
    { required: true, message: '请输入权限标识', trigger: 'blur' },
    { max: 100, message: '权限标识不能超过100个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9:_]*$/, message: '权限标识必须以字母开头，只能包含字母、数字、冒号和下划线', trigger: 'blur' },
  ],
}))

// 菜单树选项（用于父级选择，只显示目录和菜单类型）
const menuOptions = computed(() => {
  const options: { id: number; menuName: string; level: number }[] = []

  function buildOptions(menus: SysMenu[], level = 0): void {
    menus.forEach((menu) => {
      // 只显示目录(M)和菜单(C)类型
      if (menu.menuType !== 'F') {
        options.push({
          id: menu.id!,
          menuName: menu.menuName!,
          level,
        })
        if (menu.children && menu.children.length > 0) {
          buildOptions(menu.children, level + 1)
        }
      }
    })
  }

  buildOptions(menuTree.value)
  return options
})

// 获取菜单树
async function fetchMenuTree() {
  loading.value = true
  try {
    const { data } = await api.tree()
    if (data?.data) {
      menuTree.value = data.data
      // 提取所有按钮类型的菜单作为权限列表
      permissionList.value = extractPermissions(data.data)
    }
  }
  catch (error) {
    console.error('获取菜单树失败:', error)
    ElMessage.error('获取菜单树失败')
  }
  finally {
    loading.value = false
  }
}

// 提取权限列表（按钮类型的菜单）
function extractPermissions(menus: SysMenu[]): SysMenu[] {
  const permissions: SysMenu[] = []

  function extract(menuList: SysMenu[]): void {
    menuList.forEach((menu) => {
      if (menu.menuType === 'F') {
        permissions.push(menu)
      }
      if (menu.children && menu.children.length > 0) {
        extract(menu.children)
      }
    })
  }

  extract(menus)
  return permissions
}

// 筛选后的权限列表
const filteredPermissions = computed(() => {
  let list = permissionList.value

  // 按权限类型筛选
  if (filterType.value) {
    list = list.filter((p) => {
      const perm = p.perms || p.permission || ''
      if (filterType.value === 'menu' && perm.includes(':menu:')) return true
      if (filterType.value === 'button' && perm.includes(':create') || perm.includes(':update') || perm.includes(':delete')) return true
      if (filterType.value === 'api') return perm.startsWith('api:')
      return false
    })
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter((p) => {
      return (
        p.menuName?.toLowerCase().includes(keyword)
        || p.perms?.toLowerCase().includes(keyword)
        || p.permission?.toLowerCase().includes(keyword)
      )
    })
  }

  return list
})

// 获取父级菜单名称
function getParentMenuName(parentId: number | undefined): string {
  if (!parentId) return '根菜单'

  function findMenu(menus: SysMenu[]): SysMenu | undefined {
    for (const menu of menus) {
      if (menu.id === parentId) return menu
      if (menu.children) {
        const found = findMenu(menu.children)
        if (found) return found
      }
    }
    return undefined
  }

  const parent = findMenu(menuTree.value)
  return parent?.menuName || '未知'
}

// 重置表单
function resetForm() {
  Object.assign(form, {
    id: undefined,
    menuName: '',
    parentId: 0,
    menuType: 'F',
    sort: 0,
    path: '',
    component: '',
    perms: '',
    icon: '',
    isExternal: 0,
    isCache: 0,
    visible: 1,
    status: 0,
    permission: '',
    remark: '',
  })
}

// 打开新增弹窗
function handleAdd(parentId = 0) {
  resetForm()
  form.parentId = parentId
  dialogTitle.value = '新增权限'
  dialogVisible.value = true
}

// 打开编辑弹窗
async function handleEdit(row: SysMenu) {
  resetForm()
  dialogTitle.value = '编辑权限'
  formLoading.value = true
  dialogVisible.value = true

  try {
    const { data } = await api.getById3(row.id!)
    if (data?.data) {
      const menu = data.data
      Object.assign(form, {
        id: menu.id,
        menuName: menu.menuName,
        parentId: menu.parentId || 0,
        menuType: 'F',
        sort: menu.sort,
        perms: menu.perms || menu.permission,
        status: menu.status,
        remark: menu.remark,
      })
    }
  }
  catch (error) {
    console.error('获取权限详情失败:', error)
    ElMessage.error('获取权限详情失败')
    dialogVisible.value = false
  }
  finally {
    formLoading.value = false
  }
}

// 删除权限
async function handleDelete(row: SysMenu) {
  try {
    await ElMessageBox.confirm(
      `确定要删除权限「${row.menuName}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await api.delete3(row.id!)
    ElMessage.success('删除成功')
    fetchMenuTree()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除权限失败:', error)
      ElMessage.error('删除权限失败')
    }
  }
}

// 提交表单
async function submitForm() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData: MenuDTO = {
      menuName: form.menuName,
      parentId: form.parentId,
      menuType: 'F', // 权限类型固定为按钮
      sort: form.sort,
      perms: form.perms,
      status: form.status,
      remark: form.remark || undefined,
    }

    if (form.id) {
      await api.update3(form.id, submitData)
      ElMessage.success('修改成功')
    }
    else {
      await api.create3(submitData)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    fetchMenuTree()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存权限失败:', error)
      ElMessage.error('保存权限失败')
    }
  }
  finally {
    formLoading.value = false
  }
}

// 刷新
function handleRefresh() {
  fetchMenuTree()
}

// 初始化
onMounted(() => {
  fetchMenuTree()
})
</script>

<template>
  <div class="p-4">
    <!-- 工具栏 -->
    <el-card class="mb-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <el-select
            v-model="filterType"
            placeholder="权限类型"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="item in permissionTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索权限名称/标识"
            clearable
            style="width: 200px"
            :prefix-icon="Search"
          />
        </div>
        <div class="flex items-center gap-2">
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleAdd()"
          >
            新增权限
          </el-button>
          <el-button
            :icon="Refresh"
            @click="handleRefresh"
          >
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 权限列表 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="filteredPermissions"
        border
      >
        <el-table-column
          type="index"
          label="序号"
          width="60"
          align="center"
        />
        <el-table-column
          prop="menuName"
          label="权限名称"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="perms"
          label="权限标识"
          min-width="180"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <el-tag type="info">
              {{ row.perms || row.permission || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="所属菜单"
          min-width="120"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ getParentMenuName(row.parentId) }}
          </template>
        </el-table-column>
        <el-table-column
          label="权限类型"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.perms?.includes(':list') ? 'primary' : row.perms?.includes(':create') || row.perms?.includes(':update') || row.perms?.includes(':delete') ? 'warning' : 'info'"
              size="small"
            >
              {{ row.perms?.includes(':list') ? '查询' : row.perls?.includes(':create') ? '新增' : row.perms?.includes(':update') ? '修改' : row.perms?.includes(':delete') ? '删除' : '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="sort"
          label="排序"
          width="80"
          align="center"
        />
        <el-table-column
          prop="status"
          label="状态"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.status === 0 ? 'success' : 'danger'"
              size="small"
            >
              {{ statusOptions.find(o => o.value === row.status)?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="remark"
          label="备注"
          min-width="150"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="150"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && filteredPermissions.length === 0"
        description="暂无权限数据"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="所属菜单"
          prop="parentId"
        >
          <el-select
            v-model="form.parentId"
            placeholder="请选择所属菜单"
            style="width: 100%"
          >
            <el-option
              v-for="item in menuOptions"
              :key="item.id"
              :label="'  '.repeat(item.level) + item.menuName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="权限名称"
          prop="menuName"
        >
          <el-input
            v-model="form.menuName"
            placeholder="请输入权限名称，如：用户列表"
          />
        </el-form-item>
        <el-form-item
          label="权限标识"
          prop="perms"
        >
          <el-input
            v-model="form.perms"
            placeholder="请输入权限标识，如：system:user:list"
          />
          <div class="text-xs text-gray-400 mt-1">
            格式：模块:资源:操作，如 system:user:list
          </div>
        </el-form-item>
        <el-form-item
          label="显示排序"
          prop="sort"
        >
          <el-input-number
            v-model="form.sort"
            :min="0"
            :max="999"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="form.status"
            style="width: 100%"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
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
          :loading="formLoading"
          @click="submitForm"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
