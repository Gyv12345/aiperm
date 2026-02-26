<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { menuApi, type MenuVO, type MenuDTO } from '@/api/system/menu'

// 菜单树数据
const menuTree = ref<MenuVO[]>([])
const loading = ref(false)

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const formLoading = ref(false)

// 菜单类型选项
const menuTypeOptions = [
  { value: 'M', label: '目录' },
  { value: 'C', label: '菜单' },
  { value: 'F', label: '按钮' },
]

// 状态选项
const statusOptions = [
  { value: 0, label: '正常' },
  { value: 1, label: '停用' },
]

// 是否选项
const boolOptions = [
  { value: 0, label: '否' },
  { value: 1, label: '是' },
]

// 筛选条件
const filterType = ref<string>('')

// 表单数据
const form = reactive<MenuDTO & { id?: number }>({
  menuName: '',
  parentId: 0,
  menuType: 'C',
  sort: 0,
  path: '',
  component: '',
  perms: '',
  icon: '',
  isExternal: 0,
  isCache: 0,
  visible: 1,
  status: 0,
  remark: '',
})

// 表单验证规则
const rules = computed<FormRules>(() => ({
  menuName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { max: 100, message: '菜单名称不能超过100个字符', trigger: 'blur' },
  ],
  menuType: [
    { required: true, message: '请选择菜单类型', trigger: 'change' },
  ],
  path: [
    { max: 200, message: '路由地址不能超过200个字符', trigger: 'blur' },
  ],
  component: [
    { max: 200, message: '组件路径不能超过200个字符', trigger: 'blur' },
  ],
  perms: [
    { max: 100, message: '权限标识不能超过100个字符', trigger: 'blur' },
  ],
}))

// 菜单树选项（用于父级选择）
const menuOptions = computed(() => {
  const options: { id: number; menuName: string; children?: typeof options }[] = [
    { id: 0, menuName: '根菜单' },
  ]

  function buildOptions(menus: MenuVO[], level = 0): void {
    menus.forEach((menu) => {
      const prefix = '  '.repeat(level)
      options.push({
        id: menu.id!,
        menuName: prefix + menu.menuName!,
      })
      if (menu.children && menu.children.length > 0) {
        buildOptions(menu.children, level + 1)
      }
    })
  }

  buildOptions(menuTree.value)
  return options
})

// 常用图标列表
const iconList = [
  'Odometer', 'User', 'UserFilled', 'Lock', 'Menu', 'OfficeBuilding',
  'Collection', 'Setting', 'Document', 'Folder', 'Files', 'DataAnalysis',
  'Monitor', 'Connection', 'Promotion', 'ChatDotRound', 'Message',
  'Bell', 'Calendar', 'Picture', 'VideoCamera', 'Microphone', 'Location',
  'Phone', 'Email', 'Compass', 'Guide', 'HomeFilled', 'ShoppingCart',
  'Goods', 'Wallet', 'Coin', 'TrendCharts', 'DataLine', 'Histogram',
  'PieChart', 'CircleCheck', 'CircleClose', 'Warning', 'InfoFilled',
  'QuestionFilled', 'SuccessFilled', 'WarningFilled', 'Star', 'StarFilled',
  'Key', 'Tools', 'Operation', 'SetUp', 'SwitchButton', 'Sort', 'Finished',
]

// 图标选择器
const showIconPicker = ref(false)
const selectedIcon = ref('')

function selectIcon(icon: string) {
  form.icon = icon
  selectedIcon.value = icon
  showIconPicker.value = false
}

// 获取菜单树
async function fetchMenuTree() {
  loading.value = true
  try {
    const data = await menuApi.tree()
    menuTree.value = filterMenuTree(data)
  }
  catch (error) {
    console.error('获取菜单树失败:', error)
    ElMessage.error('获取菜单树失败')
  }
  finally {
    loading.value = false
  }
}

// 筛选菜单树
function filterMenuTree(menus: MenuVO[]): MenuVO[] {
  if (!filterType.value) return menus

  return menus.filter((menu) => {
    const match = menu.menuType === filterType.value
    if (menu.children && menu.children.length > 0) {
      const filteredChildren = filterMenuTree(menu.children)
      if (filteredChildren.length > 0) {
        menu.children = filteredChildren
        return true
      }
    }
    return match
  }).filter((menu) => menu.menuType === filterType.value || (menu.children && menu.children.length > 0))
}

// 重置表单
function resetForm() {
  Object.assign(form, {
    id: undefined,
    menuName: '',
    parentId: 0,
    menuType: 'C',
    sort: 0,
    path: '',
    component: '',
    perms: '',
    icon: '',
    isExternal: 0,
    isCache: 0,
    visible: 1,
    status: 0,
    remark: '',
  })
  selectedIcon.value = ''
}

// 打开新增弹窗
function handleAdd(parentId = 0) {
  resetForm()
  form.parentId = parentId
  dialogTitle.value = '新增菜单'
  dialogVisible.value = true
}

// 打开编辑弹窗
async function handleEdit(row: MenuVO) {
  resetForm()
  dialogTitle.value = '编辑菜单'
  formLoading.value = true
  dialogVisible.value = true

  try {
    const menu = await menuApi.getById(row.id!)
    Object.assign(form, {
      id: menu.id,
      menuName: menu.menuName,
      parentId: menu.parentId || 0,
      menuType: menu.menuType,
      sort: menu.sort,
      path: menu.path,
      component: menu.component,
      perms: menu.perms,
      icon: menu.icon,
      isExternal: menu.isExternal,
      isCache: menu.isCache,
      visible: menu.visible,
      status: menu.status,
      remark: menu.remark,
    })
    selectedIcon.value = menu.icon || ''
  }
  catch (error) {
    console.error('获取菜单详情失败:', error)
    ElMessage.error('获取菜单详情失败')
    dialogVisible.value = false
  }
  finally {
    formLoading.value = false
  }
}

