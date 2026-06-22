/**
 * 认证相关接口（手写，移动端）
 *
 * 与桌面端 services/auth.ts 一致，差异：移除 getMenus（移动端硬编码路由，
 * getInitialState 只拉 userInfo）。登录页用的 unifiedLogin/captcha 取自
 * OpenAPI 生成的 services/aiperm/auth.ts（类型同源）。
 *
 * 响应已被 src/requestErrorConfig.ts 自动解包（R<T>.data）。
 */
import { request } from '@umijs/max';

/** 登录方式 */
export type LoginType = 'PASSWORD' | 'SMS' | 'EMAIL';

/** 用户信息（/auth/info 返回） */
export interface UserInfo {
  id: number;
  username: string;
  nickname?: string;
  avatar?: string;
  roles: string[];
  permissions: string[];
}

/** 统一登录请求体 */
export interface UnifiedLoginParams {
  loginType: LoginType;
  identifier: string;
  credential: string;
  imageCaptcha?: string;
  imageCaptchaKey?: string;
}

/** 登录页可用方式配置 */
export interface LoginConfig {
  loginTypes?: LoginType[];
  [k: string]: any;
}

/** 当前用户信息（含 roles / permissions） */
export async function getUserInfo() {
  return request<UserInfo>('/auth/info', { method: 'GET' });
}

/** 登出 */
export async function logout() {
  return request<void>('/auth/logout', { method: 'POST' });
}

/** 登录页可用方式 */
export async function getLoginConfig() {
  return request<LoginConfig>('/auth/login-config', { method: 'GET' });
}
