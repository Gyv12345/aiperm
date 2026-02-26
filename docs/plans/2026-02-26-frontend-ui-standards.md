# 前端 UI 规范组件实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 创建 4 个通用 UI 组件（DictRadio、TableToolbar、ColumnSetting、SelectionBar），并全局注册，统一前端列表页面的交互规范。

**Architecture:** 纯 Vue 3 组合式 API 组件，无外部依赖，基于 Element Plus。字典组件放 `components/dict/`，表格工具组件放 `components/table/`。`TableColumn` 类型定义在 `types/index.ts`。所有组件在 `main.ts` 全局注册，无需页面手动导入。

**Tech Stack:** Vue 3 + TypeScript + Element Plus + UnoCSS

---

## Task 1: 添加 TableColumn 类型定义

**Files:**
- Modify: `frontend/src/types/index.ts`

**Step 1: 读取现有类型文件**

```bash
cat frontend/src/types/index.ts
```

**Step 2: 追加 TableColumn 接口**

在文件末尾追加：

```typescript
// 表格列配置（用于 ColumnSetting 组件）
export interface TableColumn {
  key: string       // 对应 el-table-column 的 prop
  label: string     // 列标题
  visible: boolean  // 是否显示
  fixed?: boolean   // 固定列（固定列不允许隐藏）
}
```

**Step 3: 验证编译通过**

```bash
cd frontend && pnpm run build 2>&1 | tail -20
```

期望：无 TS 错误（可以有 warning，但不能有 error）

**Step 4: Commit**

```bash
git add frontend/src/types/index.ts
git commit -m "feat(types): add TableColumn interface for column settings"
```

---

## Task 2: 创建 DictRadio 组件

**Files:**
- Create: `frontend/src/components/dict/DictRadio.vue`

**Step 1: 创建文件**

完整内容：

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { dictApi, type DictDataVO } from '@/api/system/dict'

/**
 * 字典单选组件（用于表单，选项 ≤ 4 项时使用）
 *
 * 用法：
 * <DictRadio v-model="form.status" dict-type="sys_status" />
 */
const props = defineProps<{
  /** 字典类型标识 */
  dictType: string
  /** 是否禁用 */
  disabled?: boolean
}>()

const model = defineModel<string | number | null>()
const options = ref<DictDataVO[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    options.value = await dictApi.dataList(props.dictType)
  }
  finally {
    loading.value = false
  }
})
</script>

<template>
  <el-radio-group
    v-model="model"
    :disabled="disabled"
  >
    <el-radio
      v-for="item in options"
      :key="item.dictValue"
      :value="item.dictValue"
    >
      {{ item.dictLabel }}
    </el-radio>
  </el-radio-group>
</template>
```

**Step 2: 验证编译**

```bash
cd frontend && pnpm run build 2>&1 | grep -E "error|Error" | head -20
```

期望：无错误输出

**Step 3: Commit**

```bash
git add frontend/src/components/dict/DictRadio.vue
git commit -m "feat(components): add DictRadio component for form dict fields"
```

---

## Task 3: 创建 TableToolbar 组件

**Files:**
- Create: `frontend/src/components/table/TableToolbar.vue`

> 注意：需要先创建 `frontend/src/components/table/` 目录

**Step 1: 创建文件**

完整内容：

```vue
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
```

**Step 2: 验证编译**

```bash
cd frontend && pnpm run build 2>&1 | grep -E "error|Error" | head -20
```

期望：无错误输出

**Step 3: Commit**

```bash
git add frontend/src/components/table/TableToolbar.vue
git commit -m "feat(components): add TableToolbar layout component"
```

---

## Task 4: 创建 ColumnSetting 组件

**Files:**
- Create: `frontend/src/components/table/ColumnSetting.vue`

**Step 1: 创建文件**

完整内容：

```vue
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
            :disabled="col.fixed === true"
            :label="col.label"
          />
        </div>
      </div>
    </div>
  </el-popover>
</template>
```

**Step 2: 验证编译**

```bash
cd frontend && pnpm run build 2>&1 | grep -E "error|Error" | head -20
```

期望：无错误输出

**Step 3: Commit**

```bash
git add frontend/src/components/table/ColumnSetting.vue
git commit -m "feat(components): add ColumnSetting popover for table column visibility"
```

---

## Task 5: 创建 SelectionBar 组件

**Files:**
- Create: `frontend/src/components/table/SelectionBar.vue`

**Step 1: 创建文件**

完整内容：

```vue
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
```

**Step 2: 验证编译**

```bash
cd frontend && pnpm run build 2>&1 | grep -E "error|Error" | head -20
```

期望：无错误输出

**Step 3: Commit**

```bash
git add frontend/src/components/table/SelectionBar.vue
git commit -m "feat(components): add SelectionBar floating multi-select action bar"
```

---

## Task 6: 全局注册所有新组件

**Files:**
- Modify: `frontend/src/main.ts`

**Step 1: 读取当前 main.ts**

```bash
cat frontend/src/main.ts
```

**Step 2: 添加导入和注册**

在现有字典组件注册后追加（`app.component('DictSelect', DictSelect)` 这行之后）：

```typescript
import DictRadio from './components/dict/DictRadio.vue'
import TableToolbar from './components/table/TableToolbar.vue'
import ColumnSetting from './components/table/ColumnSetting.vue'
import SelectionBar from './components/table/SelectionBar.vue'
```

注册语句追加：

```typescript
app.component('DictRadio', DictRadio)
app.component('TableToolbar', TableToolbar)
app.component('ColumnSetting', ColumnSetting)
app.component('SelectionBar', SelectionBar)
```

**Step 3: 验证编译**

```bash
cd frontend && pnpm run build 2>&1 | tail -30
```

期望：build 成功，无 TS 错误

**Step 4: Commit**

```bash
git add frontend/src/main.ts
git commit -m "feat: globally register DictRadio, TableToolbar, ColumnSetting, SelectionBar"
```

---

## Task 7: 手动验证（浏览器测试）

**验证清单（在浏览器中检查）：**

1. **DictRadio** — 打开用户管理，在新增/编辑表单中，将状态字段改为 `<DictRadio>` 并确认选项正常渲染
2. **TableToolbar** — 页面右上角工具区布局正确（左操作/右工具）
3. **ColumnSetting** — 点击设置图标，弹出列复选框，勾选/取消后列正确显示/隐藏
4. **SelectionBar** — 勾选表格行，底部浮动条出现；取消勾选，浮动条动画消失

**如发现问题：** 定位到对应组件文件修改，重新 build 验证后 commit。

---

## 组件文件清单

```
frontend/src/
├── types/index.ts                    ← 追加 TableColumn 接口
├── components/
│   ├── dict/
│   │   ├── DictTag.vue               ✅ 已有
│   │   ├── DictSelect.vue            ✅ 已有
│   │   └── DictRadio.vue             🆕 Task 2
│   └── table/
│       ├── TableToolbar.vue          🆕 Task 3
│       ├── ColumnSetting.vue         🆕 Task 4
│       └── SelectionBar.vue          🆕 Task 5
└── main.ts                           ← Task 6 追加全局注册
```
