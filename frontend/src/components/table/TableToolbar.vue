<script setup lang="ts">
/**
 * 表格工具栏布局组件
 *
 * 用法：
 * <TableToolbar>
 *   <template #actions>
 *     <el-button type="primary" @click="handleCreate">新增</el-button>
 *   </template>
 *   <template #tools>
 *     <el-button :icon="Refresh" circle @click="fetchList" />
 *     <ColumnSetting v-model="columns" />
 *   </template>
 * </TableToolbar>
 */
</script>

<template>
  <div class="flex items-center justify-between mb-3">
    <!-- 左侧：操作按钮区（新增、导出等） -->
    <div class="flex items-center gap-2">
      <slot name="actions" />
    </div>
    <!-- 右侧：工具图标区（刷新、列设置等） -->
    <div class="flex items-center gap-1">
      <slot name="tools" />
    </div>
  </div>
</template>
