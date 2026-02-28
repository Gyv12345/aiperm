package com.devlovecode.aiperm.modules.agent.tool;

import com.devlovecode.aiperm.modules.agent.dto.ToolResult;
import com.devlovecode.aiperm.modules.system.service.DeptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        return "查询部门列表，返回树形结构，包含所有部门层级。无需参数。";
    }

    @Override
    public String getParameterSchema() {
        return """
            {
              "type": "object",
              "properties": {}
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
            var result = deptService.getDeptTree();
            return ToolResult.success("查询成功", result);
        } catch (Exception e) {
            log.error("DeptAgentTool execute failed", e);
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }
}
