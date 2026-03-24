/**
 * 菜单管理 API
 * 对应后端 SysMenuController (/system/menu)
 */
import request from '@/utils/request'
import type {PageParams} from '@/types'

// ==================== 类型定义 ====================

/** 菜单实体 */
export interface MenuVO {
  id: number
  parentId: number
  menuName: string
  menuCode: string
  path: string
  icon: string
  menuType: number | string  // 1-目录 2-菜单 3-按钮 (可能是数字或字符串)
  sort: number
  status: number
  visible: number
  component: string
  perms: string
  remark: string
  isExternal: number
  isCache: number
  children?: MenuVO[]
  createTime: string
  createBy: string
  updateTime: string
  updateBy: string
}

/** 菜单查询/创建/更新 DTO */
export interface MenuDTO extends PageParams {
  id?: number
  parentId?: number
  menuName?: string
  menuCode?: string
  path?: string
  icon?: string
  menuType?: number
  sort?: number
  status?: number
  visible?: number
  component?: string
  perms?: string
  remark?: string
  isExternal?: number
  isCache?: number
}

// ==================== API 函数 ====================

export const menuApi = {
  /** 查询所有菜单 */
  list: () =>
    request.get<MenuVO[]>('/system/menu'),

  /** 查询菜单树 */
  tree: () =>
    request.get<MenuVO[]>('/system/menu/tree'),

  /** 根据 ID 查询菜单 */
  getById: (id: number) =>
    request.get<MenuVO>(`/system/menu/${id}`),

  /** 查询子菜单列表 */
  getChildren: (parentId: number) =>
    request.get<MenuVO[]>(`/system/menu/children/${parentId}`),

  /** 创建菜单 */
  create: (data: MenuDTO) =>
    request.post<void>('/system/menu', data),

  /** 更新菜单 */
  update: (id: number, data: MenuDTO) =>
    request.put<void>(`/system/menu/${id}`, data),

  /** 删除菜单 */
  delete: (id: number) =>
    request.delete<void>(`/system/menu/${id}`),
}
