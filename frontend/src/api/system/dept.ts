/**
 * 部门管理 API
 * 对应后端 SysDeptController (/system/dept)
 */
import request from '@/utils/request'
import type { PageParams } from '@/types'

// ==================== 类型定义 ====================

/** 部门实体 */
export interface DeptVO {
  id: number
  parentId: number
  deptName: string
  deptCode: string
  leader: string
  phone: string
  email: string
  sort: number
  status: number
  remark: string
  children?: DeptVO[]
  createTime: string
  createBy: string
  updateTime: string
  updateBy: string
}

/** 部门查询/创建/更新 DTO */
export interface DeptDTO extends PageParams {
  id?: number
  parentId?: number
  deptName?: string
  deptCode?: string
  leader?: string
  phone?: string
  email?: string
  sort?: number
  status?: number
  remark?: string
}

// ==================== API 函数 ====================

export const deptApi = {
  /** 查询部门树 */
  tree: () =>
    request.get<DeptVO[]>('/system/dept/tree'),

  /** 查询所有部门 */
  list: () =>
    request.get<DeptVO[]>('/system/dept'),

  /** 根据 ID 查询部门 */
  getById: (id: number) =>
    request.get<DeptVO>(`/system/dept/${id}`),

  /** 查询子部门列表 */
  getChildren: (parentId: number) =>
    request.get<DeptVO[]>(`/system/dept/children/${parentId}`),

  /** 创建部门 */
  create: (data: DeptDTO) =>
    request.post<void>('/system/dept', data),

  /** 更新部门 */
  update: (id: number, data: DeptDTO) =>
    request.put<void>(`/system/dept/${id}`, data),

  /** 删除部门 */
  delete: (id: number) =>
    request.delete<void>(`/system/dept/${id}`),
}
