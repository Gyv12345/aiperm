/**
 * 系统配置管理 API
 * 对应后端 SysConfigController (/enterprise/config)
 */
import request from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

// ==================== 类型定义 ====================

/** 系统配置实体 */
export interface ConfigVO {
  id: number
  configName: string
  configKey: string
  configValue: string
  configType: number
  remark: string
  createTime: string
  createBy: string
  updateTime: string
  updateBy: string
}

/** 系统配置查询/创建/更新 DTO */
export interface ConfigDTO extends PageParams {
  id?: number
  configName?: string
  configKey?: string
  configValue?: string
  configType?: number
  remark?: string
}

// ==================== API 函数 ====================

export const configApi = {
  /** 分页查询系统配置 */
  list: (params: ConfigDTO) =>
    request.get<PageResult<ConfigVO>>('/enterprise/config', { params }),

  /** 查询系统配置详情 */
  getById: (id: number) =>
    request.get<ConfigVO>(`/enterprise/config/${id}`),

  /** 根据配置键查询 */
  getByKey: (configKey: string) =>
    request.get<ConfigVO>(`/enterprise/config/key/${configKey}`),

  /** 创建系统配置 */
  create: (data: ConfigDTO) =>
    request.post<number>('/enterprise/config', data),

  /** 更新系统配置 */
  update: (id: number, data: ConfigDTO) =>
    request.put<void>(`/enterprise/config/${id}`, data),

  /** 删除系统配置 */
  delete: (id: number) =>
    request.delete<void>(`/enterprise/config/${id}`),
}
