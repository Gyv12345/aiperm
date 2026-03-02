package com.devlovecode.aiperm.modules.agent.tool;

import cn.dev33.satoken.stp.StpUtil;
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
 * 角色创建工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleCreateAgentTool implements AgentTool {

    private final ToolRegistry toolRegistry;
    private final RoleService roleService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        toolRegistry.register(this);
    }

    @Override
    public String getName() {
        return "role_create";
    }

    @Override
    public String getDescription() {
        return "创建角色。参数: roleName(必填), roleCode(必填), sort(可选), status(可选,0正常1停用), remark(可选)";
    }

    @Override
    public String getParameterSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "roleName": { "type": "string", "description": "角色名称" },
                "roleCode": { "type": "string", "description": "角色编码" },
                "sort": { "type": "integer", "description": "排序，默认0" },
                "status": { "type": "integer", "description": "状态: 0正常, 1停用" },
                "remark": { "type": "string", "description": "备注" }
              },
              "required": ["roleName", "roleCode"]
            }
            """;
    }

    @Override
    public boolean isSensitive() {
        return true;
    }

    @Override
    public ToolResult execute(String argsJson, Long userId) {
        try {
            StpUtil.checkPermission("system:role:create");
            @SuppressWarnings("unchecked")
            Map<String, Object> args = objectMapper.readValue(argsJson, Map.class);

            RoleDTO dto = new RoleDTO();
            dto.setRoleName((String) args.get("roleName"));
            dto.setRoleCode((String) args.get("roleCode"));
            dto.setSort(args.get("sort") != null ? ((Number) args.get("sort")).intValue() : 0);
            dto.setStatus(args.get("status") != null ? ((Number) args.get("status")).intValue() : 0);
            dto.setRemark((String) args.get("remark"));

            roleService.create(dto);
            return ToolResult.success("角色创建成功", Map.of("roleName", dto.getRoleName(), "roleCode", dto.getRoleCode()));
        } catch (Exception e) {
            log.error("RoleCreateAgentTool execute failed", e);
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }
}
