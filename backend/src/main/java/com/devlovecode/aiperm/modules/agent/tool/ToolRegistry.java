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

    /**
     * 获取白名单中的工具对象
     */
    public List<AgentTool> getWhitelistedTools() {
        List<AgentTool> result = new ArrayList<>();
        for (String toolName : whitelist) {
            AgentTool tool = tools.get(toolName);
            if (tool != null) {
                result.add(tool);
            }
        }
        return result;
    }
}
