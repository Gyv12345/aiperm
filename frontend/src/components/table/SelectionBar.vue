<script setup lang="ts">
import { Close } from '@element-plus/icons-vue'

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
      class="fixed bottom-6 left-1/2 -translate-x-1/2 z-50"
    >
      <div class="flex items-center gap-3 px-5 py-3 bg-white rounded-xl shadow-2xl border border-gray-100">
        <!-- 已选数量 -->
        <span class="text-sm text-gray-600 whitespace-nowrap">
          已选 <span class="font-semibold text-primary">{{ props.count }}</span> 项
        </span>

        <el-divider direction="vertical" />

        <!-- 批量操作插槽 -->
        <div class="flex items-center gap-2">
          <slot />
        </div>

        <el-divider direction="vertical" />

        <!-- 清空按钮 -->
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
.selection-bar-enter-active,
.selection-bar-leave-active {
  transition: all 0.25s ease;
}

.selection-bar-enter-from,
.selection-bar-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(20px);
}
</style>
