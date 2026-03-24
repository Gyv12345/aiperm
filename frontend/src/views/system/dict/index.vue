<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {Delete, Edit, Plus, Search} from '@element-plus/icons-vue'
import {dictApi, type DictDataDTO, type DictDataVO, type DictTypeDTO, type DictTypeVO} from '@/api/system/dict'

// 字典类型相关
const dictTypeList = ref<DictTypeVO[]>([])
const dictTypeLoading = ref(false)
const selectedDictType = ref<DictTypeVO | null>(null)

// 字典数据相关
const dictDataList = ref<DictDataVO[]>([])
const dictDataLoading = ref(false)

// 搜索条件
const searchForm = reactive({
  dictName: '',
  dictType: '',
})

// 弹窗相关
const typeDialogVisible = ref(false)
const typeDialogTitle = ref('')
const typeFormRef = ref<FormInstance>()
const typeFormLoading = ref(false)

const dataDialogVisible = ref(false)
const dataDialogTitle = ref('')
const dataFormRef = ref<FormInstance>()
const dataFormLoading = ref(false)

// 状态选项
const statusOptions = [
  { value: 0, label: '禁用' },
  { value: 1, label: '启用' },
]

// 字典类型表单
const typeForm = reactive<DictTypeDTO & { id?: number }>({
  dictName: '',
  dictType: '',
  status: 1,
  remark: '',
})

// 字典数据表单
const dataForm = reactive<DictDataDTO & { id?: number }>({
  dictType: '',
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: 1,
  listClass: '',
  remark: '',
})

// 预设 Tag 样式
const tagStyleOptions = [
  { value: 'default', label: '默认' },
  { value: 'primary', label: '主要' },
  { value: 'success', label: '成功' },
  { value: 'warning', label: '警告' },
  { value: 'danger', label: '危险' },
  { value: 'info', label: '信息' },
] as const

// 判断是否是预设 Tag 类型
function isPresetTagType(val: string) {
  return tagStyleOptions.some(o => o.value === val)
}

// 选中预设 Tag 类型
function selectTagType(val: string) {
  dataForm.listClass = val
}

// 自定义颜色值（从 listClass 提取，仅当 listClass 是十六进制颜色时）
const customColor = computed({
  get() {
    return dataForm.listClass && !isPresetTagType(dataForm.listClass) ? dataForm.listClass : null
  },
  set(val: string | null) {
    dataForm.listClass = val || ''
  },
})

