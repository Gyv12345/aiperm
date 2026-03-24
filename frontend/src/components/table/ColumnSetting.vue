<script setup lang="ts">
import {Setting} from '@element-plus/icons-vue'
import type {TableColumn} from '@/types'

/**
 * 列显示设置组件
 *
 * 用法：
 * <ColumnSetting v-model="columns" />
 *
 * columns 格式：
 * const columns = ref<TableColumn[]>([
 *   { key: 'id', label: 'ID', visible: true, fixed: true },
 *   { key: 'name', label: '名称', visible: true },
 * ])
 * const visibleColumns = computed(() => columns.value.filter(c => c.visible))
 */

const model = defineModel<TableColumn[]>({ required: true })
</script>

<template>
  <el-dropdown
    trigger="click"
    placement="bottom-end"
    :hide-on-click="false"
  >
    <el-button
      :icon="Setting"
      circle
      title="列设置"
      @click.stop
    />

    <template #dropdown>
      <el-dropdown-menu class="column-setting-menu">
        <div class="column-setting-content">
          <div class="text-sm font-medium text-gray-700 mb-2 pb-2 border-b">
            列显示设置
          </div>
          <div class="space-y-1">
            <div
              v-for="col in model"
              :key="col.key"
              class="flex items-center"
            >
              <el-checkbox
                v-model="col.visible"
                :disabled="!!col.fixed"
                :label="col.label"
              />
            </div>
          </div>
        </div>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped>
.column-setting-menu {
  min-width: 190px;
  padding: 0;
}

.column-setting-content {
  padding: 10px 12px;
}
</style>
