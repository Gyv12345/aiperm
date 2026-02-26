<script setup lang="ts">
import { Setting } from '@element-plus/icons-vue'
import type { TableColumn } from '@/types'

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
  <el-popover
    placement="bottom-end"
    :width="180"
    trigger="click"
  >
    <template #reference>
      <el-tooltip content="列设置">
        <el-button
          :icon="Setting"
          circle
        />
      </el-tooltip>
    </template>

    <div>
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
  </el-popover>
</template>
