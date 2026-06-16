<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {DocumentCopy, Link, RefreshRight} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {approvalApi, type ApprovalTodoOverviewVO} from '@/api/approval'

const router = useRouter()
const loading = ref(false)
const platform = ref<'FEISHU' | 'WEWORK' | 'DINGTALK'>('FEISHU')
const overview = ref<ApprovalTodoOverviewVO | null>(null)

const permissionDraft = computed(() => {
  return [
    `申请平台：${platform.value}`,
    '申请能力：approval、approval:readonly、approval.list:readonly',
    '用途说明：用于企业审批实例创建、待办处理跳转与审批结果回调。',
    '上线前请以平台后台实时显示的权限清单为准做二次核验。',
  ].join('\n')
})

const platformChecks = computed(() => overview.value?.adminDiagnostics?.platformChecks ?? [])
const sceneChecks = computed(() => overview.value?.adminDiagnostics?.sceneChecks ?? [])
const quickActions = computed(() => overview.value?.quickActions ?? [])

async function fetchOverview() {
  loading.value = true
  try {
    overview.value = await approvalApi.todoOverview(platform.value)
  }
  catch (error) {
    console.error('获取待我审批总览失败:', error)
    ElMessage.error('获取待我审批总览失败')
  }
  finally {
    loading.value = false
  }
}

function actionType(code: string) {
  if (code === 'OPEN_PLATFORM_TODO') {
    return 'primary'
  }
  if (code === 'BIND_OAUTH') {
    return 'success'
  }
  return 'default'
}

function actionLabel(code: string) {
  switch (code) {
    case 'OPEN_PLATFORM_TODO':
      return '去平台处理待办'
    case 'BIND_OAUTH':
      return `绑定 ${platform.value} 账号`
    case 'VIEW_MY_APPROVAL':
      return '查看我的审批'
    default:
      return code
  }
}

function openAction(url?: string) {
  if (!url) {
    return
  }
  if (url.startsWith('/approval/')) {
    router.push(url)
    return
  }
  window.location.href = url
}

