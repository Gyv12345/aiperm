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
  <div class="table-toolbar">
    <div class="table-toolbar__actions">
      <slot name="actions" />
    </div>
    <div class="table-toolbar__tools">
      <slot name="tools" />
    </div>
  </div>
</template>

<style scoped>
.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 20px;
  background: var(--color-surface-container-low);
}

.table-toolbar__actions,
.table-toolbar__tools {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.table-toolbar__tools {
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .table-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .table-toolbar__tools {
    justify-content: flex-start;
  }
}
</style>
