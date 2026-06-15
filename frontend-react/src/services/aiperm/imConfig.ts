// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 查询全部IM平台配置 GET /system/im-config */
export async function list10(options?: { [key: string]: any }) {
  return request<API.RListImConfigVO>("/system/im-config", {
    method: "GET",
    ...(options || {}),
  });
}

/** 查询单个平台配置 GET /system/im-config/${param0} */
export async function detail(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.detailParams,
  options?: { [key: string]: any }
) {
  const { platform: param0, ...queryParams } = params;
  return request<API.ImConfigVO>(`/system/im-config/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新平台配置 PUT /system/im-config/${param0} */
export async function update5(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update5Params,
  body: API.ImConfigDTO,
  options?: { [key: string]: any }
) {
  const { platform: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/im-config/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}
