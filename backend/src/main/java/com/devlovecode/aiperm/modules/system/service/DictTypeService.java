package com.devlovecode.aiperm.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.dto.DictTypeDTO;
import com.devlovecode.aiperm.modules.system.entity.SysDictType;
import com.devlovecode.aiperm.modules.system.repository.DictTypeRepository;
import com.devlovecode.aiperm.modules.system.vo.DictTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictTypeService {

	private final DictTypeRepository dictTypeRepo;

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

	/**
	 * 创建
	 */
	@Transactional
	public Long create(DictTypeDTO dto) {
		// 校验字典类型是否重复
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

		// JPA save 后实体 ID 已自动填充
		return entity.getId();
	}

	/**
	 * 更新
	 */
	@Transactional
	public void update(Long id, DictTypeDTO dto) {
		SysDictType entity = dictTypeRepo.findById(id).orElseThrow(() -> new BusinessException("字典类型不存在"));

		// 校验字典类型是否重复
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

	// ========== 私有方法 ==========

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

	private String getCurrentUsername() {
		try {
			return StpUtil.getLoginIdAsString();
		}
		catch (Exception e) {
			return "system";
		}
	}

}
