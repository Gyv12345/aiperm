// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 获取OAuth平台配置 GET /system/oauth-config/${param0} */
export async function getConfig(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getConfigParams,
  options?: { [key: string]: any }
) {
  const { platform: param0, ...queryParams } = params;
  return request<API.OauthConfigVO>(`/system/oauth-config/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新OAuth平台配置 PUT /system/oauth-config/${param0} */
export async function updateConfig(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateConfigParams,
  body: API.OauthConfigDTO,
  options?: { [key: string]: any }
) {
  const { platform: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/oauth-config/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}
