package com.devlovecode.aiperm.modules.mcp.toon;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TOON (Token-Oriented Object Notation) 编码器
 * 专为 LLM 优化的紧凑数据格式，可减少 30-60% token 使用量
 *
 * @author DevLoveCode
 */
public class ToonEncoder {

    private static final String INDENT = "  ";
    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
            String.class, Integer.class, Long.class, Double.class, Float.class,
            Boolean.class, Short.class, Byte.class, Character.class
    );

    /**
     * 编码简单对象
     * 输出格式：
     * key1: value1
     * key2: value2
     */
    public static String encode(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(formatValue(entry.getValue())).append("\n");
        }
        return sb.toString().stripTrailing();
    }

    /**
     * 编码分页结果
     * 输出格式：
     * total: 100
     * pageNum: 1
     * pageSize: 10
     * list[3]{id,name,status}:
     *   1,Alice,0
     *   2,Bob,1
     */
    public static String encodePage(long total, long pageNum, long pageSize, List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("total: ").append(total).append("\n");
        sb.append("pageNum: ").append(pageNum).append("\n");
        sb.append("pageSize: ").append(pageSize).append("\n");
        sb.append(encodeList("list", list));
        return sb.toString().stripTrailing();
    }

    /**
     * 编码列表（表格式数组）
     * 输出格式：
     * key[N]{field1,field2,...}:
     *   val1,val2,...
     *   val1,val2,...
     */
    public static String encodeList(String key, List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) {
            return key + "[0]:\n";
        }

        // 获取所有字段名（保持顺序）
        List<String> fields = list.get(0).keySet().stream().toList();

        StringBuilder sb = new StringBuilder();
        sb.append(key).append("[").append(list.size()).append("]");
        sb.append("{").append(String.join(",", fields)).append("):\n");

        for (Map<String, Object> item : list) {
            sb.append(INDENT);
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(formatValue(item.get(fields.get(i))));
            }
            sb.append("\n");
        }

        return sb.toString().stripTrailing();
    }

    /**
     * 编码错误信息
     */
    public static String encodeError(String message) {
        return "error: " + message;
    }

    /**
     * 格式化值
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (SIMPLE_TYPES.contains(value.getClass()) || value instanceof Number) {
            String str = value.toString();
            // 字符串中有特殊字符时加引号
            if (value instanceof String && (str.contains(",") || str.contains("\n") || str.contains(":"))) {
                return "\"" + str.replace("\"", "\\\"") + "\"";
            }
            return str;
        }
        // 其他类型转字符串
        return value.toString();
    }
}
