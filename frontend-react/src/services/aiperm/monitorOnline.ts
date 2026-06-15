// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询在线用户 GET /monitor/online */
export async function page5(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.page5Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultOnlineUserVO>("/monitor/online", {
    method: "GET",
    params: {
      // page has a default value: 1
      page: "1",
      // pageSize has a default value: 10
      pageSize: "10",

      ...params,
    },
    ...(options || {}),
  });
}

/** 强退在线用户 DELETE /monitor/online/${param0} */
export async function forceLogout(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.forceLogoutParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/monitor/online/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 批量强退在线用户 DELETE /monitor/online/batch */
export async function forceLogoutBatch(
  body: number[],
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/monitor/online/batch", {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 导出在线用户 GET /monitor/online/export */
export async function export3(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.export3Params,
  options?: { [key: string]: any }
) {
  return request<any>("/monitor/online/export", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
