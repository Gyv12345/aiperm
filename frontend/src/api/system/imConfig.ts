import request from '@/utils/request'

export interface ImConfigVO {
  id?: number
  platform: 'WEWORK' | 'DINGTALK' | 'FEISHU'
  enabled?: number
  appId?: string
  appSecret?: string
  corpId?: string
  callbackToken?: string
  callbackAesKey?: string
  extraConfig?: string
}

export type ImConfigDTO = Omit<ImConfigVO, 'id' | 'platform'>

export const imConfigApi = {
  list: () =>
    request.get<ImConfigVO[]>('/system/im-config'),

  getByPlatform: (platform: string) =>
    request.get<ImConfigVO>(`/system/im-config/${platform}`),

  update: (platform: string, data: ImConfigDTO) =>
    request.put<void>(`/system/im-config/${platform}`, data),
}
