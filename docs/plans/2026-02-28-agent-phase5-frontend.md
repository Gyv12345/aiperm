# AI Agent Phase 5: 前端实现

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 创建前端悬浮球、抽屉对话面板和对话组件

**Architecture:** 悬浮球全局入口 + 抽屉对话面板 + Pinia 状态管理 + SSE 流式接收

**Tech Stack:** Vue 3 + TypeScript + Element Plus + Pinia

---

## Task 1: 创建 API 类型定义

**Files:**
- Create: `frontend/src/api/agent/types.ts`

**Step 1: 创建类型定义**

```typescript
// frontend/src/api/agent/types.ts

/**
 * 聊天请求
 */
export interface ChatRequest {
  message: string
  sessionId?: string
}

/**
 * 确认请求
 */
export interface ConfirmRequest {
  sessionId: string
  actionId: string
}

/**
 * SSE 事件
 */
export interface ChatEvent {
  type: 'text' | 'confirm' | 'tool_result' | 'done' | 'error'
  delta?: string
  actionId?: string
  toolName?: string
  confirmMessage?: string
  result?: unknown
  error?: string
}

/**
 * 消息
 */
export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
  toolName?: string
  needConfirm?: boolean
  actionId?: string
}

/**
 * 会话
 */
export interface Session {
  sessionId: string
  messages: Message[]
}
```

**Step 2: Commit**

```bash
git add frontend/src/api/agent/types.ts
git commit -m "feat(agent): add agent API type definitions"
```

---

## Task 2: 创建 Agent API

**Files:**
- Create: `frontend/src/api/agent/index.ts`

**Step 1: 创建 API**

```typescript
// frontend/src/api/agent/index.ts

import type { ChatRequest, ConfirmRequest } from './types'
import { request } from '@/utils/request'

const BASE_URL = '/agent'

/**
 * 创建会话
 */
export function createSession() {
  return request.post<{ sessionId: string }>(`${BASE_URL}/session`)
}

/**
 * 删除会话
 */
export function deleteSession(sessionId: string) {
  return request.delete(`${BASE_URL}/session/${sessionId}`)
}

/**
 * 同步对话 (企微等场景)
 */
export function chat(data: ChatRequest) {
  return request.post<{
    sessionId: string
    content: string
    needConfirm?: boolean
    actionId?: string
    toolName?: string
    confirmMessage?: string
  }>(`${BASE_URL}/chat`, data)
}

/**
 * 获取 SSE 流式对话 URL
 */
export function getChatStreamUrl(): string {
  return `${import.meta.env.VITE_API_BASE_URL}${BASE_URL}/chat/stream`
}

/**
 * 获取确认操作 URL
 */
export function getConfirmUrl(): string {
  return `${import.meta.env.VITE_API_BASE_URL}${BASE_URL}/confirm`
}

export * from './types'
```

**Step 2: Commit**

```bash
git add frontend/src/api/agent/index.ts
git commit -m "feat(agent): add agent API functions"
```

---

## Task 3: 创建 Pinia Store

**Files:**
- Create: `frontend/src/stores/agent.ts`

**Step 1: 创建 Store**

