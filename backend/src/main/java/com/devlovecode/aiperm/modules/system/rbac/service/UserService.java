package com.devlovecode.aiperm.modules.system.rbac.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.util.ExcelExportHelper;
import com.devlovecode.aiperm.common.util.ExcelImportHelper;
import com.devlovecode.aiperm.common.vo.ImportResultVO;
import com.devlovecode.aiperm.modules.system.rbac.dto.UserDTO;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysDept;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysPost;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysRole;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysUser;
import com.devlovecode.aiperm.modules.system.rbac.export.UserExportModel;
import com.devlovecode.aiperm.modules.system.rbac.export.UserImportModel;
import com.devlovecode.aiperm.modules.system.rbac.repository.DeptRepository;
import com.devlovecode.aiperm.modules.system.rbac.repository.PostRepository;
import com.devlovecode.aiperm.modules.system.rbac.repository.RoleRepository;
import com.devlovecode.aiperm.modules.system.rbac.repository.UserRepository;
import com.devlovecode.aiperm.modules.system.rbac.vo.UserVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private static final Pattern ROLE_CODE_SEPARATOR = Pattern.compile("[,，]");

	private final UserRepository userRepo;

	private final DeptRepository deptRepo;

	private final PostRepository postRepo;

	private final RoleRepository roleRepo;

	private final ExcelExportHelper excelExportHelper;

	private final ExcelImportHelper excelImportHelper;

	/**
	 * 分页查询
	 */
	public PageResult<UserVO> queryPage(UserDTO dto) {
		Page<SysUser> jpaPage = userRepo.queryPage(dto.getUsername(), dto.getPhone(), dto.getDeptId(), dto.getStatus(),
				dto.getPage(), dto.getPageSize());
		return PageResult.fromJpaPage(jpaPage).map(this::toVO);
	}

	/**
	 * 查询详情
	 */
	public UserVO findById(Long id) {
		SysUser user = userRepo.findById(id).orElseThrow(() -> new BusinessException("用户不存在"));
		return toVO(user);
	}

	public void export(UserDTO dto, HttpServletResponse response) {
		List<UserExportModel> rows = userRepo
			.queryPage(dto.getUsername(), dto.getPhone(), dto.getDeptId(), dto.getStatus(), 1, Integer.MAX_VALUE)
			.getContent()
			.stream()
			.map(this::toVO)
			.map(this::toExportModel)
			.toList();
		excelExportHelper.export(response, "users", UserExportModel.class, rows);
	}

	public void downloadImportTemplate(HttpServletResponse response) {
		excelExportHelper.export(response, "user-import-template", UserImportModel.class, List.of());
	}

	@Transactional
	public ImportResultVO importUsers(MultipartFile file) {
		List<UserImportModel> rows = excelImportHelper.read(file, UserImportModel.class);
		ImportResultVO result = new ImportResultVO();
		int rowNumber = 2;
		for (UserImportModel row : rows) {
			try {
				importSingleUser(row);
				result.addSuccess();
			}
			catch (Exception e) {
				result.addError(rowNumber, e.getMessage());
			}
			rowNumber++;
		}
		return result;
	}

	/**
	 * 转换为VO，填充关联数据
	 */
	private UserVO toVO(SysUser entity) {
		UserVO vo = new UserVO();
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
		vo.setRemark(entity.getRemark());
		vo.setCreateTime(entity.getCreateTime());
		vo.setUpdateTime(entity.getUpdateTime());

		if (entity.getDeptId() != null) {
			deptRepo.findById(entity.getDeptId()).ifPresent(dept -> vo.setDeptName(dept.getDeptName()));
		}

		if (entity.getPostId() != null) {
			postRepo.findById(entity.getPostId()).ifPresent(post -> {
				vo.setPostIds(List.of(post.getId()));
				vo.setPostNames(post.getPostName());
			});
		}

		List<Long> roleIds = userRepo.findRoleIdsByUserId(entity.getId());
		if (roleIds != null && !roleIds.isEmpty()) {
			vo.setRoleIds(roleIds);
			List<String> roleNames = new ArrayList<>();
			for (Long roleId : roleIds) {
				roleRepo.findById(roleId).ifPresent(role -> roleNames.add(role.getRoleName()));
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
		entity.setCreateTime(LocalDateTime.now());

		userRepo.save(entity);

		if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
			userRepo.updateUserRoles(entity.getId(), dto.getRoleIds(), getCurrentUsername());
		}
	}

	/**
	 * 更新
	 */
	@Transactional
	public void update(Long id, UserDTO dto) {
		SysUser entity = userRepo.findById(id).orElseThrow(() -> new BusinessException("用户不存在"));

		if (userRepo.existsByUsernameExcludeId(dto.getUsername(), id)) {
			throw new BusinessException("用户名已存在");
		}

		if (userRepo.isAdmin(id)) {
			if (!Objects.equals(entity.getDeptId(), dto.getDeptId())) {
				throw new BusinessException("超级管理员的部门不能修改");
			}

			if (dto.getRoleIds() != null) {
				List<Long> currentRoleIds = userRepo.findRoleIdsByUserId(id);
				if (!new HashSet<>(currentRoleIds).equals(new HashSet<>(dto.getRoleIds()))) {
					throw new BusinessException("超级管理员的角色不能修改");
				}
			}
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
		entity.setUpdateTime(LocalDateTime.now());

		userRepo.save(entity);

		if (dto.getRoleIds() != null) {
			userRepo.updateUserRoles(id, dto.getRoleIds(), getCurrentUsername());
		}
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
		userRepo.softDelete(id, LocalDateTime.now());
	}

	/**
	 * 批量删除
	 */
	@Transactional
	public void deleteBatch(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}
		List<Long> toDelete = ids.stream().filter(id -> !userRepo.isAdmin(id)).toList();
		if (!toDelete.isEmpty()) {
			userRepo.softDeleteByIds(toDelete, LocalDateTime.now());
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
		userRepo.updatePassword(id, BCrypt.hashpw(newPassword), LocalDateTime.now());
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
		userRepo.updateStatus(id, status, LocalDateTime.now());
	}

	private void importSingleUser(UserImportModel row) {
		String username = requireText(row.getUsername(), "用户名不能为空");
		String password = requireText(row.getPassword(), "密码不能为空");
		if (userRepo.existsByUsername(username)) {
			throw new BusinessException("用户名已存在: " + username);
		}

		SysUser entity = new SysUser();
		entity.setUsername(username);
		entity.setPassword(BCrypt.hashpw(password));
		entity.setNickname(defaultIfBlank(row.getNickname(), username));
		entity.setRealName(normalizeText(row.getRealName()));
		entity.setEmail(normalizeText(row.getEmail()));
		entity.setPhone(normalizeText(row.getPhone()));
		entity.setGender(resolveGender(row.getGenderText()));
		entity.setDeptId(resolveDeptId(row.getDeptName()));
		entity.setPostId(resolvePostId(row.getPostCode()));
		entity.setStatus(resolveStatus(row.getStatusText()));
		entity.setRemark(normalizeText(row.getRemark()));
		entity.setCreateBy(getCurrentUsername());
		entity.setCreateTime(LocalDateTime.now());
		userRepo.save(entity);

		List<Long> roleIds = resolveRoleIds(row.getRoleCodes());
		if (!roleIds.isEmpty()) {
			userRepo.updateUserRoles(entity.getId(), roleIds, getCurrentUsername());
		}
	}

	private UserExportModel toExportModel(UserVO vo) {
		UserExportModel model = new UserExportModel();
		model.setUsername(vo.getUsername());
		model.setNickname(vo.getNickname());
		model.setRealName(vo.getRealName());
		model.setEmail(vo.getEmail());
		model.setPhone(vo.getPhone());
		model.setGenderText(toGenderText(vo.getGender()));
		model.setDeptName(vo.getDeptName());
		model.setPostNames(vo.getPostNames());
		model.setRoleNames(vo.getRoleNames());
		model.setStatusText(vo.getStatus() != null && vo.getStatus() == 1 ? "启用" : "禁用");
		model.setRemark(vo.getRemark());
		model.setCreateTime(vo.getCreateTime() == null ? "" : vo.getCreateTime().format(DATE_TIME_FORMATTER));
		return model;
	}

	private Long resolveDeptId(String deptName) {
		String normalized = normalizeText(deptName);
		if (normalized == null) {
			return null;
		}
		SysDept dept = deptRepo.findByDeptNameAndDeleted(normalized, 0);
		if (dept == null) {
			throw new BusinessException("部门不存在: " + normalized);
		}
		return dept.getId();
	}

	private Long resolvePostId(String postCode) {
		String normalized = normalizeText(postCode);
		if (normalized == null) {
			return null;
		}
		SysPost post = postRepo.findByPostCodeAndDeleted(normalized, 0);
		if (post == null) {
			throw new BusinessException("岗位编码不存在: " + normalized);
		}
		return post.getId();
	}

	private List<Long> resolveRoleIds(String roleCodes) {
		String normalized = normalizeText(roleCodes);
		if (normalized == null) {
			return List.of();
		}
		Set<Long> roleIds = new LinkedHashSet<>();
		for (String roleCode : ROLE_CODE_SEPARATOR.split(normalized)) {
			String trimmedCode = normalizeText(roleCode);
			if (trimmedCode == null) {
				continue;
			}
			SysRole role = roleRepo.findByRoleCodeAndDeleted(trimmedCode, 0);
			if (role == null) {
				throw new BusinessException("角色编码不存在: " + trimmedCode);
			}
			roleIds.add(role.getId());
		}
		return new ArrayList<>(roleIds);
	}

	private Integer resolveGender(String genderText) {
		String normalized = normalizeText(genderText);
		if (normalized == null) {
			return 0;
		}
		return switch (normalized) {
			case "男" -> 1;
			case "女" -> 2;
			case "未知" -> 0;
			default -> throw new BusinessException("性别仅支持男/女/未知");
		};
	}

	private Integer resolveStatus(String statusText) {
		String normalized = normalizeText(statusText);
		if (normalized == null) {
			return 1;
		}
		return switch (normalized) {
			case "启用" -> 1;
			case "禁用", "停用" -> 0;
			default -> throw new BusinessException("状态仅支持启用/禁用");
		};
	}

	private String requireText(String value, String message) {
		String normalized = normalizeText(value);
		if (normalized == null) {
			throw new BusinessException(message);
		}
		return normalized;
	}

	private String defaultIfBlank(String value, String defaultValue) {
		String normalized = normalizeText(value);
		return normalized == null ? defaultValue : normalized;
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	private String toGenderText(Integer gender) {
		return switch (gender == null ? 0 : gender) {
			case 1 -> "男";
			case 2 -> "女";
			default -> "未知";
		};
	}

	private String getCurrentUsername() {
		try {
			return StpUtil.getLoginIdAsString();
		}
		catch (Exception e) {
			return "system";
		}
	}

}
