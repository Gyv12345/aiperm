package com.devlovecode.aiperm.modules.mcp.tool;

import com.devlovecode.aiperm.modules.mcp.toon.ToonEncoder;

import java.util.List;
import java.util.Map;

/**
 * MCP 工具基类
 * 使用 TOON 格式返回数据，节省 30-60% token
 *
 * @author DevLoveCode
 */
public abstract class BaseMcpTool {

    /**
     * 返回 TOON 格式错误
     */
    protected String error(String message) {
        return ToonEncoder.encodeError(message);
    }

    /**
     * 返回 TOON 格式对象
     */
    protected String toToon(Map<String, Object> map) {
        return ToonEncoder.encode(map);
    }

    /**
     * 返回 TOON 格式分页结果
     */
    protected String toToonPage(long total, long pageNum, long pageSize, List<Map<String, Object>> list) {
        return ToonEncoder.encodePage(total, pageNum, pageSize, list);
    }

    /**
     * 返回 TOON 格式列表
     */
    protected String toToonList(String key, List<Map<String, Object>> list) {
        return ToonEncoder.encodeList(key, list);
    }
}
