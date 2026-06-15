// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 获取验证码 GET /auth/captcha */
export async function captcha(options?: { [key: string]: any }) {
  return request<API.CaptchaVO>("/auth/captcha", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取当前用户信息（含角色权限） GET /auth/info */
export async function info(options?: { [key: string]: any }) {
  return request<API.UserInfoVO>("/auth/info", {
    method: "GET",
    ...(options || {}),
  });
}

/** 登录（传统方式，保持兼容） POST /auth/login */
export async function login(
  body: API.LoginRequest,
  options?: { [key: string]: any }
) {
  return request<API.LoginVO>("/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取登录配置（控制前端显示哪些登录方式） GET /auth/login-config */
export async function loginConfig(options?: { [key: string]: any }) {
  return request<API.LoginConfigVO>("/auth/login-config", {
    method: "GET",
    ...(options || {}),
  });
}

/** 登出 POST /auth/logout */
export async function logout(options?: { [key: string]: any }) {
  return request<API.RVoid>("/auth/logout", {
    method: "POST",
    ...(options || {}),
  });
}

/** 获取当前用户菜单 GET /auth/menus */
export async function menus(options?: { [key: string]: any }) {
  return request<API.RListMenuVO>("/auth/menus", {
    method: "GET",
    ...(options || {}),
  });
}

/** 统一登录（支持多种登录方式） POST /auth/unified-login */
export async function unifiedLogin(
  body: API.UnifiedLoginDTO,
  options?: { [key: string]: any }
) {
  return request<API.LoginVO>("/auth/unified-login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
