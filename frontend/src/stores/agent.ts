// frontend/src/stores/agent.ts

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Message, ChatEvent } from '@/api/agent'
import { createSession, deleteSession, getChatStreamUrl, getConfirmUrl } from '@/api/agent'
import { useUserStore } from './user'

export const useAgentStore = defineStore('agent', () => {
  const userStore = useUserStore()

  // 状态
  const sessionId = ref('')
  const messages = ref<Message[]>([])
  const loading = ref(false)
  const isOpen = ref(false)

  // 待确认操作
  const pendingConfirm = ref<{
    actionId: string
    toolName: string
    message: string
  } | null>(null)

  // 未读消息数
  const unreadCount = computed(() => {
    if (isOpen.value) return 0
    return messages.value.filter(m => m.role === 'assistant' && !m.read).length
  })

  /**
   * 初始化会话
   */
  async function initSession() {
    if (sessionId.value) return

    try {
      const res = await createSession()
      sessionId.value = res.sessionId
    } catch (e) {
      console.error('Failed to create session:', e)
    }
  }

  /**
   * 发送消息
   */
  async function sendMessage(content: string) {
    if (!content.trim() || loading.value) return

    // 确保有会话
    if (!sessionId.value) {
      await initSession()
    }

    // 添加用户消息
    const userMsg: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: content.trim(),
      timestamp: new Date()
    }
    messages.value.push(userMsg)

    // 添加空的助手消息 (用于流式填充)
    const assistantMsg: Message = {
      id: (Date.now() + 1).toString(),
      role: 'assistant',
      content: '',
      timestamp: new Date()
    }
    messages.value.push(assistantMsg)

    loading.value = true
    pendingConfirm.value = null

    // SSE 流式请求
    try {
      const response = await fetch(getChatStreamUrl(), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userStore.token}`
        },
        body: JSON.stringify({
          message: content.trim(),
          sessionId: sessionId.value
        })
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('No reader available')
      }

      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })

        // 解析 SSE 事件
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            try {
              const event: ChatEvent = JSON.parse(line.slice(5).trim())
              handleEvent(event, assistantMsg)
            } catch (e) {
              console.error('Failed to parse event:', line, e)
            }
          }
        }
      }
    } catch (e) {
      console.error('Failed to send message:', e)
      assistantMsg.content = '发送失败，请重试'
    } finally {
      loading.value = false
    }
  }

  /**
   * 处理 SSE 事件
   */
  function handleEvent(event: ChatEvent, assistantMsg: Message) {
    switch (event.type) {
      case 'text':
        assistantMsg.content += event.delta || ''
        break

      case 'confirm':
        pendingConfirm.value = {
          actionId: event.actionId || '',
          toolName: event.toolName || '',
          message: event.confirmMessage || '确认执行此操作？'
        }
        assistantMsg.needConfirm = true
        assistantMsg.actionId = event.actionId
        assistantMsg.toolName = event.toolName
        break

      case 'tool_result':
        // 可以显示工具执行结果
        break

      case 'done':
        loading.value = false
        break

      case 'error':
        assistantMsg.content = event.error || '发生错误'
        loading.value = false
        break
    }
  }

  /**
   * 确认操作
   */
  async function confirmAction() {
    if (!pendingConfirm.value) return

    loading.value = true
    const confirmMsg = pendingConfirm.value
    pendingConfirm.value = null

    try {
      const response = await fetch(getConfirmUrl(), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userStore.token}`
        },
        body: JSON.stringify({
          sessionId: sessionId.value,
          actionId: confirmMsg.actionId
        })
      })

      const reader = response.body?.getReader()
      if (!reader) return

      const decoder = new TextDecoder()
      let buffer = ''

      // 添加新的助手消息
      const assistantMsg: Message = {
        id: Date.now().toString(),
        role: 'assistant',
        content: '',
        timestamp: new Date()
      }
      messages.value.push(assistantMsg)

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            try {
              const event: ChatEvent = JSON.parse(line.slice(5).trim())
              handleEvent(event, assistantMsg)
            } catch (e) {
              console.error('Failed to parse event:', line, e)
            }
          }
        }
      }
    } catch (e) {
      console.error('Failed to confirm action:', e)
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消确认
   */
  function cancelConfirm() {
    pendingConfirm.value = null
  }

  /**
   * 清空消息
   */
  async function clearMessages() {
    if (sessionId.value) {
      await deleteSession(sessionId.value)
    }
    messages.value = []
    sessionId.value = ''
    pendingConfirm.value = null
  }

  /**
   * 切换抽屉
   */
  function toggleDrawer() {
    isOpen.value = !isOpen.value
    if (isOpen.value) {
      // 标记所有消息为已读
      messages.value.forEach(m => m.read = true)
    }
  }

  return {
    sessionId,
    messages,
    loading,
    isOpen,
    pendingConfirm,
    unreadCount,
    initSession,
    sendMessage,
    confirmAction,
    cancelConfirm,
    clearMessages,
    toggleDrawer
  }
})
