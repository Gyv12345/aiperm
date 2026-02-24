package com.devlovecode.aiperm.modules.system.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.mapper.SysUserMapper;
import com.devlovecode.aiperm.modules.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 *
 * @author devlovecode
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public SysUser getByUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return null;
        }
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public PageResult<SysUser> page(Long pageNum, Long pageSize, String username, String phone, Long deptId, Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> result = sysUserMapper.selectUserPage(page, username, phone, deptId, status);
        return PageResult.of(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(SysUser user) {
        // 检查用户名是否存在
        SysUser existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }

        // 加密密码
        user.setPassword(BCrypt.hashpw(user.getPassword()));

        return save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysUser user) {
        SysUser existUser = getById(user.getId());
        if (existUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查用户名是否被其他用户占用
        if (!existUser.getUsername().equals(user.getUsername())) {
            SysUser sameNameUser = getByUsername(user.getUsername());
            if (sameNameUser != null) {
                throw new BusinessException(ErrorCode.USER_EXISTS);
            }
        }

        // 如果密码不为空，则加密密码
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        } else {
            user.setPassword(existUser.getPassword());
        }

        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return removeById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(BCrypt.hashpw(newPassword));

        return updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long userId, Integer status) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setStatus(status);

        return updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastLoginInfo(Long userId, String ip) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastLoginIp(ip);
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);
    }
}
