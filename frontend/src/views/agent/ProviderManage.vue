<!-- frontend/src/views/agent/ProviderManage.vue -->
<template>
  <div class="provider-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>LLM 提供商管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <el-table :data="providers" v-loading="loading" stripe>
        <el-table-column prop="displayName" label="名称" width="150" />
        <el-table-column prop="name" label="标识" width="120" />
        <el-table-column prop="protocol" label="协议" width="110">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.protocol }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="模型" width="150" />
        <el-table-column prop="baseUrl" label="API 地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="默认" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button
              link
              type="primary"
              @click="handleSetDefault(row)"
              :disabled="row.isDefault"
            >
              设为默认
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(row)"
              :disabled="row.isDefault"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editId ? '编辑提供商' : '新增提供商'"
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标识" prop="name" v-if="!editId">
          <el-select v-model="form.name" placeholder="选择提供商" @change="handleProviderChange">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="通义千问" value="qwen" />
            <el-option label="OpenAI" value="openai" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="form.displayName" placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="协议" prop="protocol">
          <el-select v-model="form.protocol" placeholder="选择协议" @change="handleProtocolChange">
            <el-option label="OpenAI Compatible" value="openai" />
            <el-option label="Anthropic" value="anthropic" />
          </el-select>
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="form.apiKey"
            type="password"
            placeholder="API Key"
            show-password
          />
        </el-form-item>
        <el-form-item label="API 地址" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="API 地址" />
        </el-form-item>
        <el-form-item label="模型" prop="model">
          <el-input v-model="form.model" placeholder="模型名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="0" :inactive-value="1" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  listProviders,
  createProvider,
  updateProvider,
  deleteProvider,
  setDefaultProvider,
  type LlmProvider,
  type LlmProviderDTO
} from '@/api/agent/provider'

const loading = ref(false)
const providers = ref<LlmProvider[]>([])
const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<LlmProviderDTO>({
  name: '',
  displayName: '',
  protocol: 'openai',
  apiKey: '',
  baseUrl: '',
  model: '',
  isDefault: false,
  status: 0,
  remark: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请选择提供商', trigger: 'change' }],
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  protocol: [{ required: true, message: '请选择协议', trigger: 'change' }],
  apiKey: [{ required: true, message: '请输入 API Key', trigger: 'blur' }],
  model: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
}

const providerDefaults: Record<string, { displayName: string; protocol: 'openai' | 'anthropic'; baseUrl: string; model: string }> = {
  deepseek: { displayName: 'DeepSeek', protocol: 'openai', baseUrl: 'https://api.deepseek.com', model: 'deepseek-chat' },
  qwen: { displayName: '通义千问', protocol: 'openai', baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1', model: 'qwen-plus' },
  openai: { displayName: 'OpenAI', protocol: 'openai', baseUrl: 'https://api.openai.com', model: 'gpt-4o-mini' }
}

const handleProviderChange = (name: string) => {
  const defaults = providerDefaults[name]
  if (defaults) {
    form.displayName = defaults.displayName
    form.protocol = defaults.protocol
    form.baseUrl = defaults.baseUrl
    form.model = defaults.model
  }
}

const handleProtocolChange = (protocol: 'openai' | 'anthropic') => {
  if (protocol === 'anthropic' && !form.baseUrl) {
    form.baseUrl = 'https://api.anthropic.com'
  }
}

const loadProviders = async () => {
  loading.value = true
  try {
    const res = await listProviders()
    providers.value = res
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editId.value = null
  Object.assign(form, {
    name: '',
    displayName: '',
    protocol: 'openai',
    apiKey: '',
    baseUrl: '',
    model: '',
    isDefault: false,
    status: 0,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: LlmProvider) => {
  editId.value = row.id
  Object.assign(form, {
    name: row.name,
    displayName: row.displayName,
    protocol: row.protocol || 'openai',
    apiKey: '',
    baseUrl: row.baseUrl,
    model: row.model,
    isDefault: row.isDefault,
    status: row.status,
    remark: row.remark
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (editId.value) {
        await updateProvider(editId.value, form)
        ElMessage.success('更新成功')
      } else {
        await createProvider(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadProviders()
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleSetDefault = async (row: LlmProvider) => {
  try {
    await ElMessageBox.confirm(`确定将 "${row.displayName}" 设为默认提供商？`, '提示')
    await setDefaultProvider(row.id)
    ElMessage.success('设置成功')
    loadProviders()
  } catch {
    // 用户取消
  }
}

const handleDelete = async (row: LlmProvider) => {
  try {
    await ElMessageBox.confirm(`确定删除 "${row.displayName}"？`, '警告', {
      type: 'warning'
    })
    await deleteProvider(row.id)
    ElMessage.success('删除成功')
    loadProviders()
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadProviders()
})
</script>

<style scoped>
.provider-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
