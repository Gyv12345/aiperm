# 企业 IM 审批流与消息推送集成设计

> 创建时间：2026-03-02

## 1. 需求概述

### 1.1 背景

整合企业 IM（企业微信、钉钉、飞书）的审批流和消息推送能力，避免重复开发，复用现有平台能力。

### 1.2 核心需求

| 项目 | 决策 |
|------|------|
| 审批场景 | 业务审批（订单、合同等特定场景） |
| 触发方式 | 配置化触发，业务代码发信号 |
| 状态更新 | 审批回调后自动执行业务逻辑 |
| 通知范围 | 审批 + 告警 + 业务通知（通用消息） |
| 用户映射 | 复用现有 OAuth 绑定 |
| 平台支持 | 企微/钉钉/飞书，统一抽象 |
| 配置模式 | 单租户，所有配置存数据库 |

## 2. 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      业务模块层                              │
│   (订单、合同、报销等业务场景，通过 ApprovalClient 发起审批)    │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┴─────────────┐
        ▼                           ▼
┌───────────────────┐     ┌───────────────────┐
│   ApprovalModule  │     │ NotificationModule│
│    (审批流模块)     │     │   (消息推送模块)    │
│                   │     │                   │
│ - 审批场景配置      │     │ - 消息模板管理      │
│ - 审批记录管理      │     │ - 推送记录管理      │
│ - 回调处理分发      │     │ - 多平台适配        │
└────────┬──────────┘     └─────────┬─────────┘
         │                          │
         └──────────┬───────────────┘
                    ▼
        ┌───────────────────┐
        │  ImPlatformAdapter │
        │  (IM平台适配层)     │
        │                   │
        │ ┌─────┬─────┬────┐│
        │ │企微 │钉钉 │飞书 ││
        │ └─────┴─────┴────┘│
        └───────────────────┘
```

**核心组件：**

- **ApprovalModule**：管理审批场景配置、审批实例、回调处理
- **NotificationModule**：管理消息模板、推送记录、多平台路由
- **ImPlatformAdapter**：统一封装三个平台的 API 差异

**调用关系：**

1. 业务模块调用 `ApprovalClient.submit()` 发起审批
2. ApprovalModule 调用 NotificationModule 发送审批通知
3. 企业 IM 回调 ApprovalModule
4. ApprovalModule 根据场景找到处理器，自动执行业务逻辑

## 3. 数据库设计

### 3.1 IM 平台配置

```sql
CREATE TABLE sys_im_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    platform VARCHAR(20) NOT NULL COMMENT '平台: WEWORK/DINGTALK/FEISHU',
    enabled TINYINT DEFAULT 0 COMMENT '是否启用',
    app_id VARCHAR(100) COMMENT '应用ID',
    app_secret VARCHAR(200) COMMENT '应用密钥(加密存储)',
    corp_id VARCHAR(100) COMMENT '企业ID',
    callback_token VARCHAR(200) COMMENT '回调验证token',
    callback_aes_key VARCHAR(200) COMMENT '回调加解密key',
    extra_config JSON COMMENT '扩展配置',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50)
) COMMENT 'IM平台配置表';
```

### 3.2 审批场景配置

```sql
CREATE TABLE sys_approval_scene (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_code VARCHAR(50) NOT NULL COMMENT '场景编码: ORDER_APPROVAL',
    scene_name VARCHAR(100) NOT NULL COMMENT '场景名称: 订单审批',
    platform VARCHAR(20) NOT NULL COMMENT '使用的IM平台',
    template_id VARCHAR(100) COMMENT '平台审批模板ID',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    handler_class VARCHAR(200) COMMENT '回调处理器类名',
    timeout_hours INT DEFAULT 72 COMMENT '超时时间(小时)',
    timeout_action VARCHAR(20) COMMENT '超时动作: AUTO_PASS/AUTO_REJECT/NOTIFY',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    UNIQUE KEY uk_scene_code (scene_code)
) COMMENT '审批场景配置表';
```

### 3.3 审批实例记录

```sql
CREATE TABLE sys_approval_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_code VARCHAR(50) NOT NULL COMMENT '场景编码',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型: ORDER',
    business_id BIGINT NOT NULL COMMENT '业务ID',
    initiator_id BIGINT NOT NULL COMMENT '发起人ID',
    platform VARCHAR(20) NOT NULL COMMENT '平台',
    platform_instance_id VARCHAR(100) COMMENT '平台审批实例ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CANCELED',
    form_data JSON COMMENT '审批表单数据',
    result_time DATETIME COMMENT '审批结果时间',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    UNIQUE KEY uk_business (business_type, business_id)
) COMMENT '审批实例记录表';
```

### 3.4 消息模板

```sql
CREATE TABLE sys_message_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_code VARCHAR(50) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    category VARCHAR(20) COMMENT '分类: APPROVAL/ALERT/BUSINESS',
    platform VARCHAR(20) COMMENT '适用平台，NULL表示通用',
    title VARCHAR(200) COMMENT '消息标题模板',
    content TEXT COMMENT '消息内容模板，支持变量 ${var}',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    UNIQUE KEY uk_template_code (template_code)
) COMMENT '消息模板表';
```

### 3.5 消息推送记录

```sql
CREATE TABLE sys_message_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_code VARCHAR(50) COMMENT '使用的模板',
    platform VARCHAR(20) NOT NULL COMMENT '平台',
    receiver_id BIGINT COMMENT '接收人ID(系统用户)',
    platform_user_id VARCHAR(100) COMMENT '平台用户ID',
    title VARCHAR(200) COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
    error_msg TEXT COMMENT '错误信息',
    send_time DATETIME COMMENT '发送时间',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50)
) COMMENT '消息推送记录表';
```

## 4. 审批流程核心逻辑

### 4.1 发起审批

```java
// 业务代码调用示例
approvalClient.submit("ORDER_APPROVAL", orderId, formData);

