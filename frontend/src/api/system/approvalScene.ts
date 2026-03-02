import request from '@/utils/request'
import type { PageResult } from '@/types'

export interface ApprovalSceneVO {
  id?: number
  sceneCode: string
  sceneName: string
  platform: string
  templateId?: string
  enabled: number
  handlerClass?: string
  timeoutHours?: number
  timeoutAction?: string
  createTime?: string
}

export interface ApprovalSceneDTO extends Partial<ApprovalSceneVO> {
  page?: number
  pageSize?: number
}

export const approvalSceneApi = {
  list: (params: ApprovalSceneDTO) =>
    request.get<PageResult<ApprovalSceneVO>>('/system/approval-scene', { params }),

  getById: (id: number) =>
    request.get<ApprovalSceneVO>(`/system/approval-scene/${id}`),

  create: (data: ApprovalSceneDTO) =>
    request.post<void>('/system/approval-scene', data),

  update: (id: number, data: ApprovalSceneDTO) =>
    request.put<void>(`/system/approval-scene/${id}`, data),

  delete: (id: number) =>
    request.delete<void>(`/system/approval-scene/${id}`),
}
