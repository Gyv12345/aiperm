<!-- frontend/src/components/agent/AgentConfirm.vue -->
<template>
  <div class="confirm-container">
    <div class="confirm-card">
      <div class="confirm-header">
        <el-icon :size="20" color="#e6a23c"><WarningFilled /></el-icon>
        <span>操作确认</span>
      </div>
      <div class="confirm-body">
        <p>{{ message }}</p>
        <p class="tool-name">工具: {{ toolName }}</p>
      </div>
      <div class="confirm-actions">
        <el-button @click="$emit('cancel')">取消</el-button>
        <el-button type="primary" @click="$emit('confirm')">确认执行</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { WarningFilled } from '@element-plus/icons-vue'

defineProps<{
  message: string
  toolName: string
}>()

defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<style scoped>
.confirm-container {
  padding: 16px;
  background: white;
  border-top: 1px solid #ebeef5;
}

.confirm-card {
  background: #fdf6ec;
  border: 1px solid #e6a23c;
  border-radius: 8px;
  padding: 16px;
}

.confirm-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  margin-bottom: 12px;
}

.confirm-body p {
  margin: 0 0 8px;
  color: #606266;
}

.confirm-body .tool-name {
  font-size: 12px;
  color: #909399;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}
</style>