// ApprovalService 核心逻辑
public void submit(String sceneCode, Long businessId, Map<String, Object> formData) {
    // 1. 查询场景配置
    ApprovalScene scene = sceneRepo.findByCode(sceneCode);
    if (scene == null || !scene.isEnabled()) {
        return; // 未配置或未启用，直接跳过审批
    }

    // 2. 检查是否已有进行中的审批
    if (instanceRepo.existsPending(sceneCode, businessId)) {
        throw new BusinessException("该业务已有审批进行中");
    }

    // 3. 获取发起人的平台用户ID
    Long userId = StpUtil.getLoginIdAsLong();
    String platformUserId = oauthRepo.getPlatformUserId(userId, scene.getPlatform());

    // 4. 调用平台API创建审批实例
    String platformInstanceId = platformAdapter.createApproval(scene, formData, platformUserId);

    // 5. 保存审批实例记录
    ApprovalInstance instance = new ApprovalInstance();
    instance.setSceneCode(sceneCode);
    instance.setBusinessType(extractBusinessType(sceneCode));
    instance.setBusinessId(businessId);
    instance.setInitiatorId(userId);
    instance.setPlatform(scene.getPlatform());
    instance.setPlatformInstanceId(platformInstanceId);
    instance.setFormData(formData);
    instanceRepo.insert(instance);
}
```

### 4.2 审批回调处理

```java
// 回调入口
@PostMapping("/callback/{platform}")
public String handleCallback(@PathVariable String platform,
                             @RequestBody String body,
                             @RequestHeader Map<String, String> headers) {
    return approvalCallbackService.handle(platform, body, headers);
}

// 回调处理逻辑
public String handle(String platform, String body, Map<String, String> headers) {
    // 1. 验证签名并解析回调数据
    CallbackData data = platformAdapter.parseCallback(platform, body, headers);

    // 2. 查找审批实例
    ApprovalInstance instance = instanceRepo.findByPlatformInstanceId(data.getInstanceId());
    if (instance == null) {
        return "success"; // 忽略未知实例
    }

    // 3. 更新审批状态
    instance.setStatus(data.getStatus()); // APPROVED / REJECTED
    instance.setResultTime(LocalDateTime.now());
    instanceRepo.update(instance);

    // 4. 获取场景配置的处理器并执行
    ApprovalScene scene = sceneRepo.findByCode(instance.getSceneCode());
    ApprovalHandler handler = springContext.getBean(scene.getHandlerClass());

    if ("APPROVED".equals(data.getStatus())) {
        handler.onApproved(instance);
    } else {
        handler.onRejected(instance);
    }

    return "success";
}
```

### 4.3 业务处理器接口

```java
public interface ApprovalHandler {
    /** 审批通过 */
    void onApproved(ApprovalInstance instance);

    /** 审批拒绝 */
    void onRejected(ApprovalInstance instance);
}

// 示例：订单审批处理器
@Component("orderApprovalHandler")
public class OrderApprovalHandler implements ApprovalHandler {
    @Override
    public void onApproved(ApprovalInstance instance) {
        // 更新订单状态为"已确认"
        orderService.confirm(instance.getBusinessId());
        // 发送通知给相关人员
        notificationService.send("ORDER_CONFIRMED", instance.getBusinessId());
    }

    @Override
    public void onRejected(ApprovalInstance instance) {
        // 更新订单状态为"已拒绝"
        orderService.reject(instance.getBusinessId());
    }
}
```

## 5. 消息推送模块

### 5.1 消息推送服务

```java
// 调用示例
notificationService.send("ORDER_CONFIRMED", orderId, receiverUserIds);

