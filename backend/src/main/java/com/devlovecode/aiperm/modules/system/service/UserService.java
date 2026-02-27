package com.devlovecode.aiperm.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.dto.UserDTO;
import com.devlovecode.aiperm.modules.system.entity.SysDept;
import com.devlovecode.aiperm.modules.system.entity.SysPost;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.repository.DeptRepository;
import com.devlovecode.aiperm.modules.system.repository.PostRepository;
import com.devlovecode.aiperm.modules.system.repository.RoleRepository;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import com.devlovecode.aiperm.modules.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final DeptRepository deptRepo;
    private final PostRepository postRepo;
    private final RoleRepository roleRepo;

    /**
     * 分页查询
     */
    public PageResult<UserVO> queryPage(UserDTO dto) {
        PageResult<SysUser> result = userRepo.queryPage(
                dto.getUsername(), dto.getPhone(), dto.getDeptId(), dto.getStatus(),
                dto.getPage(), dto.getPageSize()
        );
        return result.map(this::toVO);
    }

    /**
     * 查询详情
     */
    public UserVO findById(Long id) {
        SysUser user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toVO(user);
    }

    /**
     * 转换为VO，填充关联数据
     */
    private UserVO toVO(SysUser entity) {
        UserVO vo = new UserVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setNickname(entity.getNickname());
        vo.setEmail(entity.getEmail());
        vo.setPhone(entity.getPhone());
        vo.setGender(entity.getGender());
        vo.setAvatar(entity.getAvatar());
        vo.setDeptId(entity.getDeptId());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        // 填充部门名称
        if (entity.getDeptId() != null) {
            deptRepo.findById(entity.getDeptId())
                    .ifPresent(dept -> vo.setDeptName(dept.getDeptName()));
        }

        // 填充岗位信息（单岗位）
        if (entity.getPostId() != null) {
            postRepo.findById(entity.getPostId())
                    .ifPresent(post -> {
                        vo.setPostIds(List.of(post.getId()));
                        vo.setPostNames(post.getPostName());
                    });
        }

        // 填充角色信息（需要从用户角色关联表查询）
        List<Long> roleIds = userRepo.findRoleIdsByUserId(entity.getId());
        if (roleIds != null && !roleIds.isEmpty()) {
            vo.setRoleIds(roleIds);
            List<String> roleNames = new ArrayList<>();
            for (Long roleId : roleIds) {
                roleRepo.findById(roleId)
                        .ifPresent(role -> roleNames.add(role.getRoleName()));
            }
            vo.setRoleNames(String.join(", ", roleNames));
        }

        return vo;
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
