// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 跳转绑定第三方账号授权页 GET /oauth/bind/${param0} */
export async function bindRedirect(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.bindRedirectParams,
  options?: { [key: string]: any }
) {
  const { platform: param0, ...queryParams } = params;
  return request<any>(`/oauth/bind/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 绑定回调 GET /oauth/bind/callback/${param0} */
export async function bindCallback(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.bindCallbackParams,
  options?: { [key: string]: any }
) {
  const { platform: param0, ...queryParams } = params;
  return request<API.RVoid>(`/oauth/bind/callback/${param0}`, {
    method: "GET",
    params: {
      ...queryParams,
    },
    ...(options || {}),
  });
}

/** 获取已绑定的第三方账号列表 GET /oauth/bindings */
export async function bindings(options?: { [key: string]: any }) {
  return request<API.RListOauthBindingVO>("/oauth/bindings", {
    method: "GET",
    ...(options || {}),
  });
}

/** 跳转第三方登录授权页 GET /oauth/login/${param0} */
export async function login1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.login1Params,
  options?: { [key: string]: any }
) {
  const { platform: param0, ...queryParams } = params;
  return request<any>(`/oauth/login/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 第三方登录回调 GET /oauth/login/callback/${param0} */
export async function loginCallback(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.loginCallbackParams,
  options?: { [key: string]: any }
) {
  const { platform: param0, ...queryParams } = params;
  return request<API.LoginVO>(`/oauth/login/callback/${param0}`, {
    method: "GET",
    params: {
      ...queryParams,
    },
    ...(options || {}),
  });
}

/** 解绑第三方账号 DELETE /oauth/unbind/${param0} */
export async function unbind1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.unbind1Params,
  options?: { [key: string]: any }
) {
  const { platform: param0, ...queryParams } = params;
  return request<API.RVoid>(`/oauth/unbind/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}
