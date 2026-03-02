import request from '@/utils/request'
import type { PageResult } from '@/types'

export interface MessageLogVO {
  id: number
  templateCode?: string
  platform: string
  receiverId?: number
  platformUserId?: string
  title?: string
  content?: string
  status: string
  errorMsg?: string
  sendTime?: string
  createTime?: string
}

export interface MessageLogQuery {
  page?: number
  pageSize?: number
  templateCode?: string
  platform?: string
  status?: string
}

export const messageLogApi = {
  list: (params: MessageLogQuery) =>
    request.get<PageResult<MessageLogVO>>('/enterprise/message-log', { params }),
}
