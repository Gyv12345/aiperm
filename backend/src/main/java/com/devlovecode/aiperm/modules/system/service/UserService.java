package com.devlovecode.aiperm.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.dto.UserDTO;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    /**
     * 分页查询
     */
    public PageResult<SysUser> queryPage(UserDTO dto) {
        return userRepo.queryPage(dto.getUsername(), dto.getPhone(), dto.getDeptId(), dto.getStatus(), dto.getPage(), dto.getPageSize());
    }

    /**
     * 查询详情
     */
    public SysUser findById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 创建
     */
    @Transactional
    public void create(UserDTO dto) {
        if (userRepo.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        SysUser entity = new SysUser();
        entity.setUsername(dto.getUsername());
        entity.setPassword(BCrypt.hashpw(dto.getPassword()));
        entity.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        entity.setRealName(dto.getRealName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setGender(dto.getGender() != null ? dto.getGender() : 0);
        entity.setAvatar(dto.getAvatar());
        entity.setDeptId(dto.getDeptId());
        entity.setPostId(dto.getPostId());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        entity.setRemark(dto.getRemark());
        entity.setCreateBy(getCurrentUsername());

        userRepo.insert(entity);
    }

    /**
     * 更新
     */
    @Transactional
    public void update(Long id, UserDTO dto) {
        SysUser entity = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (userRepo.existsByUsernameExcludeId(dto.getUsername(), id)) {
            throw new BusinessException("用户名已存在");
        }

        entity.setUsername(dto.getUsername());
        entity.setNickname(dto.getNickname());
        entity.setRealName(dto.getRealName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setGender(dto.getGender());
        entity.setAvatar(dto.getAvatar());
        entity.setDeptId(dto.getDeptId());
        entity.setPostId(dto.getPostId());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(getCurrentUsername());

        userRepo.update(entity);
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        if (!userRepo.existsById(id)) {
            throw new BusinessException("用户不存在");
        }
        if (userRepo.isAdmin(id)) {
            throw new BusinessException("不能删除管理员用户");
        }
        userRepo.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 过滤掉管理员用户
        List<Long> toDelete = ids.stream()
                .filter(id -> !userRepo.isAdmin(id))
                .toList();
        if (!toDelete.isEmpty()) {
            userRepo.deleteByIds(toDelete);
        }
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (!userRepo.existsById(id)) {
            throw new BusinessException("用户不存在");
        }
        userRepo.updatePassword(id, BCrypt.hashpw(newPassword));
    }

    /**
     * 修改状态
     */
    @Transactional
    public void changeStatus(Long id, Integer status) {
        if (!userRepo.existsById(id)) {
            throw new BusinessException("用户不存在");
        }
        if (userRepo.isAdmin(id)) {
            throw new BusinessException("不能修改管理员用户状态");
        }
        userRepo.updateStatus(id, status);
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
