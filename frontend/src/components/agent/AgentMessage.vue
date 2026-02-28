<!-- frontend/src/components/agent/AgentMessage.vue -->
<template>
  <div :class="['message-bubble', message.role]">
    <div v-if="message.role === 'user'" class="avatar user-avatar">
      <el-icon><User /></el-icon>
    </div>

    <div class="content">
      <div class="text" v-html="formattedContent"></div>
      <div v-if="message.toolName" class="tool-info">
        <el-tag size="small" type="info">{{ message.toolName }}</el-tag>
      </div>
    </div>

    <div v-if="message.role === 'assistant'" class="avatar assistant-avatar">
      <el-icon><Cpu /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { User, Cpu } from '@element-plus/icons-vue'
import type { Message } from '@/api/agent'

const props = defineProps<{
  message: Message
}>()

const formattedContent = computed(() => {
  // 简单的换行处理
  return props.message.content
    .replace(/\n/g, '<br>')
    .replace(/ /g, '&nbsp;')
})
</script>

<style scoped>
.message-bubble {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 85%;
}

.message-bubble.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar {
  background: #409eff;
  color: white;
}

.assistant-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.content {
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message-bubble.user .content {
  background: #409eff;
  color: white;
}

.text {
  line-height: 1.5;
  word-break: break-word;
}

.tool-info {
  margin-top: 8px;
}
</style>