async function copyPermissionDraft() {
  try {
    await navigator.clipboard.writeText(permissionDraft.value)
    ElMessage.success('申请文案已复制')
  }
  catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

onMounted(() => {
  fetchOverview()
})
</script>

<template>
  <div class="p-4 space-y-4">
    <el-card shadow="never">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-lg font-semibold">
            待我审批工作台
          </div>
          <div class="mt-1 text-sm text-[var(--el-text-color-secondary)]">
            用一个总览接口回答三个问题：当前能不能处理待办、下一步该做什么、管理员还差哪一步。
          </div>
        </div>
        <div class="flex items-center gap-3">
          <el-segmented
            v-model="platform"
            :options="['FEISHU', 'WEWORK', 'DINGTALK']"
            @change="fetchOverview"
          />
          <el-button
            :icon="RefreshRight"
            :loading="loading"
            @click="fetchOverview"
          >
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <div
      v-loading="loading"
      class="grid gap-4 xl:grid-cols-[1.3fr_0.7fr]"
    >
      <div class="space-y-4">
        <el-card shadow="never">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div class="font-semibold">
                用户引导
              </div>
              <el-tag
                v-if="overview?.userGuide"
                :type="overview.userGuide.nextStep === 'OPEN_PLATFORM_TODO' ? 'success' : 'warning'"
              >
                {{ overview.userGuide.nextStep }}
              </el-tag>
            </div>
          </template>

          <template v-if="overview?.userGuide">
            <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
              <div class="rounded-xl bg-[var(--el-fill-color-light)] p-4">
                <div class="text-xs uppercase tracking-[0.08em] text-[var(--el-text-color-secondary)]">
                  模块开关
                </div>
                <div class="mt-2 text-lg font-semibold">
                  {{ overview.userGuide.moduleEnabled ? '已开启' : '未开启' }}
                </div>
              </div>
              <div class="rounded-xl bg-[var(--el-fill-color-light)] p-4">
                <div class="text-xs uppercase tracking-[0.08em] text-[var(--el-text-color-secondary)]">
                  平台状态
                </div>
                <div class="mt-2 text-lg font-semibold">
                  {{ overview.userGuide.platformEnabled ? '已启用' : '未启用' }}
                </div>
              </div>
              <div class="rounded-xl bg-[var(--el-fill-color-light)] p-4">
                <div class="text-xs uppercase tracking-[0.08em] text-[var(--el-text-color-secondary)]">
                  平台配置
                </div>
                <div class="mt-2 text-lg font-semibold">
                  {{ overview.userGuide.platformConfigReady ? '已完整' : '待补齐' }}
                </div>
              </div>
              <div class="rounded-xl bg-[var(--el-fill-color-light)] p-4">
                <div class="text-xs uppercase tracking-[0.08em] text-[var(--el-text-color-secondary)]">
                  OAuth 绑定
                </div>
                <div class="mt-2 text-lg font-semibold">
                  {{ overview.userGuide.oauthBound ? '已绑定' : '未绑定' }}
                </div>
              </div>
              <div class="rounded-xl bg-[var(--el-fill-color-light)] p-4">
                <div class="text-xs uppercase tracking-[0.08em] text-[var(--el-text-color-secondary)]">
                  可用场景
                </div>
                <div class="mt-2 text-lg font-semibold">
                  {{ overview.userGuide.enabledSceneCount }}
                </div>
              </div>
            </div>

            <div class="mt-4 rounded-2xl bg-[linear-gradient(135deg,rgba(64,158,255,0.12),rgba(103,194,58,0.08))] p-5">
              <div class="text-sm text-[var(--el-text-color-secondary)]">
                下一步
              </div>
              <div class="mt-2 text-xl font-semibold">
                {{ overview.userGuide.nextStep }}
              </div>
              <div class="mt-2 text-sm leading-6 text-[var(--el-text-color-regular)]">
                {{ overview.userGuide.nextStepReason }}
              </div>
            </div>
          </template>
        </el-card>

        <el-card shadow="never">
          <template #header>
            <div class="font-semibold">
              快捷动作
            </div>
          </template>
          <div class="grid gap-3 md:grid-cols-3">
            <div
              v-for="item in quickActions"
              :key="item.code"
              class="rounded-xl border border-[var(--el-border-color-lighter)] p-4"
            >
              <div class="text-sm font-semibold">
                {{ actionLabel(item.code) }}
              </div>
              <div class="mt-2 min-h-[44px] text-sm leading-6 text-[var(--el-text-color-secondary)]">
                {{ item.reason || '可立即执行' }}
              </div>
              <el-button
                class="mt-4 w-full"
                :type="actionType(item.code)"
                :disabled="!item.enabled"
                :icon="Link"
                @click="openAction(item.url)"
              >
                {{ item.enabled ? '立即前往' : '暂不可用' }}
              </el-button>
            </div>
          </div>
        </el-card>

        <el-card
          v-if="overview?.adminDiagnostics"
          shadow="never"
        >
          <template #header>
            <div class="font-semibold">
              管理员诊断
            </div>
          </template>

          <div class="grid gap-4 lg:grid-cols-2">
            <div
              v-for="item in platformChecks"
              :key="item.platform"
              class="rounded-xl bg-[var(--el-fill-color-light)] p-4"
            >
              <div class="flex items-center justify-between gap-3">
                <div class="font-semibold">
                  {{ item.platform }} 配置
                </div>
                <el-tag :type="item.configReady ? 'success' : 'warning'">
                  {{ item.configReady ? '已就绪' : '待补齐' }}
                </el-tag>
              </div>
              <div class="mt-2 text-sm text-[var(--el-text-color-secondary)]">
                平台开关：{{ item.enabled ? '已启用' : '未启用' }}
              </div>
              <div class="mt-3 flex flex-wrap gap-2">
                <el-tag
                  v-for="field in item.missingFields"
                  :key="field"
                  type="danger"
                  effect="plain"
                >
                  {{ field }}
                </el-tag>
                <span
                  v-if="!item.missingFields.length"
                  class="text-sm text-[var(--el-text-color-secondary)]"
                >没有缺失字段</span>
              </div>
            </div>

            <div
              v-for="item in sceneChecks"
              :key="`${item.platform}-${item.sampleSceneCode}`"
              class="rounded-xl bg-[var(--el-fill-color-light)] p-4"
            >
              <div class="font-semibold">
                {{ item.platform }} 场景健康度
              </div>
              <div class="mt-2 text-sm text-[var(--el-text-color-secondary)]">
                已启用场景数：{{ item.enabledSceneCount }}
              </div>
              <div class="mt-2 text-sm text-[var(--el-text-color-secondary)]">
                示例场景：{{ item.sampleSceneCode || '暂无' }}
              </div>
            </div>
          </div>

          <div class="mt-4 grid gap-4 lg:grid-cols-2">
            <div class="rounded-xl border border-[var(--el-border-color-lighter)] p-4">
              <div class="font-semibold">
                最近审批回调
              </div>
              <template v-if="overview.adminDiagnostics.latestApprovalCallback">
                <div class="mt-3 text-sm leading-7 text-[var(--el-text-color-secondary)]">
                  <div>场景：{{ overview.adminDiagnostics.latestApprovalCallback.sceneCode || '-' }}</div>
                  <div>实例：{{ overview.adminDiagnostics.latestApprovalCallback.platformInstanceId || '-' }}</div>
                  <div>状态：{{ overview.adminDiagnostics.latestApprovalCallback.status || '-' }}</div>
                  <div>时间：{{ overview.adminDiagnostics.latestApprovalCallback.resultTime || '-' }}</div>
                </div>
              </template>
              <div
                v-else
                class="mt-3 text-sm text-[var(--el-text-color-secondary)]"
              >
                暂未检测到最近回调记录。
              </div>
            </div>

            <div class="rounded-xl border border-[var(--el-border-color-lighter)] p-4">
              <div class="font-semibold">
                最近消息推送
              </div>
              <template v-if="overview.adminDiagnostics.latestMessagePush">
                <div class="mt-3 text-sm leading-7 text-[var(--el-text-color-secondary)]">
                  <div>模板：{{ overview.adminDiagnostics.latestMessagePush.templateCode || '-' }}</div>
                  <div>状态：{{ overview.adminDiagnostics.latestMessagePush.status || '-' }}</div>
                  <div>错误：{{ overview.adminDiagnostics.latestMessagePush.errorMsg || '-' }}</div>
                  <div>时间：{{ overview.adminDiagnostics.latestMessagePush.sendTime || '-' }}</div>
                </div>
              </template>
              <div
                v-else
                class="mt-3 text-sm text-[var(--el-text-color-secondary)]"
              >
                暂未检测到最近推送记录。
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="space-y-4">
        <el-card shadow="never">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div class="font-semibold">
                权限申请文案
              </div>
              <el-button
                type="primary"
                plain
                :icon="DocumentCopy"
                @click="copyPermissionDraft"
              >
                复制
              </el-button>
            </div>
          </template>
          <pre class="overflow-auto whitespace-pre-wrap rounded-xl bg-[var(--el-fill-color-light)] p-4 text-sm leading-6 text-[var(--el-text-color-regular)]">{{ permissionDraft }}</pre>
        </el-card>

        <el-card shadow="never">
          <template #header>
            <div class="font-semibold">
              使用说明
            </div>
          </template>
          <div class="space-y-3 text-sm leading-7 text-[var(--el-text-color-secondary)]">
            <div>1. 先确认平台已启用，且 App ID / Secret / Callback Token 已配置完整。</div>
            <div>2. 再确认当前账号已绑定对应平台 OAuth 账号，否则无法在外部 IM 中定位待办。</div>
            <div>3. 最后确认至少有一个启用的审批场景，并在 `extraConfig.todoUrl` 中配置平台待办地址。</div>
            <div>4. 如果要本地联调，可在 `extraConfig` 中启用 `simulationMode: true`，再用统一 JSON 结构回调本系统。</div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>
