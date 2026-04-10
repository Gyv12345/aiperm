package com.devlovecode.aiperm.modules.system.rbac.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.common.vo.ImportResultVO;
import com.devlovecode.aiperm.modules.system.rbac.dto.DictDataDTO;
import com.devlovecode.aiperm.modules.system.rbac.service.DictDataService;
import com.devlovecode.aiperm.modules.system.rbac.vo.DictDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "字典数据管理")
@RestController
@RequestMapping("/system/dict/data")
@SaCheckLogin
@RequiredArgsConstructor
public class SysDictDataController {

	private final DictDataService dictDataService;

	@Operation(summary = "根据字典类型查询字典数据")
	@SaCheckPermission("system:dict:list")
	@GetMapping("/list")
	public R<List<DictDataVO>> listByDictType(@Validated({ Default.class, Views.Query.class }) DictDataDTO dto) {
		return R.ok(dictDataService.listByDictType(dto.getDictType()));
	}

	@Operation(summary = "导出字典数据")
	@SaCheckPermission("system:dict:export")
	@Log(title = "字典数据管理", operType = OperType.EXPORT)
	@GetMapping("/export")
	public void export(@RequestParam(name = "dictType", required = false) String dictType, HttpServletResponse response) {
		dictDataService.export(dictType, response);
	}

	@Operation(summary = "下载字典数据导入模板")
	@SaCheckPermission("system:dict:import")
	@GetMapping("/import-template")
	public void importTemplate(HttpServletResponse response) {
		dictDataService.downloadImportTemplate(response);
	}

	@Operation(summary = "导入字典数据")
	@SaCheckPermission("system:dict:import")
	@Log(title = "字典数据管理", operType = OperType.IMPORT)
	@PostMapping("/import")
	public R<ImportResultVO> importData(@RequestParam("file") MultipartFile file) {
		return R.ok(dictDataService.importData(file));
	}

	@Operation(summary = "创建字典数据")
	@SaCheckPermission("system:dict:create")
	@Log(title = "字典数据管理", operType = OperType.CREATE)
	@PostMapping
	public R<Void> create(@RequestBody @Validated({ Default.class, Views.Create.class }) DictDataDTO dto) {
		dictDataService.create(dto);
		return R.ok();
	}

	@Operation(summary = "更新字典数据")
	@SaCheckPermission("system:dict:update")
	@Log(title = "字典数据管理", operType = OperType.UPDATE)
	@PutMapping("/{id}")
	public R<Void> update(@PathVariable Long id,
			@RequestBody @Validated({ Default.class, Views.Update.class }) DictDataDTO dto) {
		dictDataService.update(id, dto);
		return R.ok();
	}

	@Operation(summary = "删除字典数据")
	@SaCheckPermission("system:dict:delete")
	@Log(title = "字典数据管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		dictDataService.delete(id);
		return R.ok();
	}

}
