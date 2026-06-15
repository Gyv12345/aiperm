/**
 * 认证相关接口（手写）
 *
 * 说明：
 * - 这些接口多数是免登录的（登录/验证码/登录配置），OpenAPI 生成时
 *   可能未带鉴权逻辑或结构特殊，故手写，统一走 @umijs/max 的 request。
 * - 响应已被 src/requestErrorConfig.ts 自动解包（R<T>.data）。
 */
import { request } from '@umijs/max';

/** 登录方式 */
export type LoginType = 'PASSWORD' | 'SMS' | 'EMAIL' | 'OAUTH';

/** 用户信息（/auth/info 返回） */
export interface UserInfo {
  id: number;
  username: string;
  nickname?: string;
  avatar?: string;
  roles: string[];
  permissions: string[];
}

/** 登录响应 */
export interface LoginResult {
  token: string;
  userInfo?: UserInfo;
}

/** 统一登录请求体 */
export interface UnifiedLoginParams {
  loginType: LoginType;
  identifier: string;
  credential: string;
  imageCaptcha?: string;
  imageCaptchaKey?: string;
}

/** 图形验证码 */
export interface Captcha {
  captchaKey: string;
  captchaImg: string;
}

/** 菜单节点
 * menuType 实际返回为数字字符串："1"=目录 / "2"=菜单 / "3"=按钮；
 * OpenAPI schema 标注为 M/C/F，这里用 string 兼容两种。
 * 注意：子菜单 path 是相对路径（如 "user"），需与父级拼接成完整路径。
 */
export interface MenuNode {
  id: number;
  parentId: number;
  menuName: string;
  menuType: string;
  path?: string | null;
  component?: string;
  permission?: string;
  icon?: string;
  sort?: number;
  visible?: number;
  isCache?: number;
  children?: MenuNode[];
}

/** 登录页可用方式配置 */
export interface LoginConfig {
  loginTypes?: LoginType[];
  [k: string]: any;
}

/** 统一登录（主登录入口） */
export async function unifiedLogin(data: UnifiedLoginParams) {
  return request<LoginResult>('/auth/unified-login', {
    method: 'POST',
    data,
  });
}

/** 传统登录（用户名+密码+验证码） */
export async function login(data: {
  username: string;
  password: string;
  captcha?: string;
  captchaKey?: string;
}) {
  return request<LoginResult>('/auth/login', { method: 'POST', data });
}

/** 登出 */
export async function logout() {
  return request<void>('/auth/logout', { method: 'POST' });
}

/** 当前用户信息（含 roles / permissions） */
export async function getUserInfo() {
  return request<UserInfo>('/auth/info', { method: 'GET' });
}

/** 当前用户菜单树 */
export async function getMenus() {
  return request<MenuNode[]>('/auth/menus', { method: 'GET' });
}

/** 图形验证码 */
export async function getCaptcha() {
  return request<Captcha>('/auth/captcha', { method: 'GET' });
}

/** 登录页可用方式 */
export async function getLoginConfig() {
  return request<LoginConfig>('/auth/login-config', { method: 'GET' });
}
