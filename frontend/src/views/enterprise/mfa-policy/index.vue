<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mfaPolicyApi, type MfaPolicyVO, type MfaPolicyDTO } from '@/api/system/mfaPolicy'

const tableData = ref<MfaPolicyVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saveLoading = ref(false)

const form = ref<MfaPolicyDTO>({
  name: '',
  permPattern: '',
  apiPattern: '',
  enabled: 1,
})

async function loadData() {
  loading.value = true
  try {
    const data = await mfaPolicyApi.list()
    tableData.value = data || []
  }
  finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { name: '', permPattern: '', apiPattern: '', enabled: 1 }
  dialogVisible.value = true
}

function openEdit(row: MfaPolicyVO) {
  editingId.value = row.id
  form.value = {
    name: row.name,
    permPattern: row.permPattern,
    apiPattern: row.apiPattern,
    enabled: row.enabled,
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name) {
    ElMessage.warning('策略名称不能为空')
    return
  }
  saveLoading.value = true
  try {
    if (editingId.value) {
      await mfaPolicyApi.update(editingId.value, form.value)
    } else {
      await mfaPolicyApi.create(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  }
  catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  }
  finally {
    saveLoading.value = false
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除此策略？', '提示', { type: 'warning' })
  try {
    await mfaPolicyApi.delete(id)
    ElMessage.success('删除成功')
    loadData()
  }
  catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>2FA 策略配置</h2>
      <el-button type="primary" @click="openCreate">新增策略</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border>
      <el-table-column prop="name" label="策略名称" />
      <el-table-column prop="permPattern" label="权限标识匹配" />
      <el-table-column prop="apiPattern" label="API路径匹配" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">
            {{ row.enabled ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑策略' : '新增策略'" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="策略名称" required>
          <el-input v-model="form.name" placeholder="如：修改密码" />
        </el-form-item>
        <el-form-item label="权限标识匹配">
          <el-input v-model="form.permPattern" placeholder="如：system:user:delete" />
          <div class="form-tip">支持通配符 *，如：system:user:*</div>
        </el-form-item>
        <el-form-item label="API路径匹配">
          <el-input v-model="form.apiPattern" placeholder="如：/system/user/*" />
          <div class="form-tip">支持通配符 *，如：/system/user/*</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #1e293b; }
.form-tip { font-size: 12px; color: #94a3b8; margin-top: 4px; }
</style>
