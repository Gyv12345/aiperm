<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'
import type { PageResult } from '@/types'

interface ConfigVO {
  id: number
  configKey: string
  configValue: string
  configType: string
  remark: string
  createTime: string
}

interface ConfigDTO {
  page?: number
  pageSize?: number
  configKey?: string
  configType?: string
}

// 配置列表
const configList = ref<ConfigVO[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 搜索条件
const searchForm = reactive({
  configKey: '',
  configType: '',
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const formLoading = ref(false)

// 配置表单
const form = reactive({
  id: undefined as number | undefined,
  configKey: '',
  configValue: '',
  configType: '',
  remark: '',
})

// 表单验证规则
const rules = computed<FormRules>(() => ({
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' },
    { max: 100, message: '配置键不能超过100个字符', trigger: 'blur' },
  ],
}))

// 获取配置列表
async function fetchConfigList() {
  loading.value = true
  try {
    const params: ConfigDTO = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.configKey) params.configKey = searchForm.configKey
    if (searchForm.configType) params.configType = searchForm.configType

    const data = await request.get<PageResult<ConfigVO>>('/enterprise/config', { params })
    if (data) {
      configList.value = data.list || []
      pagination.total = data.total || 0
    }
  }
  catch (error) {
    console.error('获取系统配置列表失败:', error)
    ElMessage.error('获取系统配置列表失败')
  }
  finally {
    loading.value = false
  }
}

// 重置表单
function resetForm() {
  Object.assign(form, {
    id: undefined,
    configKey: '',
    configValue: '',
    configType: '',
    remark: '',
  })
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchConfigList()
}

// 重置搜索
function handleResetSearch() {
  searchForm.configKey = ''
  searchForm.configType = ''
  pagination.page = 1
  fetchConfigList()
}

// 新增
function handleAdd() {
  resetForm()
  dialogTitle.value = '新增系统配置'
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: ConfigVO) {
  resetForm()
  dialogTitle.value = '编辑系统配置'
  Object.assign(form, {
    id: row.id,
    configKey: row.configKey,
    configValue: row.configValue,
    configType: row.configType,
    remark: row.remark,
  })
  dialogVisible.value = true
}

// 删除
async function handleDelete(row: ConfigVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除配置「${row.configKey}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await request.delete(`/enterprise/config/${row.id}`)
    ElMessage.success('删除成功')
    fetchConfigList()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除系统配置失败:', error)
      ElMessage.error('删除系统配置失败')
    }
  }
}

// 提交表单
async function submitForm() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    formLoading.value = true

    const submitData = {
      configKey: form.configKey,
      configValue: form.configValue,
      configType: form.configType,
      remark: form.remark || undefined,
    }

    if (form.id) {
      await request.put(`/enterprise/config/${form.id}`, submitData)
      ElMessage.success('修改成功')
    }
    else {
      await request.post('/enterprise/config', submitData)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    fetchConfigList()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存系统配置失败:', error)
      ElMessage.error('保存系统配置失败')
    }
  }
  finally {
    formLoading.value = false
  }
}

// 分页变化
function handlePageChange(page: number) {
  pagination.page = page
  fetchConfigList()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchConfigList()
}

// 初始化
onMounted(() => {
  fetchConfigList()
})
</script>

<template>
  <div class="p-4 h-full">
    <el-card>
      <!-- 搜索区域 -->
      <div class="mb-4">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="配置键">
            <el-input
              v-model="searchForm.configKey"
              placeholder="请输入配置键"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="配置类型">
            <el-input
              v-model="searchForm.configType"
              placeholder="请输入配置类型"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">
              搜索
            </el-button>
            <el-button :icon="Refresh" @click="handleResetSearch">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 工具栏 -->
      <div class="mb-4">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="configList" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="configKey" label="配置键" min-width="150" show-overflow-tooltip />
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip v-if="row.configValue && row.configValue.length > 50" :content="row.configValue" placement="top">
              <span>{{ row.configValue.substring(0, 50) + '...' }}</span>
            </el-tooltip>
            <span v-else>{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="configType" label="配置类型" min-width="100" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">
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

    <!-- 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" placeholder="请输入配置键" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input
            v-model="form.configValue"
            type="textarea"
            :rows="4"
            placeholder="请输入配置值"
          />
        </el-form-item>
        <el-form-item label="配置类型">
          <el-input v-model="form.configType" placeholder="请输入配置类型，如：system、email等" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="formLoading" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
