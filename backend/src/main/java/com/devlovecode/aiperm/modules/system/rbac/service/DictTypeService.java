package com.devlovecode.aiperm.modules.system.rbac.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.util.ExcelExportHelper;
import com.devlovecode.aiperm.modules.system.rbac.dto.DictTypeDTO;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysDictType;
import com.devlovecode.aiperm.modules.system.rbac.export.DictTypeExportModel;
import com.devlovecode.aiperm.modules.system.rbac.repository.DictTypeRepository;
import com.devlovecode.aiperm.modules.system.rbac.vo.DictTypeVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictTypeService {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final DictTypeRepository dictTypeRepo;

	private final ExcelExportHelper excelExportHelper;

	/**
	 * 分页查询
	 */
	public PageResult<DictTypeVO> queryPage(DictTypeDTO dto) {
		Page<SysDictType> jpaPage = dictTypeRepo.queryPage(dto.getDictName(), dto.getDictType(), dto.getStatus(),
				dto.getPage(), dto.getPageSize());
		return PageResult.fromJpaPage(jpaPage).map(this::toVO);
	}

	/**
	 * 查询详情
	 */
	public DictTypeVO findById(Long id) {
		return dictTypeRepo.findById(id).map(this::toVO).orElseThrow(() -> new BusinessException("字典类型不存在"));
	}

	public void export(DictTypeDTO dto, HttpServletResponse response) {
		List<DictTypeExportModel> rows = dictTypeRepo
			.queryPage(dto.getDictName(), dto.getDictType(), dto.getStatus(), 1, Integer.MAX_VALUE)
			.getContent()
			.stream()
			.map(this::toExportModel)
			.toList();
		excelExportHelper.export(response, "dict-types", DictTypeExportModel.class, rows);
	}

	/**
	 * 创建
	 */
	@Transactional
	public Long create(DictTypeDTO dto) {
		if (dictTypeRepo.existsByDictType(dto.getDictType())) {
			throw new BusinessException("字典类型已存在");
		}

		SysDictType entity = new SysDictType();
		entity.setDictName(dto.getDictName());
		entity.setDictType(dto.getDictType());
		entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
		entity.setRemark(dto.getRemark());
		entity.setCreateBy(getCurrentUsername());
		entity.setCreateTime(LocalDateTime.now());

		dictTypeRepo.save(entity);
		return entity.getId();
	}

	/**
	 * 更新
	 */
	@Transactional
	public void update(Long id, DictTypeDTO dto) {
		SysDictType entity = dictTypeRepo.findById(id).orElseThrow(() -> new BusinessException("字典类型不存在"));

		if (dictTypeRepo.existsByDictTypeExcludeId(dto.getDictType(), id)) {
			throw new BusinessException("字典类型已存在");
		}

		entity.setDictName(dto.getDictName());
		entity.setDictType(dto.getDictType());
		entity.setStatus(dto.getStatus());
		entity.setRemark(dto.getRemark());
		entity.setUpdateBy(getCurrentUsername());
		entity.setUpdateTime(LocalDateTime.now());

		dictTypeRepo.save(entity);
	}

	/**
	 * 删除
	 */
	@Transactional
	public void delete(Long id) {
		if (!dictTypeRepo.existsById(id)) {
			throw new BusinessException("字典类型不存在");
		}
		dictTypeRepo.softDelete(id, LocalDateTime.now());
	}

	/**
	 * 查询所有启用的字典类型
	 */
	public List<DictTypeVO> findAllEnabled() {
		return dictTypeRepo.findByStatusAndDeletedOrderByCreateTimeDesc(1, 0)
			.stream()
			.map(this::toVO)
			.collect(Collectors.toList());
	}

	private DictTypeVO toVO(SysDictType entity) {
		DictTypeVO vo = new DictTypeVO();
		vo.setId(entity.getId());
		vo.setDictName(entity.getDictName());
		vo.setDictType(entity.getDictType());
		vo.setStatus(entity.getStatus());
		vo.setRemark(entity.getRemark());
		vo.setCreateTime(entity.getCreateTime());
		return vo;
	}

	private DictTypeExportModel toExportModel(SysDictType entity) {
		DictTypeExportModel model = new DictTypeExportModel();
		model.setDictName(entity.getDictName());
		model.setDictType(entity.getDictType());
		model.setStatusText(entity.getStatus() != null && entity.getStatus() == 1 ? "启用" : "禁用");
		model.setRemark(entity.getRemark());
		model.setCreateTime(entity.getCreateTime() == null ? "" : entity.getCreateTime().format(DATE_TIME_FORMATTER));
		return model;
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
