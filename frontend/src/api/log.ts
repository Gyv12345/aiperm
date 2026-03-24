/**
 * 操作日志 API
 * 对应后端 SysOperLogController (/log/oper)
 */
import request from '@/utils/request'
import type {PageParams, PageResult} from '@/types'

// ==================== 类型定义 ====================

/** 操作日志实体 */
export interface OperLogVO {
  id: number
  title: string
  operType: number
  method: string
  requestMethod: string
  operUrl: string
  operIp: string
  operLocation: string
  operName: string
  deptName: string
  operParam: string
  jsonResult: string
  status: number
  errorMsg: string
  operTime: string
  costTime: number
}

/** 操作日志查询 DTO */
export interface OperLogDTO extends PageParams {
  id?: number
  title?: string
  operType?: number
  operName?: string
  status?: number
  startTime?: string
  endTime?: string
}

// ==================== API 函数 ====================

export const operLogApi = {
  /** 分页查询操作日志 */
  list: (params: OperLogDTO) =>
    request.get<PageResult<OperLogVO>>('/log/oper', { params }),

  /** 查询操作日志详情 */
  getById: (id: number) =>
    request.get<OperLogVO>(`/log/oper/${id}`),

  /** 删除操作日志 */
  delete: (id: number) =>
    request.delete<void>(`/log/oper/${id}`),

  /** 清空操作日志 */
  clean: () =>
    request.delete<void>('/log/oper/clean'),
}
