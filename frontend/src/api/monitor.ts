import request from '@/utils/request'
import type {PageParams, PageResult} from '@/types'

export interface OnlineUserQuery extends PageParams {
  username?: string
  ip?: string
}

export interface OnlineUserVO {
  id: number
  userId: number
  username: string
  nickname?: string
  deptName?: string
  roleNames?: string
  token: string
  ip: string
  browser: string
  os: string
  loginTime: string
  lastAccessTime: string
  tokenTimeout: number
  currentSession: boolean
}

export interface HealthComponentVO {
  name: string
  status: string
  details: string
}

export interface ServerMonitorVO {
  appName: string
  activeProfiles: string[]
  status: string
  uptime: number
  javaVersion: string
  osName: string
  processors: number
  systemCpuUsage: number
  processCpuUsage: number
  heapUsed: number
  heapMax: number
  nonHeapUsed: number
  nonHeapMax: number
  diskTotal: number
  diskUsable: number
  liveThreads: number
  daemonThreads: number
  peakThreads: number
  healthComponents: HealthComponentVO[]
}

export interface CacheEntryVO {
  cacheName: string
  keyPrefix: string
  estimatedSize: number
  sampleTtl: number
}

export interface CacheMonitorVO {
  usedMemoryHuman: string
  connectedClients: number
  totalKeys: number
  hits: number
  misses: number
  hitRate: number
  entries: CacheEntryVO[]
}

export interface LoginLogQuery extends PageParams {
  username?: string
  status?: number
  ip?: string
  startDate?: string
  endDate?: string
}

export interface LoginLogVO {
  id: number
  userId?: number
  username: string
  ip: string
  location: string
  browser: string
  os: string
  status: number
  msg: string
  loginTime: string
}

export interface OperLogQuery extends PageParams {
  title?: string
  status?: number
  operUser?: string
  operIp?: string
  startDate?: string
  endDate?: string
}

export interface OperLogVO {
  id: number
  title: string
  operType: number
  method: string
  requestMethod: string
  operUrl: string
  operIp: string
  operParam: string
  jsonResult: string
  status: number
  errorMsg: string
  costTime: number
  operUser: string
  operName: string
  createTime: string
}

export interface JobLogQuery extends PageParams {
  jobName?: string
  status?: number
  triggerSource?: string
  startDate?: string
  endDate?: string
}

export interface JobLogVO {
  id: number
  jobId: number
  jobName: string
  jobGroup: string
  beanClass: string
  triggerSource: string
  status: number
  message: string
  exceptionInfo: string
  startTime: string
  endTime: string
  costTime: number
}

export const onlineMonitorApi = {
  list: (params: OnlineUserQuery) =>
    request.get<PageResult<OnlineUserVO>>('/monitor/online', { params }),

  export: (params: Omit<OnlineUserQuery, 'page' | 'pageSize'>) =>
    request.download('/monitor/online/export', { params }, 'online-users.xlsx'),

  forceLogout: (id: number) =>
    request.delete<void>(`/monitor/online/${id}`),

  forceLogoutBatch: (ids: number[]) =>
    request.delete<void>('/monitor/online/batch', { data: ids }),
}

export const serverMonitorApi = {
  getOverview: () =>
    request.get<ServerMonitorVO>('/monitor/server'),
}

export const cacheMonitorApi = {
  getOverview: () =>
    request.get<CacheMonitorVO>('/monitor/cache'),
}

export const loginLogApi = {
  list: (params: LoginLogQuery) =>
    request.get<PageResult<LoginLogVO>>('/monitor/login-log', { params }),

  export: (params: Omit<LoginLogQuery, 'page' | 'pageSize'>) =>
    request.download('/monitor/login-log/export', { params }, 'login-logs.xlsx'),

  delete: (id: number) =>
    request.delete<void>(`/monitor/login-log/${id}`),

  clean: () =>
    request.delete<void>('/monitor/login-log/clean'),
}

export const operLogMonitorApi = {
  list: (params: OperLogQuery) =>
    request.get<PageResult<OperLogVO>>('/log/oper', { params }),

  getById: (id: number) =>
    request.get<OperLogVO>(`/log/oper/${id}`),

  export: (params: Omit<OperLogQuery, 'page' | 'pageSize'>) =>
    request.download('/log/oper/export', { params }, 'oper-logs.xlsx'),

  delete: (id: number) =>
    request.delete<void>(`/log/oper/${id}`),

  clean: () =>
    request.delete<void>('/log/oper/clean'),
}

export const jobLogApi = {
  list: (params: JobLogQuery) =>
    request.get<PageResult<JobLogVO>>('/monitor/job-log', { params }),

  export: (params: Omit<JobLogQuery, 'page' | 'pageSize'>) =>
    request.download('/monitor/job-log/export', { params }, 'job-logs.xlsx'),

  delete: (id: number) =>
    request.delete<void>(`/monitor/job-log/${id}`),

  clean: () =>
    request.delete<void>('/monitor/job-log/clean'),
}