// 删除菜单
async function handleDelete(row: MenuVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜单「${row.menuName}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await menuApi.delete(row.id!)
    ElMessage.success('删除成功')
    fetchMenuTree()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除菜单失败:', error)
      ElMessage.error('删除菜单失败')
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
      menuType: form.menuType,
      sort: form.sort,
      path: form.path || undefined,
      component: form.component || undefined,
      perms: form.perms || undefined,
      icon: form.icon || undefined,
      isExternal: form.isExternal,
      isCache: form.isCache,
      visible: form.visible,
      status: form.status,
      remark: form.remark || undefined,
    }

    if (form.id) {
      await menuApi.update(form.id, submitData)
      ElMessage.success('修改成功')
    }
    else {
      await menuApi.create(submitData)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    fetchMenuTree()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存菜单失败:', error)
      ElMessage.error('保存菜单失败')
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
            placeholder="菜单类型"
            clearable
            style="width: 120px"
            @change="fetchMenuTree"
          >
            <el-option
              v-for="item in menuTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        <div class="flex items-center gap-2">
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleAdd()"
          >
            新增菜单
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

    <!-- 菜单树表格 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="menuTree"
        row-key="id"
        border
        default-expand-all
      >
        <el-table-column
          prop="menuName"
          label="菜单名称"
          min-width="180"
        />
        <el-table-column
          prop="icon"
          label="图标"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-icon v-if="row.icon">
              <component :is="row.icon" />
            </el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="menuType"
          label="类型"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.menuType === 'M' ? 'info' : row.menuType === 'C' ? 'primary' : 'warning'"
              size="small"
            >
              {{ menuTypeOptions.find(o => o.value === row.menuType)?.label || row.menuType }}
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
          prop="perms"
          label="权限标识"
          min-width="150"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ row.perms || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="path"
          label="路由地址"
          min-width="150"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ row.path || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="component"
          label="组件路径"
          min-width="180"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ row.component || '-' }}
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
              :type="row.status === 0 ? 'success' : 'danger'"
              size="small"
            >
              {{ statusOptions.find(o => o.value === row.status)?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="200"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              :icon="Plus"
              @click="handleAdd(row.id)"
            >
              新增
            </el-button>
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
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="680px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item
              label="上级菜单"
              prop="parentId"
            >
              <el-select
                v-model="form.parentId"
                placeholder="请选择上级菜单"
                style="width: 100%"
              >
                <el-option
                  v-for="item in menuOptions"
                  :key="item.id"
                  :label="item.menuName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="菜单名称"
              prop="menuName"
            >
              <el-input
                v-model="form.menuName"
                placeholder="请输入菜单名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="菜单类型"
              prop="menuType"
            >
              <el-select
                v-model="form.menuType"
                placeholder="请选择菜单类型"
                style="width: 100%"
              >
                <el-option
                  v-for="item in menuTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            v-if="form.menuType !== 'F'"
            :span="12"
          >
            <el-form-item label="菜单图标">
              <el-input
                v-model="form.icon"
                placeholder="请选择图标"
                readonly
                @click="showIconPicker = true"
              >
                <template #prefix>
                  <el-icon v-if="form.icon">
                    <component :is="form.icon" />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="form.menuType !== 'F' ? 12 : 24">
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
          </el-col>
          <el-col
            v-if="form.menuType !== 'F'"
            :span="12"
          >
            <el-form-item
              label="路由地址"
              prop="path"
            >
              <el-input
                v-model="form.path"
                placeholder="请输入路由地址"
              />
            </el-form-item>
          </el-col>
          <el-col
            v-if="form.menuType === 'C'"
            :span="12"
          >
            <el-form-item
              label="组件路径"
              prop="component"
            >
              <el-input
                v-model="form.component"
                placeholder="请输入组件路径"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="权限标识"
              prop="perms"
            >
              <el-input
                v-model="form.perms"
                placeholder="请输入权限标识"
              />
            </el-form-item>
          </el-col>
          <el-col
            v-if="form.menuType !== 'F'"
            :span="12"
          >
            <el-form-item label="是否外链">
              <el-select
                v-model="form.isExternal"
                style="width: 100%"
              >
                <el-option
                  v-for="item in boolOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            v-if="form.menuType !== 'F'"
            :span="12"
          >
            <el-form-item label="是否缓存">
              <el-select
                v-model="form.isCache"
                style="width: 100%"
              >
                <el-option
                  v-for="item in boolOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            v-if="form.menuType !== 'F'"
            :span="12"
          >
            <el-form-item label="是否显示">
              <el-select
                v-model="form.visible"
                style="width: 100%"
              >
                <el-option
                  v-for="item in boolOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单状态">
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
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input
                v-model="form.remark"
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
          :loading="formLoading"
          @click="submitForm"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 图标选择器弹窗 -->
    <el-dialog
      v-model="showIconPicker"
      title="选择图标"
      width="600px"
    >
      <div class="grid grid-cols-8 gap-2">
        <div
          v-for="icon in iconList"
          :key="icon"
          class="icon-item flex flex-col items-center p-2 rounded cursor-pointer hover:bg-blue-50"
          :class="{ 'bg-blue-100': selectedIcon === icon }"
          @click="selectIcon(icon)"
        >
          <el-icon class="text-xl">
            <component :is="icon" />
          </el-icon>
          <span class="text-xs mt-1 text-gray-500 truncate w-full text-center">
            {{ icon }}
          </span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.icon-item {
  transition: all 0.2s;
}

.icon-item:hover {
  transform: scale(1.1);
}
</style>
