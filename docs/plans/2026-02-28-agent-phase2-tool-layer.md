# AI Agent Phase 2: 工具层

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 创建 AgentTool 接口、ToolRegistry 注册中心和具体业务工具

**Architecture:** 工具接口定义统一规范，ToolRegistry 管理白名单和工具元数据，具体工具调用业务 Service

**Tech Stack:** Spring Boot 3.5 + JSON Schema

---

## Task 1: 创建工具相关 DTO/VO

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/ToolResult.java`

**Step 1: 创建 ToolResult**

```java
package com.devlovecode.aiperm.modules.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {
    private boolean success;
    private String message;
    private Object data;

    public static ToolResult success(String message) {
        return new ToolResult(true, message, null);
    }

    public static ToolResult success(String message, Object data) {
        return new ToolResult(true, message, data);
    }

    public static ToolResult error(String message) {
        return new ToolResult(false, message, null);
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/ToolResult.java
git commit -m "feat(agent): add ToolResult DTO"
```

---

## Task 2: 创建 AgentTool 接口

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/AgentTool.java`

**Step 1: 创建 AgentTool 接口**

```java
package com.devlovecode.aiperm.modules.agent.tool;

import com.devlovecode.aiperm.modules.agent.dto.ToolResult;

/**
 * Agent 工具接口
 * 所有 Agent 可调用的工具都需要实现此接口
 */
public interface AgentTool {

    /**
     * 工具名称 (唯一标识)
     * 格式: {模块}_{操作}，如 role_create, user_list
     */
    String getName();

    /**
     * 工具描述 (供 LLM 理解用途)
     */
    String getDescription();

    /**
     * 参数 JSON Schema (供 LLM 生成参数)
     * 使用 JSON Schema 格式描述参数结构
     */
    String getParameterSchema();

    /**
     * 是否为敏感操作
     * 敏感操作需要用户二次确认
     */
    boolean isSensitive();

    /**
     * 执行工具
     *
     * @param argsJson 参数 JSON 字符串
     * @param userId   当前用户 ID
     * @return 执行结果
     */
    ToolResult execute(String argsJson, Long userId);
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/AgentTool.java
git commit -m "feat(agent): add AgentTool interface"
```

---

## Task 3: 创建 ToolRegistry

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/ToolRegistry.java`

**Step 1: 创建 ToolRegistry**

```java
package com.devlovecode.aiperm.modules.agent.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 工具注册中心
 * 管理所有可用工具，支持白名单配置
 */
@Slf4j
@Component
public class ToolRegistry {

    private final Map<String, AgentTool> tools = new HashMap<>();
    private final Set<String> whitelist = new HashSet<>();
    private final ObjectMapper objectMapper;

    public ToolRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 注册工具
     */
    public void register(AgentTool tool) {
        String name = tool.getName();
        if (tools.containsKey(name)) {
            log.warn("Tool already registered: {}", name);
            return;
        }
        tools.put(name, tool);
        whitelist.add(name); // 默认加入白名单
        log.info("Registered agent tool: {} (sensitive={})", name, tool.isSensitive());
    }

    /**
     * 获取工具
     */
    public Optional<AgentTool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    /**
     * 检查工具是否在白名单中
     */
    public boolean isAllowed(String name) {
        return whitelist.contains(name);
    }

    /**
     * 从白名单移除工具
     */
    public void removeFromWhitelist(String name) {
        whitelist.remove(name);
        log.info("Removed tool from whitelist: {}", name);
    }

    /**
     * 获取所有工具描述 (供 LLM 使用)
     */
    public String getToolsDescription() {
        StringBuilder sb = new StringBuilder();
        for (AgentTool tool : tools.values()) {
            if (whitelist.contains(tool.getName())) {
                sb.append("- ").append(tool.getName()).append(": ").append(tool.getDescription());
                if (tool.isSensitive()) {
                    sb.append(" [需确认]");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 获取所有工具的 JSON Schema (供 LLM Function Calling)
     */
    public List<Map<String, Object>> getToolsSchema() {
        List<Map<String, Object>> schemas = new ArrayList<>();
        for (AgentTool tool : tools.values()) {
            if (whitelist.contains(tool.getName())) {
                Map<String, Object> schema = new LinkedHashMap<>();
                schema.put("type", "function");
                schema.put("function", Map.of(
                    "name", tool.getName(),
                    "description", tool.getDescription(),
                    "parameters", parseSchema(tool.getParameterSchema())
                ));
                schemas.add(schema);
            }
        }
        return schemas;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseSchema(String schemaJson) {
        try {
            return objectMapper.readValue(schemaJson, Map.class);
        } catch (Exception e) {
            log.error("Failed to parse schema: {}", schemaJson, e);
            return Map.of("type", "object");
        }
    }

    /**
     * 获取所有已注册的工具名称
     */
    public Set<String> getAllToolNames() {
        return Collections.unmodifiableSet(tools.keySet());
    }

    /**
     * 获取白名单中的工具名称
     */
    public Set<String> getWhitelistedToolNames() {
        return Collections.unmodifiableSet(whitelist);
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/ToolRegistry.java
git commit -m "feat(agent): add ToolRegistry for tool management"
```

---

## Task 4: 创建 RoleAgentTool

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/RoleAgentTool.java`

**Step 1: 创建 RoleAgentTool**

```java
package com.devlovecode.aiperm.modules.agent.tool;

import com.devlovecode.aiperm.modules.agent.dto.ToolResult;
import com.devlovecode.aiperm.modules.system.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * 角色管理工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleAgentTool implements AgentTool {

    private final ToolRegistry toolRegistry;
    private final RoleService roleService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        toolRegistry.register(this);
    }

    @Override
    public String getName() {
        return "role_list";
    }

    @Override
    public String getDescription() {
        return "查询角色列表，支持按角色名称模糊搜索。参数: name(可选,角色名称), pageNum(可选,页码,默认1), pageSize(可选,每页条数,默认10)";
    }

    @Override
    public String getParameterSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "name": {
                  "type": "string",
                  "description": "角色名称，支持模糊搜索"
                },
                "pageNum": {
                  "type": "integer",
                  "description": "页码，默认1",
                  "default": 1
                },
                "pageSize": {
                  "type": "integer",
                  "description": "每页条数，默认10",
                  "default": 10
                }
              }
            }
            """;
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

    @Override
    public ToolResult execute(String argsJson, Long userId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> args = objectMapper.readValue(argsJson, Map.class);

            String name = (String) args.get("name");
            int pageNum = args.containsKey("pageNum") ? ((Number) args.get("pageNum")).intValue() : 1;
            int pageSize = args.containsKey("pageSize") ? ((Number) args.get("pageSize")).intValue() : 10;

            // 调用 RoleService，权限校验由 Sa-Token 自动完成
            var result = roleService.queryPage(name, null, pageNum, pageSize);

            return ToolResult.success("查询成功", result);
        } catch (Exception e) {
            log.error("RoleAgentTool execute failed", e);
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/RoleAgentTool.java
git commit -m "feat(agent): add RoleAgentTool for role management"
```

---

## Task 5: 创建 UserAgentTool

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/UserAgentTool.java`

**Step 1: 创建 UserAgentTool**

```java
package com.devlovecode.aiperm.modules.agent.tool;

import com.devlovecode.aiperm.modules.agent.dto.ToolResult;
import com.devlovecode.aiperm.modules.system.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * 用户管理工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAgentTool implements AgentTool {

    private final ToolRegistry toolRegistry;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        toolRegistry.register(this);
    }

    @Override
    public String getName() {
        return "user_list";
    }

    @Override
    public String getDescription() {
        return "查询用户列表，支持按用户名、手机号、状态搜索。参数: username(可选), phone(可选), status(可选,0正常1停用), pageNum(可选), pageSize(可选)";
    }

    @Override
    public String getParameterSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "username": {
                  "type": "string",
                  "description": "用户名，支持模糊搜索"
                },
                "phone": {
                  "type": "string",
                  "description": "手机号"
                },
                "status": {
                  "type": "integer",
                  "description": "状态: 0正常, 1停用"
                },
                "pageNum": {
                  "type": "integer",
                  "description": "页码，默认1",
                  "default": 1
                },
                "pageSize": {
                  "type": "integer",
                  "description": "每页条数，默认10",
                  "default": 10
                }
              }
            }
            """;
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

    @Override
    public ToolResult execute(String argsJson, Long userId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> args = objectMapper.readValue(argsJson, Map.class);

            String username = (String) args.get("username");
            String phone = (String) args.get("phone");
            Integer status = args.get("status") != null ? ((Number) args.get("status")).intValue() : null;
            int pageNum = args.containsKey("pageNum") ? ((Number) args.get("pageNum")).intValue() : 1;
            int pageSize = args.containsKey("pageSize") ? ((Number) args.get("pageSize")).intValue() : 10;

            var result = userService.queryPage(username, phone, status, pageNum, pageSize);

            return ToolResult.success("查询成功", result);
        } catch (Exception e) {
            log.error("UserAgentTool execute failed", e);
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/UserAgentTool.java
git commit -m "feat(agent): add UserAgentTool for user management"
```

---

## Task 6: 创建 DeptAgentTool

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/DeptAgentTool.java`

**Step 1: 创建 DeptAgentTool**

```java
package com.devlovecode.aiperm.modules.agent.tool;

import com.devlovecode.aiperm.modules.agent.dto.ToolResult;
import com.devlovecode.aiperm.modules.system.service.DeptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * 部门管理工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeptAgentTool implements AgentTool {

    private final ToolRegistry toolRegistry;
    private final DeptService deptService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        toolRegistry.register(this);
    }

    @Override
    public String getName() {
        return "dept_list";
    }

    @Override
    public String getDescription() {
        return "查询部门列表，返回树形结构。参数: deptName(可选,部门名称), status(可选,状态)";
    }

    @Override
    public String getParameterSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "deptName": {
                  "type": "string",
                  "description": "部门名称，支持模糊搜索"
                },
                "status": {
                  "type": "integer",
                  "description": "状态: 0正常, 1停用"
                }
              }
            }
            """;
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

    @Override
    public ToolResult execute(String argsJson, Long userId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> args = objectMapper.readValue(argsJson, Map.class);

            String deptName = (String) args.get("deptName");
            Integer status = args.get("status") != null ? ((Number) args.get("status")).intValue() : null;

            var result = deptService.queryList(deptName, status);

            return ToolResult.success("查询成功", result);
        } catch (Exception e) {
            log.error("DeptAgentTool execute failed", e);
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/DeptAgentTool.java
git commit -m "feat(agent): add DeptAgentTool for department management"
```

---

## Task 7: 创建 MenuAgentTool

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/MenuAgentTool.java`

**Step 1: 创建 MenuAgentTool**

```java
package com.devlovecode.aiperm.modules.agent.tool;

import com.devlovecode.aiperm.modules.agent.dto.ToolResult;
import com.devlovecode.aiperm.modules.system.service.MenuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * 菜单管理工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuAgentTool implements AgentTool {

    private final ToolRegistry toolRegistry;
    private final MenuService menuService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        toolRegistry.register(this);
    }

    @Override
    public String getName() {
        return "menu_list";
    }

    @Override
    public String getDescription() {
        return "查询菜单列表，返回树形结构。参数: menuName(可选,菜单名称), status(可选,状态)";
    }

    @Override
    public String getParameterSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "menuName": {
                  "type": "string",
                  "description": "菜单名称，支持模糊搜索"
                },
                "status": {
                  "type": "integer",
                  "description": "状态: 0正常, 1停用"
                }
              }
            }
            """;
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

    @Override
    public ToolResult execute(String argsJson, Long userId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> args = objectMapper.readValue(argsJson, Map.class);

            String menuName = (String) args.get("menuName");
            Integer status = args.get("status") != null ? ((Number) args.get("status")).intValue() : null;

            var result = menuService.queryList(menuName, status);

            return ToolResult.success("查询成功", result);
        } catch (Exception e) {
            log.error("MenuAgentTool execute failed", e);
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/tool/MenuAgentTool.java
git commit -m "feat(agent): add MenuAgentTool for menu management"
```

---

## Task 8: 编译验证

**Step 1: 编译后端**

```bash
cd backend && ./gradlew build -x test
```

Expected: BUILD SUCCESSFUL

**Step 2: 修复编译错误（如有）**

---

## Completion Checklist

- [ ] ToolResult DTO 已创建
- [ ] AgentTool 接口已创建
- [ ] ToolRegistry 已创建
- [ ] RoleAgentTool 已创建
- [ ] UserAgentTool 已创建
- [ ] DeptAgentTool 已创建
- [ ] MenuAgentTool 已创建
- [ ] 编译通过

---

## Next Phase

继续执行 Phase 3: 服务层实现
