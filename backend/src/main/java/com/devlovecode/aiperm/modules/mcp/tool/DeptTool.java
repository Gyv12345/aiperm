package com.devlovecode.aiperm.modules.mcp.tool;

import com.devlovecode.aiperm.modules.system.dto.DeptDTO;
import com.devlovecode.aiperm.modules.system.entity.SysDept;
import com.devlovecode.aiperm.modules.system.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 部门管理 MCP 工具
 *
 * @author DevLoveCode
 */
@Component
@RequiredArgsConstructor
public class DeptTool extends BaseMcpTool {

    private final DeptService deptService;

    @Tool(description = "查询所有部门列表")
    public String listAllDepts() {
        try {
            List<SysDept> depts = deptService.listAll();
            List<Map<String, Object>> list = new ArrayList<>();
            for (SysDept dept : depts) {
                list.add(toMap(dept));
            }
            return toToonList("depts", list);
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "根据ID查询部门详情")
    public String getDeptById(@ToolParam(description = "部门ID") Long deptId) {
        try {
            return toToon(toMap(deptService.findById(deptId)));
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "查询子部门列表")
    public String listDeptsByParentId(@ToolParam(description = "父部门ID，根部门传0") Long parentId) {
        try {
            List<SysDept> depts = deptService.listByParentId(parentId);
            List<Map<String, Object>> list = new ArrayList<>();
            for (SysDept dept : depts) {
                list.add(toMap(dept));
            }
            return toToonList("depts", list);
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "创建新部门")
    public String createDept(
            @ToolParam(description = "部门名称") String deptName,
            @ToolParam(description = "父部门ID，根部门传0") Long parentId,
            @ToolParam(description = "排序号") Integer sort,
            @ToolParam(description = "负责人") String leader,
            @ToolParam(description = "联系电话") String phone,
            @ToolParam(description = "邮箱") String email,
            @ToolParam(description = "状态：0正常，1停用") Integer status,
            @ToolParam(description = "备注") String remark) {
        try {
            DeptDTO dto = new DeptDTO();
            dto.setDeptName(deptName);
            dto.setParentId(parentId != null ? parentId : 0L);
            dto.setSort(sort != null ? sort : 0);
            dto.setLeader(leader);
            dto.setPhone(phone);
            dto.setEmail(email);
            dto.setStatus(status != null ? status : 0);
            dto.setRemark(remark);

            deptService.create(dto);
            return "deptName: " + deptName;
        } catch (Exception e) {
            return error("创建失败: " + e.getMessage());
        }
    }

    @Tool(description = "更新部门信息")
    public String updateDept(
            @ToolParam(description = "部门ID") Long deptId,
            @ToolParam(description = "部门名称") String deptName,
            @ToolParam(description = "父部门ID") Long parentId,
            @ToolParam(description = "排序号") Integer sort,
            @ToolParam(description = "负责人") String leader,
            @ToolParam(description = "联系电话") String phone,
            @ToolParam(description = "邮箱") String email,
            @ToolParam(description = "状态：0正常，1停用") Integer status,
            @ToolParam(description = "备注") String remark) {
        try {
            DeptDTO dto = new DeptDTO();
            dto.setDeptName(deptName);
            dto.setParentId(parentId);
            dto.setSort(sort);
            dto.setLeader(leader);
            dto.setPhone(phone);
            dto.setEmail(email);
            dto.setStatus(status);
            dto.setRemark(remark);

            deptService.update(deptId, dto);
            return "ok: true";
        } catch (Exception e) {
            return error("更新失败: " + e.getMessage());
        }
    }

    @Tool(description = "删除部门")
    public String deleteDept(@ToolParam(description = "部门ID") Long deptId) {
        try {
            deptService.delete(deptId);
            return "ok: true";
        } catch (Exception e) {
            return error("删除失败: " + e.getMessage());
        }
    }

    private Map<String, Object> toMap(SysDept dept) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", dept.getId());
        map.put("deptName", dept.getDeptName());
        map.put("parentId", dept.getParentId());
        map.put("sort", dept.getSort());
        map.put("leader", dept.getLeader());
        map.put("phone", dept.getPhone());
        map.put("email", dept.getEmail());
        map.put("status", dept.getStatus());
        map.put("remark", dept.getRemark());
        return map;
    }
}
