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
