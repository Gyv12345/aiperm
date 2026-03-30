package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.system.dto.MenuDTO;
import com.devlovecode.aiperm.modules.system.entity.SysMenu;
import com.devlovecode.aiperm.modules.system.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@SaCheckLogin
@RequiredArgsConstructor
public class SysMenuController {

	private final MenuService menuService;

	@Operation(summary = "查询菜单树")
	@SaCheckPermission("system:menu:list")
	@GetMapping("/tree")
	public R<List<SysMenu>> tree() {
		return R.ok(menuService.getMenuTree());
	}

	@Operation(summary = "查询所有菜单")
	@SaCheckPermission("system:menu:list")
	@GetMapping
	public R<List<SysMenu>> list() {
		return R.ok(menuService.listAll());
	}

	@Operation(summary = "根据ID查询菜单")
	@SaCheckPermission("system:menu:list")
	@GetMapping("/{id}")
	public R<SysMenu> getById(@PathVariable Long id) {
		return R.ok(menuService.findById(id));
	}

	@Operation(summary = "查询子菜单列表")
	@SaCheckPermission("system:menu:list")
	@GetMapping("/children/{parentId}")
	public R<List<SysMenu>> getChildren(@PathVariable Long parentId) {
		return R.ok(menuService.listByParentId(parentId));
	}

	@Operation(summary = "创建菜单")
	@SaCheckPermission("system:menu:create")
	@Log(title = "菜单管理", operType = OperType.CREATE)
	@PostMapping
	public R<Void> create(@RequestBody @Validated({ Default.class, Views.Create.class }) MenuDTO dto) {
		menuService.create(dto);
		return R.ok();
	}

	@Operation(summary = "更新菜单")
	@SaCheckPermission("system:menu:update")
	@Log(title = "菜单管理", operType = OperType.UPDATE)
	@PutMapping("/{id}")
	public R<Void> update(@PathVariable Long id,
			@RequestBody @Validated({ Default.class, Views.Update.class }) MenuDTO dto) {
		menuService.update(id, dto);
		return R.ok();
	}

	@Operation(summary = "删除菜单")
	@SaCheckPermission("system:menu:delete")
	@Log(title = "菜单管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		menuService.delete(id);
		return R.ok();
	}

}
