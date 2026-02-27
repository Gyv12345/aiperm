package com.devlovecode.aiperm.modules.mcp.tool;

import com.devlovecode.aiperm.modules.system.entity.SysMenu;
import com.devlovecode.aiperm.modules.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 菜单管理 MCP 工具
 *
 * @author DevLoveCode
 */
@Component
@RequiredArgsConstructor
public class MenuTool extends BaseMcpTool {

    private final MenuService menuService;

    @Tool(description = "查询所有菜单（扁平列表），返回菜单列表")
    public String listAllMenusFlat() {
        try {
            List<SysMenu> menus = menuService.listAll();
            List<Map<String, Object>> list = new ArrayList<>();
            for (SysMenu menu : menus) {
                list.add(toMap(menu));
            }
            return toToonList("menus", list);
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "根据ID查询菜单详情")
    public String getMenuById(@ToolParam(description = "菜单ID") Long id) {
        try {
            return toToon(toMap(menuService.findById(id)));
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "根据父ID查询子菜单列表")
    public String listMenusByParentId(@ToolParam(description = "父菜单ID，根菜单传0") Long parentId) {
        try {
            List<SysMenu> menus = menuService.listByParentId(parentId);
            List<Map<String, Object>> list = new ArrayList<>();
            for (SysMenu menu : menus) {
                list.add(toMap(menu));
            }
            return toToonList("menus", list);
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    private Map<String, Object> toMap(SysMenu menu) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", menu.getId());
        map.put("menuName", menu.getMenuName());
        map.put("parentId", menu.getParentId());
        map.put("path", menu.getPath());
        map.put("component", menu.getComponent());
        map.put("menuType", menu.getMenuType());
        map.put("sort", menu.getSort());
        map.put("status", menu.getStatus());
        return map;
    }
}
