/**
 * 验证码模块 API
 * 对应后端 CaptchaController (/captcha)
 */
import request from '@/utils/request'

// ==================== 类型定义 ====================

export type CaptchaType = 'SMS' | 'EMAIL'
export type CaptchaScene = 'LOGIN' | 'BIND' | 'RESET'

/** 发送验证码请求 */
export interface SendCaptchaDTO {
  target: string       // 手机号或邮箱
  type: CaptchaType
  scene: CaptchaScene
}

// ==================== API 函数 ====================

export const captchaApi = {
  /** 发送验证码（短信或邮件） */
  send: (data: SendCaptchaDTO) =>
    request.post<void>('/captcha/send', data),
}
