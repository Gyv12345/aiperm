/**
 * 用户管理 API
 * 对应后端 SysUserController (/system/user)
 */
import request from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

// ==================== 类型定义 ====================

/** 用户实体 */
export interface UserVO {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  gender: number
  avatar: string
  status: number
  deptId: number
  deptName: string
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
  email?: string
  phone?: string
  gender?: number
  avatar?: string
  status?: number
  deptId?: number
  postIds?: number[]
  roleIds?: number[]
  password?: string
  newPassword?: string
  remark?: string
  realName?: string
}

// ==================== API 函数 ====================

export const userApi = {
  /** 分页查询用户列表 */
  list: (params: UserDTO) =>
    request.get<PageResult<UserVO>>('/system/user', { params }),

  /** 根据 ID 查询用户 */
  getById: (id: number) =>
    request.get<UserVO>(`/system/user/${id}`),

  /** 创建用户 */
  create: (data: UserDTO) =>
    request.post<void>('/system/user', data),

  /** 更新用户 */
  update: (id: number, data: UserDTO) =>
    request.put<void>(`/system/user/${id}`, data),

  /** 删除用户 */
  delete: (id: number) =>
    request.delete<void>(`/system/user/${id}`),

  /** 批量删除用户 */
  deleteBatch: (ids: number[]) =>
    request.delete<void>('/system/user/batch', { data: ids }),

  /** 重置用户密码 */
  resetPassword: (id: number, newPassword: string) =>
    request.put<void>(`/system/user/${id}/reset-password`, { newPassword }),

  /** 修改用户状态 */
  changeStatus: (id: number, status: number) =>
    request.put<void>(`/system/user/${id}/status`, null, { params: { status } }),
}
