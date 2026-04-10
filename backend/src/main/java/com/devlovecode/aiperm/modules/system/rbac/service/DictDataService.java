package com.devlovecode.aiperm.modules.system.rbac.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.util.ExcelExportHelper;
import com.devlovecode.aiperm.common.util.ExcelImportHelper;
import com.devlovecode.aiperm.common.vo.ImportResultVO;
import com.devlovecode.aiperm.modules.system.rbac.dto.DictDataDTO;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysDictData;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysDictType;
import com.devlovecode.aiperm.modules.system.rbac.export.DictDataExportModel;
import com.devlovecode.aiperm.modules.system.rbac.export.DictDataImportModel;
import com.devlovecode.aiperm.modules.system.rbac.repository.DictDataRepository;
import com.devlovecode.aiperm.modules.system.rbac.repository.DictTypeRepository;
import com.devlovecode.aiperm.modules.system.rbac.vo.DictDataVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dict")
public class DictDataService {

	private final DictDataRepository dictDataRepo;

	private final DictTypeRepository dictTypeRepo;

	private final ExcelExportHelper excelExportHelper;

	private final ExcelImportHelper excelImportHelper;

	/**
	 * 根据字典类型查询
	 */
	@Cacheable(key = "#p0")
	public List<DictDataVO> listByDictType(String dictType) {
		return dictDataRepo.findByDictTypeAndStatusAndDeletedOrderBySortAsc(dictType, 1, 0)
			.stream()
			.map(this::toVO)
			.collect(Collectors.toList());
	}

	public void export(String dictType, HttpServletResponse response) {
		List<DictDataExportModel> rows = listForExport(dictType).stream().map(this::toExportModel).toList();
		excelExportHelper.export(response, "dict-data", DictDataExportModel.class, rows);
	}

	public void downloadImportTemplate(HttpServletResponse response) {
		excelExportHelper.export(response, "dict-data-import-template", DictDataImportModel.class, List.of());
	}

	/**
	 * 创建
	 */
	@Transactional
	@CacheEvict(allEntries = true)
	public void create(DictDataDTO dto) {
		SysDictData entity = new SysDictData();
		entity.setDictType(dto.getDictType());
		entity.setDictLabel(dto.getDictLabel());
		entity.setDictValue(dto.getDictValue());
		entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
		entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
		entity.setListClass(dto.getListClass() != null ? dto.getListClass() : "");
		entity.setRemark(dto.getRemark());
		entity.setCreateBy(getCurrentUsername());
		entity.setCreateTime(LocalDateTime.now());

		dictDataRepo.save(entity);
	}

	/**
	 * 更新
	 */
	@Transactional
	@CacheEvict(allEntries = true)
	public void update(Long id, DictDataDTO dto) {
		SysDictData entity = dictDataRepo.findById(id).orElseThrow(() -> new BusinessException("字典数据不存在"));

		entity.setDictLabel(dto.getDictLabel());
		entity.setDictValue(dto.getDictValue());
		entity.setSort(dto.getSort());
		entity.setStatus(dto.getStatus());
		entity.setListClass(dto.getListClass() != null ? dto.getListClass() : "");
		entity.setRemark(dto.getRemark());
		entity.setUpdateBy(getCurrentUsername());
		entity.setUpdateTime(LocalDateTime.now());

		dictDataRepo.save(entity);
	}

	/**
	 * 删除
	 */
	@Transactional
	@CacheEvict(allEntries = true)
	public void delete(Long id) {
		if (!dictDataRepo.existsById(id)) {
			throw new BusinessException("字典数据不存在");
		}
		dictDataRepo.softDelete(id, LocalDateTime.now());
	}

	@Transactional
	@CacheEvict(allEntries = true)
	public ImportResultVO importData(MultipartFile file) {
		List<DictDataImportModel> rows = excelImportHelper.read(file, DictDataImportModel.class);
		ImportResultVO result = new ImportResultVO();
		int rowNumber = 2;
		for (DictDataImportModel row : rows) {
			try {
				importSingleRow(row);
				result.addSuccess();
			}
			catch (Exception e) {
				result.addError(rowNumber, e.getMessage());
			}
			rowNumber++;
		}
		return result;
	}

	private List<SysDictData> listForExport(String dictType) {
		if (dictType == null || dictType.isBlank()) {
			return dictDataRepo.findByDeletedOrderByDictTypeAscSortAsc(0);
		}
		return dictDataRepo.findByDictTypeAndDeletedOrderBySortAsc(dictType.trim(), 0);
	}

	private void importSingleRow(DictDataImportModel row) {
		String dictType = requireText(row.getDictType(), "字典类型不能为空");
		String dictLabel = requireText(row.getDictLabel(), "字典标签不能为空");
		String dictValue = requireText(row.getDictValue(), "字典键值不能为空");

		SysDictType dictTypeEntity = dictTypeRepo.findByDictType(dictType)
			.orElseThrow(() -> new BusinessException("字典类型不存在: " + dictType));

		SysDictData entity = dictDataRepo.findByDictTypeAndDictValueAndDeleted(dictType, dictValue, 0)
			.orElseGet(SysDictData::new);
		LocalDateTime now = LocalDateTime.now();
		boolean isNew = entity.getId() == null;

		entity.setDictType(dictTypeEntity.getDictType());
		entity.setDictLabel(dictLabel);
		entity.setDictValue(dictValue);
		entity.setSort(row.getSort() == null ? 0 : row.getSort());
		entity.setStatus(resolveStatus(row.getStatusText()));
		entity.setListClass(normalizeText(row.getListClass()));
		entity.setRemark(normalizeText(row.getRemark()));
		if (isNew) {
			entity.setCreateBy(getCurrentUsername());
			entity.setCreateTime(now);
		}
		else {
			entity.setUpdateBy(getCurrentUsername());
			entity.setUpdateTime(now);
		}
		dictDataRepo.save(entity);
	}

	private DictDataVO toVO(SysDictData entity) {
		DictDataVO vo = new DictDataVO();
		vo.setId(entity.getId());
		vo.setDictType(entity.getDictType());
		vo.setDictLabel(entity.getDictLabel());
		vo.setDictValue(entity.getDictValue());
		vo.setSort(entity.getSort());
		vo.setStatus(entity.getStatus());
		vo.setListClass(entity.getListClass());
		vo.setRemark(entity.getRemark());
		return vo;
	}

	private DictDataExportModel toExportModel(SysDictData entity) {
		DictDataExportModel model = new DictDataExportModel();
		model.setDictType(entity.getDictType());
		model.setDictLabel(entity.getDictLabel());
		model.setDictValue(entity.getDictValue());
		model.setSort(entity.getSort());
		model.setStatusText(entity.getStatus() != null && entity.getStatus() == 1 ? "启用" : "禁用");
		model.setListClass(entity.getListClass());
		model.setRemark(entity.getRemark());
		return model;
	}

	private String requireText(String value, String message) {
		String normalized = normalizeText(value);
		if (normalized == null) {
			throw new BusinessException(message);
		}
		return normalized;
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	private Integer resolveStatus(String statusText) {
		String normalized = normalizeText(statusText);
		if (normalized == null) {
			return 1;
		}
		return switch (normalized) {
			case "启用" -> 1;
			case "禁用", "停用" -> 0;
			default -> throw new BusinessException("状态仅支持启用/禁用");
		};
	}

	private String getCurrentUsername() {
		try {
			return StpUtil.getLoginIdAsString();
		}
		catch (Exception e) {
			return "system";
		}
	}

}
