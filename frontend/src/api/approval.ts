import request from '@/utils/request'
import type {PageParams, PageResult} from '@/types'

export interface ApprovalHandlerVO {
  beanName: string
  displayName: string
}

export interface ApprovalSceneVO {
  id: number
  sceneCode: string
  sceneName: string
  businessType: string
  platform: string
  templateId: string
  enabled: number
  handlerBeanName: string
  autoSubmitEnabled: number
  allowDuplicatePending: number
  timeoutHours: number
  timeoutAction?: string
  notifyTemplateCode?: string
  remark?: string
  createTime?: string
}

export interface ApprovalSceneDTO extends PageParams {
  sceneCode?: string
  sceneName?: string
  businessType?: string
  platform?: string
  templateId?: string
  enabled?: number
  handlerBeanName?: string
  autoSubmitEnabled?: number
  allowDuplicatePending?: number
  timeoutHours?: number
  timeoutAction?: string
  notifyTemplateCode?: string
  remark?: string
}

export interface ApprovalInstanceVO {
  id: number
  sceneCode: string
  sceneName: string
  businessType: string
  businessId: number
  initiatorId: number
  initiatorName: string
  platform: string
  platformInstanceId: string
  status: string
  errorMessage?: string
  createTime?: string
  resultTime?: string
}

export interface ApprovalInstanceDTO extends PageParams {
  sceneCode?: string
  businessType?: string
  platform?: string
  status?: string
}

export interface ApprovalSubmitDTO {
  sceneCode: string
  businessType: string
  businessId: number
  payload?: Record<string, unknown>
}

export interface ApprovalTodoOverviewVO {
  viewer: {
    userId: number
    isAdmin: boolean
    focusPlatform: string
  }
  userGuide: {
    moduleEnabled: boolean
    platformEnabled: boolean
    platformConfigReady: boolean
    oauthBound: boolean
    enabledSceneCount: number
    nextStep: string
    nextStepReason: string
  }
  quickActions: Array<{
    code: string
    enabled: boolean
    reason?: string
    url?: string
  }>
  adminDiagnostics?: {
    platformChecks: Array<{
      platform: string
      enabled: boolean
      configReady: boolean
      missingFields: string[]
    }>
    sceneChecks: Array<{
      platform: string
      enabledSceneCount: number
      sampleSceneCode?: string
    }>
    latestApprovalCallback?: {
      platform: string
      sceneCode?: string
      platformInstanceId?: string
      status?: string
      resultTime?: string
    } | null
    latestMessagePush?: {
      platform: string
      templateCode?: string
      status?: string
      errorMsg?: string
      sendTime?: string
    } | null
  }
}

export const approvalSceneApi = {
  list: (params: ApprovalSceneDTO) =>
    request.get<PageResult<ApprovalSceneVO>>('/system/approval-scene', { params }),

  getById: (id: number) =>
    request.get<ApprovalSceneVO>(`/system/approval-scene/${id}`),

  handlers: () =>
    request.get<ApprovalHandlerVO[]>('/system/approval-scene/handlers'),

  create: (data: ApprovalSceneDTO) =>
    request.post<number>('/system/approval-scene', data),

  update: (id: number, data: ApprovalSceneDTO) =>
    request.put<void>(`/system/approval-scene/${id}`, data),

  delete: (id: number) =>
    request.delete<void>(`/system/approval-scene/${id}`),
}

export const approvalInstanceApi = {
  list: (params: ApprovalInstanceDTO) =>
    request.get<PageResult<ApprovalInstanceVO>>('/approval/instance', { params }),

  submit: (data: ApprovalSubmitDTO) =>
    request.post<number>('/approval/instance', data),
}

export const approvalApi = {
  todoOverview: (platform?: string) =>
    request.get<ApprovalTodoOverviewVO>('/approval/todo/overview', { params: { platform } }),
}
