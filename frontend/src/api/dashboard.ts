import request from '@/utils/request'

// 统计数据 VO
export interface DashboardStatsVO {
  userCount: number
  roleCount: number
  menuCount: number
  onlineCount: number
}

// Dashboard API
export const dashboardApi = {
  // 获取统计数据
  getStats: () =>
    request.get<DashboardStatsVO>('/dashboard/stats'),
}
