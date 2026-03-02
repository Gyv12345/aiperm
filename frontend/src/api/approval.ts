import request from '@/utils/request'
import type { PageResult } from '@/types'

export interface ApprovalSubmitDTO {
  sceneCode: string
  businessType: string
  businessId: number
  formData?: Record<string, any>
}

export interface ApprovalInstanceVO {
  id: number
  sceneCode: string
  businessType: string
  businessId: number
  initiatorId: number
  platform: string
  platformInstanceId: string
  status: string
  formData?: string
  resultTime?: string
  createTime?: string
}

export interface ApprovalMyQuery {
  sceneCode?: string
  status?: string
  page?: number
  pageSize?: number
}

export const approvalApi = {
  submit: (data: ApprovalSubmitDTO) =>
    request.post<void>('/approval/submit', data),

  my: (params: ApprovalMyQuery) =>
    request.get<PageResult<ApprovalInstanceVO>>('/approval/my', { params }),
}
