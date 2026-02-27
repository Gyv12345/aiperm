package com.devlovecode.aiperm.modules.profile.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.log.entity.SysLoginLog;
import com.devlovecode.aiperm.modules.log.repository.LoginLogRepository;
import com.devlovecode.aiperm.modules.profile.dto.PasswordDTO;
import com.devlovecode.aiperm.modules.profile.dto.ProfileDTO;
import com.devlovecode.aiperm.modules.profile.vo.LoginLogVO;
import com.devlovecode.aiperm.modules.profile.vo.ProfileVO;
import com.devlovecode.aiperm.modules.system.entity.SysDept;
import com.devlovecode.aiperm.modules.system.entity.SysPost;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.repository.DeptRepository;
import com.devlovecode.aiperm.modules.system.repository.PostRepository;
import com.devlovecode.aiperm.modules.system.repository.RoleRepository;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人中心 Service
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepo;
    private final DeptRepository deptRepo;
    private final PostRepository postRepo;
    private final RoleRepository roleRepo;
    private final LoginLogRepository loginLogRepo;

    /**
     * 获取当前用户个人信息
     */
    public ProfileVO getProfile() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        return toVO(user);
    }

    /**
     * 修改个人信息
     */
    @Transactional
    public void updateProfile(ProfileDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setNickname(dto.getNickname());
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setAvatar(dto.getAvatar());
        user.setUpdateBy(user.getUsername());

        userRepo.update(user);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void updatePassword(PasswordDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证旧密码
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 新密码不能与旧密码相同
        if (BCrypt.checkpw(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 更新密码
        userRepo.updatePassword(userId, BCrypt.hashpw(dto.getNewPassword()));
    }

    /**
     * 获取登录日志
     */
    public PageResult<LoginLogVO> getLoginLogs(int pageNum, int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        PageResult<SysLoginLog> result = loginLogRepo.queryPageByUserId(userId, pageNum, pageSize);
        return result.map(this::toLogVO);
    }

    /**
     * 转换为 ProfileVO
     */
    private ProfileVO toVO(SysUser entity) {
        ProfileVO vo = new ProfileVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setNickname(entity.getNickname());
        vo.setRealName(entity.getRealName());
        vo.setEmail(entity.getEmail());
        vo.setPhone(entity.getPhone());
        vo.setGender(entity.getGender());
        vo.setAvatar(entity.getAvatar());
        vo.setDeptId(entity.getDeptId());
        vo.setStatus(entity.getStatus());
        vo.setLastLoginIp(entity.getLastLoginIp());
        vo.setLastLoginTime(entity.getLastLoginTime());
        vo.setCreateTime(entity.getCreateTime());

        // 填充部门名称
        if (entity.getDeptId() != null) {
            deptRepo.findById(entity.getDeptId())
                    .ifPresent(dept -> vo.setDeptName(dept.getDeptName()));
        }

        // 填充岗位信息
        if (entity.getPostId() != null) {
            postRepo.findById(entity.getPostId())
                    .ifPresent(post -> vo.setPostName(post.getPostName()));
        }

        // 填充角色信息
        List<Long> roleIds = userRepo.findRoleIdsByUserId(entity.getId());
        if (roleIds != null && !roleIds.isEmpty()) {
            List<String> roleNames = new ArrayList<>();
            for (Long roleId : roleIds) {
                roleRepo.findById(roleId)
                        .ifPresent(role -> roleNames.add(role.getRoleName()));
            }
            vo.setRoleNames(roleNames);
        }

        return vo;
    }

    /**
     * 转换为 LoginLogVO
     */
    private LoginLogVO toLogVO(SysLoginLog entity) {
        LoginLogVO vo = new LoginLogVO();
        vo.setId(entity.getId());
        vo.setIp(entity.getIp());
        vo.setLocation(entity.getLocation());
        vo.setBrowser(entity.getBrowser());
        vo.setOs(entity.getOs());
        vo.setStatus(entity.getStatus());
        vo.setMsg(entity.getMsg());
        vo.setLoginTime(entity.getLoginTime());
        return vo;
    }
}
