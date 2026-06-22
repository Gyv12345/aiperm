/**
 * 全局类型定义
 * 与后端 common/domain 下的 R、PageResult 对齐。
 */

/** 后端统一响应结构 R<T>（拦截器会自动解包，直接返回 data） */
interface API<T = any> {
  code: number;
  msg?: string;
  message?: string;
  data: T;
}

/** 后端分页结构 PageResult<T>（注意字段是 list / pageNum，非 records / current） */
interface PageResult<T = any> {
  total: number;
  list: T[];
  pageNum: number;
  pageSize: number;
  pages: number;
}

/** 分页请求参数 */
interface PageParams {
  pageNum?: number;
  pageSize?: number;
}

declare namespace API {
  // 预留给 umi 生成类型的命名空间扩展
}