```typescript
// frontend/src/stores/agent.ts

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Message, ChatEvent } from '@/api/agent'
import { createSession, deleteSession, getChatStreamUrl, getConfirmUrl } from '@/api/agent'
import { useUserStore } from './user'

export const useAgentStore = defineStore('agent', () => {
  const userStore = useUserStore()

  // 状态
  const sessionId = ref('')
  const messages = ref<Message[]>([])
  const loading = ref(false)
  const isOpen = ref(false)

  // 待确认操作
  const pendingConfirm = ref<{
    actionId: string
    toolName: string
    message: string
  } | null>(null)

  // 未读消息数
  const unreadCount = computed(() => {
    if (isOpen.value) return 0
    return messages.value.filter(m => m.role === 'assistant' && !m.read).length
  })

  /**
   * 初始化会话
   */
  async function initSession() {
    if (sessionId.value) return

    try {
      const res = await createSession()
      sessionId.value = res.data.sessionId
    } catch (e) {
      console.error('Failed to create session:', e)
    }
  }

  /**
   * 发送消息
   */
  async function sendMessage(content: string) {
    if (!content.trim() || loading.value) return

    // 确保有会话
    if (!sessionId.value) {
      await initSession()
    }

    // 添加用户消息
    const userMsg: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: content.trim(),
      timestamp: new Date()
    }
    messages.value.push(userMsg)

    // 添加空的助手消息 (用于流式填充)
    const assistantMsg: Message = {
      id: (Date.now() + 1).toString(),
      role: 'assistant',
      content: '',
      timestamp: new Date()
    }
    messages.value.push(assistantMsg)

    loading.value = true
    pendingConfirm.value = null

    // SSE 流式请求
    const eventSource = new EventSource(
      `${getChatStreamUrl()}?sessionId=${sessionId.value}`,
      { withCredentials: true }
    )

    // 发送消息需要通过 POST，但 EventSource 只支持 GET
    // 所以我们改用 fetch + ReadableStream
    try {
      const response = await fetch(getChatStreamUrl(), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userStore.token}`
        },
        body: JSON.stringify({
          message: content.trim(),
          sessionId: sessionId.value
        })
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('No reader available')
      }

      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })

        // 解析 SSE 事件
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            try {
              const event: ChatEvent = JSON.parse(line.slice(5).trim())
              handleEvent(event, assistantMsg)
            } catch (e) {
              console.error('Failed to parse event:', line, e)
            }
          }
        }
      }
    } catch (e) {
      console.error('Failed to send message:', e)
      assistantMsg.content = '发送失败，请重试'
    } finally {
      loading.value = false
    }
  }

  /**
   * 处理 SSE 事件
   */
  function handleEvent(event: ChatEvent, assistantMsg: Message) {
    switch (event.type) {
      case 'text':
        assistantMsg.content += event.delta || ''
        break

      case 'confirm':
        pendingConfirm.value = {
          actionId: event.actionId || '',
          toolName: event.toolName || '',
          message: event.confirmMessage || '确认执行此操作？'
        }
        assistantMsg.needConfirm = true
        assistantMsg.actionId = event.actionId
        assistantMsg.toolName = event.toolName
        break

      case 'tool_result':
        // 可以显示工具执行结果
        break

      case 'done':
        loading.value = false
        break

      case 'error':
        assistantMsg.content = event.error || '发生错误'
        loading.value = false
        break
    }
  }

  /**
   * 确认操作
   */
  async function confirmAction() {
    if (!pendingConfirm.value) return

    loading.value = true
    const confirmMsg = pendingConfirm.value
    pendingConfirm.value = null

    try {
      const response = await fetch(getConfirmUrl(), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userStore.token}`
        },
        body: JSON.stringify({
          sessionId: sessionId.value,
          actionId: confirmMsg.actionId
        })
      })

      const reader = response.body?.getReader()
      if (!reader) return

      const decoder = new TextDecoder()
      let buffer = ''

      // 添加新的助手消息
      const assistantMsg: Message = {
        id: Date.now().toString(),
        role: 'assistant',
        content: '',
        timestamp: new Date()
      }
      messages.value.push(assistantMsg)

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            try {
              const event: ChatEvent = JSON.parse(line.slice(5).trim())
              handleEvent(event, assistantMsg)
            } catch (e) {
              console.error('Failed to parse event:', line, e)
            }
          }
        }
      }
    } catch (e) {
      console.error('Failed to confirm action:', e)
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消确认
   */
  function cancelConfirm() {
    pendingConfirm.value = null
  }

  /**
   * 清空消息
   */
  async function clearMessages() {
    if (sessionId.value) {
      await deleteSession(sessionId.value)
    }
    messages.value = []
    sessionId.value = ''
    pendingConfirm.value = null
  }

  /**
   * 切换抽屉
   */
  function toggleDrawer() {
    isOpen.value = !isOpen.value
    if (isOpen.value) {
      // 标记所有消息为已读
      messages.value.forEach(m => m.read = true)
    }
  }

  return {
    sessionId,
    messages,
    loading,
    isOpen,
    pendingConfirm,
    unreadCount,
    initSession,
    sendMessage,
    confirmAction,
    cancelConfirm,
    clearMessages,
    toggleDrawer
  }
})
```

**Step 2: Commit**

```bash
git add frontend/src/stores/agent.ts
git commit -m "feat(agent): add agent Pinia store"
```

---

## Task 4: 创建 AgentFloat 组件

**Files:**
- Create: `frontend/src/components/agent/AgentFloat.vue`

**Step 1: 创建悬浮球组件**

```vue
<!-- frontend/src/components/agent/AgentFloat.vue -->
<template>
  <!-- 悬浮球 -->
  <div
    v-show="!agentStore.isOpen"
    class="agent-float"
    @click="agentStore.toggleDrawer"
  >
    <el-badge :value="agentStore.unreadCount" :hidden="!agentStore.unreadCount" :max="99">
      <el-icon :size="28"><ChatDotRound /></el-icon>
    </el-badge>
  </div>

  <!-- 抽屉面板 -->
  <el-drawer
    v-model="agentStore.isOpen"
    direction="rtl"
    :size="400"
    :with-header="false"
    :append-to-body="true"
    :z-index="9999"
    class="agent-drawer"
  >
    <AgentDrawer />
  </el-drawer>
</template>

<script setup lang="ts">
import { ChatDotRound } from '@element-plus/icons-vue'
import { useAgentStore } from '@/stores/agent'
import AgentDrawer from './AgentDrawer.vue'

const agentStore = useAgentStore()
</script>

<style scoped>
.agent-float {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  transition: all 0.3s ease;
  z-index: 9998;
}

.agent-float:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

:deep(.agent-drawer) {
  .el-drawer__body {
    padding: 0;
  }
}
</style>
```

**Step 2: Commit**

```bash
git add frontend/src/components/agent/AgentFloat.vue
git commit -m "feat(agent): add AgentFloat component"
```

---

## Task 5: 创建 AgentDrawer 组件

**Files:**
- Create: `frontend/src/components/agent/AgentDrawer.vue`

**Step 1: 创建抽屉组件**

```vue
<!-- frontend/src/components/agent/AgentDrawer.vue -->
<template>
  <div class="agent-drawer-content">
    <!-- 头部 -->
    <div class="drawer-header">
      <div class="title">
        <el-icon><Cpu /></el-icon>
        <span>智能助手</span>
      </div>
      <div class="actions">
        <el-tooltip content="清空对话" placement="bottom">
          <el-button text @click="handleClear">
            <el-icon><Delete /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 对话区域 -->
    <div class="chat-container" ref="chatContainer">
      <div v-if="agentStore.messages.length === 0" class="empty-state">
        <el-icon :size="48" color="#c0c4cc"><ChatDotRound /></el-icon>
        <p>你好！我是智能助手，有什么可以帮你的？</p>
      </div>

      <div
        v-for="msg in agentStore.messages"
        :key="msg.id"
        :class="['message', msg.role]"
      >
        <AgentMessage :message="msg" />
      </div>

      <!-- 加载中 -->
      <div v-if="agentStore.loading" class="message assistant">
        <div class="typing-indicator">
          <span></span><span></span><span></span>
        </div>
      </div>
    </div>

    <!-- 确认弹窗 -->
    <AgentConfirm
      v-if="agentStore.pendingConfirm"
      :message="agentStore.pendingConfirm.message"
      :tool-name="agentStore.pendingConfirm.toolName"
      @confirm="agentStore.confirmAction"
      @cancel="agentStore.cancelConfirm"
    />

    <!-- 输入区域 -->
    <AgentInput
      :disabled="agentStore.loading || !!agentStore.pendingConfirm"
      @send="handleSend"
    />
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Cpu, Delete, ChatDotRound } from '@element-plus/icons-vue'
import { useAgentStore } from '@/stores/agent'
import AgentMessage from './AgentMessage.vue'
import AgentInput from './AgentInput.vue'
import AgentConfirm from './AgentConfirm.vue'

const agentStore = useAgentStore()
const chatContainer = ref<HTMLElement>()

const handleSend = (message: string) => {
  agentStore.sendMessage(message)
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空对话记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await agentStore.clearMessages()
  } catch {
    // 用户取消
  }
}

// 新消息自动滚动到底部
watch(
  () => agentStore.messages.length,
  () => {
    nextTick(() => {
      if (chatContainer.value) {
        chatContainer.value.scrollTop = chatContainer.value.scrollHeight
      }
    })
  }
)
</script>

<style scoped>
.agent-drawer-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.actions {
  display: flex;
  gap: 8px;
}

.actions .el-button {
  color: white;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-state p {
  margin-top: 16px;
}

.message {
  margin-bottom: 16px;
}

.message.user {
  display: flex;
  justify-content: flex-end;
}

.message.assistant {
  display: flex;
  justify-content: flex-start;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}
</style>
```

**Step 2: Commit**

```bash
git add frontend/src/components/agent/AgentDrawer.vue
git commit -m "feat(agent): add AgentDrawer component"
```

---

## Task 6: 创建 AgentMessage 组件

**Files:**
- Create: `frontend/src/components/agent/AgentMessage.vue`

**Step 1: 创建消息组件**

```vue
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
```

**Step 2: Commit**

```bash
git add frontend/src/components/agent/AgentMessage.vue
git commit -m "feat(agent): add AgentMessage component"
```

---

## Task 7: 创建 AgentInput 组件

**Files:**
- Create: `frontend/src/components/agent/AgentInput.vue`

**Step 1: 创建输入组件**

```vue
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

.input-container .el-textarea {
  flex: 1;
}

.input-container .el-button {
  align-self: flex-end;
}
</style>
```

**Step 2: Commit**

```bash
git add frontend/src/components/agent/AgentInput.vue
git commit -m "feat(agent): add AgentInput component"
```

---

## Task 8: 创建 AgentConfirm 组件

**Files:**
- Create: `frontend/src/components/agent/AgentConfirm.vue`

**Step 1: 创建确认组件**

```vue
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
```

**Step 2: Commit**

```bash
git add frontend/src/components/agent/AgentConfirm.vue
git commit -m "feat(agent): add AgentConfirm component"
```

---

## Task 9: 全局注册悬浮球

**Files:**
- Modify: `frontend/src/layouts/DefaultLayout.vue` (或 App.vue)

**Step 1: 在布局中添加悬浮球**

```vue
<!-- 在布局组件的 template 末尾添加 -->
<template>
  <!-- 现有布局内容 -->
  <div class="layout-content">
    <router-view />
  </div>

  <!-- 添加 Agent 悬浮球 -->
  <AgentFloat />
</template>

<script setup lang="ts">
import AgentFloat from '@/components/agent/AgentFloat.vue'
</script>
```

**Step 2: Commit**

```bash
git add frontend/src/layouts/
git commit -m "feat(agent): integrate AgentFloat into layout"
```

---

## Task 10: 编译验证

**Step 1: 安装依赖**

```bash
cd frontend && pnpm install
```

**Step 2: 编译前端**

```bash
cd frontend && pnpm run build
```

Expected: Build success

**Step 3: 修复编译错误（如有）**

---

## Completion Checklist

- [ ] API 类型定义已创建
- [ ] Agent API 已创建
- [ ] Pinia Store 已创建
- [ ] AgentFloat 组件已创建
- [ ] AgentDrawer 组件已创建
- [ ] AgentMessage 组件已创建
- [ ] AgentInput 组件已创建
- [ ] AgentConfirm 组件已创建
- [ ] 悬浮球已集成到布局
- [ ] 编译通过

---

## Next Phase

继续执行 Phase 6: LLM 提供商管理界面
