// @ts-ignore
/* eslint-disable */
import { request } from "@umijs/max";

/** 获取统计数据 GET /dashboard/stats */
export async function getStats(options?: { [key: string]: any }) {
  return request<API.DashboardStatsVO>("/dashboard/stats", {
    method: "GET",
    ...(options || {}),
  });
}
