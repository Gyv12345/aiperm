// frontend/src/stores/agent.ts

import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'
import { Chat } from '@ai-sdk/vue'
import type { ChatTransport, UIMessage, UIMessageChunk } from 'ai'
import type { Message, ChatEvent } from '@/api/agent'
import { createSession, deleteSession, getChatStreamUrl, getConfirmUrl } from '@/api/agent'
import { useUserStore } from './user'

type PendingConfirm = {
  actionId: string
  toolName: string
  message: string
}

type ConfirmDataChunk = {
  type: 'data-confirm'
  data: PendingConfirm
}

const isTextPart = (part: unknown): part is { type: 'text'; text: string } =>
  !!part && typeof part === 'object' && (part as { type?: string }).type === 'text'

const generateMessageId = () => `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`

const isObject = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null

const isConfirmDataChunk = (value: unknown): value is ConfirmDataChunk =>
  isObject(value)
  && value.type === 'data-confirm'
  && isObject(value.data)
  && typeof value.data.actionId === 'string'
  && typeof value.data.toolName === 'string'
  && typeof value.data.message === 'string'

const isUiMessageChunkLike = (value: unknown): value is UIMessageChunk =>
  isObject(value) && typeof value.type === 'string' && (
    value.type === 'start'
    || value.type === 'finish'
    || value.type === 'text-start'
    || value.type === 'text-delta'
    || value.type === 'text-end'
    || value.type === 'error'
  )

const isLegacyChatEvent = (value: unknown): value is ChatEvent =>
  isObject(value) && typeof value.type === 'string'

