package com.devlovecode.aiperm.modules.agent.tool;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.modules.agent.dto.ToolResult;
import com.devlovecode.aiperm.modules.system.dto.DeptDTO;
import com.devlovecode.aiperm.modules.system.service.DeptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 部门创建工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeptCreateAgentTool implements AgentTool {

    private final ToolRegistry toolRegistry;
    private final DeptService deptService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        toolRegistry.register(this);
    }

    @Override
    public String getName() {
        return "dept_create";
    }

    @Override
    public String getDescription() {
        return "创建部门。参数: deptName(必填), parentId(可选,默认0), sort(可选), leader(可选), phone(可选), email(可选), status(可选,0正常1停用), remark(可选)";
    }

    @Override
    public String getParameterSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "deptName": { "type": "string", "description": "部门名称" },
                "parentId": { "type": "integer", "description": "父部门ID，默认0" },
                "sort": { "type": "integer", "description": "排序，默认0" },
                "leader": { "type": "string", "description": "负责人" },
                "phone": { "type": "string", "description": "联系电话" },
                "email": { "type": "string", "description": "邮箱" },
                "status": { "type": "integer", "description": "状态: 0正常, 1停用" },
                "remark": { "type": "string", "description": "备注" }
              },
              "required": ["deptName"]
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
            StpUtil.checkPermission("system:dept:create");
            @SuppressWarnings("unchecked")
            Map<String, Object> args = objectMapper.readValue(argsJson, Map.class);

            DeptDTO dto = new DeptDTO();
            dto.setDeptName((String) args.get("deptName"));
            dto.setParentId(args.get("parentId") != null ? ((Number) args.get("parentId")).longValue() : 0L);
            dto.setSort(args.get("sort") != null ? ((Number) args.get("sort")).intValue() : 0);
            dto.setLeader((String) args.get("leader"));
            dto.setPhone((String) args.get("phone"));
            dto.setEmail((String) args.get("email"));
            dto.setStatus(args.get("status") != null ? ((Number) args.get("status")).intValue() : 0);
            dto.setRemark((String) args.get("remark"));

            deptService.create(dto);
            return ToolResult.success("部门创建成功", Map.of("deptName", dto.getDeptName()));
        } catch (Exception e) {
            log.error("DeptCreateAgentTool execute failed", e);
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }
}
