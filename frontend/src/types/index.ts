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
