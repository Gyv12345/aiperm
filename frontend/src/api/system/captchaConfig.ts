/**
 * 验证码配置管理 API
 * 对应后端 CaptchaConfigController (/system/captcha-config)
 */
import request from '@/utils/request'

export type CaptchaType = 'SMS' | 'EMAIL'

export interface CaptchaConfigVO {
  id: number
  type: CaptchaType
  enabled: number
  smsProvider?: string
  smsAccessKey?: string
  smsSignName?: string
  smsTemplateCode?: string
  emailHost?: string
  emailPort?: number
  emailUsername?: string
  emailFrom?: string
  emailFromName?: string
  codeLength: number
  expireMinutes: number
  dailyLimit: number
}

export interface CaptchaConfigDTO {
  enabled?: number
  smsProvider?: string
  smsAccessKey?: string
  smsSecretKey?: string
  smsSignName?: string
  smsTemplateCode?: string
  emailHost?: string
  emailPort?: number
  emailUsername?: string
  emailPassword?: string
  emailFrom?: string
  emailFromName?: string
  codeLength?: number
  expireMinutes?: number
  dailyLimit?: number
}

export const captchaConfigApi = {
  getConfig: (type: CaptchaType) =>
    request.get<CaptchaConfigVO>(`/system/captcha-config/${type}`),

  updateConfig: (type: CaptchaType, data: CaptchaConfigDTO) =>
    request.put<void>(`/system/captcha-config/${type}`, data),
}
