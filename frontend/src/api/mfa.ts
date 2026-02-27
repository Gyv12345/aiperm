/**
 * 2FA 模块 API
 * 对应后端 MfaController (/mfa)
 */
import request from '@/utils/request'

// ==================== 类型定义 ====================

/** 2FA 状态 */
export interface MfaStatusVO {
  bound: boolean       // 是否已绑定
  required: boolean    // 是否强制要求（超管必须绑定）
  verified: boolean    // Redis 中是否已验证
}

/** 2FA 绑定二维码 */
export interface MfaQrcodeVO {
  totpUri: string      // TOTP URI（用于生成二维码）
  secretKey: string    // 密钥（用于手动输入）
}

/** 2FA 验证请求 */
export interface MfaVerifyDTO {
  code: string         // 6位 TOTP 验证码
}

// ==================== API 函数 ====================

export const mfaApi = {
  /** 获取2FA绑定状态 */
  status: () =>
    request.get<MfaStatusVO>('/mfa/status'),

  /** 获取绑定二维码 */
  qrcode: () =>
    request.get<MfaQrcodeVO>('/mfa/bind/qrcode'),

  /** 确认绑定（验证码验证通过后持久化） */
  confirmBind: (data: MfaVerifyDTO) =>
    request.post<void>('/mfa/bind/confirm', data),

  /** 解绑2FA */
  unbind: (data: MfaVerifyDTO) =>
    request.post<void>('/mfa/unbind', data),

  /** 验证2FA（敏感操作前调用） */
  verify: (data: MfaVerifyDTO) =>
    request.post<void>('/mfa/verify', data),
}
