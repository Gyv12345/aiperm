<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { mfaApi, type MfaQrcodeVO } from '@/api/mfa'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'success'): void
}>()

const qrcodeData = ref<MfaQrcodeVO | null>(null)
const verifyCode = ref('')
const loading = ref(false)
const step = ref<1 | 2>(1)  // 步骤1：扫码，步骤2：验证

// 生成二维码图片（使用公共二维码生成服务）
function getTotpQrUrl(uri: string) {
  return `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(uri)}`
}

async function loadQrCode() {
  try {
    const data = await mfaApi.qrcode()
    if (data) qrcodeData.value = data
    step.value = 1
    verifyCode.value = ''
  }
  catch (e: any) {
    ElMessage.error(e?.message || '获取二维码失败')
  }
}

async function confirmBind() {
  if (!verifyCode.value || verifyCode.value.length !== 6) {
    ElMessage.warning('请输入6位验证码')
    return
  }
  loading.value = true
  try {
    await mfaApi.confirmBind({ code: verifyCode.value })
    ElMessage.success('2FA绑定成功')
    emit('success')
    emit('update:visible', false)
  }
  catch (e: any) {
    ElMessage.error(e?.message || '绑定失败')
    verifyCode.value = ''
  }
  finally {
    loading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val) loadQrCode()
})
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="绑定双因素认证（2FA）"
    width="460px"
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="mfa-bind-content">
      <!-- 步骤说明 -->
      <el-steps :active="step" finish-status="success" class="steps">
        <el-step title="扫描二维码" />
        <el-step title="输入验证码" />
      </el-steps>

      <div v-if="qrcodeData" class="qr-section">
        <p class="step-desc">
          1. 打开 <strong>Google Authenticator</strong> 或 <strong>Microsoft Authenticator</strong>
        </p>
        <p class="step-desc">2. 扫描下方二维码，或手动输入密钥：</p>

        <div class="qrcode-wrapper">
          <img :src="getTotpQrUrl(qrcodeData.totpUri)" alt="2FA QR Code" class="qrcode-img">
        </div>

        <div class="secret-key">
          <span>手动输入密钥：</span>
          <code>{{ qrcodeData.secretKey }}</code>
        </div>

        <el-divider />

        <p class="step-desc">3. 输入 App 显示的 <strong>6位验证码</strong>：</p>
        <el-input
          v-model="verifyCode"
          placeholder="000000"
          maxlength="6"
          class="code-input"
          @keyup.enter="confirmBind"
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="confirmBind">
        确认绑定
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.mfa-bind-content { padding: 0 8px; }
.steps { margin-bottom: 24px; }
.step-desc { color: #4b5563; margin-bottom: 8px; line-height: 1.6; }
.qrcode-wrapper { display: flex; justify-content: center; margin: 16px 0; }
.qrcode-img { width: 180px; height: 180px; border: 1px solid #e5e7eb; border-radius: 8px; }
.secret-key { background: #f8fafc; padding: 8px 12px; border-radius: 6px; font-size: 13px; margin-bottom: 12px; }
.secret-key code { font-family: monospace; color: #2563eb; margin-left: 8px; letter-spacing: 2px; }
.code-input :deep(.el-input__inner) { font-size: 20px; letter-spacing: 8px; text-align: center; }
</style>
