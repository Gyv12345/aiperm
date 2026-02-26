/**
 * 定时任务管理 API
 * 对应后端 SysJobController (/enterprise/job)
 */
import request from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

// ==================== 类型定义 ====================

/** 定时任务实体 */
export interface JobVO {
  id: number
  jobName: string
  jobGroup: string
  invokeTarget: string
  cronExpression: string
  misfirePolicy: number
  concurrent: number
  status: number
  remark: string
  nextTime: string
  createTime: string
  createBy: string
  updateTime: string
  updateBy: string
}

/** 定时任务查询/创建/更新 DTO */
export interface JobDTO extends PageParams {
  id?: number
  jobName?: string
  jobGroup?: string
  invokeTarget?: string
  cronExpression?: string
  misfirePolicy?: number
  concurrent?: number
  status?: number
  remark?: string
}

// ==================== API 函数 ====================

export const jobApi = {
  /** 分页查询定时任务 */
  list: (params: JobDTO) =>
    request.get<PageResult<JobVO>>('/enterprise/job', { params }),

  /** 查询定时任务详情 */
  getById: (id: number) =>
    request.get<JobVO>(`/enterprise/job/${id}`),

  /** 创建定时任务 */
  create: (data: JobDTO) =>
    request.post<number>('/enterprise/job', data),

  /** 更新定时任务 */
  update: (id: number, data: JobDTO) =>
    request.put<void>(`/enterprise/job/${id}`, data),

  /** 删除定时任务 */
  delete: (id: number) =>
    request.delete<void>(`/enterprise/job/${id}`),

  /** 暂停定时任务 */
  pause: (id: number) =>
    request.put<void>(`/enterprise/job/${id}/pause`),

  /** 恢复定时任务 */
  resume: (id: number) =>
    request.put<void>(`/enterprise/job/${id}/resume`),
}
