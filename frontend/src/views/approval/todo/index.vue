<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { approvalApi, type ApprovalTodoOverviewVO } from '@/api/approval'

type Platform = 'WEWORK' | 'DINGTALK' | 'FEISHU'
type QuickActionCode = 'OPEN_PLATFORM_TODO' | 'BIND_OAUTH' | 'VIEW_MY_APPROVAL'

const router = useRouter()
const loading = ref(false)
const focusPlatform = ref<Platform>('FEISHU')
const overview = ref<ApprovalTodoOverviewVO | null>(null)

const guide = computed(() => overview.value?.userGuide)
const isAdmin = computed(() => overview.value?.viewer.isAdmin ?? false)
const diagnostics = computed(() => overview.value?.adminDiagnostics)
const platformChecks = computed(() => diagnostics.value?.platformChecks ?? [])
const sceneChecks = computed(() => diagnostics.value?.sceneChecks ?? [])
const actionItems = computed(() => {
  const list = overview.value?.quickActions ?? []
  return list.map(item => ({
    ...item,
    label: actionLabel(item.code),
    buttonType: item.code === 'OPEN_PLATFORM_TODO' ? 'primary' : 'default',
  }))
})

const feishuPermissions = [
  {
    permission: 'approval:approval',
    purpose: '创建/更新/处理审批实例',
    example: '用于发起审批与处理审批状态流转',
  },
  {
    permission: 'approval:approval:readonly',
    purpose: '只读访问审批应用基础信息',
    example: '用于审批应用可用性校验',
  },
  {
    permission: 'approval:approval.list:readonly',
    purpose: '查询审批列表与状态',
    example: '用于待办与回调对账核验',
  },
  {
    permission: 'contact:user.id:readonly',
    purpose: '查询用户身份映射（条件必需）',
    example: '用于内部用户与飞书用户标识映射',
  },
  {
    permission: 'im:message:send_as_bot',
    purpose: '机器人消息发送（条件必需）',
    example: '用于审批通知与告警通知推送',
  },
]

const applyTemplate = computed(() => {
  return `【AIPerm 飞书权限申请说明】
1. 业务场景：
   通过飞书审批承接 AIPerm 待我审批处理，并回传审批结果到系统。

2. 申请权限点：
   - approval:approval
   - approval:approval:readonly
   - approval:approval.list:readonly
   - contact:user.id:readonly（用于用户映射）
   - im:message:send_as_bot（用于消息推送）

3. 数据范围与安全说明：
   仅访问审批与消息流程所需最小权限，不读取无关业务数据。
`
})

const stepActive = computed(() => {
  if (!guide.value) return 0
  if (!guide.value.platformEnabled) return 0
  if (!guide.value.oauthBound) return 1
  if (guide.value.enabledSceneCount <= 0) return 2
  return 3
})

async function fetchOverview() {
  loading.value = true
  try {
    overview.value = await approvalApi.todoOverview(focusPlatform.value)
    focusPlatform.value = overview.value?.viewer.focusPlatform ?? 'FEISHU'
  }
  finally {
    loading.value = false
  }
}

function actionLabel(code: QuickActionCode) {
  if (code === 'OPEN_PLATFORM_TODO') return `去${platformLabel(focusPlatform.value)}处理待办`
  if (code === 'BIND_OAUTH') return `绑定${platformLabel(focusPlatform.value)}账号`
  return '查看我的审批'
}

function platformLabel(platform: Platform | string) {
  if (platform === 'WEWORK') return '企业微信'
  if (platform === 'DINGTALK') return '钉钉'
  return '飞书'
}

function statusTagType(status?: string) {
  if (status === 'APPROVED' || status === 'SUCCESS') return 'success'
  if (status === 'REJECTED' || status === 'FAILED') return 'danger'
  if (status === 'CANCELED') return 'info'
  return 'warning'
}

function formatTime(time?: string) {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 19)
}

function missingFieldName(field: string) {
  const map: Record<string, string> = {
    platformConfig: '平台配置记录',
    appId: '应用ID(appId)',
    appSecret: '应用密钥(appSecret)',
    corpId: '企业ID(corpId)',
    callbackToken: '回调Token(callbackToken)',
    callbackAesKey: '回调AESKey(callbackAesKey)',
  }
  return map[field] || field
}

function missingFieldsText(fields: string[]) {
  if (!fields.length) return '无'
  return fields.map(missingFieldName).join('、')
}

