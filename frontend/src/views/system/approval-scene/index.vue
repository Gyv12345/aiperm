<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {approvalSceneApi, type ApprovalHandlerVO, type ApprovalSceneDTO, type ApprovalSceneVO} from '@/api/approval'
import type {PageResult} from '@/types'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dialogType = ref<'create' | 'update'>('create')
const currentId = ref(0)
const formRef = ref<FormInstance>()

const tableData = ref<ApprovalSceneVO[]>([])
const handlers = ref<ApprovalHandlerVO[]>([])
const platformOptions = ['FEISHU', 'WEWORK', 'DINGTALK']

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const queryForm = reactive({
  sceneCode: '',
  sceneName: '',
  platform: '',
  enabled: undefined as number | undefined,
})

const formData = reactive<ApprovalSceneDTO>({
  sceneCode: '',
  sceneName: '',
  businessType: '',
  platform: 'FEISHU',
  templateId: '',
  enabled: 1,
  handlerBeanName: '',
  autoSubmitEnabled: 1,
  allowDuplicatePending: 0,
  timeoutHours: 72,
  timeoutAction: 'NOTIFY',
  notifyTemplateCode: '',
  remark: '',
})

const rules = computed<FormRules>(() => ({
  sceneCode: [
    { required: true, message: '请输入场景编码', trigger: 'blur' },
  ],
  sceneName: [
    { required: true, message: '请输入场景名称', trigger: 'blur' },
  ],
  businessType: [
    { required: true, message: '请输入业务类型', trigger: 'blur' },
  ],
  platform: [
    { required: true, message: '请选择平台', trigger: 'change' },
  ],
  templateId: [
    { required: true, message: '请输入模板 ID', trigger: 'blur' },
  ],
  handlerBeanName: [
    { required: true, message: '请选择处理器', trigger: 'change' },
  ],
}))

async function fetchHandlers() {
  try {
    handlers.value = await approvalSceneApi.handlers()
  }
  catch (error) {
    console.error('获取审批处理器失败:', error)
    ElMessage.error('获取审批处理器失败')
  }
}

async function fetchScenes() {
  loading.value = true
  try {
    const result = await approvalSceneApi.list({
      page: pagination.page,
      pageSize: pagination.pageSize,
      sceneCode: queryForm.sceneCode || undefined,
      sceneName: queryForm.sceneName || undefined,
      platform: queryForm.platform || undefined,
      enabled: queryForm.enabled,
    }) as PageResult<ApprovalSceneVO>
    tableData.value = result.list || []
    pagination.total = result.total || 0
  }
  catch (error) {
    console.error('获取审批场景失败:', error)
    ElMessage.error('获取审批场景失败')
  }
  finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchScenes()
}

function handleReset() {
  queryForm.sceneCode = ''
  queryForm.sceneName = ''
  queryForm.platform = ''
  queryForm.enabled = undefined
  pagination.page = 1
  fetchScenes()
}

function resetFormData() {
  Object.assign(formData, {
    sceneCode: '',
    sceneName: '',
    businessType: '',
    platform: 'FEISHU',
    templateId: '',
    enabled: 1,
    handlerBeanName: '',
    autoSubmitEnabled: 1,
    allowDuplicatePending: 0,
    timeoutHours: 72,
    timeoutAction: 'NOTIFY',
    notifyTemplateCode: '',
    remark: '',
  })
}

function handleCreate() {
  dialogType.value = 'create'
  currentId.value = 0
  resetFormData()
  dialogVisible.value = true
}

async function handleUpdate(row: ApprovalSceneVO) {
  try {
    const detail = await approvalSceneApi.getById(row.id)
    dialogType.value = 'update'
    currentId.value = row.id
    Object.assign(formData, {
      sceneCode: detail.sceneCode,
      sceneName: detail.sceneName,
      businessType: detail.businessType,
      platform: detail.platform,
      templateId: detail.templateId,
      enabled: detail.enabled,
      handlerBeanName: detail.handlerBeanName,
      autoSubmitEnabled: detail.autoSubmitEnabled,
      allowDuplicatePending: detail.allowDuplicatePending,
      timeoutHours: detail.timeoutHours,
      timeoutAction: detail.timeoutAction || 'NOTIFY',
      notifyTemplateCode: detail.notifyTemplateCode || '',
      remark: detail.remark || '',
    })
    dialogVisible.value = true
  }
  catch (error) {
    console.error('获取审批场景详情失败:', error)
    ElMessage.error('获取审批场景详情失败')
  }
}

