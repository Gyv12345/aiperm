// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 获取待我审批总览 GET /approval/todo/overview */
export async function overview2(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.overview2Params,
  options?: { [key: string]: any }
) {
  return request<API.ApprovalTodoOverviewVO>("/approval/todo/overview", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
