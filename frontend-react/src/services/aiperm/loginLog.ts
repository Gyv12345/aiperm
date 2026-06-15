// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询登录日志 GET /monitor/login-log */
export async function page6(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.page6Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultLoginLogVO>("/monitor/login-log", {
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

/** 删除登录日志 DELETE /monitor/login-log/${param0} */
export async function delete15(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete15Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/monitor/login-log/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 清空登录日志 DELETE /monitor/login-log/clean */
export async function clean(options?: { [key: string]: any }) {
  return request<API.RVoid>("/monitor/login-log/clean", {
    method: "DELETE",
    ...(options || {}),
  });
}

/** 导出登录日志 GET /monitor/login-log/export */
export async function export4(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.export4Params,
  options?: { [key: string]: any }
) {
  return request<any>("/monitor/login-log/export", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
