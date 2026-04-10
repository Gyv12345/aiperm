package com.devlovecode.aiperm.modules.auth.mfa.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.auth.mfa.dto.MfaVerifyDTO;
import com.devlovecode.aiperm.modules.auth.mfa.service.MfaService;
import com.devlovecode.aiperm.modules.auth.mfa.vo.MfaQrcodeVO;
import com.devlovecode.aiperm.modules.auth.mfa.vo.MfaStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 双因素认证（2FA）控制器
 */
@Tag(name = "双因素认证（2FA）")
@RestController
@RequestMapping("/mfa")
@SaCheckLogin
@RequiredArgsConstructor
public class MfaController {

	private final MfaService mfaService;

	@Operation(summary = "获取2FA绑定状态")
	@GetMapping("/status")
	public R<MfaStatusVO> status() {
		return R.ok(mfaService.getStatus());
	}

	@Operation(summary = "获取绑定二维码（TOTP URI）")
	@GetMapping("/bind/qrcode")
	public R<MfaQrcodeVO> qrcode() {
		return R.ok(mfaService.generateQrCode());
	}

	@Operation(summary = "确认绑定（验证TOTP码后持久化）")
	@Log(title = "2FA管理", operType = OperType.CREATE)
	@PostMapping("/bind/confirm")
	public R<Void> confirmBind(@RequestBody @Valid MfaVerifyDTO dto) {
		mfaService.confirmBind(dto.getCode());
		return R.ok();
	}

	@Operation(summary = "解绑2FA（需先验证）")
	@Log(title = "2FA管理", operType = OperType.DELETE)
	@PostMapping("/unbind")
	public R<Void> unbind(@RequestBody @Valid MfaVerifyDTO dto) {
		mfaService.unbind(dto.getCode());
		return R.ok();
	}

	@Operation(summary = "验证2FA（敏感操作前调用，写入Redis有效期）")
	@Log(title = "2FA管理", operType = OperType.OTHER)
	@PostMapping("/verify")
	public R<Void> verify(@RequestBody @Valid MfaVerifyDTO dto) {
		mfaService.verify(dto.getCode());
		return R.ok();
	}

}
