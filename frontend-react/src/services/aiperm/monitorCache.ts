// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 获取缓存监控概览 GET /monitor/cache */
export async function overview1(options?: { [key: string]: any }) {
  return request<API.CacheMonitorVO>("/monitor/cache", {
    method: "GET",
    ...(options || {}),
  });
}
