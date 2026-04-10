/**
 * 用户管理 API
 * 对应后端 SysUserController (/system/user)
 */
import request from '@/utils/request'
import type {ImportResult, PageParams, PageResult} from '@/types'

/** 用户实体 */
export interface UserVO {
  id: number
  username: string
  nickname: string
  realName?: string
  email: string
  phone: string
  gender: number
  avatar: string
  status: number
  deptId: number
  deptName: string
  postId?: number
  postIds: number[]
  postNames: string
  roleIds: number[]
  roleNames: string
  createTime: string
  createBy: string
  updateTime: string
  updateBy: string
}

/** 用户查询/创建/更新 DTO */
export interface UserDTO extends PageParams {
  id?: number
  username?: string
  nickname?: string
  realName?: string
  email?: string
  phone?: string
  gender?: number
  avatar?: string
  status?: number
  deptId?: number
  postId?: number
  postIds?: number[]
  roleIds?: number[]
  password?: string
  newPassword?: string
  remark?: string
}

export const userApi = {
  list: (params: UserDTO) =>
    request.get<PageResult<UserVO>>('/system/user', { params }),

  export: (params: UserDTO) =>
    request.download('/system/user/export', { params }, 'users.xlsx'),

  downloadImportTemplate: () =>
    request.download('/system/user/import-template', undefined, 'user-import-template.xlsx'),

  importUsers: (file: File) =>
    request.upload<ImportResult>('/system/user/import', file),

  getById: (id: number) =>
    request.get<UserVO>(`/system/user/${id}`),

  create: (data: UserDTO) =>
    request.post<void>('/system/user', data),

  update: (id: number, data: UserDTO) =>
    request.put<void>(`/system/user/${id}`, data),

  delete: (id: number) =>
    request.delete<void>(`/system/user/${id}`),

  deleteBatch: (ids: number[]) =>
    request.delete<void>('/system/user/batch', { data: ids }),

  resetPassword: (id: number, newPassword: string) =>
    request.put<void>(`/system/user/${id}/reset-password`, { newPassword }),

  changeStatus: (id: number, status: number) =>
    request.put<void>(`/system/user/${id}/status`, null, { params: { status } }),
}
