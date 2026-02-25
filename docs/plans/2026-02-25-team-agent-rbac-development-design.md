# Team Agent RBAC 全栈开发设计方案

> 创建日期：2026-02-25
> 状态：已批准

## 一、项目背景

aiperm 是一个 RBAC 权限管理系统，采用前后端分离架构。当前系统已有基础框架，但缺少完整的业务功能。本项目目标是使用 Team Agent 并行开发完整的 RBAC 功能模块。

## 二、开发阶段

### 阶段 0：登录认证（前置依赖）

**必须先完成**，否则无法进入系统。

| 功能 | 后端 | 前端 |
|------|------|------|
| 登录 | `AuthController.login()` | 对接真实 API |
| 验证码 | `AuthController.captcha()` | 验证码图片组件 |
| 登出 | `AuthController.logout()` | 清除 token |
| 用户信息 | `AuthController.userInfo()` | 获取当前用户 |

**API 设计：**
```
POST /auth/login      → { token, userInfo }
GET  /auth/captcha    → { captchaKey, captchaImage }
POST /auth/logout     → { }
GET  /auth/user-info  → { id, username, roles, permissions }
```

### 阶段 1：Team Agent 并行开发

启动 5 个 Agent 并行开发：

| Agent | 负责模块 | 后端 | 前端 |
|-------|---------|------|------|
| Agent 1 | 部门 + 岗位 + 组织架构 | 3 个 | 3 个 |
| Agent 2 | 用户 + 角色管理 | 0 | 2 个 |
| Agent 3 | 权限 + 菜单 + 字典 | 0 | 3 个 |
| Agent 4 | 公告通知 + 消息中心 | 2 个 | 2 个 |
| Agent 5 | 定时任务 + 系统配置 | 2 个 | 2 个 |

## 三、模块详细设计

### 3.1 Agent 1：组织架构模块

**数据表：**
```sql
-- 部门表
CREATE TABLE sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    name VARCHAR(50) NOT NULL COMMENT '部门名称',
    code VARCHAR(50) COMMENT '部门编码',
    sort INT DEFAULT 0 COMMENT '排序',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    status INT DEFAULT 1 COMMENT '状态(0禁用 1启用)',
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 岗位表
CREATE TABLE sys_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_id BIGINT COMMENT '部门ID',
    name VARCHAR(50) NOT NULL COMMENT '岗位名称',
    code VARCHAR(50) COMMENT '岗位编码',
    sort INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态(0禁用 1启用)',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**前端页面：**
- `views/system/dept/index.vue` - 部门管理（树形表格）
- `views/system/post/index.vue` - 岗位管理
- `views/system/org/index.vue` - 组织架构图（可视化）

### 3.2 Agent 2：用户角色管理（前端）

**前端页面：**
- `views/system/user/index.vue` - 用户管理
  - 列表、新增、编辑、删除
  - 分配角色、重置密码
- `views/system/role/index.vue` - 角色管理
  - 列表、新增、编辑、删除
  - 分配权限

### 3.3 Agent 3：权限菜单字典（前端）

**前端页面：**
- `views/system/permission/index.vue` - 权限管理（树形）
- `views/system/menu/index.vue` - 菜单管理（树形）
- `views/system/dict/index.vue` - 字典管理（双表联动）

### 3.4 Agent 4：公告消息模块

**数据表：**
```sql
-- 公告表
CREATE TABLE sys_notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type INT DEFAULT 1 COMMENT '类型(1通知 2公告)',
    status INT DEFAULT 1 COMMENT '状态(0关闭 1发布)',
    publish_time DATETIME COMMENT '发布时间',
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 消息表
CREATE TABLE sys_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT COMMENT '发送人ID',
    receiver_id BIGINT COMMENT '接收人ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    is_read INT DEFAULT 0 COMMENT '是否已读(0未读 1已读)',
    read_time DATETIME COMMENT '阅读时间',
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**前端页面：**
- `views/enterprise/notice/index.vue` - 公告通知
- `views/enterprise/message/index.vue` - 消息中心

### 3.5 Agent 5：系统配置模块

**数据表：**
```sql
-- 定时任务
CREATE TABLE sys_job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(50) COMMENT '任务分组',
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    bean_class VARCHAR(200) COMMENT '执行类',
    status INT DEFAULT 1 COMMENT '状态(0暂停 1运行)',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统配置
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(50) COMMENT '配置类型',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**前端页面：**
- `views/enterprise/job/index.vue` - 定时任务
- `views/enterprise/config/index.vue` - 系统配置

## 四、技术规范

### 后端规范
- 所有 Controller 加 `@SaCheckLogin`
- 写操作加 `@Log` + `@SaCheckPermission`
- 使用 JdbcClient 替代 MyBatis-Plus
- 遵循 Entity → Repository → Service → Controller 分层

### 前端规范
- 禁止手写 API，必须使用 Orval 生成
- 使用 Vue 3 Composition API + script setup
- 使用 Element Plus 组件
- 遵循项目目录结构约定

## 五、执行计划

1. **阶段 0**：完成登录认证（主会话）
2. **阶段 1**：启动 Team Agent 并行开发
   - 创建 Team 和任务列表
   - 启动 5 个 Agent
   - 监控进度，协调依赖

## 六、验收标准

- [ ] 登录功能正常（含验证码）
- [ ] 所有模块后端 API 可用
- [ ] 所有模块前端页面可访问
- [ ] 权限控制正常工作
- [ ] 操作日志正常记录
