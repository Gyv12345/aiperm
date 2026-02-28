<!-- frontend/src/components/agent/AgentInput.vue -->
<template>
  <div class="input-container">
    <el-input
      v-model="inputValue"
      type="textarea"
      :rows="1"
      :autosize="{ minRows: 1, maxRows: 4 }"
      placeholder="输入消息..."
      :disabled="disabled"
      @keydown.enter.exact.prevent="handleSend"
    />
    <el-button
      type="primary"
      circle
      :disabled="!inputValue.trim() || disabled"
      @click="handleSend"
    >
      <el-icon><Promotion /></el-icon>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Promotion } from '@element-plus/icons-vue'

defineProps<{
  disabled: boolean
}>()

const emit = defineEmits<{
  send: [message: string]
}>()

const inputValue = ref('')

const handleSend = () => {
  if (!inputValue.value.trim()) return

  emit('send', inputValue.value.trim())
  inputValue.value = ''
}
</script>

<style scoped>
.input-container {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: white;
  border-top: 1px solid #ebeef5;
}

.input-container :deep(.el-textarea) {
  flex: 1;
}

.input-container .el-button {
  align-self: flex-end;
}
</style>
