package com.devlovecode.aiperm.modules.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.entity.SysPermission;
import com.devlovecode.aiperm.modules.system.mapper.SysPermissionMapper;
import com.devlovecode.aiperm.modules.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 权限服务实现类
 *
 * @author devlovecode
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public SysPermission getByPermissionCode(String permissionCode) {
        if (StrUtil.isBlank(permissionCode)) {
            return null;
        }
        return sysPermissionMapper.selectByPermissionCode(permissionCode);
    }

    @Override
    public List<SysPermission> listByRoleId(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        return sysPermissionMapper.selectByRoleId(roleId);
    }

    @Override
    public List<SysPermission> listByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return sysPermissionMapper.selectByUserId(userId);
    }

    @Override
    public List<SysPermission> listByMenuId(Long menuId) {
        if (menuId == null) {
            return List.of();
        }
        return sysPermissionMapper.selectByMenuId(menuId);
    }

    @Override
    public PageResult<SysPermission> page(Long pageNum, Long pageSize, String permissionName, String permissionCode,
                                          Integer permissionType, Long menuId, Integer status) {
        Page<SysPermission> page = new Page<>(pageNum, pageSize);
        IPage<SysPermission> result = sysPermissionMapper.selectPermissionPage(page, permissionName, permissionCode,
                permissionType, menuId, status);
        return PageResult.of(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(SysPermission permission) {
        // 检查权限编码是否存在
        SysPermission existPermission = getByPermissionCode(permission.getPermissionCode());
        if (existPermission != null) {
            throw new BusinessException("权限编码已存在");
        }

        return save(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysPermission permission) {
        SysPermission existPermission = getById(permission.getId());
        if (existPermission == null) {
            throw new BusinessException("权限不存在");
        }

        // 检查权限编码是否被其他权限占用
        if (!existPermission.getPermissionCode().equals(permission.getPermissionCode())) {
            SysPermission sameCodePermission = getByPermissionCode(permission.getPermissionCode());
            if (sameCodePermission != null) {
                throw new BusinessException("权限编码已存在");
            }
        }

        return updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long permissionId) {
        SysPermission permission = getById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        return removeById(permissionId);
    }
}
