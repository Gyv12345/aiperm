/**
 * 认证模块 API
 * 对应后端 AuthController (/auth)
 */
import request from '@/utils/request'
import type {MenuVO} from './system/menu'

export type { MenuVO }

// ==================== 类型定义 ====================

/** 登录请求（传统方式） */
export interface LoginRequest {
  username: string
  password: string
  captchaKey: string
  captcha: string
}

/** 统一登录请求 */
export interface UnifiedLoginDTO {
  loginType: 'PASSWORD' | 'SMS' | 'EMAIL' | 'OAUTH'
  identifier: string      // 用户名/手机号/邮箱
  credential: string       // 密码/验证码/OAuth code
  imageCaptcha?: string    // 图形验证码（密码登录时需要）
  imageCaptchaKey?: string // 图形验证码Key
}

/** 登录响应 */
export interface LoginVO {
  token: string
  userInfo: {
    id: number
    username: string
    nickname: string
    avatar: string
    email?: string
    phone?: string
  }
}

/** 用户信息（含角色权限） */
export interface UserInfoVO {
  id: number
  username: string
  nickname: string
  avatar: string
  roles: string[]
  permissions: string[]
}

/** 验证码 */
export interface CaptchaVO {
  captchaKey: string
  captchaImage: string
}

/** 登录配置 */
export interface LoginConfigVO {
  passwordEnabled: boolean
  smsEnabled: boolean
  emailEnabled: boolean
  oauthConfigs: OAuthConfig[]
}

interface OAuthConfig {
  platform: string
  displayName: string
  icon: string
  enabled: boolean
}

// ==================== API 函数 ====================

export const authApi = {
  /** 获取验证码 */
  captcha: () =>
    request.get<CaptchaVO>('/auth/captcha'),

  /** 登录（传统方式） */
  login: (data: LoginRequest) =>
    request.post<LoginVO>('/auth/login', data),

  /** 统一登录（支持多种登录方式） */
  unifiedLogin: (data: UnifiedLoginDTO) =>
    request.post<LoginVO>('/auth/unified-login', data),

  /** 获取登录配置 */
  loginConfig: () =>
    request.get<LoginConfigVO>('/auth/login-config'),

  /** 登出 */
  logout: () =>
    request.post<void>('/auth/logout'),

  /** 获取用户详细信息（含角色权限） */
  info: () =>
    request.get<UserInfoVO>('/auth/info'),

  /** 获取用户菜单 */
  menus: () =>
    request.get<MenuVO[]>('/auth/menus'),
}
