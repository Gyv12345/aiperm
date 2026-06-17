package com.devlovecode.aiperm.modules.storage.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.storage.dto.FileDTO;
import com.devlovecode.aiperm.modules.storage.entity.SysFile;
import com.devlovecode.aiperm.modules.storage.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理 Controller
 *
 * <p>提供文件列表/详情/上传/删除接口，区别于 OssController（通用上传，不落库）。
 *
 * @author DevLoveCode
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/system/file")
@SaCheckLogin
@RequiredArgsConstructor
public class SysFileController {

	private final FileService fileService;

	@Operation(summary = "分页查询文件")
	@Log(title = "文件管理", operType = OperType.QUERY)
	@SaCheckPermission("system:file:list")
	@GetMapping
	public R<PageResult<SysFile>> page(@Validated(Views.Query.class) FileDTO dto) {
		return R.ok(fileService.queryPage(dto));
	}

	@Operation(summary = "文件详情")
	@SaCheckPermission("system:file:list")
	@GetMapping("/{id}")
	public R<SysFile> getById(@PathVariable Long id) {
		return R.ok(fileService.findById(id));
	}

	@Operation(summary = "上传文件")
	@Log(title = "文件管理", operType = OperType.UPLOAD)
	@SaCheckPermission("system:file:upload")
	@PostMapping("/upload")
	public R<SysFile> upload(@RequestParam("file") MultipartFile file) {
		return R.ok(fileService.upload(file));
	}

	@Operation(summary = "删除文件")
	@Log(title = "文件管理", operType = OperType.DELETE)
	@SaCheckPermission("system:file:delete")
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		fileService.delete(id);
		return R.ok();
	}

}
