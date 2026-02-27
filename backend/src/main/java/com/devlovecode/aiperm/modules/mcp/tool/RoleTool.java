package com.devlovecode.aiperm.modules.mcp.tool;

import com.devlovecode.aiperm.modules.system.dto.RoleDTO;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import com.devlovecode.aiperm.modules.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 角色管理 MCP 工具
 *
 * @author DevLoveCode
 */
@Component
@RequiredArgsConstructor
public class RoleTool extends BaseMcpTool {

    private final RoleService roleService;

    @Tool(description = "查询所有角色列表")
    public String listAllRoles() {
        try {
            List<SysRole> roles = roleService.listAll();
            List<Map<String, Object>> list = new ArrayList<>();
            for (SysRole role : roles) {
                list.add(toMap(role));
            }
            return toToonList("roles", list);
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "根据ID查询角色详情")
    public String getRoleById(@ToolParam(description = "角色ID") Long roleId) {
        try {
            return toToon(toMap(roleService.findById(roleId)));
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "创建新角色")
    public String createRole(
            @ToolParam(description = "角色名称") String roleName,
            @ToolParam(description = "角色编码") String roleCode,
            @ToolParam(description = "排序号") Integer sort,
            @ToolParam(description = "状态：0正常，1停用") Integer status,
            @ToolParam(description = "备注") String remark) {
        try {
            RoleDTO dto = new RoleDTO();
            dto.setRoleName(roleName);
            dto.setRoleCode(roleCode);
            dto.setSort(sort != null ? sort : 0);
            dto.setStatus(status != null ? status : 0);
            dto.setRemark(remark);

            roleService.create(dto);
            return "roleCode: " + roleCode;
        } catch (Exception e) {
            return error("创建失败: " + e.getMessage());
        }
    }

    @Tool(description = "更新角色信息")
    public String updateRole(
            @ToolParam(description = "角色ID") Long roleId,
            @ToolParam(description = "角色名称") String roleName,
            @ToolParam(description = "角色编码") String roleCode,
            @ToolParam(description = "排序号") Integer sort,
            @ToolParam(description = "状态：0正常，1停用") Integer status,
            @ToolParam(description = "备注") String remark) {
        try {
            RoleDTO dto = new RoleDTO();
            dto.setRoleName(roleName);
            dto.setRoleCode(roleCode);
            dto.setSort(sort);
            dto.setStatus(status);
            dto.setRemark(remark);

            roleService.update(roleId, dto);
            return "ok: true";
        } catch (Exception e) {
            return error("更新失败: " + e.getMessage());
        }
    }

    @Tool(description = "删除角色")
    public String deleteRole(@ToolParam(description = "角色ID") Long roleId) {
        try {
            roleService.delete(roleId);
            return "ok: true";
        } catch (Exception e) {
            return error("删除失败: " + e.getMessage());
        }
    }

    @Tool(description = "为角色分配菜单权限")
    public String assignMenusToRole(
            @ToolParam(description = "角色ID") Long roleId,
            @ToolParam(description = "菜单ID列表，多个用逗号分隔") String menuIds) {
        try {
            List<Long> menuIdList = new ArrayList<>();
            if (menuIds != null && !menuIds.isBlank()) {
                for (String id : menuIds.split(",")) {
                    menuIdList.add(Long.parseLong(id.trim()));
                }
            }
            roleService.assignMenus(roleId, menuIdList);
            return "ok: true";
        } catch (Exception e) {
            return error("分配失败: " + e.getMessage());
        }
    }

    @Tool(description = "获取角色的菜单ID列表")
    public String getRoleMenuIds(@ToolParam(description = "角色ID") Long roleId) {
        try {
            List<Long> menuIds = roleService.getMenuIds(roleId);
            return "menuIds: " + menuIds.toString().replace("[", "").replace("]", "");
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    private Map<String, Object> toMap(SysRole role) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", role.getId());
        map.put("roleName", role.getRoleName());
        map.put("roleCode", role.getRoleCode());
        map.put("sort", role.getSort());
        map.put("status", role.getStatus());
        map.put("remark", role.getRemark());
        map.put("isBuiltin", role.getIsBuiltin());
        map.put("createTime", role.getCreateTime());
        return map;
    }
}
