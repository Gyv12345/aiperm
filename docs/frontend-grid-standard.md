# Frontend 筛选区栅格规范

## 目标
- 统一列表页筛选区的布局行为
- 桌面端整齐对齐，移动端自动换行
- 输入框/下拉框宽度由栅格控制，不再使用页面内硬编码宽度

## 标准写法
```vue
<el-form :model="queryForm" label-width="72px" class="grid-filter-form">
  <el-row :gutter="12">
    <el-col :xs="24" :sm="12" :md="8" :lg="6">
      <el-form-item label="名称">
        <el-input v-model="queryForm.name" class="filter-control" clearable />
      </el-form-item>
    </el-col>

    <el-col :xs="24" :sm="12" :md="8" :lg="6">
      <el-form-item label="状态">
        <DictSelect v-model="queryForm.status" class="filter-control" clearable />
      </el-form-item>
    </el-col>

    <el-col :xs="24" :sm="12" :md="24" :lg="6">
      <el-form-item class="filter-actions">
        <el-button type="primary">搜索</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-col>
  </el-row>
</el-form>
```

## 断点约定
- `xs=24`：手机单列
- `sm=12`：平板两列
- `md=8`：中屏三列
- `lg=6`：大屏四列

## 约束
- 输入类组件（`el-input`/`el-select`/`DictSelect`）统一加 `class="filter-control"`
- 按钮组统一放在 `class="filter-actions"` 的 `el-form-item`
- 避免在页面里直接写 `width: 140px/160px` 这类固定宽度

## 全局样式位置
- `frontend/src/style.css`
  - `.grid-filter-form`
  - `.filter-control`
  - `.filter-actions`
