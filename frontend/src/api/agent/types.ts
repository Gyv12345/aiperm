// frontend/src/api/agent/types.ts

/**
 * 聊天请求
 */
export interface ChatRequest {
  message: string
  sessionId?: string
}

/**
 * 确认请求
 */
export interface ConfirmRequest {
  sessionId: string
  actionId: string
}

/**
 * SSE 事件
 */
export interface ChatEvent {
  type: 'text' | 'confirm' | 'tool_result' | 'done' | 'error'
  delta?: string
  actionId?: string
  toolName?: string
  confirmMessage?: string
  result?: unknown
  error?: string
}

/**
 * 消息
 */
export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
  toolName?: string
  needConfirm?: boolean
  actionId?: string
  read?: boolean
}

/**
 * 会话
 */
export interface Session {
  sessionId: string
  messages: Message[]
}
