package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.system.dto.RoleDTO;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import com.devlovecode.aiperm.modules.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@SaCheckLogin
@RequiredArgsConstructor
public class SysRoleController {

	private final RoleService roleService;

	@Operation(summary = "分页查询角色列表")
	@SaCheckPermission("system:role:list")
	@Log(title = "角色管理", operType = OperType.QUERY)
	@GetMapping
	public R<PageResult<SysRole>> page(@Validated({ Default.class, Views.Query.class }) RoleDTO dto) {
		return R.ok(roleService.queryPage(dto));
	}

	@Operation(summary = "查询所有角色")
	@SaCheckPermission("system:role:list")
	@GetMapping("/all")
	public R<List<SysRole>> list() {
		return R.ok(roleService.listAll());
	}

	@Operation(summary = "根据ID查询角色")
	@SaCheckPermission("system:role:list")
	@GetMapping("/{id}")
	public R<SysRole> getById(@PathVariable Long id) {
		return R.ok(roleService.findById(id));
	}

	@Operation(summary = "获取角色的菜单ID列表")
	@SaCheckPermission("system:role:list")
	@GetMapping("/{id}/menus")
	public R<List<Long>> getRoleMenus(@PathVariable Long id) {
		return R.ok(roleService.getMenuIds(id));
	}

	@Operation(summary = "创建角色")
	@SaCheckPermission("system:role:create")
	@Log(title = "角色管理", operType = OperType.CREATE)
	@PostMapping
	public R<Void> create(@RequestBody @Validated({ Default.class, Views.Create.class }) RoleDTO dto) {
		roleService.create(dto);
		return R.ok();
	}

	@Operation(summary = "更新角色")
	@SaCheckPermission("system:role:update")
	@Log(title = "角色管理", operType = OperType.UPDATE)
	@PutMapping("/{id}")
	public R<Void> update(@PathVariable Long id,
			@RequestBody @Validated({ Default.class, Views.Update.class }) RoleDTO dto) {
		roleService.update(id, dto);
		return R.ok();
	}

	@Operation(summary = "删除角色")
	@SaCheckPermission("system:role:delete")
	@Log(title = "角色管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		roleService.delete(id);
		return R.ok();
	}

	@Operation(summary = "分配角色菜单")
	@SaCheckPermission("system:role:update")
	@Log(title = "角色管理", operType = OperType.UPDATE)
	@PostMapping("/{id}/menus")
	public R<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
		roleService.assignMenus(id, menuIds);
		return R.ok();
	}

}
