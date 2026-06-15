// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 获取验证码配置（SMS或EMAIL） GET /system/captcha-config/${param0} */
export async function getConfig1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getConfig1Params,
  options?: { [key: string]: any }
) {
  const { type: param0, ...queryParams } = params;
  return request<API.CaptchaConfigVO>(`/system/captcha-config/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新验证码配置 PUT /system/captcha-config/${param0} */
export async function updateConfig1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateConfig1Params,
  body: API.CaptchaConfigDTO,
  options?: { [key: string]: any }
) {
  const { type: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/captcha-config/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}
