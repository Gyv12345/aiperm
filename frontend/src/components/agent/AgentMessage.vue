<!-- frontend/src/components/agent/AgentMessage.vue -->
<template>
  <div :class="['message-bubble', message.role]">
    <div
      v-if="message.role === 'user'"
      class="avatar user-avatar"
    >
      <el-icon><User /></el-icon>
    </div>

    <div class="content">
      <div
        class="text markdown-body"
        v-html="formattedContent"
      />
      <div
        v-if="message.uiPayload"
        class="ui-payload"
      >
        <div
          v-if="uiTitle"
          class="ui-title"
        >
          {{ uiTitle }}
        </div>
        <div
          v-if="uiDescription"
          class="ui-description"
        >
          {{ uiDescription }}
        </div>
        <pre class="ui-json">{{ uiJson }}</pre>
      </div>
      <div
        v-if="message.toolName"
        class="tool-info"
      >
        <el-tag
          size="small"
          type="info"
        >
          {{ message.toolName }}
        </el-tag>
      </div>
    </div>

    <div
      v-if="message.role === 'assistant'"
      class="avatar assistant-avatar"
    >
      <el-icon><Cpu /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { User, Cpu } from '@element-plus/icons-vue'
import type { Message } from '@/api/agent'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps<{
  message: Message
}>()

const formattedContent = computed(() => {
  return renderMarkdown(props.message.content || '')
})

const uiPayload = computed(() => props.message.uiPayload ?? {})

const uiTitle = computed(() => {
  const value = uiPayload.value.title ?? uiPayload.value.name ?? uiPayload.value.type
  return typeof value === 'string' ? value : ''
})

const uiDescription = computed(() => {
  const value = uiPayload.value.description ?? uiPayload.value.text ?? uiPayload.value.markdown
  return typeof value === 'string' ? value : ''
})

const uiJson = computed(() => JSON.stringify(uiPayload.value, null, 2))
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

.markdown-body :deep(p) {
  margin: 0 0 8px;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 0 0 8px 18px;
  padding: 0;
}

.markdown-body :deep(pre) {
  margin: 6px 0;
  padding: 10px;
  border-radius: 8px;
  background: #0f172a;
  color: #e2e8f0;
  overflow-x: auto;
}

.markdown-body :deep(code) {
  font-family: Menlo, Monaco, Consolas, monospace;
}

.markdown-body :deep(a) {
  color: #2563eb;
}

.ui-payload {
  margin-top: 10px;
  border-top: 1px dashed #dcdfe6;
  padding-top: 8px;
}

.ui-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.ui-description {
  color: #606266;
  margin-bottom: 6px;
}

.ui-json {
  margin: 0;
  max-height: 220px;
  overflow: auto;
  border-radius: 6px;
  padding: 8px;
  background: #f5f7fa;
  font-size: 12px;
}

.tool-info {
  margin-top: 8px;
}
</style>
