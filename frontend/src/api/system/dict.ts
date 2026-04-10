/**
 * 字典管理 API
 * 对应后端 SysDictTypeController 和 SysDictDataController
 */
import request from '@/utils/request'
import type {ImportResult, PageParams, PageResult} from '@/types'

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

export interface DictTypeDTO extends PageParams {
  id?: number
  dictName?: string
  dictType?: string
  status?: number
  remark?: string
}

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

export const dictApi = {
  typeList: (params: DictTypeDTO) =>
    request.get<PageResult<DictTypeVO>>('/system/dict/type', { params }),

  typeAll: () =>
    request.get<DictTypeVO[]>('/system/dict/type/all'),

  typeExport: (params: DictTypeDTO) =>
    request.download('/system/dict/type/export', { params }, 'dict-types.xlsx'),

  typeGetById: (id: number) =>
    request.get<DictTypeVO>(`/system/dict/type/${id}`),

  typeCreate: (data: DictTypeDTO) =>
    request.post<number>('/system/dict/type', data),

  typeUpdate: (id: number, data: DictTypeDTO) =>
    request.put<void>(`/system/dict/type/${id}`, data),

  typeDelete: (id: number) =>
    request.delete<void>(`/system/dict/type/${id}`),

  dataList: (dictType: string) =>
    request.get<DictDataVO[]>('/system/dict/data/list', { params: { dictType } }),

  dataExport: (dictType?: string) =>
    request.download('/system/dict/data/export', { params: { dictType } }, 'dict-data.xlsx'),

  dataDownloadImportTemplate: () =>
    request.download('/system/dict/data/import-template', undefined, 'dict-data-import-template.xlsx'),

  dataImport: (file: File) =>
    request.upload<ImportResult>('/system/dict/data/import', file),

  dataCreate: (data: DictDataDTO) =>
    request.post<void>('/system/dict/data', data),

  dataUpdate: (id: number, data: DictDataDTO) =>
    request.put<void>(`/system/dict/data/${id}`, data),

  dataDelete: (id: number) =>
    request.delete<void>(`/system/dict/data/${id}`),
}
