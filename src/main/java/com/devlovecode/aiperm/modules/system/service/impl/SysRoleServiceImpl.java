package com.devlovecode.aiperm.modules.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import com.devlovecode.aiperm.modules.system.mapper.SysRoleMapper;
import com.devlovecode.aiperm.modules.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现类
 *
 * @author devlovecode
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public SysRole getByRoleCode(String roleCode) {
        if (StrUtil.isBlank(roleCode)) {
            return null;
        }
        return sysRoleMapper.selectByRoleCode(roleCode);
    }

    @Override
    public List<SysRole> listByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return sysRoleMapper.selectByUserId(userId);
    }

    @Override
    public PageResult<SysRole> page(Long pageNum, Long pageSize, String roleName, String roleCode, Integer status) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        IPage<SysRole> result = sysRoleMapper.selectRolePage(page, roleName, roleCode, status);
        return PageResult.of(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(SysRole role) {
        // 检查角色编码是否存在
        SysRole existRole = getByRoleCode(role.getRoleCode());
        if (existRole != null) {
            throw new BusinessException(ErrorCode.ROLE_EXISTS);
        }

        return save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysRole role) {
        SysRole existRole = getById(role.getId());
        if (existRole == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }

        // 内置角色不允许修改
        if (existRole.getIsBuiltin() == 1) {
            throw new BusinessException("内置角色不允许修改");
        }

        // 检查角色编码是否被其他角色占用
        if (!existRole.getRoleCode().equals(role.getRoleCode())) {
            SysRole sameCodeRole = getByRoleCode(role.getRoleCode());
            if (sameCodeRole != null) {
                throw new BusinessException(ErrorCode.ROLE_EXISTS);
            }
        }

        return updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }

        // 内置角色不允许删除
        if (role.getIsBuiltin() == 1) {
            throw new BusinessException("内置角色不允许删除");
        }

        return removeById(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        // TODO: 实现角色菜单分配逻辑
        log.info("分配角色菜单: roleId={}, menuIds={}", roleId, menuIds);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        // TODO: 实现角色权限分配逻辑
        log.info("分配角色权限: roleId={}, permissionIds={}", roleId, permissionIds);
        return true;
    }
}
