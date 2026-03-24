/**
 * 字典管理 API
 * 对应后端 SysDictTypeController 和 SysDictDataController
 */
import request from '@/utils/request'
import type {PageParams, PageResult} from '@/types'

// ==================== 类型定义 ====================

/** 字典类型实体 */
export interface DictTypeVO {
  id: number
  dictName: string
  dictType: string
  status: number
  remark: string
  createTime: string
  createBy: string
  updateTime: string
  updateBy: string
}

/** 字典数据实体 */
export interface DictDataVO {
  id: number
  dictType: string
  dictLabel: string
  dictValue: string
  sort: number
  status: number
  remark: string
  cssClass: string
  listClass: string
  isDefault: number
  createTime: string
  createBy: string
  updateTime: string
  updateBy: string
}

/** 字典类型查询/创建/更新 DTO */
export interface DictTypeDTO extends PageParams {
  id?: number
  dictName?: string
  dictType?: string
  status?: number
  remark?: string
}

/** 字典数据查询/创建/更新 DTO */
export interface DictDataDTO extends PageParams {
  id?: number
  dictType?: string
  dictLabel?: string
  dictValue?: string
  sort?: number
  status?: number
  remark?: string
  cssClass?: string
  listClass?: string
  isDefault?: number
}

// ==================== API 函数 ====================

export const dictApi = {
  // ==================== 字典类型 ====================

  /** 分页查询字典类型 */
  typeList: (params: DictTypeDTO) =>
    request.get<PageResult<DictTypeVO>>('/system/dict/type', { params }),

  /** 查询所有启用的字典类型 */
  typeAll: () =>
    request.get<DictTypeVO[]>('/system/dict/type/all'),

  /** 根据 ID 查询字典类型 */
  typeGetById: (id: number) =>
    request.get<DictTypeVO>(`/system/dict/type/${id}`),

  /** 创建字典类型 */
  typeCreate: (data: DictTypeDTO) =>
    request.post<number>('/system/dict/type', data),

  /** 更新字典类型 */
  typeUpdate: (id: number, data: DictTypeDTO) =>
    request.put<void>(`/system/dict/type/${id}`, data),

  /** 删除字典类型 */
  typeDelete: (id: number) =>
    request.delete<void>(`/system/dict/type/${id}`),

  // ==================== 字典数据 ====================

  /** 根据字典类型查询字典数据 */
  dataList: (dictType: string) =>
    request.get<DictDataVO[]>('/system/dict/data/list', { params: { dictType } }),

  /** 创建字典数据 */
  dataCreate: (data: DictDataDTO) =>
    request.post<void>('/system/dict/data', data),

  /** 更新字典数据 */
  dataUpdate: (id: number, data: DictDataDTO) =>
    request.put<void>(`/system/dict/data/${id}`, data),

  /** 删除字典数据 */
  dataDelete: (id: number) =>
    request.delete<void>(`/system/dict/data/${id}`),
}
