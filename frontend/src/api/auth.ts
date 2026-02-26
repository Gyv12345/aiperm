/**
 * 认证模块 API
 * 对应后端 AuthController (/auth)
 */
import request from '@/utils/request'
import type { MenuVO } from './system/menu'

export type { MenuVO }

// ==================== 类型定义 ====================

/** 登录请求 */
export interface LoginRequest {
  username: string
  password: string
  captchaKey: string
  captcha: string
}

/** 登录响应 */
export interface LoginVO {
  token: string
  username: string
  nickname: string
  avatar: string
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

// ==================== API 函数 ====================

export const authApi = {
  /** 获取验证码 */
  captcha: () =>
    request.get<CaptchaVO>('/auth/captcha'),

  /** 登录 */
  login: (data: LoginRequest) =>
    request.post<LoginVO>('/auth/login', data),

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