// 字典类型表单验证规则
const typeRules = computed<FormRules>(() => ({
  dictName: [
    { required: true, message: '请输入字典名称', trigger: 'blur' },
    { max: 100, message: '字典名称不能超过100个字符', trigger: 'blur' },
  ],
  dictType: [
    { required: true, message: '请输入字典类型', trigger: 'blur' },
    { max: 100, message: '字典类型不能超过100个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '字典类型必须以字母开头，只能包含字母、数字和下划线', trigger: 'blur' },
  ],
}))

// 字典数据表单验证规则
const dataRules = computed<FormRules>(() => ({
  dictLabel: [
    { required: true, message: '请输入字典标签', trigger: 'blur' },
  ],
  dictValue: [
    { required: true, message: '请输入字典键值', trigger: 'blur' },
  ],
}))

// 获取字典类型列表
async function fetchDictTypeList() {
  dictTypeLoading.value = true
  try {
    const data = await dictApi.typeAll()
    // 根据搜索条件过滤
    let list = data
    if (searchForm.dictName) {
      list = list.filter(item => item.dictName?.includes(searchForm.dictName))
    }
    if (searchForm.dictType) {
      list = list.filter(item => item.dictType?.includes(searchForm.dictType))
    }
    dictTypeList.value = list

    // 如果有选中的字典类型，重新获取字典数据
    if (selectedDictType.value) {
      const found = list.find(item => item.id === selectedDictType.value?.id)
      if (found) {
        selectedDictType.value = found
      }
      else {
        selectedDictType.value = null
        dictDataList.value = []
      }
    }
  }
  catch (error) {
    console.error('获取字典类型列表失败:', error)
    ElMessage.error('获取字典类型列表失败')
  }
  finally {
    dictTypeLoading.value = false
  }
}

// 选择字典类型
function selectDictType(row: DictTypeVO) {
  selectedDictType.value = row
  fetchDictDataList()
}

// 获取字典数据列表
async function fetchDictDataList() {
  if (!selectedDictType.value?.dictType) {
    dictDataList.value = []
    return
  }

  dictDataLoading.value = true
  try {
    dictDataList.value = await dictApi.dataList(selectedDictType.value.dictType)
  }
  catch (error) {
    console.error('获取字典数据列表失败:', error)
    ElMessage.error('获取字典数据列表失败')
  }
  finally {
    dictDataLoading.value = false
  }
}

// 重置字典类型表单
function resetTypeForm() {
  Object.assign(typeForm, {
    id: undefined,
    dictName: '',
    dictType: '',
    status: 1,
    remark: '',
  })
}

// 重置字典数据表单
function resetDataForm() {
  Object.assign(dataForm, {
    id: undefined,
    dictType: selectedDictType.value?.dictType || '',
    dictLabel: '',
    dictValue: '',
    sort: 0,
    status: 1,
    remark: '',
  })
}

// 搜索
function handleSearch() {
  fetchDictTypeList()
}

// 重置搜索
function handleResetSearch() {
  searchForm.dictName = ''
  searchForm.dictType = ''
  fetchDictTypeList()
}

// 新增字典类型
function handleAddType() {
  resetTypeForm()
  typeDialogTitle.value = '新增字典类型'
  typeDialogVisible.value = true
}

// 编辑字典类型
function handleEditType(row: DictTypeVO) {
  resetTypeForm()
  typeDialogTitle.value = '编辑字典类型'
  typeFormLoading.value = true
  typeDialogVisible.value = true

  try {
    Object.assign(typeForm, {
      id: row.id,
      dictName: row.dictName,
      dictType: row.dictType,
      status: row.status,
      remark: row.remark,
    })
  }
  finally {
    typeFormLoading.value = false
  }
}

// 删除字典类型
async function handleDeleteType(row: DictTypeVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除字典类型「${row.dictName}」吗？删除后该类型下的所有字典数据也将被删除！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await dictApi.typeDelete(row.id!)
    ElMessage.success('删除成功')

    // 如果删除的是当前选中的类型，清空选中状态
    if (selectedDictType.value?.id === row.id) {
      selectedDictType.value = null
      dictDataList.value = []
    }

    fetchDictTypeList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除字典类型失败:', error)
      ElMessage.error('删除字典类型失败')
    }
  }
}

// 新增字典数据
function handleAddData() {
  if (!selectedDictType.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  resetDataForm()
  dataForm.dictType = selectedDictType.value.dictType!
  dataDialogTitle.value = '新增字典数据'
  dataDialogVisible.value = true
}

// 编辑字典数据
function handleEditData(row: DictDataVO) {
  resetDataForm()
  dataDialogTitle.value = '编辑字典数据'
  dataFormLoading.value = true
  dataDialogVisible.value = true

  try {
    Object.assign(dataForm, {
      id: row.id,
      dictType: row.dictType,
      dictLabel: row.dictLabel,
      dictValue: row.dictValue,
      sort: row.sort,
      status: row.status,
      listClass: row.listClass || '',
      remark: row.remark,
    })
  }
  finally {
    dataFormLoading.value = false
  }
}

// 删除字典数据
async function handleDeleteData(row: DictDataVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除字典数据「${row.dictLabel}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await dictApi.dataDelete(row.id!)
    ElMessage.success('删除成功')
    fetchDictDataList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除字典数据失败:', error)
      ElMessage.error('删除字典数据失败')
    }
  }
}

