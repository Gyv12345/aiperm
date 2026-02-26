# 字典样式与通用组件设计方案

日期：2026-02-26

## 背景

字典管理系统需要支持：
1. 字典项配置样式（Tag 类型或自定义颜色）
2. 提供 `DictTag`、`DictSelect` 两个通用组件，方便业务页面使用

---

## 一、后端变更

### 数据库迁移

```sql
ALTER TABLE sys_dict_data
ADD COLUMN list_class VARCHAR(50) DEFAULT '' COMMENT '样式属性（tag类型或十六进制颜色）';
```

### `list_class` 取值规则

| 类型 | 示例值 |
|------|--------|
| Element Plus Tag 类型 | `default` / `primary` / `success` / `warning` / `danger` / `info` |
| 自定义十六进制颜色 | `#ff5500` |
| 无样式 | `''`（空字符串） |

### 涉及文件

- `SysDictData.java` - 新增 `String listClass`
- `DictDataDTO.java` - 新增 `listClass` 字段（可选）
- `DictDataVO.java` - 新增 `listClass` 字段
- `DictDataRepository.java` - `insert` / `update` SQL 同步新增
- `Vx.x.x__add_dict_list_class.sql` - Flyway 迁移脚本

---

## 二、字典管理页面更新

在编辑字典数据弹窗中新增"样式"表单项：

- 上方展示 6 个预设 Tag 按钮：`default` / `primary` / `success` / `warning` / `danger` / `info`
- 下方提供 `el-color-picker` 支持自定义颜色
- 两者互斥：选预设则清空颜色值，选颜色则清空预设
- 字典数据列表的"字典标签"列使用 `DictTag` 组件预览实际效果

---

## 三、通用组件

### 3.1 `DictTag` 组件

**路径**：`frontend/src/components/dict/DictTag.vue`

**使用方式（模仿若依）**：

```vue
<!-- 使用 useDict 组合式函数 -->
<script setup>
const { sys_status } = useDict('sys_status')
</script>

<template>
  <DictTag :options="sys_status" :value="row.status" />
</template>
```

**渲染逻辑**：
- 找到 `options` 中 `value === props.value` 的项
- 若 `listClass` 为预设类型（success/danger 等）→ `<el-tag :type="listClass">`
- 若 `listClass` 为 `#xxxxxx` 颜色 → `<el-tag :style="{ backgroundColor, borderColor, color: '#fff' }">`
- 找不到匹配项 → 显示原始 value 文本（无样式）

### 3.2 `DictSelect` 组件

**路径**：`frontend/src/components/dict/DictSelect.vue`

**使用方式**：

```vue
<DictSelect v-model="form.status" dict-type="sys_status" />
```

**行为**：
- 根据 `dictType` 调用 API 加载所有启用的字典项
- 每个选项左侧带彩色小圆点预览 `listClass` 样式
- 支持 `v-model` 双向绑定

### 3.3 `useDict` 组合式函数

**路径**：`frontend/src/composables/useDict.ts`

```typescript
// 支持传入多个字典类型
const { sys_status, sys_user_sex } = useDict('sys_status', 'sys_user_sex')
```

- 每次调用直接请求后端接口（后端 Redis 缓存）
- 返回 `Ref<DictDataVO[]>` 类型

---

## 四、技能更新

完成后更新 `aiperm-dev` 技能，加入字典组件使用示例。
