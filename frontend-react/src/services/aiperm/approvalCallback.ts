// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 处理外部审批回调 POST /approval/callback/${param0} */
export async function callback(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.callbackParams,
  body: string,
  options?: { [key: string]: any }
) {
  const { platform: param0, ...queryParams } = params;
  return request<string>(`/approval/callback/${param0}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}