// 提交字典类型表单
async function submitTypeForm() {
  if (!typeFormRef.value) return

  try {
    await typeFormRef.value.validate()
    typeFormLoading.value = true

    const submitData: DictTypeDTO = {
      dictName: typeForm.dictName,
      dictType: typeForm.dictType,
      status: typeForm.status,
      remark: typeForm.remark || undefined,
    }

    if (typeForm.id) {
      await dictApi.typeUpdate(typeForm.id, submitData)
      ElMessage.success('修改成功')
    }
    else {
      await dictApi.typeCreate(submitData)
      ElMessage.success('新增成功')
    }

    typeDialogVisible.value = false
    fetchDictTypeList()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存字典类型失败:', error)
      ElMessage.error('保存字典类型失败')
    }
  }
  finally {
    typeFormLoading.value = false
  }
}

// 提交字典数据表单
async function submitDataForm() {
  if (!dataFormRef.value) return

  try {
    await dataFormRef.value.validate()
    dataFormLoading.value = true

    const submitData: DictDataDTO = {
      dictType: dataForm.dictType,
      dictLabel: dataForm.dictLabel,
      dictValue: dataForm.dictValue,
      sort: dataForm.sort,
      status: dataForm.status,
      listClass: dataForm.listClass || '',
      remark: dataForm.remark || undefined,
    }

    if (dataForm.id) {
      await dictApi.dataUpdate(dataForm.id, submitData)
      ElMessage.success('修改成功')
    }
    else {
      await dictApi.dataCreate(submitData)
      ElMessage.success('新增成功')
    }

    dataDialogVisible.value = false
    fetchDictDataList()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存字典数据失败:', error)
      ElMessage.error('保存字典数据失败')
    }
  }
  finally {
    dataFormLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchDictTypeList()
})
</script>

