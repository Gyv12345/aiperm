/**
 * 公告通知管理 API
 * 对应后端 SysNoticeController (/enterprise/notice)
 */
import request from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

// ==================== 类型定义 ====================

/** 公告实体 */
export interface NoticeVO {
  id: number
  noticeTitle: string
  noticeContent: string
  noticeType: number
  status: number
  publishTime: string
  publishBy: string
  createTime: string
  createBy: string
  updateTime: string
  updateBy: string
}

/** 公告查询/创建/更新 DTO */
export interface NoticeDTO extends PageParams {
  id?: number
  noticeTitle?: string
  noticeContent?: string
  noticeType?: number
  status?: number
  type?: number
  limit?: number
}

// ==================== API 函数 ====================

export const noticeApi = {
  /** 分页查询公告 */
  list: (params: NoticeDTO) =>
    request.get<PageResult<NoticeVO>>('/enterprise/notice', { params }),

  /** 查询已发布公告 */
  published: (type?: number, limit?: number) =>
    request.get<NoticeVO[]>('/enterprise/notice/published', { params: { type, limit } }),

  /** 查询公告详情 */
  getById: (id: number) =>
    request.get<NoticeVO>(`/enterprise/notice/${id}`),

  /** 创建公告 */
  create: (data: NoticeDTO) =>
    request.post<number>('/enterprise/notice', data),

  /** 更新公告 */
  update: (id: number, data: NoticeDTO) =>
    request.put<void>(`/enterprise/notice/${id}`, data),

  /** 发布公告 */
  publish: (id: number) =>
    request.put<void>(`/enterprise/notice/${id}/publish`),

  /** 撤回公告 */
  withdraw: (id: number) =>
    request.put<void>(`/enterprise/notice/${id}/withdraw`),

  /** 删除公告 */
  delete: (id: number) =>
    request.delete<void>(`/enterprise/notice/${id}`),

  /** 批量删除公告 */
  deleteBatch: (ids: number[]) =>
    request.delete<void>('/enterprise/notice/batch', { data: ids }),
}
