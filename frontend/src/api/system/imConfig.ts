import request from '@/utils/request'

export interface ImConfigVO {
  id: number
  platform: string
  enabled: number
  appId?: string
  appSecret?: string
  corpId?: string
  callbackToken?: string
  callbackAesKey?: string
  extraConfig?: string
  remark?: string
  configReady: boolean
  missingFields: string[]
}

export interface ImConfigDTO {
  enabled: number
  appId?: string
  appSecret?: string
  corpId?: string
  callbackToken?: string
  callbackAesKey?: string
  extraConfig?: string
  remark?: string
}

export const imConfigApi = {
  list: () =>
    request.get<ImConfigVO[]>('/system/im-config'),

  get: (platform: string) =>
    request.get<ImConfigVO>(`/system/im-config/${platform}`),

  update: (platform: string, data: ImConfigDTO) =>
    request.put<void>(`/system/im-config/${platform}`, data),
}
