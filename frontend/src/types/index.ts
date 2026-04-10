/**
 * 通用类型定义
 * 与后端 common.domain 包中的类型对应
 */

// 统一响应结构 R<T>
export interface R<T = any> {
  code: number
  message: string
  data: T
}

// 分页结果
export interface PageResult<T> {
  total: number
  list: T[]
  pageNum: number
  pageSize: number
  pages: number
}

// 分页请求参数
export interface PageParams {
  page?: number
  pageSize?: number
}

export interface ImportError {
  rowNumber: number
  message: string
}

export interface ImportResult {
  successCount: number
  failureCount: number
  errors: ImportError[]
}

// 表格列配置（用于 ColumnSetting 组件）
export interface TableColumn {
  key: string       // 对应 el-table-column 的 prop
  label: string     // 列标题
  visible: boolean  // 是否显示
  fixed?: boolean | 'left' | 'right'   // 固定列：true/'left'为左固定，'right'为右固定（固定列不允许隐藏）
}
