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

import java.util.List;
import java.util.Map;

/**
 * 用户创建工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreateAgentTool implements AgentTool {

    private final ToolRegistry toolRegistry;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        toolRegistry.register(this);
    }

    @Override
    public String getName() {
        return "user_create";
    }

    @Override
    public String getDescription() {
        return "创建用户。参数: username(必填), password(必填), nickname(可选), realName(可选), phone(可选), email(可选), deptId(可选), postId(可选), roleIds(可选), status(可选,0正常1停用), remark(可选)";
    }

    @Override
    public String getParameterSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "username": { "type": "string", "description": "用户名" },
                "password": { "type": "string", "description": "密码，至少6位" },
                "nickname": { "type": "string", "description": "昵称" },
                "realName": { "type": "string", "description": "真实姓名" },
                "phone": { "type": "string", "description": "手机号" },
                "email": { "type": "string", "description": "邮箱" },
                "deptId": { "type": "integer", "description": "部门ID" },
                "postId": { "type": "integer", "description": "岗位ID" },
                "roleIds": {
                  "type": "array",
                  "items": { "type": "integer" },
                  "description": "角色ID列表"
                },
                "status": { "type": "integer", "description": "状态: 0正常, 1停用" },
                "remark": { "type": "string", "description": "备注" }
              },
              "required": ["username", "password"]
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
            StpUtil.checkPermission("system:user:create");
            @SuppressWarnings("unchecked")
            Map<String, Object> args = objectMapper.readValue(argsJson, Map.class);

            UserDTO dto = new UserDTO();
            dto.setUsername((String) args.get("username"));
            dto.setPassword((String) args.get("password"));
            dto.setNickname((String) args.get("nickname"));
            dto.setRealName((String) args.get("realName"));
            dto.setPhone((String) args.get("phone"));
            dto.setEmail((String) args.get("email"));
            dto.setDeptId(args.get("deptId") != null ? ((Number) args.get("deptId")).longValue() : null);
            dto.setPostId(args.get("postId") != null ? ((Number) args.get("postId")).longValue() : null);
            dto.setStatus(args.get("status") != null ? ((Number) args.get("status")).intValue() : 0);
            dto.setRemark((String) args.get("remark"));

            Object roleIdsObj = args.get("roleIds");
            if (roleIdsObj instanceof List<?> rawList) {
                List<Long> roleIds = rawList.stream()
                        .filter(Number.class::isInstance)
                        .map(Number.class::cast)
                        .map(Number::longValue)
                        .toList();
                dto.setRoleIds(roleIds);
            }

            userService.create(dto);
            return ToolResult.success("用户创建成功", Map.of("username", dto.getUsername()));
        } catch (Exception e) {
            log.error("UserCreateAgentTool execute failed", e);
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }
}
