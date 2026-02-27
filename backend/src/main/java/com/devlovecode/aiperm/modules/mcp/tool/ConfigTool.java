package com.devlovecode.aiperm.modules.mcp.tool;

import com.devlovecode.aiperm.modules.enterprise.dto.ConfigDTO;
import com.devlovecode.aiperm.modules.enterprise.service.ConfigService;
import com.devlovecode.aiperm.modules.enterprise.vo.ConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 参数配置 MCP 工具
 *
 * @author DevLoveCode
 */
@Component
@RequiredArgsConstructor
public class ConfigTool extends BaseMcpTool {

    private final ConfigService configService;

    @Tool(description = "根据ID查询参数配置详情")
    public String getConfigById(@ToolParam(description = "配置ID") Long configId) {
        try {
            return toToon(toMap(configService.findById(configId)));
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "根据配置键查询参数值")
    public String getConfigByKey(@ToolParam(description = "配置键") String configKey) {
        try {
            return toToon(toMap(configService.findByConfigKey(configKey)));
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "创建新的参数配置")
    public String createConfig(
            @ToolParam(description = "配置键") String configKey,
            @ToolParam(description = "配置值") String configValue,
            @ToolParam(description = "配置类型") String configType,
            @ToolParam(description = "备注") String remark) {
        try {
            ConfigDTO dto = new ConfigDTO();
            dto.setConfigKey(configKey);
            dto.setConfigValue(configValue);
            dto.setConfigType(configType);
            dto.setRemark(remark);

            Long id = configService.create(dto);
            return "id: " + id + "\nconfigKey: " + configKey;
        } catch (Exception e) {
            return error("创建失败: " + e.getMessage());
        }
    }

    @Tool(description = "更新参数配置")
    public String updateConfig(
            @ToolParam(description = "配置ID") Long configId,
            @ToolParam(description = "配置键") String configKey,
            @ToolParam(description = "配置值") String configValue,
            @ToolParam(description = "配置类型") String configType,
            @ToolParam(description = "备注") String remark) {
        try {
            ConfigDTO dto = new ConfigDTO();
            dto.setConfigKey(configKey);
            dto.setConfigValue(configValue);
            dto.setConfigType(configType);
            dto.setRemark(remark);

            configService.update(configId, dto);
            return "ok: true";
        } catch (Exception e) {
            return error("更新失败: " + e.getMessage());
        }
    }

    @Tool(description = "删除参数配置")
    public String deleteConfig(@ToolParam(description = "配置ID") Long configId) {
        try {
            configService.delete(configId);
            return "ok: true";
        } catch (Exception e) {
            return error("删除失败: " + e.getMessage());
        }
    }

    private Map<String, Object> toMap(ConfigVO config) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", config.getId());
        map.put("configKey", config.getConfigKey());
        map.put("configValue", config.getConfigValue());
        map.put("configType", config.getConfigType());
        map.put("remark", config.getRemark());
        map.put("createTime", config.getCreateTime());
        return map;
    }
}
