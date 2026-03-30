package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.system.dto.DictTypeDTO;
import com.devlovecode.aiperm.modules.system.service.DictTypeService;
import com.devlovecode.aiperm.modules.system.vo.DictTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "字典类型管理")
@RestController
@RequestMapping("/system/dict/type")
@SaCheckLogin
@RequiredArgsConstructor
public class SysDictTypeController {

	private final DictTypeService dictTypeService;

	@Operation(summary = "分页查询字典类型")
	@SaCheckPermission("system:dict:list")
	@Log(title = "字典类型管理", operType = OperType.QUERY)
	@GetMapping
	public R<PageResult<DictTypeVO>> list(@Validated({ Default.class, Views.Query.class }) DictTypeDTO dto) {
		return R.ok(dictTypeService.queryPage(dto));
	}

	@Operation(summary = "查询所有启用的字典类型")
	@SaCheckPermission("system:dict:list")
	@GetMapping("/all")
	public R<List<DictTypeVO>> all() {
		return R.ok(dictTypeService.findAllEnabled());
	}

	@Operation(summary = "查询字典类型详情")
	@SaCheckPermission("system:dict:list")
	@GetMapping("/{id}")
	public R<DictTypeVO> detail(@PathVariable Long id) {
		return R.ok(dictTypeService.findById(id));
	}

	@Operation(summary = "创建字典类型")
	@SaCheckPermission("system:dict:create")
	@Log(title = "字典类型管理", operType = OperType.CREATE)
	@PostMapping
	public R<Long> create(@RequestBody @Validated({ Default.class, Views.Create.class }) DictTypeDTO dto) {
		return R.ok(dictTypeService.create(dto));
	}

	@Operation(summary = "更新字典类型")
	@SaCheckPermission("system:dict:update")
	@Log(title = "字典类型管理", operType = OperType.UPDATE)
	@PutMapping("/{id}")
	public R<Void> update(@PathVariable Long id,
			@RequestBody @Validated({ Default.class, Views.Update.class }) DictTypeDTO dto) {
		dictTypeService.update(id, dto);
		return R.ok();
	}

	@Operation(summary = "删除字典类型")
	@SaCheckPermission("system:dict:delete")
	@Log(title = "字典类型管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		dictTypeService.delete(id);
		return R.ok();
	}

}