function openUrl(url: string, newTab = false) {
  if (!url) return
  if (url.startsWith('/')) {
    if (newTab) {
      window.open(url, '_blank', 'noopener,noreferrer')
      return
    }
    router.push(url)
    return
  }
  window.open(url, '_blank', 'noopener,noreferrer')
}

function handleQuickAction(code: QuickActionCode) {
  const action = (overview.value?.quickActions ?? []).find(item => item.code === code)
  if (!action) return
  if (!action.enabled) {
    ElMessage.warning(action.reason || '当前条件不足，暂不可执行该操作')
    return
  }
  if (code === 'BIND_OAUTH') {
    openUrl(action.url, true)
    return
  }
  openUrl(action.url)
}

function go(path: string) {
  router.push(path)
}

async function copyApplyTemplate() {
  try {
    await navigator.clipboard.writeText(applyTemplate.value)
    ElMessage.success('申请说明已复制')
  }
  catch {
    ElMessage.warning('复制失败，请手动复制页面文案')
  }
}

onMounted(fetchOverview)
</script>

<template>
  <div class="p-4 todo-guide-page">
    <el-card
      v-loading="loading"
      class="mb-4"
    >
      <template #header>
        <div class="card-header">
          <div class="font-semibold">
            待我审批引导工作台
          </div>
          <el-button
            text
            type="primary"
            @click="fetchOverview"
          >
            刷新状态
          </el-button>
        </div>
      </template>

      <div
        v-if="guide"
        class="guide-section"
      >
        <el-alert
          :title="guide.nextStepReason"
          :type="guide.nextStep === 'OPEN_PLATFORM_TODO' ? 'success' : 'warning'"
          :closable="false"
          show-icon
        />

        <el-steps
          :active="stepActive"
          finish-status="success"
          class="mt-4"
        >
          <el-step
            title="管理员开通平台"
            description="确认飞书审批通道可用"
          />
          <el-step
            title="绑定平台账号"
            description="在个人中心完成第三方绑定"
          />
          <el-step
            title="处理待办审批"
            description="在企业IM客户端完成审批"
          />
        </el-steps>

        <div class="quick-actions">
          <el-button
            v-for="item in actionItems"
            :key="item.code"
            :type="item.buttonType as any"
            :disabled="!item.enabled"
            @click="handleQuickAction(item.code)"
          >
            {{ item.label }}
          </el-button>
        </div>

        <div class="action-reasons">
          <div
            v-for="item in actionItems"
            :key="`${item.code}-reason`"
            class="text-sm text-gray-500"
          >
            <template v-if="!item.enabled && item.reason">
              {{ item.label }}：{{ item.reason }}
            </template>
          </div>
        </div>
      </div>
    </el-card>

    <el-card
      v-if="isAdmin"
      class="mb-4"
    >
      <template #header>
        <div class="card-header">
          <div class="font-semibold">
            管理员快速排查区
          </div>
          <div class="admin-links">
            <el-button
              text
              type="primary"
              @click="go('/system/im-config')"
            >
              IM 平台配置
            </el-button>
            <el-button
              text
              type="primary"
              @click="go('/system/approval-scene')"
            >
              审批场景管理
            </el-button>
            <el-button
              text
              type="primary"
              @click="go('/enterprise/message-log')"
            >
              消息记录
            </el-button>
          </div>
        </div>
      </template>

      <el-row
        :gutter="12"
        class="mb-3"
      >
        <el-col
          :xs="24"
          :md="12"
        >
          <div class="diagnostic-card">
            <div class="diagnostic-title">
              平台配置健康度
            </div>
            <div
              v-for="item in platformChecks"
              :key="item.platform"
              class="diagnostic-item"
            >
              <div class="flex items-center gap-2 mb-1">
                <span>{{ platformLabel(item.platform) }}</span>
                <el-tag
                  :type="item.enabled ? 'success' : 'info'"
                  size="small"
                >
                  {{ item.enabled ? '已启用' : '未启用' }}
                </el-tag>
                <el-tag
                  :type="item.configReady ? 'success' : 'danger'"
                  size="small"
                >
                  {{ item.configReady ? '配置完整' : '配置缺失' }}
                </el-tag>
              </div>
              <div class="text-xs text-gray-500">
                缺失字段：{{ missingFieldsText(item.missingFields) }}
              </div>
            </div>
          </div>
        </el-col>
        <el-col
          :xs="24"
          :md="12"
        >
          <div class="diagnostic-card">
            <div class="diagnostic-title">
              审批场景健康度
            </div>
            <div
              v-for="item in sceneChecks"
              :key="`scene-${item.platform}`"
              class="diagnostic-item"
            >
              <div class="flex items-center gap-2 mb-1">
                <span>{{ platformLabel(item.platform) }}</span>
                <el-tag
                  :type="item.enabledSceneCount > 0 ? 'success' : 'warning'"
                  size="small"
                >
                  启用场景 {{ item.enabledSceneCount }}
                </el-tag>
              </div>
              <div class="text-xs text-gray-500">
                示例场景：{{ item.sampleSceneCode || '-' }}
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col
          :xs="24"
          :md="12"
        >
          <div class="diagnostic-card">
            <div class="diagnostic-title">
              最近审批回调
            </div>
            <template v-if="diagnostics?.latestApprovalCallback">
              <div class="text-sm mb-1">
                实例号：{{ diagnostics.latestApprovalCallback.platformInstanceId }}
              </div>
              <div class="text-sm mb-1">
                场景：{{ diagnostics.latestApprovalCallback.sceneCode }}
              </div>
              <div class="text-sm mb-1">
                状态：
                <el-tag
                  size="small"
                  :type="statusTagType(diagnostics.latestApprovalCallback.status)"
                >
                  {{ diagnostics.latestApprovalCallback.status }}
                </el-tag>
              </div>
              <div class="text-xs text-gray-500">
                时间：{{ formatTime(diagnostics.latestApprovalCallback.resultTime) }}
              </div>
            </template>
            <el-empty
              v-else
              description="暂未检测到最近回调记录"
              :image-size="56"
            />
          </div>
        </el-col>
        <el-col
          :xs="24"
          :md="12"
        >
          <div class="diagnostic-card">
            <div class="diagnostic-title">
              最近消息推送
            </div>
            <template v-if="diagnostics?.latestMessagePush">
              <div class="text-sm mb-1">
                模板：{{ diagnostics.latestMessagePush.templateCode }}
              </div>
              <div class="text-sm mb-1">
                状态：
                <el-tag
                  size="small"
                  :type="statusTagType(diagnostics.latestMessagePush.status)"
                >
                  {{ diagnostics.latestMessagePush.status }}
                </el-tag>
              </div>
              <div class="text-xs text-gray-500">
                错误：{{ diagnostics.latestMessagePush.errorMsg || '-' }}
              </div>
              <div class="text-xs text-gray-500">
                时间：{{ formatTime(diagnostics.latestMessagePush.sendTime) }}
              </div>
            </template>
            <el-empty
              v-else
              description="暂未检测到最近推送记录"
              :image-size="56"
            />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <div class="font-semibold">
            飞书权限申请清单（审批场景）
          </div>
          <el-button
            type="primary"
            plain
            @click="copyApplyTemplate"
          >
            复制申请说明
          </el-button>
        </div>
      </template>

      <el-table
        :data="feishuPermissions"
        border
      >
        <el-table-column
          prop="permission"
          label="权限点"
          min-width="220"
        />
        <el-table-column
          prop="purpose"
          label="用途"
          min-width="180"
        />
        <el-table-column
          prop="example"
          label="申请说明示例"
          min-width="260"
        />
      </el-table>

      <el-alert
        class="mt-3"
        type="info"
        :closable="false"
        show-icon
        title="权限点可能因飞书开放平台版本更新而调整，请以上线时后台显示为准。"
      />

      <div class="mt-3 text-sm check-list">
        <div class="font-medium mb-2">
          配置检查单
        </div>
        <ul>
          <li>回调 URL 已公网可访问。</li>
          <li>Verification Token 与系统配置一致。</li>
          <li>Encrypt Key 与系统配置一致。</li>
          <li>飞书审批应用已发布到目标租户。</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.todo-guide-page {
  background: linear-gradient(180deg, #f8fbff 0%, #f4f7fb 100%);
  min-height: calc(100vh - 64px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.guide-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.action-reasons {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.admin-links {
  display: flex;
  align-items: center;
  gap: 4px;
}

.diagnostic-card {
  border: 1px solid #e6edf8;
  border-radius: 10px;
  background: #fbfdff;
  padding: 12px;
  min-height: 180px;
}

.diagnostic-title {
  font-weight: 600;
  margin-bottom: 10px;
}

.diagnostic-item {
  border-bottom: 1px dashed #edf2f9;
  padding: 8px 0;
}

.diagnostic-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.check-list ul {
  margin: 0;
  padding-left: 16px;
}

.check-list li {
  line-height: 1.8;
  color: #4b5563;
}

@media (max-width: 768px) {
  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .admin-links {
    flex-wrap: wrap;
  }
}
</style>

