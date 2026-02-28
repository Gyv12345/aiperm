// frontend/src/api/agent/index.ts

import type { ChatRequest } from './types'
import request from '@/utils/request'

const BASE_URL = '/agent'

/**
 * 创建会话
 */
export function createSession() {
  return request.post<{ sessionId: string }>(`${BASE_URL}/session`)
}

/**
 * 删除会话
 */
export function deleteSession(sessionId: string) {
  return request.delete(`${BASE_URL}/session/${sessionId}`)
}

/**
 * 同步对话 (企微等场景)
 */
export function chat(data: ChatRequest) {
  return request.post<{
    sessionId: string
    content: string
    needConfirm?: boolean
    actionId?: string
    toolName?: string
    confirmMessage?: string
  }>(`${BASE_URL}/chat`, data)
}

/**
 * 获取 SSE 流式对话 URL
 */
export function getChatStreamUrl(): string {
  return `${import.meta.env.VITE_API_BASE_URL}${BASE_URL}/chat/stream`
}

/**
 * 获取确认操作 URL
 */
export function getConfirmUrl(): string {
  return `${import.meta.env.VITE_API_BASE_URL}${BASE_URL}/confirm`
}

export * from './types'
