// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 获取个人信息 GET /profile/info */
export async function getProfile(options?: { [key: string]: any }) {
  return request<API.ProfileVO>("/profile/info", {
    method: "GET",
    ...(options || {}),
  });
}

/** 修改个人信息 PUT /profile/info */
export async function updateProfile(
  body: API.ProfileDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/profile/info", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取登录日志 GET /profile/logs */
export async function getLoginLogs(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getLoginLogsParams,
  options?: { [key: string]: any }
) {
  return request<API.PageResultLoginLogVO>("/profile/logs", {
    method: "GET",
    params: {
      // pageNum has a default value: 1
      pageNum: "1",
      // pageSize has a default value: 10
      pageSize: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 修改密码 PUT /profile/password */
export async function updatePassword(
  body: API.PasswordDTO,
  options?: { [key: string]: any }
) {
  return request<API.RVoid>("/profile/password", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
