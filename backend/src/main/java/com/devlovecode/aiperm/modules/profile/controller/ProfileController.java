package com.devlovecode.aiperm.modules.profile.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.devlovecode.aiperm.common.annotation.Idempotent;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.AccessLimitScope;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.profile.dto.PasswordDTO;
import com.devlovecode.aiperm.modules.profile.dto.ProfileDTO;
import com.devlovecode.aiperm.modules.profile.service.ProfileService;
import com.devlovecode.aiperm.modules.profile.vo.LoginLogVO;
import com.devlovecode.aiperm.modules.profile.vo.ProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心 Controller
 */
@Tag(name = "个人中心")
@RestController
@RequestMapping("/profile")
@SaCheckLogin
@RequiredArgsConstructor
public class ProfileController {

	private final ProfileService profileService;

	@Operation(summary = "获取个人信息")
	@GetMapping("/info")
	public R<ProfileVO> getProfile() {
		return R.ok(profileService.getProfile());
	}

	@Operation(summary = "修改个人信息")
	@Log(title = "个人中心", operType = OperType.UPDATE)
	@PutMapping("/info")
	@Idempotent(expireSeconds = 5, scope = AccessLimitScope.USER, key = "profile:update-info", message = "请勿重复提交个人信息")
	public R<Void> updateProfile(@RequestBody @Valid ProfileDTO dto) {
		profileService.updateProfile(dto);
		return R.ok();
	}

	@Operation(summary = "修改密码")
	@Log(title = "个人中心", operType = OperType.UPDATE)
	@PutMapping("/password")
	@Idempotent(expireSeconds = 8, scope = AccessLimitScope.USER, key = "profile:update-password",
			message = "请勿重复提交修改密码请求")
	public R<Void> updatePassword(@RequestBody @Valid PasswordDTO dto) {
		profileService.updatePassword(dto);
		return R.ok();
	}

	@Operation(summary = "获取登录日志")
	@GetMapping("/logs")
	public R<PageResult<LoginLogVO>> getLoginLogs(@RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
			@RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
		return R.ok(profileService.getLoginLogs(pageNum, pageSize));
	}

}
