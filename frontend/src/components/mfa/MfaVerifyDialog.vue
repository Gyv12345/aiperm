<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { mfaApi } from '@/api/mfa'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'success'): void
}>()

const code = ref('')
const loading = ref(false)

async function handleVerify() {
  if (!code.value || code.value.length !== 6) {
    ElMessage.warning('请输入6位验证码')
    return
  }
  loading.value = true
  try {
    await mfaApi.verify({ code: code.value })
    ElMessage.success('验证成功')
    emit('success')
    emit('update:visible', false)
  }
  catch (e: any) {
    ElMessage.error(e?.message || '验证失败')
    code.value = ''
  }
  finally {
    loading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    code.value = ''
  }
})
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="安全验证"
    width="360px"
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="mfa-verify-content">
      <p class="desc">
        此操作需要二次验证，请打开 Authenticator App 输入当前验证码：
      </p>
      <el-input
        v-model="code"
        placeholder="000000"
        maxlength="6"
        class="code-input"
        @keyup.enter="handleVerify"
      />
    </div>

    <template #footer>
      <el-button @click="emit('update:visible', false)">
        取消
      </el-button>
      <el-button
        type="primary"
        :loading="loading"
        @click="handleVerify"
      >
        确认验证
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.mfa-verify-content { padding: 0 8px; }
.desc { color: #4b5563; margin-bottom: 20px; line-height: 1.6; }
.code-input :deep(.el-input__inner) { font-size: 24px; letter-spacing: 10px; text-align: center; }
</style>
