package com.devlovecode.aiperm.modules.agent.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.agent.service.AgentConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Agent配置管理")
@RestController
@RequestMapping("/agent/config")
@SaCheckLogin
@RequiredArgsConstructor
public class AgentConfigController {

    private final AgentConfigService configService;

    @Operation(summary = "获取所有配置")
    @SaCheckPermission("agent:config:query")
    @GetMapping
    public R<Map<String, String>> getAll() {
        return R.ok(configService.getAllConfigs());
    }

    @Operation(summary = "更新配置")
    @SaCheckPermission("agent:config:update")
    @PutMapping("/{key}")
    public R<Void> update(@PathVariable String key, @RequestBody Map<String, String> body) {
        configService.updateConfig(key, body.get("value"));
        return R.ok();
    }

    @Operation(summary = "获取缓存统计")
    @SaCheckPermission("agent:config:query")
    @GetMapping("/cache/stats")
    public R<Map<String, Integer>> getCacheStats() {
        return R.ok(Map.of("count", configService.getCacheCount()));
    }
}