async function handleDelete(row: ApprovalSceneVO) {
  try {
    await ElMessageBox.confirm(`确定要删除审批场景「${row.sceneName}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await approvalSceneApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchScenes()
  }
  catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除审批场景失败:', error)
      ElMessage.error('删除审批场景失败')
    }
  }
}

async function handleSubmit() {
  if (!formRef.value) {
    return
  }

  try {
    await formRef.value.validate()
    saving.value = true
    const payload: ApprovalSceneDTO = {
      sceneCode: formData.sceneCode,
      sceneName: formData.sceneName,
      businessType: formData.businessType,
      platform: formData.platform,
      templateId: formData.templateId,
      enabled: formData.enabled,
      handlerBeanName: formData.handlerBeanName,
      autoSubmitEnabled: formData.autoSubmitEnabled,
      allowDuplicatePending: formData.allowDuplicatePending,
      timeoutHours: formData.timeoutHours,
      timeoutAction: formData.timeoutAction || undefined,
      notifyTemplateCode: formData.notifyTemplateCode || undefined,
      remark: formData.remark || undefined,
    }
    if (dialogType.value === 'create') {
      await approvalSceneApi.create(payload)
      ElMessage.success('创建成功')
    }
    else {
      await approvalSceneApi.update(currentId.value, payload)
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    fetchScenes()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存审批场景失败:', error)
      ElMessage.error('保存审批场景失败')
    }
  }
  finally {
    saving.value = false
  }
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchScenes()
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchScenes()
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

onMounted(async () => {
  await Promise.all([fetchHandlers(), fetchScenes()])
})
</script>

<template>
  <div class="p-4 space-y-4">
    <el-card shadow="never">
      <el-form
        :model="queryForm"
        label-width="72px"
      >
        <el-row :gutter="12">
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <el-form-item label="场景编码">
              <el-input
                v-model="queryForm.sceneCode"
                placeholder="请输入场景编码"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <el-form-item label="场景名称">
              <el-input
                v-model="queryForm.sceneName"
                placeholder="请输入场景名称"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="5"
          >
            <el-form-item label="平台">
              <el-select
                v-model="queryForm.platform"
                placeholder="全部平台"
                clearable
              >
                <el-option
                  v-for="item in platformOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="5"
          >
            <el-form-item label="状态">
              <el-select
                v-model="queryForm.enabled"
                placeholder="全部状态"
                clearable
              >
                <el-option
                  label="启用"
                  :value="1"
                />
                <el-option
                  label="停用"
                  :value="0"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="24"
            :md="24"
            :lg="2"
            class="flex items-center justify-end"
          >
            <div class="flex gap-2">
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
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div class="text-base font-semibold">
            审批场景列表
          </div>
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleCreate"
          >
            新增场景
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
      >
        <el-table-column
          prop="sceneCode"
          label="场景编码"
          min-width="160"
        />
        <el-table-column
          prop="sceneName"
          label="场景名称"
          min-width="160"
        />
        <el-table-column
          prop="businessType"
          label="业务类型"
          min-width="120"
        />
        <el-table-column
          prop="platform"
          label="平台"
          width="110"
        />
        <el-table-column
          prop="templateId"
          label="模板ID"
          min-width="160"
          show-overflow-tooltip
        />
        <el-table-column
          prop="handlerBeanName"
          label="处理器"
          min-width="160"
        />
        <el-table-column
          label="启用"
          width="90"
        >
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="自动提交"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.autoSubmitEnabled === 1 ? 'success' : 'warning'">
              {{ row.autoSubmitEnabled === 1 ? '开启' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="重复待审"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.allowDuplicatePending === 1 ? 'warning' : 'info'">
              {{ row.allowDuplicatePending === 1 ? '允许' : '拦截' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          min-width="180"
        />
        <el-table-column
          label="操作"
          fixed="right"
          width="150"
        >
          <template #default="{ row }">
            <div class="flex gap-2">
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
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :current-page="pagination.page"
          :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '新增审批场景' : '编辑审批场景'"
      width="760px"
      @closed="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item
              label="场景编码"
              prop="sceneCode"
            >
              <el-input
                v-model="formData.sceneCode"
                placeholder="如 ORDER_APPROVAL"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="场景名称"
              prop="sceneName"
            >
              <el-input
                v-model="formData.sceneName"
                placeholder="请输入场景名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="业务类型"
              prop="businessType"
            >
              <el-input
                v-model="formData.businessType"
                placeholder="如 ORDER"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="平台"
              prop="platform"
            >
              <el-select v-model="formData.platform">
                <el-option
                  v-for="item in platformOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="模板ID"
              prop="templateId"
            >
              <el-input
                v-model="formData.templateId"
                placeholder="平台审批模板ID"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="处理器"
              prop="handlerBeanName"
            >
              <el-select
                v-model="formData.handlerBeanName"
                placeholder="请选择业务处理器"
                filterable
              >
                <el-option
                  v-for="item in handlers"
                  :key="item.beanName"
                  :label="`${item.beanName} (${item.displayName})`"
                  :value="item.beanName"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="启用状态">
              <el-radio-group v-model="formData.enabled">
                <el-radio :value="1">
                  启用
                </el-radio>
                <el-radio :value="0">
                  停用
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="自动提交">
              <el-radio-group v-model="formData.autoSubmitEnabled">
                <el-radio :value="1">
                  开启
                </el-radio>
                <el-radio :value="0">
                  关闭
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="重复待审">
              <el-radio-group v-model="formData.allowDuplicatePending">
                <el-radio :value="0">
                  拦截
                </el-radio>
                <el-radio :value="1">
                  允许
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="超时小时数">
              <el-input-number
                v-model="formData.timeoutHours"
                :min="1"
                :max="720"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="超时动作">
              <el-select v-model="formData.timeoutAction">
                <el-option
                  label="通知"
                  value="NOTIFY"
                />
                <el-option
                  label="自动通过"
                  value="AUTO_PASS"
                />
                <el-option
                  label="自动拒绝"
                  value="AUTO_REJECT"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="通知模板编码">
              <el-input
                v-model="formData.notifyTemplateCode"
                placeholder="如 APPROVAL_SUBMIT"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="4"
                placeholder="可记录场景规则、模板约束、处理器说明等"
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
          :loading="saving"
          @click="handleSubmit"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
