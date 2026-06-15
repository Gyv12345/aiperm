// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 获取服务监控概览 GET /monitor/server */
export async function overview(options?: { [key: string]: any }) {
  return request<API.ServerMonitorVO>("/monitor/server", {
    method: "GET",
    ...(options || {}),
  });
}
