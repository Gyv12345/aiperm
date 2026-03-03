<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { captchaConfigApi, type CaptchaConfigDTO, type CaptchaConfigVO } from '@/api/system/captchaConfig'

const activeTab = ref<'SMS' | 'EMAIL'>('SMS')

// SMS 表单
const smsForm = ref<CaptchaConfigDTO & { enabled?: number }>({
  enabled: 0,
  smsProvider: 'ALIYUN',
  smsAccessKey: '',
  smsSecretKey: '',
  smsSignName: '',
  smsTemplateCode: '',
  codeLength: 6,
  expireMinutes: 5,
  dailyLimit: 10,
})

// Email 表单
const emailForm = ref<CaptchaConfigDTO & { enabled?: number }>({
  enabled: 0,
  emailHost: '',
  emailPort: 465,
  emailUsername: '',
  emailPassword: '',
  emailFrom: '',
  emailFromName: '',
  codeLength: 6,
  expireMinutes: 5,
  dailyLimit: 10,
})

const loading = ref(false)
const saveLoading = ref(false)

async function loadConfig(type: 'SMS' | 'EMAIL') {
  loading.value = true
  try {
    const data = await captchaConfigApi.getConfig(type)
    if (!data) return
    if (type === 'SMS') {
      Object.assign(smsForm.value, data)
      smsForm.value.smsSecretKey = ''  // 密钥不回显
    } else {
      Object.assign(emailForm.value, data)
      emailForm.value.emailPassword = ''  // 密码不回显
    }
  }
  catch (e: any) {
    ElMessage.error('加载配置失败：' + (e?.message || '未知错误'))
  }
  finally {
    loading.value = false
  }
}

async function saveConfig(type: 'SMS' | 'EMAIL') {
  saveLoading.value = true
  try {
    const dto = type === 'SMS' ? smsForm.value : emailForm.value
    await captchaConfigApi.updateConfig(type, dto)
    ElMessage.success('保存成功')
  }
  catch (e: any) {
    ElMessage.error('保存失败：' + (e?.message || '未知错误'))
  }
  finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  loadConfig('SMS')
  loadConfig('EMAIL')
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>验证码服务配置</h2>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 短信配置 -->
      <el-tab-pane
        label="短信（SMS）"
        name="SMS"
      >
        <el-form
          v-loading="loading"
          :model="smsForm"
          label-width="140px"
        >
          <el-form-item label="启用短信服务">
            <el-switch
              v-model="smsForm.enabled"
              :active-value="1"
              :inactive-value="0"
            />
          </el-form-item>
          <el-form-item label="服务商">
            <el-select v-model="smsForm.smsProvider">
              <el-option
                label="阿里云"
                value="ALIYUN"
              />
              <el-option
                label="腾讯云"
                value="TENCENT"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="AccessKey">
            <el-input
              v-model="smsForm.smsAccessKey"
              placeholder="前4位+****（修改时重新输入完整值）"
            />
          </el-form-item>
          <el-form-item label="SecretKey">
            <el-input
              v-model="smsForm.smsSecretKey"
              type="password"
              placeholder="修改时输入新值，不修改留空"
            />
          </el-form-item>
          <el-form-item label="签名名称">
            <el-input v-model="smsForm.smsSignName" />
          </el-form-item>
          <el-form-item label="模板ID">
            <el-input v-model="smsForm.smsTemplateCode" />
          </el-form-item>
          <el-form-item label="验证码长度">
            <el-input-number
              v-model="smsForm.codeLength"
              :min="4"
              :max="8"
            />
          </el-form-item>
          <el-form-item label="过期时间（分钟）">
            <el-input-number
              v-model="smsForm.expireMinutes"
              :min="1"
              :max="30"
            />
          </el-form-item>
          <el-form-item label="每日发送上限">
            <el-input-number
              v-model="smsForm.dailyLimit"
              :min="1"
              :max="100"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="saveLoading"
              @click="saveConfig('SMS')"
            >
              保存配置
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 邮件配置 -->
      <el-tab-pane
        label="邮件（EMAIL）"
        name="EMAIL"
      >
        <el-form
          v-loading="loading"
          :model="emailForm"
          label-width="140px"
        >
          <el-form-item label="启用邮件服务">
            <el-switch
              v-model="emailForm.enabled"
              :active-value="1"
              :inactive-value="0"
            />
          </el-form-item>
          <el-form-item label="SMTP 服务器">
            <el-input
              v-model="emailForm.emailHost"
              placeholder="smtp.example.com"
            />
          </el-form-item>
          <el-form-item label="SMTP 端口">
            <el-input-number
              v-model="emailForm.emailPort"
              :min="1"
              :max="65535"
            />
          </el-form-item>
          <el-form-item label="邮箱账号">
            <el-input v-model="emailForm.emailUsername" />
          </el-form-item>
          <el-form-item label="密码/授权码">
            <el-input
              v-model="emailForm.emailPassword"
              type="password"
              placeholder="修改时输入新值，不修改留空"
            />
          </el-form-item>
          <el-form-item label="发件人地址">
            <el-input v-model="emailForm.emailFrom" />
          </el-form-item>
          <el-form-item label="发件人名称">
            <el-input
              v-model="emailForm.emailFromName"
              placeholder="AIPerm"
            />
          </el-form-item>
          <el-form-item label="验证码长度">
            <el-input-number
              v-model="emailForm.codeLength"
              :min="4"
              :max="8"
            />
          </el-form-item>
          <el-form-item label="过期时间（分钟）">
            <el-input-number
              v-model="emailForm.expireMinutes"
              :min="1"
              :max="30"
            />
          </el-form-item>
          <el-form-item label="每日发送上限">
            <el-input-number
              v-model="emailForm.dailyLimit"
              :min="1"
              :max="100"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="saveLoading"
              @click="saveConfig('EMAIL')"
            >
              保存配置
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.page-container { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #1e293b; }
</style>
