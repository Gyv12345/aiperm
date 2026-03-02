/**
 * 角色管理 API
 * 对应后端 SysRoleController (/system/role)
 */
import request from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

// ==================== 类型定义 ====================

/** 角色实体 */
export interface RoleVO {
  id: number
  roleName: string
  roleCode: string
  sort: number
  status: number
  dataScope: number
  remark: string
  createTime: string
  createBy: string
  updateTime: string
  updateBy: string
}

/** 角色查询/创建/更新 DTO */
export interface RoleDTO extends PageParams {
  id?: number
  roleName?: string
  roleCode?: string
  sort?: number
  status?: number
  dataScope?: number
  remark?: string
}

// ==================== API 函数 ====================

export const roleApi = {
  /** 分页查询角色列表 */
  list: (params: RoleDTO) =>
    request.get<PageResult<RoleVO>>('/system/role', { params }),

  /** 查询所有角色 */
  all: () =>
    request.get<RoleVO[]>('/system/role/all'),

  /** 根据 ID 查询角色 */
  getById: (id: number) =>
    request.get<RoleVO>(`/system/role/${id}`),

  /** 获取角色的菜单 ID 列表 */
  getRoleMenus: (id: number) =>
    request.get<number[]>(`/system/role/${id}/menus`),

  /** 创建角色 */
  create: (data: RoleDTO) =>
    request.post<void>('/system/role', data),

  /** 更新角色 */
  update: (id: number, data: RoleDTO) =>
    request.put<void>(`/system/role/${id}`, data),

  /** 删除角色 */
  delete: (id: number) =>
    request.delete<void>(`/system/role/${id}`),

  /** 批量删除角色 */
  deleteBatch: (ids: number[]) =>
    request.delete<void>('/system/role/batch', { data: ids }),

  /** 分配角色菜单 */
  assignMenus: (id: number, menuIds: number[]) =>
    request.post<void>(`/system/role/${id}/menus`, menuIds),
}
