<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, reactive, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import {ElMessage} from 'element-plus'
import {MagicStick, Setting} from '@element-plus/icons-vue'
import {PageAgent, tool, type PageAgentConfig, type PageAgentCore} from 'page-agent'
import {z} from 'zod/v4'

type AgentLanguage = 'zh-CN' | 'en-US'

interface FormAgentConfig {
  baseURL: string
  apiKey: string
  model: string
  language: AgentLanguage
  maxSteps: number
  systemDescription: string
}

interface PageMemoryEntry {
  path: string
  title: string
  firstSeenAt: string
  lastSeenAt: string
  visitCount: number
  notes: string[]
}

interface WorkflowMemoryEntry {
  id: string
  name: string
  steps: string
  result: string
  pagePath: string
  pageTitle: string
  createdAt: string
}

interface AgentMemoryStore {
  pages: Record<string, PageMemoryEntry>
  workflows: WorkflowMemoryEntry[]
}

interface RecordWorkflowInput {
  name: string
  steps: string
  result?: string
  pagePath?: string
}

interface RememberPageNoteInput {
  note: string
  pagePath?: string
}

interface PageAgentCoreWithObservation extends PageAgentCore {
  pushObservation?: (content: string) => void
}

const STORAGE_KEY = 'aiperm.form-page-agent.config.v2'
const MEMORY_STORAGE_KEY = 'aiperm.form-page-agent.memory.v1'
const MAX_WORKFLOW_COUNT = 200
const MAX_PAGE_NOTE_COUNT = 20

const DEFAULT_SYSTEM_DESCRIPTION = `你是 AIPerm 的全局页面智能助手。
- 你可以在任意页面执行任务，不受“必须有表单”的限制。
- 优先复用已有页面控件与交互，不要凭空假设页面元素存在。
- 遇到可复用的操作流程后，必须调用 record_workflow 记录流程名称、步骤和结果。
- 当你发现页面关键规则、字段含义或注意事项时，调用 remember_page_note 写入页面记忆。
- 输出使用中文，步骤清晰，避免冗长。`

const DEFAULT_CONFIG: FormAgentConfig = {
  baseURL: '',
  apiKey: '',
  model: '',
  language: 'zh-CN',
  maxSteps: 40,
  systemDescription: DEFAULT_SYSTEM_DESCRIPTION,
}

const route = useRoute()
const configDialogVisible = ref(false)
const configForm = reactive<FormAgentConfig>({ ...DEFAULT_CONFIG })

let agent: PageAgent | null = null
let configSignature = ''
let agentDisposeHandlerBound = false
let lastRememberedPath = ''

const isConfigReady = computed(() => {
  return Boolean(configForm.baseURL.trim() && configForm.apiKey.trim() && configForm.model.trim())
})

function normalizePath(input?: string | null): string {
  const raw = (input ?? '').trim()
  if (!raw) {
    return '/'
  }

  let path = raw
  if (raw.startsWith('http://') || raw.startsWith('https://')) {
    try {
      path = new URL(raw).pathname
    }
    catch {
      path = raw
    }
  }

  const noQuery = (path.split('?')[0] ?? '').split('#')[0] ?? ''
  const withLeadingSlash = noQuery.startsWith('/') ? noQuery : `/${noQuery}`
  const normalized = withLeadingSlash.replace(/\/{2,}/g, '/')
  return normalized || '/'
}

