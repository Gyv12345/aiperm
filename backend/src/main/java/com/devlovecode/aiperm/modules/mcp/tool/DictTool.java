package com.devlovecode.aiperm.modules.mcp.tool;

import com.devlovecode.aiperm.modules.system.service.DictDataService;
import com.devlovecode.aiperm.modules.system.service.DictTypeService;
import com.devlovecode.aiperm.modules.system.vo.DictDataVO;
import com.devlovecode.aiperm.modules.system.vo.DictTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 字典管理 MCP 工具
 *
 * @author DevLoveCode
 */
@Component
@RequiredArgsConstructor
public class DictTool extends BaseMcpTool {

    private final DictTypeService dictTypeService;
    private final DictDataService dictDataService;

    @Tool(description = "查询所有启用的字典类型")
    public String listDictTypes() {
        try {
            List<DictTypeVO> types = dictTypeService.findAllEnabled();
            List<Map<String, Object>> list = new ArrayList<>();
            for (DictTypeVO type : types) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", type.getId());
                map.put("dictName", type.getDictName());
                map.put("dictType", type.getDictType());
                map.put("status", type.getStatus());
                list.add(map);
            }
            return toToonList("dictTypes", list);
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "根据字典类型查询字典数据列表")
    public String queryDictData(@ToolParam(description = "字典类型，如：sys_normal_disable, sys_user_sex") String dictType) {
        try {
            List<DictDataVO> data = dictDataService.listByDictType(dictType);
            List<Map<String, Object>> list = new ArrayList<>();
            for (DictDataVO item : data) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("dictValue", item.getDictValue());
                map.put("dictLabel", item.getDictLabel());
                map.put("sort", item.getSort());
                map.put("status", item.getStatus());
                list.add(map);
            }
            return toToonList("dictData", list);
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "根据字典类型和字典值获取字典标签")
    public String getDictLabel(
            @ToolParam(description = "字典类型") String dictType,
            @ToolParam(description = "字典值") String dictValue) {
        try {
            List<DictDataVO> dataList = dictDataService.listByDictType(dictType);
            String label = dataList.stream()
                    .filter(d -> d.getDictValue().equals(dictValue))
                    .map(DictDataVO::getDictLabel)
                    .findFirst()
                    .orElse(dictValue);
            return "label: " + label;
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }
}
