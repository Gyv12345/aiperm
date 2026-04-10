package com.devlovecode.aiperm.modules.system.api;

import com.devlovecode.aiperm.modules.system.config.repository.ConfigRepository;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysMenu;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysUser;
import com.devlovecode.aiperm.modules.system.rbac.repository.DeptRepository;
import com.devlovecode.aiperm.modules.system.rbac.repository.MenuRepository;
import com.devlovecode.aiperm.modules.system.rbac.repository.RoleRepository;
import com.devlovecode.aiperm.modules.system.rbac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SystemAccess {

	private final UserRepository userRepository;

	private final MenuRepository menuRepository;

	private final RoleRepository roleRepository;

	private final DeptRepository deptRepository;

	private final ConfigRepository configRepository;

	public Optional<SystemUserAccount> findUserById(Long userId) {
		return userRepository.findById(userId).map(this::toUserAccount);
	}

	public Optional<SystemUserAccount> findUserByUsername(String username) {
		return userRepository.findByUsername(username).map(this::toUserAccount);
	}

	public Optional<SystemUserAccount> findUserByPhone(String phone) {
		return userRepository.findByPhone(phone).map(this::toUserAccount);
	}

	public Optional<SystemUserAccount> findUserByEmail(String email) {
		return userRepository.findByEmail(email).map(this::toUserAccount);
	}

	public void updateLoginInfo(Long userId, String ip, LocalDateTime loginTime) {
		userRepository.updateLoginInfo(userId, ip, loginTime);
	}

	public boolean isAdmin(Long userId) {
		return userRepository.isAdmin(userId);
	}

	public List<String> getUserRoleKeys(Long userId) {
		return menuRepository.findRoleKeysByUserId(userId);
	}

	public List<String> getUserPermissions(Long userId) {
		return menuRepository.findPermissionsByUserId(userId);
	}

	public List<String> getAllEnabledPermissions() {
		return menuRepository.findAllEnabledPermissions();
	}

	public List<SystemMenuDescriptor> getAccessibleMenus(Long userId) {
		List<SysMenu> menus = isAdmin(userId)
				? menuRepository.findAllEnabled()
				: menuRepository.findByIds(menuRepository.findMenuIdsByUserId(userId));
		return menus.stream().map(this::toMenuDescriptor).toList();
	}

	public boolean getBooleanConfig(String key, boolean defaultValue) {
		return configRepository.findByConfigKey(key).map(config -> "1".equals(config.getConfigValue())).orElse(defaultValue);
	}

	public int getIntConfig(String key, int defaultValue) {
		return configRepository.findByConfigKey(key).map(config -> {
			try {
				return Integer.parseInt(config.getConfigValue());
			}
			catch (NumberFormatException ignored) {
				return defaultValue;
			}
		}).orElse(defaultValue);
	}

	public long countUsers() {
		return userRepository.count();
	}

	public long countRoles() {
		return roleRepository.count();
	}

	public long countMenus() {
		return menuRepository.count();
	}

	public Optional<SystemOnlineUserProfile> findOnlineUserProfile(Long userId) {
		return userRepository.findById(userId).map(user -> {
			String deptName = null;
			if (user.getDeptId() != null) {
				deptName = deptRepository.findById(user.getDeptId()).map(dept -> dept.getDeptName()).orElse(null);
			}

			List<String> roleNames = new ArrayList<>();
			List<Long> roleIds = userRepository.findRoleIdsByUserId(userId);
			if (roleIds != null) {
				for (Long roleId : roleIds) {
					roleRepository.findById(roleId).ifPresent(role -> roleNames.add(role.getRoleName()));
				}
			}

			return new SystemOnlineUserProfile(user.getNickname(), deptName, String.join(", ", roleNames));
		});
	}

	private SystemUserAccount toUserAccount(SysUser user) {
		return new SystemUserAccount(user.getId(), user.getUsername(), user.getPassword(), user.getStatus(),
				user.getNickname(), user.getAvatar(), user.getEmail(), user.getPhone());
	}

	private SystemMenuDescriptor toMenuDescriptor(SysMenu menu) {
		return new SystemMenuDescriptor(menu.getId(), menu.getMenuName(), menu.getParentId(), menu.getMenuType(),
				menu.getSort(), menu.getPath(), menu.getComponent(), menu.getPerms(), menu.getIcon(), menu.getVisible(),
				menu.getStatus());
	}

}
