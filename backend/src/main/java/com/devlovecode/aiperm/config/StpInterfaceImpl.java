package com.devlovecode.aiperm.config;

import cn.dev33.satoken.stp.StpInterface;
import com.devlovecode.aiperm.modules.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限认证实现
 * 实现 StpInterface 接口，提供用户的角色和权限数据
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final AuthService authService;

    private static final Long SUPER_ADMIN_ID = 1L;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());

        if (SUPER_ADMIN_ID.equals(userId)) {
            // 超级管理员返回所有权限
            return authService.getAllPermissions();
        }

        return authService.getUserPermissions(userId);
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        // 统一从数据库获取角色
        return authService.getUserRoles(userId);
    }
}
