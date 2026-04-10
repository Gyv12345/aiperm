package com.devlovecode.aiperm.modules.monitor.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.util.ExcelExportHelper;
import com.devlovecode.aiperm.common.util.UserAgentUtils;
import com.devlovecode.aiperm.modules.monitor.export.OnlineUserExportModel;
import com.devlovecode.aiperm.modules.monitor.repository.OnlineUserRepository;
import com.devlovecode.aiperm.modules.monitor.vo.OnlineUserVO;
import com.devlovecode.aiperm.modules.system.api.SystemAccess;
import com.devlovecode.aiperm.modules.monitor.entity.SysOnlineUser;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnlineUserService {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final OnlineUserRepository onlineUserRepo;

	private final OnlineUserHeartbeatService onlineUserHeartbeatService;

	private final SystemAccess systemAccess;

	private final ExcelExportHelper excelExportHelper;

	@Transactional
	public void registerLoginSession(Long userId, String username, String ip, String userAgent) {
		String token = StpUtil.getTokenValue();
		if (token == null || token.isBlank() || userId == null) {
			return;
		}

		SysOnlineUser entity = onlineUserRepo.findByTokenAndDeleted(token, 0).orElseGet(SysOnlineUser::new);
		LocalDateTime now = LocalDateTime.now();
		entity.setUserId(userId);
		entity.setUsername(username);
		entity.setToken(token);
		entity.setIp(ip);
		entity.setBrowser(UserAgentUtils.resolveBrowser(userAgent));
		entity.setOs(UserAgentUtils.resolveOs(userAgent));
		entity.setLoginTime(entity.getLoginTime() == null ? now : entity.getLoginTime());
		entity.setLastAccessTime(now);
		if (entity.getId() == null) {
			entity.setCreateTime(now);
			entity.setCreateBy(username);
		}
		entity.setUpdateTime(now);
		entity.setUpdateBy(username);
		onlineUserRepo.save(entity);
		onlineUserHeartbeatService.recordHeartbeat(token, now);
	}

	public void touchCurrentSession() {
		if (!StpUtil.isLogin()) {
			return;
		}
		String token = StpUtil.getTokenValue();
		if (token == null || token.isBlank()) {
			return;
		}
		onlineUserHeartbeatService.recordHeartbeat(token, LocalDateTime.now());
	}

	@Transactional
	public void logoutCurrentToken() {
		String token = StpUtil.getTokenValue();
		if (token == null || token.isBlank()) {
			return;
		}
		onlineUserHeartbeatService.removeHeartbeat(token);
		onlineUserRepo.findByTokenAndDeleted(token, 0)
			.ifPresent(entity -> onlineUserRepo.softDelete(entity.getId(), LocalDateTime.now()));
	}

	@Transactional
	public long countActiveSessions() {
		syncOnlineSessions();
		return onlineUserRepo.countByDeleted(0);
	}

	@Transactional
	public PageResult<OnlineUserVO> queryPage(String username, String ip, Integer page, Integer pageSize) {
		syncOnlineSessions();
		Page<SysOnlineUser> jpaPage = onlineUserRepo.queryPage(username, ip, page, pageSize);
		return PageResult.fromJpaPage(jpaPage).map(this::toVO);
	}

	@Transactional
	public List<OnlineUserVO> listForExport(String username, String ip) {
		syncOnlineSessions();
		return onlineUserRepo.findAllByDeletedOrderByLastAccessTimeDesc(0)
			.stream()
			.filter(item -> username == null || username.isBlank() || item.getUsername().contains(username))
			.filter(item -> ip == null || ip.isBlank() || (item.getIp() != null && item.getIp().contains(ip)))
			.map(this::toVO)
			.toList();
	}

	public void export(String username, String ip, HttpServletResponse response) {
		List<OnlineUserExportModel> rows = listForExport(username, ip).stream().map(this::toExportModel).toList();
		excelExportHelper.export(response, "online-users", OnlineUserExportModel.class, rows);
	}

	@Transactional
	public void forceLogout(Long id) {
		SysOnlineUser entity = onlineUserRepo.findByIdAndDeleted(id, 0).orElseThrow(() -> new BusinessException("在线会话不存在"));
		StpUtil.logoutByTokenValue(entity.getToken());
		onlineUserHeartbeatService.removeHeartbeat(entity.getToken());
		onlineUserRepo.softDelete(entity.getId(), LocalDateTime.now());
	}

	@Transactional
	public void forceLogoutBatch(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}
		for (Long id : ids) {
			onlineUserRepo.findByIdAndDeleted(id, 0).ifPresent(entity -> {
				StpUtil.logoutByTokenValue(entity.getToken());
				onlineUserHeartbeatService.removeHeartbeat(entity.getToken());
				onlineUserRepo.softDelete(entity.getId(), LocalDateTime.now());
			});
		}
	}

	private void syncOnlineSessions() {
		onlineUserHeartbeatService.flushToDatabase();
		cleanupInvalidSessions();
	}

	private void cleanupInvalidSessions() {
		List<SysOnlineUser> sessions = onlineUserRepo.findAllByDeletedOrderByLastAccessTimeDesc(0);
		LocalDateTime now = LocalDateTime.now();
		for (SysOnlineUser session : sessions) {
			if (isTokenInvalid(session.getToken())) {
				onlineUserHeartbeatService.removeHeartbeat(session.getToken());
				onlineUserRepo.softDelete(session.getId(), now);
			}
		}
	}

	private boolean isTokenInvalid(String token) {
		if (token == null || token.isBlank()) {
			return true;
		}
		try {
			Object loginId = StpUtil.getLoginIdByToken(token);
			long timeout = StpUtil.getTokenTimeout(token);
			return loginId == null || timeout == -2;
		}
		catch (Exception e) {
			return true;
		}
	}

	private OnlineUserVO toVO(SysOnlineUser entity) {
		OnlineUserVO vo = new OnlineUserVO();
		vo.setId(entity.getId());
		vo.setUserId(entity.getUserId());
		vo.setUsername(entity.getUsername());
		vo.setToken(entity.getToken());
		vo.setIp(entity.getIp());
		vo.setBrowser(entity.getBrowser());
		vo.setOs(entity.getOs());
		vo.setLoginTime(entity.getLoginTime());
		vo.setLastAccessTime(entity.getLastAccessTime());
		vo.setCurrentSession(entity.getToken() != null && entity.getToken().equals(StpUtil.getTokenValue()));

		try {
			long timeout = StpUtil.getTokenTimeout(entity.getToken());
			vo.setTokenTimeout(timeout < 0 ? 0 : timeout);
		}
		catch (Exception e) {
			vo.setTokenTimeout(0L);
		}

		systemAccess.findOnlineUserProfile(entity.getUserId()).ifPresent(profile -> {
			vo.setNickname(profile.nickname());
			vo.setDeptName(profile.deptName());
			vo.setRoleNames(profile.roleNames());
		});

		return vo;
	}

	private OnlineUserExportModel toExportModel(OnlineUserVO vo) {
		OnlineUserExportModel model = new OnlineUserExportModel();
		model.setUserId(vo.getUserId());
		model.setUsername(vo.getUsername());
		model.setNickname(vo.getNickname());
		model.setDeptName(vo.getDeptName());
		model.setRoleNames(vo.getRoleNames());
		model.setIp(vo.getIp());
		model.setBrowser(vo.getBrowser());
		model.setOs(vo.getOs());
		model.setLoginTime(formatDateTime(vo.getLoginTime()));
		model.setLastAccessTime(formatDateTime(vo.getLastAccessTime()));
		model.setTokenTimeout(vo.getTokenTimeout());
		return model;
	}

	private String formatDateTime(LocalDateTime value) {
		return value == null ? "" : value.format(DATE_TIME_FORMATTER);
	}

}
