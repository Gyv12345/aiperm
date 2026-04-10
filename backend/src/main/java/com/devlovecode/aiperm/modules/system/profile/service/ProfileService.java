package com.devlovecode.aiperm.modules.system.profile.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.audit.api.AuditLoginLogRecord;
import com.devlovecode.aiperm.modules.audit.api.LoginAuditApi;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysUser;
import com.devlovecode.aiperm.modules.system.profile.dto.PasswordDTO;
import com.devlovecode.aiperm.modules.system.profile.dto.ProfileDTO;
import com.devlovecode.aiperm.modules.system.profile.vo.LoginLogVO;
import com.devlovecode.aiperm.modules.system.profile.vo.ProfileVO;
import com.devlovecode.aiperm.modules.system.rbac.repository.DeptRepository;
import com.devlovecode.aiperm.modules.system.rbac.repository.PostRepository;
import com.devlovecode.aiperm.modules.system.rbac.repository.RoleRepository;
import com.devlovecode.aiperm.modules.system.rbac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

	private final LoginAuditApi loginAuditApi;

	public ProfileVO getProfile() {
		Long userId = StpUtil.getLoginIdAsLong();
		SysUser user = userRepo.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
		return toVO(user);
	}

	@Transactional
	public void updateProfile(ProfileDTO dto) {
		Long userId = StpUtil.getLoginIdAsLong();
		SysUser user = userRepo.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));

		user.setNickname(dto.getNickname());
		user.setRealName(dto.getRealName());
		user.setEmail(dto.getEmail());
		user.setPhone(dto.getPhone());
		user.setGender(dto.getGender());
		user.setAvatar(dto.getAvatar());
		user.setUpdateBy(user.getUsername());

		userRepo.save(user);
	}

	@Transactional
	public void updatePassword(PasswordDTO dto) {
		Long userId = StpUtil.getLoginIdAsLong();
		SysUser user = userRepo.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));

		if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
			throw new BusinessException("旧密码错误");
		}
		if (BCrypt.checkpw(dto.getNewPassword(), user.getPassword())) {
			throw new BusinessException("新密码不能与旧密码相同");
		}

		userRepo.updatePassword(userId, BCrypt.hashpw(dto.getNewPassword()), LocalDateTime.now());
	}

	public PageResult<LoginLogVO> getLoginLogs(int pageNum, int pageSize) {
		Long userId = StpUtil.getLoginIdAsLong();
		SysUser user = userRepo.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
		return loginAuditApi.queryUserLoginLogs(userId, user.getUsername(), pageNum, pageSize).map(this::toLogVO);
	}

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

		if (entity.getDeptId() != null) {
			deptRepo.findById(entity.getDeptId()).ifPresent(dept -> vo.setDeptName(dept.getDeptName()));
		}
		if (entity.getPostId() != null) {
			postRepo.findById(entity.getPostId()).ifPresent(post -> vo.setPostName(post.getPostName()));
		}

		List<Long> roleIds = userRepo.findRoleIdsByUserId(entity.getId());
		if (roleIds != null && !roleIds.isEmpty()) {
			List<String> roleNames = new ArrayList<>();
			for (Long roleId : roleIds) {
				roleRepo.findById(roleId).ifPresent(role -> roleNames.add(role.getRoleName()));
			}
			vo.setRoleNames(roleNames);
		}

		return vo;
	}

	private LoginLogVO toLogVO(AuditLoginLogRecord entity) {
		LoginLogVO vo = new LoginLogVO();
		vo.setId(entity.id());
		vo.setIp(entity.ip());
		vo.setLocation(entity.location());
		vo.setBrowser(entity.browser());
		vo.setOs(entity.os());
		vo.setStatus(entity.status());
		vo.setMsg(entity.msg());
		vo.setLoginTime(entity.loginTime());
		return vo;
	}

}
