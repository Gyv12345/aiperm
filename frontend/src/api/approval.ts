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

export interface ApprovalTodoOverviewVO {
  viewer: {
    userId: number
    isAdmin: boolean
    focusPlatform: 'WEWORK' | 'DINGTALK' | 'FEISHU'
  }
  userGuide: {
    platformEnabled: boolean
    oauthBound: boolean
    enabledSceneCount: number
    nextStep: 'CONTACT_ADMIN_ENABLE_PLATFORM' | 'BIND_OAUTH' | 'CONTACT_ADMIN_CONFIG_SCENE' | 'OPEN_PLATFORM_TODO'
    nextStepReason: string
  }
  quickActions: {
    code: 'OPEN_PLATFORM_TODO' | 'BIND_OAUTH' | 'VIEW_MY_APPROVAL'
    enabled: boolean
    reason?: string
    url: string
  }[]
  adminDiagnostics?: {
    platformChecks: {
      platform: 'WEWORK' | 'DINGTALK' | 'FEISHU'
      enabled: boolean
      configReady: boolean
      missingFields: string[]
    }[]
    sceneChecks: {
      platform: 'WEWORK' | 'DINGTALK' | 'FEISHU'
      enabledSceneCount: number
      sampleSceneCode?: string
    }[]
    latestApprovalCallback?: {
      platform: string
      sceneCode: string
      platformInstanceId: string
      status: string
      resultTime?: string
    }
    latestMessagePush?: {
      platform: string
      templateCode: string
      status: string
      errorMsg?: string
      sendTime?: string
    }
  }
}

export const approvalApi = {
  submit: (data: ApprovalSubmitDTO) =>
    request.post<void>('/approval/submit', data),

  my: (params: ApprovalMyQuery) =>
    request.get<PageResult<ApprovalInstanceVO>>('/approval/my', { params }),

  todoOverview: (platform?: 'WEWORK' | 'DINGTALK' | 'FEISHU') =>
    request.get<ApprovalTodoOverviewVO>('/approval/todo/overview', {
      params: platform ? { platform } : undefined,
    }),
}
