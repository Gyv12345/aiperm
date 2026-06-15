// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 分页查询我发起的审批 GET /approval/instance */
export async function page4(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.page4Params,
  options?: { [key: string]: any }
) {
  return request<API.PageResultApprovalInstanceVO>("/approval/instance", {
    method: "GET",
    params: {
      ...params,
      dto: undefined,
      ...params["dto"],
    },
    ...(options || {}),
  });
}

/** 通用提交审批 POST /approval/instance */
export async function submit(
  body: API.ApprovalSubmitDTO,
  options?: { [key: string]: any }
) {
  return request<API.RLong>("/approval/instance", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
