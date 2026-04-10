package com.devlovecode.aiperm.modules.system.rbac.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.system.rbac.dto.PostDTO;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysPost;
import com.devlovecode.aiperm.modules.system.rbac.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "岗位管理")
@RestController
@RequestMapping("/system/post")
@SaCheckLogin
@RequiredArgsConstructor
public class SysPostController {

	private final PostService postService;

	@Operation(summary = "分页查询岗位列表")
	@SaCheckPermission("system:post:list")
	@Log(title = "岗位管理", operType = OperType.QUERY)
	@GetMapping
	public R<PageResult<SysPost>> page(@Validated({ Default.class, Views.Query.class }) PostDTO dto) {
		return R.ok(postService.queryPage(dto));
	}

	@Operation(summary = "查询所有岗位")
	@SaCheckPermission("system:post:list")
	@GetMapping("/all")
	public R<List<SysPost>> list() {
		return R.ok(postService.listAll());
	}

	@Operation(summary = "根据ID查询岗位")
	@SaCheckPermission("system:post:list")
	@GetMapping("/{id}")
	public R<SysPost> getById(@PathVariable Long id) {
		return R.ok(postService.findById(id));
	}

	@Operation(summary = "创建岗位")
	@SaCheckPermission("system:post:create")
	@Log(title = "岗位管理", operType = OperType.CREATE)
	@PostMapping
	public R<Void> create(@RequestBody @Validated({ Default.class, Views.Create.class }) PostDTO dto) {
		postService.create(dto);
		return R.ok();
	}

	@Operation(summary = "更新岗位")
	@SaCheckPermission("system:post:update")
	@Log(title = "岗位管理", operType = OperType.UPDATE)
	@PutMapping("/{id}")
	public R<Void> update(@PathVariable Long id,
			@RequestBody @Validated({ Default.class, Views.Update.class }) PostDTO dto) {
		postService.update(id, dto);
		return R.ok();
	}

	@Operation(summary = "删除岗位")
	@SaCheckPermission("system:post:delete")
	@Log(title = "岗位管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		postService.delete(id);
		return R.ok();
	}

}
