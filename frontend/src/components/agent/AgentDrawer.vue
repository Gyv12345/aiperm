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
        <el-tooltip
          content="清空对话"
          placement="bottom"
        >
          <el-button
            text
            @click="handleClear"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 对话区域 -->
    <div
      ref="chatContainer"
      class="chat-container"
    >
      <div
        v-if="agentStore.messages.length === 0"
        class="empty-state"
      >
        <el-icon
          :size="48"
          color="#c0c4cc"
        >
          <ChatDotRound />
        </el-icon>
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
      <div
        v-if="agentStore.loading"
        class="message assistant"
      >
        <div class="typing-indicator">
          <span /><span /><span />
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
