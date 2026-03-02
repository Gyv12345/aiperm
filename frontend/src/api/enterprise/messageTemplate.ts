import request from '@/utils/request'
import type { PageResult } from '@/types'

export interface MessageTemplateVO {
  id?: number
  templateCode: string
  templateName: string
  category?: string
  platform?: string
  title?: string
  content?: string
  createTime?: string
}

export interface MessageTemplateDTO extends Partial<MessageTemplateVO> {
  page?: number
  pageSize?: number
}

export const messageTemplateApi = {
  list: (params: MessageTemplateDTO) =>
    request.get<PageResult<MessageTemplateVO>>('/enterprise/message-template', { params }),

  getById: (id: number) =>
    request.get<MessageTemplateVO>(`/enterprise/message-template/${id}`),

  create: (data: MessageTemplateDTO) =>
    request.post<void>('/enterprise/message-template', data),

  update: (id: number, data: MessageTemplateDTO) =>
    request.put<void>(`/enterprise/message-template/${id}`, data),

  delete: (id: number) =>
    request.delete<void>(`/enterprise/message-template/${id}`),
}
