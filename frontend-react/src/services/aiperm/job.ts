// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询定时任务 GET /enterprise/job */
export async function list6(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.list6Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultJobVO>("/enterprise/job", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}

/** 创建定时任务 POST /enterprise/job */
export async function create10(
  body: API.JobDTO,
  options?: { [key: string]: any }
) {
  return request<API.RLong>("/enterprise/job", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询定时任务详情 GET /enterprise/job/${param0} */
export async function detail4(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.detail4Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.JobVO>(`/enterprise/job/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新定时任务 PUT /enterprise/job/${param0} */
export async function update11(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update11Params,
  body: API.JobDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/job/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除定时任务 DELETE /enterprise/job/${param0} */
export async function delete10(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete10Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/job/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 暂停定时任务 PUT /enterprise/job/${param0}/pause */
export async function pause(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.pauseParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/job/${param0}/pause`, {
    method: "PUT",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 恢复定时任务 PUT /enterprise/job/${param0}/resume */
export async function resume(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.resumeParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/job/${param0}/resume`, {
    method: "PUT",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 立即执行一次定时任务 PUT /enterprise/job/${param0}/run */
export async function runOnce(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.runOnceParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/enterprise/job/${param0}/run`, {
    method: "PUT",
    params: { ...queryParams },
    ...(options || {}),
  });
}
