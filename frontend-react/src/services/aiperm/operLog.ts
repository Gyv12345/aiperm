// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询操作日志 GET /log/oper */
export async function page8(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.page8Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultSysOperLog>("/log/oper", {
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

/** 查询操作日志详情 GET /log/oper/${param0} */
export async function getById5(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getById5Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.SysOperLog>(`/log/oper/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 删除操作日志 DELETE /log/oper/${param0} */
export async function delete12(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete12Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/log/oper/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 清空操作日志 DELETE /log/oper/clean */
export async function clean2(options?: { [key: string]: any }) {
  return request<API.RVoid>("/log/oper/clean", {
    method: "DELETE",
    ...(options || {}),
  });
}

/** 导出操作日志 GET /log/oper/export */
export async function export6(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.export6Params,
  options?: { [key: string]: any }
) {
  return request<any>("/log/oper/export", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