// NotificationService 核心逻辑
public void send(String templateCode, Long businessId, List<Long> receiverIds) {
    // 1. 获取消息模板
    MessageTemplate template = templateRepo.findByCode(templateCode);
    if (template == null) {
        log.warn("消息模板不存在: {}", templateCode);
        return;
    }

    // 2. 构建模板变量
    Map<String, Object> variables = buildVariables(businessId);

    // 3. 渲染消息内容
    String title = renderTemplate(template.getTitle(), variables);
    String content = renderTemplate(template.getContent(), variables);

    // 4. 逐个接收人发送
    for (Long receiverId : receiverIds) {
        sendToUser(template, receiverId, title, content);
    }
}
```

### 5.2 消息模板示例

```
模板编码: ORDER_CONFIRMED
标题: 订单审批通过通知
内容:
您的订单 ${orderNo} 已审批通过。
客户：${customerName}
金额：${amount} 元
请及时处理。
```

### 5.3 使用场景对照

| 场景 | 模板编码 | 触发时机 |
|------|---------|---------|
| 审批发起 | APPROVAL_SUBMIT | 提交审批时通知审批人 |
| 审批通过 | APPROVAL_PASSED | 审批通过通知发起人 |
| 审批拒绝 | APPROVAL_REJECTED | 审批拒绝通知发起人 |
| 订单确认 | ORDER_CONFIRMED | 订单审批通过后 |
| 系统告警 | SYSTEM_ALERT | 监控检测到异常 |

## 6. IM 平台适配层

### 6.1 适配器接口

```java
public interface ImPlatformAdapter {
    /** 获取平台标识 */
    String getPlatform();

    /** 发送消息 */
    void sendMessage(String toUserId, String title, String content);

    /** 创建审批实例 */
    String createApproval(ApprovalScene scene, Map<String, Object> formData, String initiatorId);

    /** 解析回调数据 */
    CallbackData parseCallback(String body, Map<String, String> headers);

    /** 验证回调签名 */
    boolean verifySignature(String body, Map<String, String> headers);
}
```

### 6.2 平台 API 差异对照

| 能力 | 企业微信 | 钉钉 | 飞书 |
|------|---------|------|------|
| 发送消息 | 应用消息 API | 工作通知 API | 消息 API |
| 审批接口 | 审批应用 API | 审批实例 API | 审批定义 API |
| 回调验证 | XML + 签名 | JSON + 签名 | JSON + 签名 |
| 用户标识 | userId | userid | user_id |

### 6.3 适配器工厂

```java
@Component
public class ImPlatformAdapterFactory {

    @Autowired
    private List<ImPlatformAdapter> adapters;

    private Map<String, ImPlatformAdapter> adapterMap;

    @PostConstruct
    public void init() {
        adapterMap = adapters.stream()
            .collect(Collectors.toMap(ImPlatformAdapter::getPlatform, a -> a));
    }

    public ImPlatformAdapter getAdapter(String platform) {
        ImPlatformAdapter adapter = adapterMap.get(platform);
        if (adapter == null) {
            throw new BusinessException("不支持的平台: " + platform);
        }
        return adapter;
    }
}
```

## 7. 后台管理界面

### 7.1 菜单结构

```
系统管理
├── IM 平台配置      # 配置企微/钉钉/飞书
├── 审批场景管理      # 配置审批场景
├── 消息模板管理      # 管理消息模板
└── 消息记录查询      # 查看推送记录

审批中心（新增）
├── 我的审批         # 我发起的审批
└── 待我审批         # 待处理的审批（跳转到企业IM）
```

### 7.2 IM 平台配置页面

| 字段 | 说明 |
|------|------|
| 平台 | 企微/钉钉/飞书 |
| 启用状态 | 开关 |
| 应用 ID | 企业微信的 AgentId |
| 应用密钥 | 加密存储 |
| 企业 ID | CorpId |
| 回调地址 | 自动生成，需配置到企业 IM 后台 |
| 回调 Token | 用于验证回调 |
| 加解密 Key | 回调消息加解密 |

### 7.3 审批场景配置页面

| 字段 | 说明 |
|------|------|
| 场景编码 | ORDER_APPROVAL |
| 场景名称 | 订单审批 |
| 使用平台 | 选择已启用的平台 |
| 审批模板 ID | 平台上的审批模板 ID |
| 处理器类名 | OrderApprovalHandler |
| 是否启用 | 开关 |
| 超时时间 | 默认 72 小时 |
| 超时动作 | 自动通过/自动拒绝/通知 |

## 8. 实施路线图

### 8.1 分阶段实施

| 阶段 | 内容 | 预计工作量 |
|------|------|-----------|
| **Phase 1: 基础设施** | 数据库表 + IM 配置管理 + 平台适配层框架 | 2-3 天 |
| **Phase 2: 消息推送** | 消息模板 + 推送服务 + 企微适配器 | 2 天 |
| **Phase 3: 审批流** | 审批场景 + 审批服务 + 回调处理 | 3 天 |
| **Phase 4: 扩展平台** | 钉钉适配器 + 飞书适配器 | 各 1-2 天 |
| **Phase 5: 管理界面** | 后台配置页面 | 2 天 |

### 8.2 优先级

```
Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 5
                │
                └──► Phase 4 (并行或后续)
```

### 8.3 最小可用版本（MVP）

先实现：企微 + 消息推送 + 审批流核心

后续迭代：钉钉/飞书适配器、高级功能
