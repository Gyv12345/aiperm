<script setup lang="ts">
import {Close} from '@element-plus/icons-vue'

/**
 * 浮动多选操作条
 *
 * 勾选表格行后从底部滑入，取消选择后动画收起。
 *
 * 用法：
 * <SelectionBar :count="selectedRows.length" @clear="tableRef?.clearSelection()">
 *   <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
 * </SelectionBar>
 */
const props = defineProps<{
  /** 已选行数，为 0 时自动隐藏 */
  count: number
}>()

const emit = defineEmits<{
  /** 点击「清空选择」按钮时触发 */
  clear: []
}>()
</script>

<template>
  <Transition name="selection-bar">
    <div
      v-if="props.count > 0"
      class="selection-bar-wrap"
    >
      <div
        class="selection-bar"
      >
        <span class="selection-bar__count">
          已选 <span class="selection-bar__count-number">{{ props.count }}</span> 项
        </span>

        <el-divider direction="vertical" />

        <div class="selection-bar__actions">
          <slot />
        </div>

        <el-divider direction="vertical" />

        <el-button
          size="small"
          :icon="Close"
          text
          @click="emit('clear')"
        >
          清空
        </el-button>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.selection-bar-wrap {
  position: fixed;
  left: 50%;
  bottom: 24px;
  z-index: 50;
  transform: translateX(-50%);
}

.selection-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  border-radius: 999px;
  background: var(--color-overlay-surface);
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(18px);
}

.selection-bar__count {
  white-space: nowrap;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.selection-bar__count-number {
  color: var(--color-primary);
  font-weight: 700;
}

.selection-bar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selection-bar-enter-active,
.selection-bar-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.selection-bar-enter-from,
.selection-bar-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(20px);
}

@media (max-width: 768px) {
  .selection-bar-wrap {
    left: 16px;
    right: 16px;
    bottom: 16px;
    transform: none;
  }

  .selection-bar {
    width: 100%;
    justify-content: center;
    border-radius: 24px;
  }
}
</style>
