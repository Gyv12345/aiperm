# AI Agent 功能设计文档

> 创建日期：2026-02-28
> 状态：设计完成，待实现

## 一、需求概述

在 aiperm 系统中内置 AI Agent，支持：
1. **Web 端**：悬浮球 + 抽屉式对话面板，全局可用
2. **企业微信**：通过回调接口调用，支持 AI 卡片（用户已有方案）

用户通过自然语言与 Agent 对话，Agent 帮助用户操作系统（创建角色、管理用户等）。

---

## 二、核心决策

| 维度 | 决策 |
|------|------|
| 交互方式 | 自然语言对话 |
| LLM 部署 | 后端代理云服务，统一管理 API Key |
| 权限模式 | 继承用户 Token，直接调用 Service 层 |
| 安全策略 | 白名单 + 敏感操作二次确认 |
| 会话管理 | 短期会话，Redis 存储，30 分钟超时 |
| LLM 提供商 | 多提供商支持，页面配置切换 |
| 前端 UI | 悬浮球 + 抽屉，全局可用 |

---

## 三、整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      前端 / 企业微信                          │
└─────────────────────────┬───────────────────────────────────┘
                          │ 用户消息 + Token
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   AgentController                            │
│  - POST /agent/chat/stream   SSE 流式对话                    │
│  - POST /agent/chat          同步对话 (企微用)                │
│  - POST /agent/confirm       确认敏感操作                     │
│  - GET  /agent/session/{id}  获取会话历史                     │
└─────────────────────────┬───────────────────────────────────┘
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   AgentService                               │
│  - 会话管理 (Redis)                                          │
│  - LLM 调用 (多提供商切换)                                    │
│  - 意图识别 → 工具选择                                        │
│  - 敏感操作拦截 → 返回确认请求                                 │
│  - 语义缓存 (Embedding)                                       │
└─────────────────────────┬───────────────────────────────────┘
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   ToolRegistry                               │
│  - 工具白名单注册                                             │
│  - 工具元数据 (名称、描述、参数、是否敏感)                      │
└─────────────────────────┬───────────────────────────────────┘
                          ▼
