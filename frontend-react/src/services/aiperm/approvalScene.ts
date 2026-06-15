// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询审批场景 GET /system/approval-scene */
export async function page3(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.page3Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultApprovalSceneVO>("/system/approval-scene", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}

/** 创建审批场景 POST /system/approval-scene */
export async function create8(
  body: API.ApprovalSceneDTO,
  options?: { [key: string]: any }
) {
  return request<API.RLong>("/system/approval-scene", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询审批场景详情 GET /system/approval-scene/${param0} */
export async function detail2(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.detail2Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.ApprovalSceneVO>(`/system/approval-scene/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新审批场景 PUT /system/approval-scene/${param0} */
export async function update9(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.update9Params,
  body: API.ApprovalSceneDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/approval-scene/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除审批场景 DELETE /system/approval-scene/${param0} */
export async function delete8(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete8Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.RVoid>(`/system/approval-scene/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 查询审批处理器列表 GET /system/approval-scene/handlers */
export async function handlers(options?: { [key: string]: any }) {
  return request<API.RListApprovalHandlerVO>(
    "/system/approval-scene/handlers",
    {
      method: "GET",
      ...(options || {}),
    }
  );
}
