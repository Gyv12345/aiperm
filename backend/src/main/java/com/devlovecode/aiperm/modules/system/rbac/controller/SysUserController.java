package com.devlovecode.aiperm.modules.system.rbac.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.common.vo.ImportResultVO;
import com.devlovecode.aiperm.modules.system.rbac.dto.UserDTO;
import com.devlovecode.aiperm.modules.system.rbac.service.UserService;
import com.devlovecode.aiperm.modules.system.rbac.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@SaCheckLogin
@RequiredArgsConstructor
public class SysUserController {

	private final UserService userService;

	@Operation(summary = "分页查询用户列表")
	@SaCheckPermission("system:user:list")
	@Log(title = "用户管理", operType = OperType.QUERY)
	@GetMapping
	public R<PageResult<UserVO>> page(@Validated({ Default.class, Views.Query.class }) UserDTO dto) {
		return R.ok(userService.queryPage(dto));
	}

	@Operation(summary = "导出用户")
	@SaCheckPermission("system:user:export")
	@Log(title = "用户管理", operType = OperType.EXPORT)
	@GetMapping("/export")
	public void export(@Validated({ Default.class, Views.Query.class }) UserDTO dto, HttpServletResponse response) {
		userService.export(dto, response);
	}

	@Operation(summary = "下载用户导入模板")
	@SaCheckPermission("system:user:import")
	@GetMapping("/import-template")
	public void importTemplate(HttpServletResponse response) {
		userService.downloadImportTemplate(response);
	}

	@Operation(summary = "导入用户")
	@SaCheckPermission("system:user:import")
	@Log(title = "用户管理", operType = OperType.IMPORT)
	@PostMapping("/import")
	public R<ImportResultVO> importUsers(@RequestParam("file") MultipartFile file) {
		return R.ok(userService.importUsers(file));
	}

	@Operation(summary = "根据ID查询用户")
	@SaCheckPermission("system:user:list")
	@GetMapping("/{id}")
	public R<UserVO> getById(@PathVariable Long id) {
		return R.ok(userService.findById(id));
	}

	@Operation(summary = "创建用户")
	@SaCheckPermission("system:user:create")
	@Log(title = "用户管理", operType = OperType.CREATE)
	@PostMapping
	public R<Void> create(@RequestBody @Validated({ Default.class, Views.Create.class }) UserDTO dto) {
		userService.create(dto);
		return R.ok();
	}

	@Operation(summary = "更新用户")
	@SaCheckPermission("system:user:update")
	@Log(title = "用户管理", operType = OperType.UPDATE)
	@PutMapping("/{id}")
	public R<Void> update(@PathVariable Long id,
			@RequestBody @Validated({ Default.class, Views.Update.class }) UserDTO dto) {
		userService.update(id, dto);
		return R.ok();
	}

	@Operation(summary = "删除用户")
	@SaCheckPermission("system:user:delete")
	@Log(title = "用户管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		userService.delete(id);
		return R.ok();
	}

	@Operation(summary = "批量删除用户")
	@SaCheckPermission("system:user:delete")
	@Log(title = "用户管理", operType = OperType.DELETE)
	@DeleteMapping("/batch")
	public R<Void> deleteBatch(@RequestBody List<Long> ids) {
		userService.deleteBatch(ids);
		return R.ok();
	}

	@Operation(summary = "重置用户密码")
	@SaCheckPermission("system:user:update")
	@Log(title = "用户管理", operType = OperType.UPDATE)
	@PutMapping("/{id}/reset-password")
	public R<Void> resetPassword(@PathVariable Long id, @RequestBody UserDTO dto) {
		userService.resetPassword(id, dto.getNewPassword());
		return R.ok();
	}

	@Operation(summary = "修改用户状态")
	@SaCheckPermission("system:user:update")
	@Log(title = "用户管理", operType = OperType.UPDATE)
	@PutMapping("/{id}/status")
	public R<Void> changeStatus(@PathVariable Long id, @RequestParam(name = "status") Integer status) {
		userService.changeStatus(id, status);
		return R.ok();
	}

}