┌─────────────────────────────────────────────────────────────┐
│              具体工具 (AgentTool)                             │
│  - RoleAgentTool: 创建/查询角色                               │
│  - UserAgentTool: 查询用户                                    │
│  - DeptAgentTool: 部门管理                                    │
│  - ...                                                       │
└─────────────────────────┬───────────────────────────────────┘
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   业务 Service 层                             │
│  (RoleService, UserService, DeptService...)                  │
│  Sa-Token 权限校验自动生效                                    │
└─────────────────────────────────────────────────────────────┘
```

**核心流程：**
1. 用户发送自然语言 → AgentController
2. AgentService 从 Redis 获取/创建会话
3. 检查语义缓存，命中则直接返回
4. 调用 LLM 识别意图，选择工具
5. 检查白名单 + 敏感操作标记
6. 敏感操作返回确认请求，普通操作直接执行
7. 调用对应 Tool → Service，权限自动校验

---

## 四、数据库设计

### 4.1 Agent 会话表 (sys_agent_session)

```sql
CREATE TABLE IF NOT EXISTS `sys_agent_session` (
    `id`            VARCHAR(64)  NOT NULL COMMENT '会话ID (UUID)',
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
    `channel`       VARCHAR(20)  NOT NULL COMMENT '渠道: WEB/WEWORK',
    `status`        INT          DEFAULT 0 COMMENT '状态: 0活跃, 1已过期',
    `last_active`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_last_active` (`last_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent会话表';
```

### 4.2 Agent 对话记录表 (sys_agent_message)

```sql
CREATE TABLE IF NOT EXISTS `sys_agent_message` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `session_id`    VARCHAR(64)  NOT NULL COMMENT '会话ID',
    `role`          VARCHAR(20)  NOT NULL COMMENT '角色: USER/ASSISTANT/TOOL',
    `content`       TEXT         NOT NULL COMMENT '消息内容',
    `tool_name`     VARCHAR(100) DEFAULT NULL COMMENT '调用的工具名',
    `tool_args`     TEXT         DEFAULT NULL COMMENT '工具参数 (JSON)',
    `need_confirm`  TINYINT      DEFAULT 0 COMMENT '是否需要确认',
    `confirmed`     TINYINT      DEFAULT NULL COMMENT '确认结果: 1同意, 0拒绝',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent对话记录表';
```

### 4.3 LLM 提供商配置表 (sys_llm_provider)

```sql
CREATE TABLE IF NOT EXISTS `sys_llm_provider` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(50)  NOT NULL COMMENT '提供商名称: deepseek/qwen/openai',
    `display_name`  VARCHAR(100) NOT NULL COMMENT '显示名称',
    `api_key`       VARCHAR(255) NOT NULL COMMENT 'API Key (加密存储)',
    `base_url`      VARCHAR(255) DEFAULT NULL COMMENT 'API 地址',
    `model`         VARCHAR(100) NOT NULL COMMENT '模型名称',
    `is_default`    TINYINT      DEFAULT 0 COMMENT '是否默认: 0否, 1是',
    `status`        INT          DEFAULT 0 COMMENT '状态: 0启用, 1停用',
    `sort`          INT          DEFAULT 0 COMMENT '排序',
    `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`       TINYINT      DEFAULT 0,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `create_by`     VARCHAR(50)  DEFAULT NULL,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_by`     VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM提供商配置表';
```

### 4.4 Agent 全局配置表 (sys_agent_config)

```sql
CREATE TABLE IF NOT EXISTS `sys_agent_config` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `config_key`    VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value`  VARCHAR(500) NOT NULL COMMENT '配置值',
    `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent全局配置表';

-- 初始配置数据
INSERT INTO `sys_agent_config` (`config_key`, `config_value`, `remark`) VALUES
('session_timeout', '30', '会话超时时间(分钟)'),
('max_history', '20', '最大对话历史条数'),
('default_provider_id', '1', '默认LLM提供商ID'),
('semantic_cache_enabled', 'true', '是否启用语义缓存'),
('semantic_cache_ttl', '60', '语义缓存 TTL (分钟)'),
('semantic_cache_threshold', '0.95', '语义相似度阈值');
```

### 4.5 语义缓存表 (sys_agent_cache)

```sql
CREATE TABLE IF NOT EXISTS `sys_agent_cache` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID (0表示全局缓存)',
    `question_hash` VARCHAR(64)  NOT NULL COMMENT '问题哈希',
    `question`      VARCHAR(500) NOT NULL COMMENT '原始问题',
    `answer`        TEXT         NOT NULL COMMENT '缓存答案',
    `embedding`     BLOB         DEFAULT NULL COMMENT '向量',
    `hit_count`     INT          DEFAULT 0 COMMENT '命中次数',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_hash` (`user_id`, `question_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语义缓存表';
```

---

## 五、Redis 存储设计

### 5.1 会话存储

```
Key: agent:session:{sessionId}
Value: {
    "userId": 123,
    "channel": "WEB",
    "messages": [
        {"role": "user", "content": "帮我创建角色"},
        {"role": "assistant", "content": "好的，请提供..."}
    ],
    "pendingAction": {...}
}
TTL: 30 分钟 (每次活跃续期)
```

### 5.2 配置缓存

```
Key: agent:config:providers
Value: List<LlmProvider> (所有启用的提供商)
TTL: 10 分钟
```

---

## 六、后端代码结构

```
modules/agent/
├── config/
│   └── AgentProperties.java        # Agent 配置属性
├── controller/
│   └── AgentController.java        # API 入口
├── service/
│   ├── AgentService.java           # 核心逻辑
│   ├── LlmService.java             # LLM 调用封装
│   ├── SessionService.java         # 会话管理
│   ├── SemanticCacheService.java   # 语义缓存
│   └── EmbeddingService.java       # 向量生成
├── tool/
│   ├── AgentTool.java              # 工具接口
│   ├── ToolRegistry.java           # 工具注册中心
│   ├── RoleAgentTool.java          # 角色管理工具
│   ├── UserAgentTool.java          # 用户管理工具
│   ├── DeptAgentTool.java          # 部门管理工具
│   └── ...                         # 其他工具
├── dto/
│   ├── ChatRequest.java            # 对话请求
│   ├── ChatResponse.java           # 对话响应
│   └── ConfirmRequest.java         # 确认请求
├── vo/
│   └── SessionVO.java              # 会话信息
├── entity/
│   ├── SysAgentSession.java        # 会话实体
│   ├── SysAgentMessage.java        # 消息实体
│   ├── SysLlmProvider.java         # LLM 提供商
│   ├── SysAgentConfig.java         # Agent 配置
│   └── SysAgentCache.java          # 语义缓存
└── repository/
    ├── AgentSessionRepository.java
    ├── AgentMessageRepository.java
    ├── LlmProviderRepository.java
    ├── AgentConfigRepository.java
    └── AgentCacheRepository.java
```

---

## 七、核心接口设计

### 7.1 AgentTool 接口

```java
public interface AgentTool {
    /** 工具名称 (唯一标识) */
    String getName();

    /** 工具描述 (供 LLM 理解用途) */
    String getDescription();

    /** 参数 JSON Schema (供 LLM 生成参数) */
    String getParameterSchema();

    /** 是否为敏感操作 */
    boolean isSensitive();

    /** 执行工具 */
    ToolResult execute(String argsJson, Long userId);
}
```

### 7.2 API 接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/agent/chat/stream` | POST | SSE 流式对话 (Web 端) |
| `/agent/chat` | POST | 同步对话 (企微用) |
| `/agent/confirm` | POST | 确认敏感操作 |
| `/agent/session/{id}` | GET | 获取会话历史 |
| `/agent/session` | DELETE | 删除会话 |

### 7.3 SSE 事件格式

```
data: {"type":"text","delta":"你好"}
data: {"type":"text","delta":"，我是"}
data: {"type":"tool_call","name":"role_create","args":{...}}
data: {"type":"confirm","actionId":"xxx","message":"确认创建角色？"}
data: {"type":"tool_result","result":{...}}
data: {"type":"done"}
```

---

## 八、流式响应实现 (Servlet 兼容)

### 8.1 SseEmitter 使用

```java
@PostMapping("/chat/stream")
public SseEmitter chatStream(
        @RequestParam String sessionId,
        @RequestBody ChatRequest request) {

    SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

    CompletableFuture.runAsync(() -> {
        try {
            agentService.chatStream(sessionId, request.getMessage(),
                new StreamCallback() {
                    @Override
                    public void onText(String delta) {
                        emitter.send(SseEmitter.event()
                            .data(new ChatEvent("text", delta)));
                    }

                    @Override
                    public void onConfirm(String actionId, String message) {
                        emitter.send(SseEmitter.event()
                            .data(new ChatEvent("confirm", actionId, message)));
                        emitter.complete();
                    }

                    @Override
                    public void onDone() {
                        emitter.complete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        emitter.completeWithError(e);
                    }
                });
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }, agentTaskExecutor);

    emitter.onTimeout(emitter::complete);
    return emitter;
}
```

### 8.2 线程池配置

```java
@Bean("agentTaskExecutor")
public Executor agentTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("agent-");
    executor.initialize();
    return executor;
}
```

---

## 九、语义缓存设计

### 9.1 缓存层级

```
┌─────────────────────────────────────────────────────┐
│  Level 1: Prompt Caching (提供商侧)                  │
│  - 系统提示词、工具定义等固定内容                      │
│  - DeepSeek/OpenAI 自动缓存                         │
└─────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│  Level 2: 语义缓存 (应用侧)                          │
│  - 相似问题的 Embedding → 缓存答案                    │
│  - 命中时直接返回，不调用 LLM                         │
└─────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│  Level 3: 会话上下文 (应用侧 Redis)                  │
│  - 对话历史                                         │
└─────────────────────────────────────────────────────┘
```

### 9.2 缓存策略

| 场景 | 是否缓存 | 原因 |
|------|----------|------|
| "帮我创建角色" | ❌ | 涉及工具调用，不缓存 |
| "什么是 RBAC" | ✅ | 纯知识问答，可缓存 |
| "列出所有角色" | ⚠️ | 数据可能变化，短 TTL 或不缓存 |

### 9.3 Embedding 服务

- 使用 DeepSeek/OpenAI Embedding API
- 余弦相似度计算
- 阈值：0.95

---

## 十、会话上下文隔离

### 10.1 隔离层级

```
用户 A
├── 会话 A1 (Web 端)     → 独立对话历史
├── 会话 A2 (企微端)     → 独立对话历史

用户 B
└── 会话 B1 (Web 端)     → 独立对话历史
```

### 10.2 安全保障

| 安全点 | 措施 |
|--------|------|
| 会话归属校验 | 每次请求校验 `session.userId == currentUserId` |
| Token 传递 | 前端传 Token，后端解析出 userId |
| 企微场景 | 企微回调带的 userId 映射到系统用户 |

---

## 十一、前端设计

### 11.1 交互设计

- **悬浮球**：固定右下角，紫色渐变，hover 放大
- **抽屉面板**：右侧滑出，宽度 400px
- **打字机效果**：AI 回复逐字显示
- **确认弹窗**：敏感操作需用户确认

### 11.2 组件结构

```
components/agent/
├── AgentFloat.vue       # 悬浮球入口
├── AgentDrawer.vue      # 抽屉式对话面板
├── AgentChat.vue        # 对话内容区
├── AgentInput.vue       # 输入框
├── AgentConfirm.vue     # 敏感操作确认弹窗
└── AgentMessage.vue     # 单条消息组件
```

### 11.3 全局注册

```vue
<!-- App.vue 或 Layout.vue -->
<template>
  <router-view />
  <AgentFloat />
</template>
```

---

## 十二、管理界面

| 菜单 | 功能 |
|------|------|
| **LLM 提供商管理** | 增删改查提供商、配置 API Key、设置默认、启用/停用 |
| **Agent 配置** | 会话超时、历史条数、语义缓存开关等全局参数 |

---

## 十三、实现优先级

| 阶段 | 内容 | 优先级 |
|------|------|--------|
| P0 | 核心对话功能（Controller + Service + Tool） | 高 |
| P0 | 前端悬浮球 + 抽屉 | 高 |
| P1 | LLM 提供商管理界面 | 中 |
| P1 | 敏感操作二次确认 | 中 |
| P2 | 语义缓存 | 低 |
| P2 | 会话历史管理界面 | 低 |

---

## 十四、待办事项

- [ ] 创建 Flyway 迁移脚本
- [ ] 实现 AgentTool 接口和 ToolRegistry
- [ ] 实现 AgentService 核心逻辑
- [ ] 实现 LlmService 多提供商支持
- [ ] 实现 SessionService 会话管理
- [ ] 实现 AgentController
- [ ] 实现前端悬浮球和抽屉组件
- [ ] 实现前端 SSE 流式接收
- [ ] 实现 LLM 提供商管理界面
- [ ] 实现语义缓存（可选）
