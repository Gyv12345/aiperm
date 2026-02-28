package com.devlovecode.aiperm.modules.agent.tool;

import com.devlovecode.aiperm.modules.agent.dto.ToolResult;
import com.devlovecode.aiperm.modules.system.dto.RoleDTO;
import com.devlovecode.aiperm.modules.system.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        return "查询角色列表，支持按角色名称模糊搜索。参数: roleName(可选,角色名称), pageNum(可选,页码,默认1), pageSize(可选,每页条数,默认10)";
    }

    @Override
    public String getParameterSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "roleName": {
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

            RoleDTO dto = new RoleDTO();
            dto.setRoleName((String) args.get("roleName"));
            dto.setPage(args.containsKey("pageNum") ? ((Number) args.get("pageNum")).intValue() : 1);
            dto.setPageSize(args.containsKey("pageSize") ? ((Number) args.get("pageSize")).intValue() : 10);

            var result = roleService.queryPage(dto);
            return ToolResult.success("查询成功", result);
        } catch (Exception e) {
            log.error("RoleAgentTool execute failed", e);
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }
}
