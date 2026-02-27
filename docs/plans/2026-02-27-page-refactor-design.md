# 页面改造设计方案

> 日期：2026-02-27
> 目标：将 6 个页面改造为与用户页面一致的现代化布局

## 改造目标

| 页面 | 路径 | 改造内容 |
|------|------|----------|
| 角色 | `system/role` | TableToolbar + ColumnSetting + SelectionBar + DictTag |
| 岗位 | `system/post` | TableToolbar + ColumnSetting + SelectionBar + DictTag |
| 系统配置 | `enterprise/config` | TableToolbar + ColumnSetting + SelectionBar |
| 定时任务 | `enterprise/job` | TableToolbar + ColumnSetting（无批量操作） |
| 消息管理 | `enterprise/message` | TableToolbar + ColumnSetting + SelectionBar |
| 通知管理 | `enterprise/notice` | TableToolbar + ColumnSetting + SelectionBar |

## 统一规范

- **搜索区域**：独立 `<el-card class="mb-4">` 包裹
- **表格区域**：使用 `TableToolbar` 组件（左侧操作按钮 + 右侧工具图标）
- **列设置**：所有页面添加 `ColumnSetting` 组件
- **批量操作**：添加多选列 + `SelectionBar` 组件（定时任务除外）
- **字典组件**：状态字段使用 `DictTag`/`DictSelect`（定时任务保持手写）

## 改造细节

### 角色页面 (`system/role/index.vue`)

1. 搜索区域：状态选择器改用 `DictSelect` 组件
2. 表格区域：
   - 移除 `<template #header>`，改用 `TableToolbar`
   - 添加多选列 `<el-table-column type="selection">`
   - 添加 `ColumnSetting` 组件
   - 状态列改用 `DictTag` 组件
   - 添加 `visibleColumns` 动态渲染普通列
3. 添加 `SelectionBar` + 批量删除功能
4. 表单弹窗：状态改用 `DictRadio` 组件

### 岗位页面 (`system/post/index.vue`)

1. 搜索区域：
   - 移除手写 `class="post-content"`
   - 状态选择器改用 `DictSelect` 组件
2. 表格区域：
   - 移除 `<template #header>`，改用 `TableToolbar`
   - 添加多选列
   - 添加 `ColumnSetting` 组件
   - 状态列改用 `DictTag` 组件
   - 添加 `visibleColumns` 动态渲染
3. 添加 `SelectionBar` + 批量删除功能
4. 表单弹窗：状态改用 `DictRadio` 组件

### 系统配置页面 (`enterprise/config/index.vue`)

1. 搜索区域：从表格卡片内移出，改为独立 `<el-card class="mb-4">`
2. 表格区域：
   - 移除手写工具栏 `<div class="mb-4">`，改用 `TableToolbar`
   - 添加多选列
   - 添加 `ColumnSetting` 组件
   - 添加 `visibleColumns` 动态渲染
3. 添加 `SelectionBar` + 批量删除功能

### 定时任务页面 (`enterprise/job/index.vue`)

1. 搜索区域：从表格卡片内移出，改为独立 `<el-card class="mb-4">`
2. 表格区域：
   - 移除手写工具栏，改用 `TableToolbar`
   - **不添加多选列**（任务特殊，无批量操作）
   - 添加 `ColumnSetting` 组件
   - 添加 `visibleColumns` 动态渲染
   - 状态列保持手写（暂停/运行含义不同）
3. **不添加 SelectionBar**

### 消息/通知页面

- 独立搜索卡片
- TableToolbar + ColumnSetting
- 多选 + SelectionBar + 批量删除

## 公共组件

| 组件 | 路径 | 用途 |
|------|------|------|
| TableToolbar | `components/table/TableToolbar.vue` | 表格工具栏布局 |
| ColumnSetting | `components/table/ColumnSetting.vue` | 列显示/隐藏控制 |
| SelectionBar | `components/table/SelectionBar.vue` | 浮动多选操作条 |
| DictTag | `components/dict/DictTag.vue` | 字典标签显示 |
| DictSelect | `components/dict/DictSelect.vue` | 字典下拉选择 |
| DictRadio | `components/dict/DictRadio.vue` | 字典单选组 |

## 代码结构模板

```vue
<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import { useDict } from '@/composables/useDict'
import type { PageResult, TableColumn } from '@/types'

// 字典
const dictData = useDict('sys_status')
const sys_status = dictData.sys_status!

// 表格列配置
const columns = ref<TableColumn[]>([
  { key: 'id', label: 'ID', visible: true, fixed: 'left' },
  // ...
])
const visibleColumns = computed(() => columns.value.filter(c => c.visible))

// 多选
const tableRef = ref()
const selectedRows = ref<XXX[]>([])
function handleSelectionChange(rows: XXX[]) { selectedRows.value = rows }

// 批量删除
async function handleBatchDelete() { /* ... */ }
</script>

<template>
  <div class="p-4">
    <!-- 搜索区域 -->
    <el-card class="mb-4">
      <el-form :inline="true" :model="queryForm">
        <!-- DictSelect -->
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card>
      <TableToolbar>
        <template #actions><el-button type="primary">新增</el-button></template>
        <template #tools><ColumnSetting v-model="columns" /></template>
      </TableToolbar>

      <el-table ref="tableRef" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" fixed="left" />
        <!-- 动态列 + 状态列 + 操作列 -->
      </el-table>
    </el-card>

    <!-- SelectionBar -->
    <SelectionBar :count="selectedRows.length" @clear="tableRef?.clearSelection()">
      <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
    </SelectionBar>
  </div>
</template>
```
