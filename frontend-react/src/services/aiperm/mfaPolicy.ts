// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 查询所有2FA策略 GET /system/mfa-policy */
export async function list(options?: { [key: string]: any }) {
  return request<API.RListMfaPolicyVO>("/system/mfa-policy", {
    method: "GET",
    ...(options || {}),
  });
}

/** 创建2FA策略 POST /system/mfa-policy */
export async function create3(
  body: API.MfaPolicyDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/system/mfa-policy", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 更新2FA策略 PUT /system/mfa-policy/${param0} */
export async function update3(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update3Params,
  body: API.MfaPolicyDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/mfa-policy/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除2FA策略 DELETE /system/mfa-policy/${param0} */
export async function delete3(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete3Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/mfa-policy/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}
