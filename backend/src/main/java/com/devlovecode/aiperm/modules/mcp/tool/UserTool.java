package com.devlovecode.aiperm.modules.mcp.tool;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.system.dto.UserDTO;
import com.devlovecode.aiperm.modules.system.service.UserService;
import com.devlovecode.aiperm.modules.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 用户管理 MCP 工具
 *
 * @author DevLoveCode
 */
@Component
@RequiredArgsConstructor
public class UserTool extends BaseMcpTool {

    private final UserService userService;

    @Tool(description = "分页查询用户列表")
    public String listUsers(
            @ToolParam(description = "用户名，支持模糊查询") String username,
            @ToolParam(description = "手机号，支持模糊查询") String phone,
            @ToolParam(description = "部门ID") Long deptId,
            @ToolParam(description = "状态：0正常，1停用") Integer status,
            @ToolParam(description = "页码，默认1") Integer page,
            @ToolParam(description = "每页条数，默认10") Integer pageSize) {
        try {
            UserDTO dto = new UserDTO();
            dto.setUsername(username);
            dto.setPhone(phone);
            dto.setDeptId(deptId);
            dto.setStatus(status);
            dto.setPage(page != null ? page : 1);
            dto.setPageSize(pageSize != null ? pageSize : 10);

            PageResult<UserVO> result = userService.queryPage(dto);
            return toToonPage(result.getTotal(), result.getPageNum(), result.getPageSize(), toMapList(result.getList()));
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "根据ID查询用户详情")
    public String getUserById(@ToolParam(description = "用户ID") Long userId) {
        try {
            return toToon(toMap(userService.findById(userId)));
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "创建新用户")
    public String createUser(
            @ToolParam(description = "用户名") String username,
            @ToolParam(description = "密码") String password,
            @ToolParam(description = "昵称") String nickname,
            @ToolParam(description = "邮箱") String email,
            @ToolParam(description = "手机号") String phone,
            @ToolParam(description = "性别：0未知，1男，2女") Integer gender,
            @ToolParam(description = "头像地址") String avatar,
            @ToolParam(description = "部门ID") Long deptId,
            @ToolParam(description = "岗位ID") Long postId,
            @ToolParam(description = "状态：0正常，1停用") Integer status,
            @ToolParam(description = "备注") String remark) {
        try {
            UserDTO dto = new UserDTO();
            dto.setUsername(username);
            dto.setPassword(password);
            dto.setNickname(nickname);
            dto.setEmail(email);
            dto.setPhone(phone);
            dto.setGender(gender != null ? gender : 0);
            dto.setAvatar(avatar);
            dto.setDeptId(deptId);
            dto.setPostId(postId);
            dto.setStatus(status != null ? status : 0);
            dto.setRemark(remark);

            userService.create(dto);
            return "username: " + username;
        } catch (Exception e) {
            return error("创建失败: " + e.getMessage());
        }
    }

    @Tool(description = "更新用户信息")
    public String updateUser(
            @ToolParam(description = "用户ID") Long userId,
            @ToolParam(description = "用户名") String username,
            @ToolParam(description = "昵称") String nickname,
            @ToolParam(description = "邮箱") String email,
            @ToolParam(description = "手机号") String phone,
            @ToolParam(description = "性别：0未知，1男，2女") Integer gender,
            @ToolParam(description = "头像地址") String avatar,
            @ToolParam(description = "部门ID") Long deptId,
            @ToolParam(description = "岗位ID") Long postId,
            @ToolParam(description = "状态：0正常，1停用") Integer status,
            @ToolParam(description = "备注") String remark) {
        try {
            UserDTO dto = new UserDTO();
            dto.setUsername(username);
            dto.setNickname(nickname);
            dto.setEmail(email);
            dto.setPhone(phone);
            dto.setGender(gender);
            dto.setAvatar(avatar);
            dto.setDeptId(deptId);
            dto.setPostId(postId);
            dto.setStatus(status);
            dto.setRemark(remark);

            userService.update(userId, dto);
            return "ok: true";
        } catch (Exception e) {
            return error("更新失败: " + e.getMessage());
        }
    }

    @Tool(description = "删除用户")
    public String deleteUser(@ToolParam(description = "用户ID") Long userId) {
        try {
            userService.delete(userId);
            return "ok: true";
        } catch (Exception e) {
            return error("删除失败: " + e.getMessage());
        }
    }

    @Tool(description = "批量删除用户")
    public String batchDeleteUsers(
            @ToolParam(description = "用户ID列表，多个用逗号分隔") String userIds) {
        try {
            List<Long> ids = new ArrayList<>();
            if (userIds != null && !userIds.isBlank()) {
                for (String id : userIds.split(",")) {
                    ids.add(Long.parseLong(id.trim()));
                }
            }
            userService.deleteBatch(ids);
            return "ok: true";
        } catch (Exception e) {
            return error("删除失败: " + e.getMessage());
        }
    }

    @Tool(description = "重置用户密码")
    public String resetPassword(
            @ToolParam(description = "用户ID") Long userId,
            @ToolParam(description = "新密码") String newPassword) {
        try {
            userService.resetPassword(userId, newPassword);
            return "ok: true";
        } catch (Exception e) {
            return error("重置失败: " + e.getMessage());
        }
    }

    @Tool(description = "修改用户状态")
    public String changeStatus(
            @ToolParam(description = "用户ID") Long userId,
            @ToolParam(description = "状态：0正常，1停用") Integer status) {
        try {
            userService.changeStatus(userId, status);
            return "ok: true";
        } catch (Exception e) {
            return error("修改失败: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> toMapList(List<UserVO> users) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserVO user : users) {
            list.add(toMap(user));
        }
        return list;
    }

    private Map<String, Object> toMap(UserVO user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("gender", user.getGender());
        map.put("avatar", user.getAvatar());
        map.put("deptId", user.getDeptId());
        map.put("deptName", user.getDeptName());
        map.put("postNames", user.getPostNames());
        map.put("roleNames", user.getRoleNames());
        map.put("status", user.getStatus());
        map.put("remark", user.getRemark());
        map.put("createTime", user.getCreateTime());
        return map;
    }
}