function createEmptyMemoryStore(): AgentMemoryStore {
  return {
    pages: {},
    workflows: [],
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function parsePageMemoryEntry(path: string, value: unknown): PageMemoryEntry | null {
  if (!isRecord(value)) {
    return null
  }

  const title = typeof value.title === 'string' && value.title.trim()
    ? value.title.trim()
    : path

  const firstSeenAt = typeof value.firstSeenAt === 'string' && value.firstSeenAt
    ? value.firstSeenAt
    : new Date().toISOString()
  const lastSeenAt = typeof value.lastSeenAt === 'string' && value.lastSeenAt
    ? value.lastSeenAt
    : firstSeenAt
  const visitCount = typeof value.visitCount === 'number' && Number.isFinite(value.visitCount)
    ? Math.max(1, Math.trunc(value.visitCount))
    : 1

  const notes = Array.isArray(value.notes)
    ? value.notes.filter((item): item is string => typeof item === 'string').slice(-MAX_PAGE_NOTE_COUNT)
    : []

  return {
    path,
    title,
    firstSeenAt,
    lastSeenAt,
    visitCount,
    notes,
  }
}

function parseWorkflowMemoryEntry(value: unknown): WorkflowMemoryEntry | null {
  if (!isRecord(value)) {
    return null
  }

  const name = typeof value.name === 'string' ? value.name.trim() : ''
  const steps = typeof value.steps === 'string' ? value.steps.trim() : ''
  if (!name || !steps) {
    return null
  }

  return {
    id: typeof value.id === 'string' && value.id ? value.id : `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    name,
    steps,
    result: typeof value.result === 'string' ? value.result : '',
    pagePath: normalizePath(typeof value.pagePath === 'string' ? value.pagePath : '/'),
    pageTitle: typeof value.pageTitle === 'string' && value.pageTitle.trim()
      ? value.pageTitle.trim()
      : '未命名页面',
    createdAt: typeof value.createdAt === 'string' && value.createdAt
      ? value.createdAt
      : new Date().toISOString(),
  }
}

function loadMemoryStore(): AgentMemoryStore {
  try {
    const raw = localStorage.getItem(MEMORY_STORAGE_KEY)
    if (!raw) {
      return createEmptyMemoryStore()
    }

    const parsed = JSON.parse(raw) as unknown
    if (!isRecord(parsed)) {
      return createEmptyMemoryStore()
    }

    const pagesRaw = isRecord(parsed.pages) ? parsed.pages : {}
    const workflowsRaw = Array.isArray(parsed.workflows) ? parsed.workflows : []

    const pages: Record<string, PageMemoryEntry> = {}
    for (const [rawPath, value] of Object.entries(pagesRaw)) {
      const path = normalizePath(rawPath)
      const parsedEntry = parsePageMemoryEntry(path, value)
      if (parsedEntry) {
        pages[path] = parsedEntry
      }
    }

    const workflows: WorkflowMemoryEntry[] = []
    for (const item of workflowsRaw) {
      const parsedItem = parseWorkflowMemoryEntry(item)
      if (parsedItem) {
        workflows.push(parsedItem)
      }
    }

    return {
      pages,
      workflows: workflows.slice(-MAX_WORKFLOW_COUNT),
    }
  }
  catch {
    localStorage.removeItem(MEMORY_STORAGE_KEY)
    return createEmptyMemoryStore()
  }
}

function saveMemoryStore(store: AgentMemoryStore): void {
  localStorage.setItem(MEMORY_STORAGE_KEY, JSON.stringify(store))
}

function getCurrentPagePath(): string {
  return normalizePath(route.path || window.location.pathname)
}

function getCurrentPageTitle(): string {
  const routeTitle = typeof route.meta?.title === 'string' ? route.meta.title.trim() : ''
  if (routeTitle) {
    return routeTitle
  }
  const docTitle = document.title.trim()
  if (docTitle) {
    return docTitle
  }
  return getCurrentPagePath()
}

function rememberPage(pathInput: string, pageTitle: string): void {
  const path = normalizePath(pathInput)
  const title = pageTitle.trim() || path
  const now = new Date().toISOString()
  const store = loadMemoryStore()
  const previous = store.pages[path]

  if (previous) {
    previous.title = title
    previous.lastSeenAt = now
    if (lastRememberedPath !== path) {
      previous.visitCount += 1
    }
  }
  else {
    store.pages[path] = {
      path,
      title,
      firstSeenAt: now,
      lastSeenAt: now,
      visitCount: 1,
      notes: [],
    }
  }

  saveMemoryStore(store)
  lastRememberedPath = path
}

function rememberCurrentPage(): void {
  rememberPage(getCurrentPagePath(), getCurrentPageTitle())
}

function rememberPageFromBrowser(): void {
  const path = normalizePath(window.location.pathname)
  const title = document.title.trim() || path
  rememberPage(path, title)
}

function appendPageNote(pagePathInput: string | undefined, noteInput: string): { path: string, note: string } {
  const note = noteInput.trim()
  if (!note) {
    return { path: getCurrentPagePath(), note: '' }
  }

  const path = normalizePath(pagePathInput || window.location.pathname)
  const title = getCurrentPageTitle()
  const now = new Date().toISOString()
  const store = loadMemoryStore()
  const pageEntry = store.pages[path] ?? {
    path,
    title,
    firstSeenAt: now,
    lastSeenAt: now,
    visitCount: 1,
    notes: [],
  }

  pageEntry.title = title
  pageEntry.lastSeenAt = now
  pageEntry.notes = [...pageEntry.notes, note].slice(-MAX_PAGE_NOTE_COUNT)
  store.pages[path] = pageEntry
  saveMemoryStore(store)
  return { path, note }
}

function appendWorkflowMemory(input: RecordWorkflowInput): WorkflowMemoryEntry {
  const path = normalizePath(input.pagePath || window.location.pathname)
  const title = getCurrentPageTitle()
  const now = new Date().toISOString()
  const store = loadMemoryStore()
  const pageEntry = store.pages[path] ?? {
    path,
    title,
    firstSeenAt: now,
    lastSeenAt: now,
    visitCount: 1,
    notes: [],
  }

  pageEntry.title = title
  pageEntry.lastSeenAt = now
  store.pages[path] = pageEntry

  const workflow: WorkflowMemoryEntry = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    name: input.name.trim(),
    steps: input.steps.trim(),
    result: input.result?.trim() ?? '',
    pagePath: path,
    pageTitle: title,
    createdAt: now,
  }

  store.workflows = [...store.workflows, workflow].slice(-MAX_WORKFLOW_COUNT)
  saveMemoryStore(store)
  return workflow
}

function getMemorySummaryForPath(path: string): string {
  const store = loadMemoryStore()
  const page = store.pages[path]
  const pageWorkflows = store.workflows.filter(item => item.pagePath === path).slice(-5)
  const recentPages = Object.values(store.pages)
    .sort((a, b) => b.lastSeenAt.localeCompare(a.lastSeenAt))
    .slice(0, 8)

  const lines: string[] = []
  lines.push(`当前页面路径: ${path}`)
  lines.push(`已记录页面总数: ${Object.keys(store.pages).length}`)

  if (page) {
    lines.push(`当前页面标题: ${page.title}`)
    lines.push(`当前页面访问次数: ${page.visitCount}`)
    if (page.notes.length > 0) {
      lines.push('当前页面已记录要点:')
      for (const note of page.notes.slice(-5)) {
        lines.push(`- ${note}`)
      }
    }
  }

  if (pageWorkflows.length > 0) {
    lines.push('当前页面最近流程:')
    for (const workflow of pageWorkflows) {
      lines.push(`- ${workflow.name}: ${workflow.steps}`)
    }
  }

  if (recentPages.length > 0) {
    lines.push('最近访问页面:')
    for (const visited of recentPages) {
      lines.push(`- ${visited.path} (${visited.title})`)
    }
  }

  lines.push('规则: 完成可复用流程后，调用 record_workflow 记录流程。')
  lines.push('规则: 发现页面关键规则时，调用 remember_page_note 写入页面记忆。')
  return lines.join('\n')
}

function createCustomTools(): NonNullable<PageAgentConfig['customTools']> {
  return {
    record_workflow: tool<RecordWorkflowInput>({
      description: '记录可复用的页面操作流程到长期记忆中。完成关键流程后必须调用。',
      inputSchema: z.object({
        name: z.string().min(2).max(120),
        steps: z.string().min(4).max(2000),
        result: z.string().max(1000).optional(),
        pagePath: z.string().max(300).optional(),
      }),
      execute: async function (input): Promise<string> {
        const workflow = appendWorkflowMemory(input)
        ;(this as PageAgentCoreWithObservation).pushObservation?.(
          `流程已记录: ${workflow.name} @ ${workflow.pagePath}`,
        )
        return `✅ 已记录流程「${workflow.name}」，页面：${workflow.pagePath}`
      },
    }),
    remember_page_note: tool<RememberPageNoteInput>({
      description: '记录当前页面的重要规则、字段含义或注意事项到页面记忆。',
      inputSchema: z.object({
        note: z.string().min(2).max(1000),
        pagePath: z.string().max(300).optional(),
      }),
      execute: async function (input): Promise<string> {
        const { path, note } = appendPageNote(input.pagePath, input.note)
        ;(this as PageAgentCoreWithObservation).pushObservation?.(
          `页面要点已记录: ${path} -> ${note}`,
        )
        return `✅ 已写入页面记忆：${path}`
      },
    }),
  }
}

function loadConfigFromLocal(): void {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return
    }
    const parsed = JSON.parse(raw) as Partial<FormAgentConfig>
    Object.assign(configForm, {
      ...DEFAULT_CONFIG,
      ...parsed,
    })
  }
  catch {
    localStorage.removeItem(STORAGE_KEY)
  }
}

function saveConfigToLocal(): void {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      baseURL: configForm.baseURL.trim(),
      apiKey: configForm.apiKey.trim(),
      model: configForm.model.trim(),
      language: configForm.language,
      maxSteps: configForm.maxSteps,
      systemDescription: configForm.systemDescription.trim(),
    }),
  )
}

function getConfigSignature(cfg: FormAgentConfig): string {
  return [
    cfg.baseURL.trim(),
    cfg.apiKey.trim(),
    cfg.model.trim(),
    cfg.language,
    cfg.maxSteps,
    cfg.systemDescription.trim(),
  ].join('||')
}

function handleAgentDisposed(): void {
  agent = null
  configSignature = ''
  agentDisposeHandlerBound = false
}

function isAgentUsable(instance: PageAgent): boolean {
  const panelWrapper = (instance.panel as { wrapper?: HTMLElement } | undefined)?.wrapper
  return Boolean(panelWrapper && document.body.contains(panelWrapper))
}

function ensureAgentReady(): PageAgent | null {
  if (!isConfigReady.value) {
    return null
  }

  const nextSignature = getConfigSignature(configForm)
  if (agent && configSignature === nextSignature && isAgentUsable(agent)) {
    return agent
  }

  if (agent && isAgentUsable(agent)) {
    agent.dispose()
  }
  handleAgentDisposed()

  const agentConfig: PageAgentConfig = {
    baseURL: configForm.baseURL.trim(),
    apiKey: configForm.apiKey.trim(),
    model: configForm.model.trim(),
    language: configForm.language,
    maxSteps: configForm.maxSteps,
    enableMask: false,
    instructions: {
      system: configForm.systemDescription.trim() || DEFAULT_SYSTEM_DESCRIPTION,
      getPageInstructions: (url: string) => getMemorySummaryForPath(normalizePath(url)),
    },
    customTools: createCustomTools(),
    onBeforeTask: (instance) => {
      rememberCurrentPage()
      const currentPath = normalizePath(window.location.pathname)
      ;(instance as PageAgentCoreWithObservation).pushObservation?.(
        `当前页面: ${currentPath}。你可以在全局页面执行任务，不受表单限制。`,
      )
    },
    onBeforeStep: () => {
      rememberPageFromBrowser()
    },
  }

  agent = new PageAgent(agentConfig)
  if (!agentDisposeHandlerBound) {
    agent.addEventListener('dispose', handleAgentDisposed)
    agentDisposeHandlerBound = true
  }
  configSignature = nextSignature
  return agent
}

function openAgentPanel(): void {
  if (!isConfigReady.value) {
    configDialogVisible.value = true
    ElMessage.info('请先配置 AI 参数')
    return
  }

  const pageAgent = ensureAgentReady()
  if (!pageAgent) {
    ElMessage.error('助手初始化失败')
    return
  }

  pageAgent.panel.show()
}

function saveConfigAndOpen(): void {
  if (!isConfigReady.value) {
    ElMessage.error('请完整填写 Base URL / API Key / Model')
    return
  }
  saveConfigToLocal()
  configDialogVisible.value = false
  openAgentPanel()
}

function resetConfig(): void {
  Object.assign(configForm, { ...DEFAULT_CONFIG })
  localStorage.removeItem(STORAGE_KEY)
  configSignature = ''

  if (agent) {
    agent.dispose()
    handleAgentDisposed()
  }

  ElMessage.success('已重置 AI 配置')
}

watch(
  () => route.fullPath,
  () => {
    rememberCurrentPage()
  },
)

onMounted(() => {
  loadConfigFromLocal()
  rememberCurrentPage()
})

onBeforeUnmount(() => {
  if (agent) {
    agent.dispose()
    handleAgentDisposed()
  }
})
</script>

<template>
  <teleport to="body">
    <div
      class="form-agent-fab-group"
      data-page-agent-ignore="true"
      data-browser-use-ignore="true"
    >
      <el-tooltip content="打开页面助手" placement="left">
        <el-button
          type="primary"
          circle
          class="form-agent-fab"
          @click="openAgentPanel"
        >
          <el-icon><MagicStick /></el-icon>
        </el-button>
      </el-tooltip>

      <el-tooltip content="AI 参数配置" placement="left">
        <el-button
          circle
          class="form-agent-fab"
          @click="configDialogVisible = true"
        >
          <el-icon><Setting /></el-icon>
        </el-button>
      </el-tooltip>
    </div>
  </teleport>

  <el-dialog
    v-model="configDialogVisible"
    title="页面助手 AI 配置"
    width="560px"
    destroy-on-close
  >
    <el-alert
      title="配置与页面记忆仅保存在当前浏览器本地存储"
      type="warning"
      :closable="false"
      show-icon
      class="mb-3"
    />

    <el-form
      :model="configForm"
      label-width="110px"
    >
      <el-form-item label="Base URL" required>
        <el-input
          v-model="configForm.baseURL"
          placeholder="https://api.openai.com/v1"
          clearable
        />
      </el-form-item>

      <el-form-item label="API Key" required>
        <el-input
          v-model="configForm.apiKey"
          type="password"
          show-password
          placeholder="sk-..."
          clearable
        />
      </el-form-item>

      <el-form-item label="Model" required>
        <el-input
          v-model="configForm.model"
          placeholder="gpt-4o-mini / qwen-plus / deepseek-chat"
          clearable
        />
      </el-form-item>

      <el-form-item label="Language">
        <el-select
          v-model="configForm.language"
          style="width: 100%"
        >
          <el-option
            label="中文 (zh-CN)"
            value="zh-CN"
          />
          <el-option
            label="English (en-US)"
            value="en-US"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="Max Steps">
        <el-input-number
          v-model="configForm.maxSteps"
          :min="5"
          :max="80"
          :step="1"
          controls-position="right"
        />
      </el-form-item>

      <el-form-item label="系统描述">
        <el-input
          v-model="configForm.systemDescription"
          type="textarea"
          :rows="6"
          placeholder="给 page-agent 的全局系统描述（会写入 instructions.system）"
          show-word-limit
          :maxlength="2000"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="resetConfig">
        重置
      </el-button>
      <el-button @click="configDialogVisible = false">
        取消
      </el-button>
      <el-button
        type="primary"
        @click="saveConfigAndOpen"
      >
        保存并打开助手
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.form-agent-fab-group {
  position: fixed;
  right: 20px;
  bottom: 24px;
  z-index: 2147483646;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-agent-fab {
  width: 44px;
  height: 44px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.16);
}
</style>
