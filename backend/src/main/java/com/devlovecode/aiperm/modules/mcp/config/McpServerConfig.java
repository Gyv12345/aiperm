package com.devlovecode.aiperm.modules.mcp.config;

import com.devlovecode.aiperm.modules.mcp.tool.*;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server 配置
 * 注册所有 MCP 工具
 *
 * @author DevLoveCode
 */
@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider menuToolProvider(MenuTool menuTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(menuTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider dictToolProvider(DictTool dictTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(dictTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider roleToolProvider(RoleTool roleTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(roleTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider deptToolProvider(DeptTool deptTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(deptTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider configToolProvider(ConfigTool configTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(configTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider jobToolProvider(JobTool jobTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(jobTool)
                .build();
    }
}
