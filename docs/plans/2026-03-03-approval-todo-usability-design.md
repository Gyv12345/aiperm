# 审批中心待我审批可用性改造设计

## 1. 背景与目标

当前 `审批中心/待我审批` 页面仅为空态说明，对小白用户不友好，无法回答三个核心问题：

1. 我要去哪处理审批？
2. 为什么我现在处理不了？
3. 管理员要去哪里排查？

本次目标是在不改变现有审批主流程的前提下，完成“用户引导 + 管理员诊断”一体化改造，重点覆盖飞书接入场景。

## 2. 设计结论

### 2.1 页面定位

`待我审批` 改为“混合版工作台”：

1. 面向所有用户的强引导区（下一步动作 + 快捷入口）
2. 面向管理员的快速排查区（配置/场景/回调/推送摘要）
3. 面向飞书接入的权限申请与配置清单（完全具体版）

### 2.2 分层可见策略

按“分层可见”执行：

1. 所有人可见：个人引导、快捷操作、飞书申请说明
2. 管理员额外可见：平台健康度、审批场景、最近回调、最近推送

管理员判定条件（后端）：

- 满足任一权限：`system:im-config:list` / `system:approval-scene:list` / `enterprise:message-log:list`

### 2.3 实现路线

采用“后端聚合版（推荐）”：

1. 新增聚合接口 `GET /approval/todo/overview`
2. 前端 `approval/todo` 单页消费该接口并渲染分层 UI
3. 保持 `我的审批`、`IM 平台配置`、`审批场景管理` 现有接口与行为不变

## 3. 信息架构与交互

## 3.1 区块 A：我现在该做什么（全员）

基于状态自动计算下一步：

1. 未启用平台：提示联系管理员开通
2. 已启用但未绑定：引导去绑定飞书账号
3. 已绑定但无场景：提示管理员配置审批场景
4. 条件满足：主按钮“去飞书处理审批待办”

字段来源：

- `sys_im_config`（平台启用）
- `sys_user_oauth`（用户绑定）
- `sys_approval_scene`（可用场景数）

### 3.2 区块 B：快捷操作（全员）

按钮策略：

1. `去飞书审批待办`：仅在 `platformEnabled && oauthBound && enabledSceneCount > 0` 时可用
2. `立即绑定飞书`：始终可用
3. `查看我的审批`：始终可用

禁用按钮必须显示原因文案，不允许无反馈禁用。

### 3.3 区块 C：管理员快速排查区（管理员）

四张诊断卡：

1. 平台配置健康度：启用状态、关键字段缺失项
2. 审批场景健康度：平台启用场景数量
3. 最近审批回调摘要：实例号、状态、时间
4. 最近消息推送摘要：模板、状态、错误摘要、时间

### 3.4 区块 D：飞书权限申请与配置清单（全员）

页面固定展示三列：`权限点` / `用途` / `申请说明示例`。

权限分组：

1. 必需（审批打通）
  - `approval:approval`
  - `approval:approval:readonly`
  - `approval:approval.list:readonly`
2. 条件必需（用户身份映射）
  - `contact:user.id:readonly`
3. 条件必需（机器人发消息）
  - `im:message:send_as_bot`（或平台最新同类权限名）

附配置检查单：

1. 回调 URL 可公网访问
2. Verification Token 一致
3. Encrypt Key 一致
4. 飞书应用已发布到目标租户

说明：权限点以飞书开放平台后台实时显示为准，页面文案将标记“上线前二次核验”。

## 4. 接口设计

新增接口：`GET /approval/todo/overview`

请求参数：

- `platform`（可选，默认 `FEISHU`）

返回结构（摘要）：

```json
{
  "viewer": {
    "userId": 1,
    "isAdmin": true,
    "focusPlatform": "FEISHU"
  },
  "userGuide": {
    "platformEnabled": true,
    "oauthBound": false,
    "enabledSceneCount": 2,
    "nextStep": "BIND_OAUTH",
    "nextStepReason": "当前账号未绑定飞书"
  },
  "quickActions": [
    { "code": "OPEN_PLATFORM_TODO", "enabled": false, "reason": "未绑定飞书账号", "url": "" },
    { "code": "BIND_OAUTH", "enabled": true, "url": "/api/oauth/bind/FEISHU" },
    { "code": "VIEW_MY_APPROVAL", "enabled": true, "url": "/approval/my" }
  ],
  "adminDiagnostics": {
    "platformChecks": [
      {
        "platform": "FEISHU",
        "enabled": true,
        "configReady": false,
        "missingFields": ["appId", "appSecret", "callbackToken"]
      }
    ],
    "sceneChecks": [
      { "platform": "FEISHU", "enabledSceneCount": 2, "sampleSceneCode": "ORDER_APPROVAL" }
    ],
    "latestApprovalCallback": {
      "platform": "FEISHU",
      "sceneCode": "ORDER_APPROVAL",
      "platformInstanceId": "FEISHU-xxx",
      "status": "APPROVED",
      "resultTime": "2026-03-03T10:20:00"
    },
    "latestMessagePush": {
      "platform": "FEISHU",
      "templateCode": "APPROVAL_SUBMIT",
      "status": "FAILED",
      "errorMsg": "invalid tenant access token",
      "sendTime": "2026-03-03T10:19:00"
    }
  }
}
```

## 5. 异常与文案规范

统一短句提示：

1. `当前账号未绑定飞书，请先完成绑定后再处理待办。`
2. `管理员尚未启用飞书审批通道。`
3. `飞书通道已开通，但尚未配置可用审批场景。`
4. `暂未检测到最近回调/推送记录。`

## 6. 验收标准

### 6.1 用户侧

1. 普通用户可明确看到下一步动作，且至少有一个可执行操作
2. 未绑定时可一键跳转绑定入口
3. 移动端（<=768px）可读、无严重挤压

### 6.2 管理员侧

1. 管理员可看到完整诊断区（配置、场景、回调、推送）
2. 回调与推送均显示最近记录；无数据时有明确空态

### 6.3 技术约束

1. 新增只读聚合接口，不影响现有审批提交流程
2. 不改现有 `system/im-config`、`approval/my` 等既有接口契约

