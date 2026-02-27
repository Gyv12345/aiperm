/**
 * OAuth 配置管理 API
 * 对应后端 OauthConfigController (/system/oauth-config)
 */
import request from '@/utils/request'

export type OAuthPlatform = 'WEWORK' | 'DINGTALK' | 'FEISHU'

export interface OauthConfigVO {
  id: number
  platform: OAuthPlatform
  enabled: number
  corpId?: string
  agentId?: string
  appKey?: string
  callbackUrl?: string
  remark?: string
}

export interface OauthConfigDTO {
  enabled?: number
  corpId?: string
  agentId?: string
  appKey?: string
  appSecret?: string
  callbackUrl?: string
  remark?: string
}

export const oauthConfigApi = {
  getConfig: (platform: OAuthPlatform) =>
    request.get<OauthConfigVO>(`/system/oauth-config/${platform}`),

  updateConfig: (platform: OAuthPlatform, data: OauthConfigDTO) =>
    request.put<void>(`/system/oauth-config/${platform}`, data),
}
