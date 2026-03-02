package com.devlovecode.aiperm.modules.agent.tool;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.modules.agent.dto.ToolResult;
import com.devlovecode.aiperm.modules.system.dto.UserDTO;
import com.devlovecode.aiperm.modules.system.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
            StpUtil.checkPermission("system:user:list");
            @SuppressWarnings("unchecked")
            Map<String, Object> args = objectMapper.readValue(argsJson, Map.class);

            UserDTO dto = new UserDTO();
            dto.setUsername((String) args.get("username"));
            dto.setPhone((String) args.get("phone"));
            dto.setStatus(args.get("status") != null ? ((Number) args.get("status")).intValue() : null);
            dto.setPage(args.containsKey("pageNum") ? ((Number) args.get("pageNum")).intValue() : 1);
            dto.setPageSize(args.containsKey("pageSize") ? ((Number) args.get("pageSize")).intValue() : 10);

            var result = userService.queryPage(dto);
            return ToolResult.success("查询成功", result);
        } catch (Exception e) {
            log.error("UserAgentTool execute failed", e);
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }
}
