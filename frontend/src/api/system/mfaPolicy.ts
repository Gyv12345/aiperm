/**
 * 2FA 策略管理 API
 * 对应后端 MfaPolicyController (/system/mfa-policy)
 */
import request from '@/utils/request'

export interface MfaPolicyVO {
  id: number
  name: string
  permPattern: string
  apiPattern: string
  enabled: number
  createTime: string
}

export interface MfaPolicyDTO {
  name: string
  permPattern?: string
  apiPattern?: string
  enabled?: number
}

export const mfaPolicyApi = {
  list: () =>
    request.get<MfaPolicyVO[]>('/system/mfa-policy'),

  create: (data: MfaPolicyDTO) =>
    request.post<void>('/system/mfa-policy', data),

  update: (id: number, data: MfaPolicyDTO) =>
    request.put<void>(`/system/mfa-policy/${id}`, data),

  delete: (id: number) =>
    request.delete<void>(`/system/mfa-policy/${id}`),
}