<template>
  <div class="p-4 h-full flex gap-4">
    <!-- 左侧：字典类型列表 -->
    <el-card class="flex-none w-96">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-bold">字典类型</span>
          <el-button
            type="primary"
            size="small"
            :icon="Plus"
            @click="handleAddType"
          >
            新增
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="mb-4">
        <el-form
          :model="searchForm"
          class="grid-filter-form dict-type-search-form"
        >
          <el-row :gutter="8">
            <el-col
              :xs="24"
              :sm="12"
              :md="24"
              :lg="24"
            >
              <el-form-item>
                <el-input
                  v-model="searchForm.dictName"
                  placeholder="字典名称"
                  clearable
                  class="filter-control"
                  :prefix-icon="Search"
                  @keyup.enter="handleSearch"
                />
              </el-form-item>
            </el-col>
            <el-col
              :xs="24"
              :sm="12"
              :md="24"
              :lg="24"
            >
              <el-form-item>
                <el-input
                  v-model="searchForm.dictType"
                  placeholder="字典类型"
                  clearable
                  class="filter-control"
                  :prefix-icon="Search"
                  @keyup.enter="handleSearch"
                />
              </el-form-item>
            </el-col>
            <el-col
              :xs="24"
              :sm="24"
              :md="24"
              :lg="24"
            >
              <el-form-item class="filter-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleSearch"
                >
                  搜索
                </el-button>
                <el-button
                  size="small"
                  @click="handleResetSearch"
                >
                  重置
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 字典类型列表 -->
      <el-table
        v-loading="dictTypeLoading"
        :data="dictTypeList"
        highlight-current-row
        height="calc(100vh - 380px)"
        @current-change="selectDictType"
      >
        <el-table-column
          prop="dictName"
          label="字典名称"
          min-width="100"
          show-overflow-tooltip
        />
        <el-table-column
          prop="dictType"
          label="字典类型"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="status"
          label="状态"
          width="70"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ statusOptions.find(o => o.value === row.status)?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="100"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              :icon="Edit"
              @click.stop="handleEditType(row)"
            />
            <el-button
              type="danger"
              link
              size="small"
              :icon="Delete"
              @click.stop="handleDeleteType(row)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 右侧：字典数据列表 -->
    <el-card class="flex-1">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-bold">
            字典数据
            <span
              v-if="selectedDictType"
              class="text-gray-500 font-normal ml-2"
            >
              ({{ selectedDictType.dictName }} - {{ selectedDictType.dictType }})
            </span>
          </span>
          <el-button
            type="primary"
            size="small"
            :icon="Plus"
            :disabled="!selectedDictType"
            @click="handleAddData"
          >
            新增
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="dictDataLoading"
        :data="dictDataList"
        height="calc(100vh - 280px)"
      >
        <el-table-column
          type="index"
          label="序号"
          width="60"
          align="center"
        />
        <el-table-column
          prop="dictLabel"
          label="字典标签"
          min-width="120"
        >
          <template #default="{ row }">
            <el-tag
              v-if="row.listClass && isPresetTagType(row.listClass)"
              :type="row.listClass === 'default' ? '' : row.listClass"
              size="small"
            >
              {{ row.dictLabel }}
            </el-tag>
            <el-tag
              v-else-if="row.listClass && row.listClass.startsWith('#')"
              :style="{ backgroundColor: row.listClass, borderColor: row.listClass, color: '#fff' }"
              size="small"
            >
              {{ row.dictLabel }}
            </el-tag>
            <span v-else>{{ row.dictLabel }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="dictValue"
          label="字典键值"
          min-width="100"
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
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
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
        />
        <el-table-column
          label="操作"
          width="120"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              :icon="Edit"
              @click="handleEditData(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              :icon="Delete"
              @click="handleDeleteData(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态提示 -->
      <el-empty
        v-if="!selectedDictType && !dictDataLoading"
        description="请从左侧选择字典类型"
        class="mt-20"
      />
    </el-card>

    <!-- 字典类型弹窗 -->
    <el-dialog
      v-model="typeDialogVisible"
      :title="typeDialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="typeFormRef"
        v-loading="typeFormLoading"
        :model="typeForm"
        :rules="typeRules"
        label-width="100px"
      >
        <el-form-item
          label="字典名称"
          prop="dictName"
        >
          <el-input
            v-model="typeForm.dictName"
            placeholder="请输入字典名称"
          />
        </el-form-item>
        <el-form-item
          label="字典类型"
          prop="dictType"
        >
          <el-input
            v-model="typeForm.dictType"
            placeholder="请输入字典类型"
            :disabled="!!typeForm.id"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio
              v-for="item in statusOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="typeForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="typeFormLoading"
          @click="submitTypeForm"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog
      v-model="dataDialogVisible"
      :title="dataDialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="dataFormRef"
        v-loading="dataFormLoading"
        :model="dataForm"
        :rules="dataRules"
        label-width="100px"
      >
        <el-form-item label="字典类型">
          <el-input
            v-model="dataForm.dictType"
            disabled
          />
        </el-form-item>
        <el-form-item
          label="字典标签"
          prop="dictLabel"
        >
          <el-input
            v-model="dataForm.dictLabel"
            placeholder="请输入字典标签"
          />
        </el-form-item>
        <el-form-item
          label="字典键值"
          prop="dictValue"
        >
          <el-input
            v-model="dataForm.dictValue"
            placeholder="请输入字典键值"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number
            v-model="dataForm.sort"
            :min="0"
            :max="999"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="样式">
          <div class="flex flex-wrap gap-2 mb-2">
            <el-tag
              v-for="opt in tagStyleOptions"
              :key="opt.value"
              :type="opt.value === 'default' ? '' : opt.value as any"
              :effect="dataForm.listClass === opt.value ? 'dark' : 'light'"
              class="cursor-pointer"
              @click="selectTagType(opt.value)"
            >
              {{ opt.label }}
            </el-tag>
          </div>
          <div class="flex items-center gap-2">
            <el-color-picker
              v-model="customColor"
              show-alpha
              size="small"
            />
            <span class="text-gray-400 text-xs">自定义颜色</span>
            <el-tag
              v-if="customColor"
              :style="{ backgroundColor: customColor, borderColor: customColor, color: '#fff' }"
              size="small"
            >
              预览
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio
              v-for="item in statusOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="dataForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="dataFormLoading"
          @click="submitDataForm"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
