package com.devlovecode.aiperm.modules.approval.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.approval.dto.ImConfigDTO;
import com.devlovecode.aiperm.modules.approval.service.ImConfigService;
import com.devlovecode.aiperm.modules.approval.vo.ImConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "IM平台配置")
@RestController
@RequestMapping("/system/im-config")
@SaCheckLogin
@RequiredArgsConstructor
public class SysImConfigController {

	private final ImConfigService imConfigService;

	@Operation(summary = "查询全部IM平台配置")
	@SaCheckPermission("system:im-config:list")
	@GetMapping
	public R<List<ImConfigVO>> list() {
		return R.ok(imConfigService.listAll());
	}

	@Operation(summary = "查询单个平台配置")
	@SaCheckPermission("system:im-config:list")
	@GetMapping("/{platform}")
	public R<ImConfigVO> detail(@PathVariable String platform) {
		return R.ok(imConfigService.getConfig(platform));
	}

	@Operation(summary = "更新平台配置")
	@SaCheckPermission("system:im-config:update")
	@Log(title = "IM平台配置", operType = OperType.UPDATE)
	@PutMapping("/{platform}")
	public R<Void> update(@PathVariable String platform,
			@RequestBody @Validated({ Default.class, Views.Update.class }) ImConfigDTO dto) {
		imConfigService.updateConfig(platform, dto);
		return R.ok();
	}

}
