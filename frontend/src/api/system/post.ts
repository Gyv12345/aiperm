/**
 * 岗位管理 API
 * 对应后端 SysPostController (/system/post)
 */
import request from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

// ==================== 类型定义 ====================

/** 岗位实体 */
export interface PostVO {
  id: number
  postCode: string
  postName: string
  sort: number
  status: number
  remark: string
  createTime: string
  createBy: string
  updateTime: string
  updateBy: string
}

/** 岗位查询/创建/更新 DTO */
export interface PostDTO extends PageParams {
  id?: number
  postCode?: string
  postName?: string
  sort?: number
  status?: number
  remark?: string
}

// ==================== API 函数 ====================

export const postApi = {
  /** 分页查询岗位列表 */
  list: (params: PostDTO) =>
    request.get<PageResult<PostVO>>('/system/post', { params }),

  /** 查询所有岗位 */
  all: () =>
    request.get<PostVO[]>('/system/post/all'),

  /** 根据 ID 查询岗位 */
  getById: (id: number) =>
    request.get<PostVO>(`/system/post/${id}`),

  /** 创建岗位 */
  create: (data: PostDTO) =>
    request.post<void>('/system/post', data),

  /** 更新岗位 */
  update: (id: number, data: PostDTO) =>
    request.put<void>(`/system/post/${id}`, data),

  /** 删除岗位 */
  delete: (id: number) =>
    request.delete<void>(`/system/post/${id}`),
}