export const useAgentStore = defineStore('agent', () => {
  const userStore = useUserStore()

  const sessionId = ref('')
  const isOpen = ref(false)
  const pendingConfirm = ref<PendingConfirm | null>(null)
  const messages = ref<Message[]>([])

  const ensureSession = async () => {
    if (sessionId.value) return
    const res = await createSession()
    sessionId.value = res.sessionId
  }

  const appendAssistantMessage = (content: string) => {
    const uiMessage: UIMessage = {
      id: generateMessageId(),
      role: 'assistant',
      parts: [{ type: 'text', text: content || '' }]
    }
    chat.messages = [...chat.messages, uiMessage]
  }

  const parseSseEvents = (
    response: Response,
    onEvent: (event: unknown) => void
  ): Promise<void> =>
    new Promise(async (resolve, reject) => {
      const contentType = response.headers.get('content-type') || ''
      if (contentType.includes('application/json')) {
        try {
          const event = await response.json() as unknown
          onEvent(event)
          resolve()
        } catch (e) {
          reject(e)
        }
        return
      }

      const reader = response.body?.getReader()
      if (!reader) {
        reject(new Error('No reader available'))
        return
      }

      const decoder = new TextDecoder()
      let buffer = ''
      const separator = /\r?\n\r?\n/

      const emitBlock = (rawBlock: string) => {
        const payload = rawBlock
          .split(/\r?\n/)
          .map(line => line.trimEnd())
          .filter(line => line.startsWith('data:'))
          .map(line => line.slice(5).trim())
          .join('\n')

        if (!payload) return
        try {
          onEvent(JSON.parse(payload) as unknown)
        } catch (e) {
          // ignore malformed chunks but keep stream alive
          console.error('Failed to parse SSE payload:', payload, e)
        }
      }

      try {
        while (true) {
          const { done, value } = await reader.read()
          if (done) break

          buffer += decoder.decode(value, { stream: true })
          let match = buffer.match(separator)
          while (match && match.index !== undefined) {
            const block = buffer.slice(0, match.index)
            buffer = buffer.slice(match.index + match[0].length)
            emitBlock(block)
            match = buffer.match(separator)
          }
        }

        const remaining = buffer.trim()
        if (remaining) {
          emitBlock(remaining)
        }
        resolve()
      } catch (e) {
        reject(e)
      }
    })

  const transport: ChatTransport<UIMessage> = {
    async sendMessages({ messages: uiMessages, abortSignal }) {
      await ensureSession()

      const lastUser = [...uiMessages].reverse().find(m => m.role === 'user')
      const userText = (lastUser?.parts ?? [])
        .filter(isTextPart)
        .map(p => p.text)
        .join('\n')
        .trim()

      const response = await fetch(getChatStreamUrl(), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `${userStore.token ?? ''}`
        },
        signal: abortSignal,
        body: JSON.stringify({
          message: userText,
          sessionId: sessionId.value
        })
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      return new ReadableStream<UIMessageChunk>({
        start(controller) {
          parseSseEvents(response, event => {
            if (isConfirmDataChunk(event)) {
              pendingConfirm.value = event.data
              return
            }

            if (isUiMessageChunkLike(event)) {
              controller.enqueue(event)
              return
            }

            // 兼容旧版后端事件格式
            if (isLegacyChatEvent(event)) {
              const textId = generateMessageId()
              switch (event.type) {
                case 'text':
                  controller.enqueue({ type: 'text-start', id: textId })
                  controller.enqueue({
                    type: 'text-delta',
                    id: textId,
                    delta: event.delta || ''
                  })
                  controller.enqueue({ type: 'text-end', id: textId })
                  return
                case 'confirm':
                  pendingConfirm.value = {
                    actionId: event.actionId || '',
                    toolName: event.toolName || '',
                    message: event.confirmMessage || '确认执行此操作？'
                  }
                  return
                case 'error':
                  controller.enqueue({
                    type: 'error',
                    errorText: event.error || '发生错误'
                  })
                  return
                case 'done':
                case 'tool_result':
                default:
                  return
              }
            }
          }).then(() => {
            controller.close()
          }).catch(err => {
            controller.error(err)
          })
        }
      })
    },
    async reconnectToStream() {
      return null
    }
  }

  const chat = new Chat<UIMessage>({
    transport,
    onError: error => {
      console.error('Agent chat failed:', error)
    }
  })

  const loading = computed(() => chat.status === 'submitted' || chat.status === 'streaming')

  const unreadCount = computed(() => {
    if (isOpen.value) return 0
    return messages.value.filter(m => m.role === 'assistant' && !m.read).length
  })

  // 同步 AI SDK UIMessage -> 现有页面使用的 Message 结构
  watch(
    () => chat.messages,
    uiMessages => {
      const prevReadMap = new Map(messages.value.map(m => [m.id, !!m.read]))
      messages.value = uiMessages.map(m => ({
        id: m.id,
        role: m.role === 'assistant' ? 'assistant' : 'user',
        content: (m.parts ?? []).filter(isTextPart).map(p => p.text).join(''),
        timestamp: new Date(),
        read: m.role === 'assistant' ? (prevReadMap.get(m.id) ?? false) : true
      }))
    },
    { immediate: true, deep: true }
  )

  async function sendMessage(content: string) {
    if (!content.trim() || loading.value) return
    pendingConfirm.value = null
    await ensureSession()
    await chat.sendMessage({ text: content.trim() })
  }

  async function confirmAction() {
    if (!pendingConfirm.value) return

    const confirmMsg = pendingConfirm.value
    pendingConfirm.value = null

    const response = await fetch(getConfirmUrl(), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `${userStore.token ?? ''}`
      },
      body: JSON.stringify({
        sessionId: sessionId.value,
        actionId: confirmMsg.actionId
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    let assistantContent = ''
    let currentTextId = ''
    await parseSseEvents(response, event => {
      if (isConfirmDataChunk(event)) {
        pendingConfirm.value = event.data
        return
      }

      if (isUiMessageChunkLike(event)) {
        switch (event.type) {
          case 'text-start':
            currentTextId = event.id
            break
          case 'text-delta':
            if (!currentTextId || event.id === currentTextId) {
              assistantContent += event.delta
            }
            break
          case 'error':
            assistantContent = event.errorText || '发生错误'
            break
          default:
            break
        }
        return
      }

      if (isLegacyChatEvent(event)) {
        switch (event.type) {
          case 'text':
            assistantContent += event.delta || ''
            break
          case 'confirm':
            pendingConfirm.value = {
              actionId: event.actionId || '',
              toolName: event.toolName || '',
              message: event.confirmMessage || '确认执行此操作？'
            }
            break
          case 'error':
            assistantContent = event.error || '发生错误'
            break
          default:
            break
        }
      }
    })

    appendAssistantMessage(assistantContent || '操作已确认')
  }

  function cancelConfirm() {
    pendingConfirm.value = null
  }

  async function clearMessages() {
    if (sessionId.value) {
      await deleteSession(sessionId.value)
    }
    sessionId.value = ''
    pendingConfirm.value = null
    chat.messages = []
  }

  function toggleDrawer() {
    isOpen.value = !isOpen.value
    if (isOpen.value) {
      messages.value.forEach(m => {
        m.read = true
      })
    }
  }

  return {
    sessionId,
    messages,
    loading,
    isOpen,
    pendingConfirm,
    unreadCount,
    sendMessage,
    confirmAction,
    cancelConfirm,
    clearMessages,
    toggleDrawer
  }
})
