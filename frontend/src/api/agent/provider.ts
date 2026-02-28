// frontend/src/api/agent/provider.ts

import request from '@/utils/request'

const BASE_URL = '/agent/provider'

export interface LlmProvider {
  id: number
  name: string
  displayName: string
  baseUrl: string
  model: string
  isDefault: boolean
  status: number
  sort: number
  remark: string
  createTime: string
  updateTime: string
}

export interface LlmProviderDTO {
  id?: number
  name: string
  displayName: string
  apiKey?: string
  baseUrl?: string
  model: string
  isDefault?: boolean
  status?: number
  sort?: number
  remark?: string
}

export function listProviders() {
  return request.get<LlmProvider[]>(BASE_URL)
}

export function getProvider(id: number) {
  return request.get<LlmProvider>(`${BASE_URL}/${id}`)
}

export function createProvider(data: LlmProviderDTO) {
  return request.post<number>(BASE_URL, data)
}

export function updateProvider(id: number, data: LlmProviderDTO) {
  return request.put(`${BASE_URL}/${id}`, data)
}

export function deleteProvider(id: number) {
  return request.delete(`${BASE_URL}/${id}`)
}

export function setDefaultProvider(id: number) {
  return request.put(`${BASE_URL}/${id}/default`)
}
