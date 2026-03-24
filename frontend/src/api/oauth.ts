/**
 * OAuth 第三方登录模块 API
 * 对应后端 OAuthController (/oauth)
 */
import request from '@/utils/request'

// ==================== 类型定义 ====================

export type OAuthPlatform = 'WEWORK' | 'DINGTALK' | 'FEISHU'

/** 已绑定的第三方账号信息 */
export interface OauthBindingVO {
  platform: OAuthPlatform
  nickname: string
  avatar: string
  createTime: string
  lastLoginTime: string
}

// ==================== API 函数 ====================

export const oauthApi = {
  /** 获取已绑定的第三方账号列表 */
  bindings: () =>
    request.get<OauthBindingVO[]>('/oauth/bindings'),

  /** 解绑第三方账号 */
  unbind: (platform: OAuthPlatform) =>
    request.delete<void>(`/oauth/unbind/${platform}`),

  /** 获取授权跳转 URL（前端直接跳转，非 API 调用） */
  getLoginUrl: (platform: OAuthPlatform) =>
    `/api/oauth/login/${platform}`,

  getBindUrl: (platform: OAuthPlatform) =>
    `/api/oauth/bind/${platform}`,
}
