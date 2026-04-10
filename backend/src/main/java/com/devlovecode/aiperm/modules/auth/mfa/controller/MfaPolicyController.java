package com.devlovecode.aiperm.modules.auth.mfa.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.auth.mfa.dto.MfaPolicyDTO;
import com.devlovecode.aiperm.modules.auth.mfa.service.MfaPolicyService;
import com.devlovecode.aiperm.modules.auth.mfa.vo.MfaPolicyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 2FA 策略管理控制器
 */
@Tag(name = "2FA策略管理")
@RestController
@RequestMapping("/system/mfa-policy")
@SaCheckLogin
@RequiredArgsConstructor
public class MfaPolicyController {

	private final MfaPolicyService mfaPolicyService;

	@Operation(summary = "查询所有2FA策略")
	@SaCheckPermission("system:mfa:policy")
	@GetMapping
	public R<List<MfaPolicyVO>> list() {
		return R.ok(mfaPolicyService.listAll());
	}

	@Operation(summary = "创建2FA策略")
	@SaCheckPermission("system:mfa:policy")
	@Log(title = "2FA策略管理", operType = OperType.CREATE)
	@PostMapping
	public R<Void> create(
			@RequestBody @Validated({ jakarta.validation.groups.Default.class, Views.Create.class }) MfaPolicyDTO dto) {
		mfaPolicyService.create(dto);
		return R.ok();
	}

	@Operation(summary = "更新2FA策略")
	@SaCheckPermission("system:mfa:policy")
	@Log(title = "2FA策略管理", operType = OperType.UPDATE)
	@PutMapping("/{id}")
	public R<Void> update(@PathVariable Long id,
			@RequestBody @Validated({ jakarta.validation.groups.Default.class, Views.Update.class }) MfaPolicyDTO dto) {
		mfaPolicyService.update(id, dto);
		return R.ok();
	}

	@Operation(summary = "删除2FA策略")
	@SaCheckPermission("system:mfa:policy")
	@Log(title = "2FA策略管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		mfaPolicyService.delete(id);
		return R.ok();
	}

}
