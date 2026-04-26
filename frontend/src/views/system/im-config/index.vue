<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, type FormInstance, type FormRules} from 'element-plus'
import {Edit, Refresh} from '@element-plus/icons-vue'
import {imConfigApi, type ImConfigDTO, type ImConfigVO} from '@/api/system/imConfig'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const currentPlatform = ref('')
const configList = ref<ImConfigVO[]>([])

const formData = reactive<ImConfigDTO>({
  enabled: 0,
  appId: '',
  appSecret: '',
  corpId: '',
  callbackToken: '',
  callbackAesKey: '',
  extraConfig: '{\n  "simulationMode": true,\n  "todoUrl": ""\n}',
  remark: '',
})

const rules = computed<FormRules>(() => ({
  enabled: [
    { required: true, message: '请选择启用状态', trigger: 'change' },
  ],
}))

const enabledCount = computed(() => configList.value.filter(item => item.enabled === 1).length)

async function fetchConfigs() {
  loading.value = true
  try {
    configList.value = await imConfigApi.list()
  }
  catch (error) {
    console.error('获取 IM 配置失败:', error)
    ElMessage.error('获取 IM 配置失败')
  }
  finally {
    loading.value = false
  }
}

function openEdit(row: ImConfigVO) {
  currentPlatform.value = row.platform
  Object.assign(formData, {
    enabled: row.enabled ?? 0,
    appId: row.appId ?? '',
    appSecret: row.appSecret ?? '',
    corpId: row.corpId ?? '',
    callbackToken: row.callbackToken ?? '',
    callbackAesKey: row.callbackAesKey ?? '',
    extraConfig: row.extraConfig || '{\n  "simulationMode": true,\n  "todoUrl": ""\n}',
    remark: row.remark ?? '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) {
    return
  }

  try {
    await formRef.value.validate()
    saving.value = true
    await imConfigApi.update(currentPlatform.value, {
      enabled: formData.enabled,
      appId: formData.appId || undefined,
      appSecret: formData.appSecret || undefined,
      corpId: formData.corpId || undefined,
      callbackToken: formData.callbackToken || undefined,
      callbackAesKey: formData.callbackAesKey || undefined,
      extraConfig: formData.extraConfig || undefined,
      remark: formData.remark || undefined,
    })
    ElMessage.success('配置已保存')
    dialogVisible.value = false
    fetchConfigs()
  }
  catch (error: unknown) {
    if (error !== false) {
      console.error('保存 IM 配置失败:', error)
      ElMessage.error('保存 IM 配置失败')
    }
  }
  finally {
    saving.value = false
  }
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchConfigs()
})
</script>

<template>
  <div class="p-4 space-y-4">
    <el-card shadow="never">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-lg font-semibold">
            IM 平台配置
          </div>
          <div class="mt-1 text-sm text-[var(--el-text-color-secondary)]">
            当前已启用 {{ enabledCount }} / {{ configList.length }} 个审批平台。`extraConfig` 支持 `simulationMode` 与 `todoUrl`。
          </div>
        </div>
        <el-button
          :icon="Refresh"
          :loading="loading"
          @click="fetchConfigs"
        >
          刷新
        </el-button>
      </div>
    </el-card>

    <div
      v-loading="loading"
      class="grid gap-4 md:grid-cols-2 xl:grid-cols-3"
    >
      <el-card
        v-for="item in configList"
        :key="item.platform"
        shadow="hover"
        class="min-h-[280px]"
      >
        <div class="flex items-start justify-between gap-3">
          <div>
            <div class="text-base font-semibold">
              {{ item.platform }}
            </div>
            <div class="mt-1 flex flex-wrap gap-2">
              <el-tag :type="item.enabled === 1 ? 'success' : 'info'">
                {{ item.enabled === 1 ? '已启用' : '未启用' }}
              </el-tag>
              <el-tag :type="item.configReady ? 'success' : 'warning'">
                {{ item.configReady ? '配置完整' : '待补齐' }}
              </el-tag>
            </div>
          </div>
          <el-button
            type="primary"
            plain
            :icon="Edit"
            @click="openEdit(item)"
          >
            编辑
          </el-button>
        </div>

        <el-descriptions
          :column="1"
          border
          class="mt-4"
        >
          <el-descriptions-item label="App ID">
            {{ item.appId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="Corp ID">
            {{ item.corpId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="缺失字段">
            <template v-if="item.missingFields?.length">
              <div class="flex flex-wrap gap-2">
                <el-tag
                  v-for="field in item.missingFields"
                  :key="field"
                  type="danger"
                  effect="plain"
                >
                  {{ field }}
                </el-tag>
              </div>
            </template>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="备注">
            {{ item.remark || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="`编辑 ${currentPlatform} 配置`"
      width="680px"
      @closed="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="128px"
      >
        <el-form-item
          label="启用状态"
          prop="enabled"
        >
          <el-radio-group v-model="formData.enabled">
            <el-radio :value="1">
              启用
            </el-radio>
            <el-radio :value="0">
              禁用
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="App ID">
          <el-input
            v-model="formData.appId"
            placeholder="请输入平台 App ID"
          />
        </el-form-item>
        <el-form-item label="App Secret">
          <el-input
            v-model="formData.appSecret"
            placeholder="保留现值可直接提交"
            show-password
          />
        </el-form-item>
        <el-form-item label="Corp ID">
          <el-input
            v-model="formData.corpId"
            placeholder="企业ID（企微等平台需要）"
          />
        </el-form-item>
        <el-form-item label="Callback Token">
          <el-input
            v-model="formData.callbackToken"
            placeholder="请输入回调校验 Token"
          />
        </el-form-item>
        <el-form-item label="Callback AES Key">
          <el-input
            v-model="formData.callbackAesKey"
            placeholder="请输入回调 AES Key"
          />
        </el-form-item>
        <el-form-item label="扩展配置 JSON">
          <el-input
            v-model="formData.extraConfig"
            type="textarea"
            :rows="8"
            placeholder="例如：{ &quot;simulationMode&quot;: true, &quot;todoUrl&quot;: &quot;https://...&quot; }"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="记录平台对接说明、回调地址约定等"
          />
        </el-form-item>
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
