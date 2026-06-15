// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询任务日志 GET /monitor/job-log */
export async function page7(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.page7Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultJobLogVO>("/monitor/job-log", {
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

/** 删除任务日志 DELETE /monitor/job-log/${param0} */
export async function delete16(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete16Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/monitor/job-log/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 清空任务日志 DELETE /monitor/job-log/clean */
export async function clean1(options?: { [key: string]: any }) {
  return request<API.RVoid>("/monitor/job-log/clean", {
    method: "DELETE",
    ...(options || {}),
  });
}

/** 导出任务日志 GET /monitor/job-log/export */
export async function export5(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.export5Params,
  options?: { [key: string]: any }
) {
  return request<any>("/monitor/job-log/export", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
