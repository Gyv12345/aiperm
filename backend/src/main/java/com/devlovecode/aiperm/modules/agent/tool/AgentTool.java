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
