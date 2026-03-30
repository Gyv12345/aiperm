package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.system.dto.DeptDTO;
import com.devlovecode.aiperm.modules.system.entity.SysDept;
import com.devlovecode.aiperm.modules.system.service.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/system/dept")
@SaCheckLogin
@RequiredArgsConstructor
public class SysDeptController {

	private final DeptService deptService;

	@Operation(summary = "查询部门树")
	@SaCheckPermission("system:dept:list")
	@GetMapping("/tree")
	public R<List<SysDept>> tree() {
		return R.ok(deptService.getDeptTree());
	}

	@Operation(summary = "查询所有部门")
	@SaCheckPermission("system:dept:list")
	@GetMapping
	public R<List<SysDept>> list() {
		return R.ok(deptService.listAll());
	}

	@Operation(summary = "根据ID查询部门")
	@SaCheckPermission("system:dept:list")
	@GetMapping("/{id}")
	public R<SysDept> getById(@PathVariable Long id) {
		return R.ok(deptService.findById(id));
	}

	@Operation(summary = "查询子部门列表")
	@SaCheckPermission("system:dept:list")
	@GetMapping("/children/{parentId}")
	public R<List<SysDept>> getChildren(@PathVariable Long parentId) {
		return R.ok(deptService.listByParentId(parentId));
	}

	@Operation(summary = "创建部门")
	@SaCheckPermission("system:dept:create")
	@Log(title = "部门管理", operType = OperType.CREATE)
	@PostMapping
	public R<Void> create(@RequestBody @Validated({ Default.class, Views.Create.class }) DeptDTO dto) {
		deptService.create(dto);
		return R.ok();
	}

	@Operation(summary = "更新部门")
	@SaCheckPermission("system:dept:update")
	@Log(title = "部门管理", operType = OperType.UPDATE)
	@PutMapping("/{id}")
	public R<Void> update(@PathVariable Long id,
			@RequestBody @Validated({ Default.class, Views.Update.class }) DeptDTO dto) {
		deptService.update(id, dto);
		return R.ok();
	}

	@Operation(summary = "删除部门")
	@SaCheckPermission("system:dept:delete")
	@Log(title = "部门管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		deptService.delete(id);
		return R.ok();
	}

}
