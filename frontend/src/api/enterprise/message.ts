/**
 * 消息中心管理 API
 * 对应后端 SysMessageController (/enterprise/message)
 */
import request from '@/utils/request'
import type { PageResult, PageParams } from '@/types'

// ==================== 类型定义 ====================

/** 消息实体 */
export interface MessageVO {
  id: number
  title: string
  content: string
  messageType: number
  senderId: number
  senderName: string
  receiverId: number
  isRead: number
  readTime: string
  createTime: string
}

/** 消息查询/创建/更新 DTO */
export interface MessageDTO extends PageParams {
  id?: number
  title?: string
  content?: string
  messageType?: number
  receiverId?: number
  receiverIds?: number[]
  ids?: number[]
  isRead?: number
}

// ==================== API 函数 ====================

export const messageApi = {
  /** 分页查询消息 */
  list: (params: MessageDTO) =>
    request.get<PageResult<MessageVO>>('/enterprise/message', { params }),

  /** 查询消息详情 */
  getById: (id: number) =>
    request.get<MessageVO>(`/enterprise/message/${id}`),

  /** 获取未读消息数量 */
  unreadCount: () =>
    request.get<number>('/enterprise/message/unread-count'),

  /** 发送消息 */
  send: (data: MessageDTO) =>
    request.post<number>('/enterprise/message', data),

  /** 标记消息为已读 */
  markAsRead: (id: number) =>
    request.put<void>(`/enterprise/message/${id}/read`),

  /** 批量标记消息为已读 */
  markAsReadByIds: (ids: number[]) =>
    request.put<{ count: number }>('/enterprise/message/read-batch', { ids }),

  /** 标记所有消息为已读 */
  markAllAsRead: () =>
    request.put<{ count: number }>('/enterprise/message/read-all'),

  /** 删除消息 */
  delete: (id: number) =>
    request.delete<void>(`/enterprise/message/${id}`),
}
