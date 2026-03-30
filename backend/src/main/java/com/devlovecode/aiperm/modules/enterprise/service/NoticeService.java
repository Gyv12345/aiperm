package com.devlovecode.aiperm.modules.enterprise.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.enterprise.dto.NoticeDTO;
import com.devlovecode.aiperm.modules.enterprise.entity.SysNotice;
import com.devlovecode.aiperm.modules.enterprise.repository.NoticeRepository;
import com.devlovecode.aiperm.modules.enterprise.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

	private final NoticeRepository noticeRepo;

	/**
	 * 分页查询
	 */
	public PageResult<NoticeVO> queryPage(NoticeDTO dto) {
		Specification<SysNotice> spec = SpecificationUtils.and(SpecificationUtils.like("title", dto.getTitle()),
				SpecificationUtils.eq("type", dto.getType()), SpecificationUtils.eq("status", dto.getStatus()));
		PageRequest pageRequest = PageRequest.of(dto.getPage() - 1, dto.getPageSize());
		Page<SysNotice> page = noticeRepo.findAll(spec, pageRequest);
		PageResult<SysNotice> result = PageResult.fromJpaPage(page);
		return result.map(this::toVO);
	}

	/**
	 * 查询详情
	 */
	public NoticeVO findById(Long id) {
		return noticeRepo.findById(id).map(this::toVO).orElseThrow(() -> new BusinessException("公告不存在"));
	}

	/**
	 * 查询已发布公告列表
	 */
	public List<NoticeVO> findPublished(Integer type, int limit) {
		return noticeRepo.findPublished(type).stream().limit(limit).map(this::toVO).collect(Collectors.toList());
	}

	/**
	 * 创建
	 */
	@Transactional
	public Long create(NoticeDTO dto) {
		SysNotice entity = new SysNotice();
		entity.setTitle(dto.getTitle());
		entity.setContent(dto.getContent());
		entity.setType(dto.getType() != null ? dto.getType() : 1);
		entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
		entity.setCreateBy(getCurrentUsername());
		entity.setCreateTime(LocalDateTime.now());

		noticeRepo.save(entity);

		return entity.getId();
	}

	/**
	 * 更新
	 */
	@Transactional
	public void update(Long id, NoticeDTO dto) {
		SysNotice entity = noticeRepo.findById(id).orElseThrow(() -> new BusinessException("公告不存在"));

		entity.setTitle(dto.getTitle());
		entity.setContent(dto.getContent());
		entity.setType(dto.getType());
		entity.setStatus(dto.getStatus());
		entity.setUpdateBy(getCurrentUsername());
		entity.setUpdateTime(LocalDateTime.now());

		noticeRepo.save(entity);
	}

	/**
	 * 发布
	 */
	@Transactional
	public void publish(Long id) {
		SysNotice entity = noticeRepo.findById(id).orElseThrow(() -> new BusinessException("公告不存在"));

		if (entity.getStatus() == 1) {
			throw new BusinessException("公告已发布");
		}

		LocalDateTime now = LocalDateTime.now();
		noticeRepo.publish(id, getCurrentUsername(), now, now);
	}

	/**
	 * 撤回
	 */
	@Transactional
	public void withdraw(Long id) {
		SysNotice entity = noticeRepo.findById(id).orElseThrow(() -> new BusinessException("公告不存在"));

		if (entity.getStatus() == 0) {
			throw new BusinessException("公告已是草稿状态");
		}

		noticeRepo.withdraw(id, getCurrentUsername(), LocalDateTime.now());
	}

	/**
	 * 删除
	 */
	@Transactional
	public void delete(Long id) {
		if (!noticeRepo.existsById(id)) {
			throw new BusinessException("公告不存在");
		}
		noticeRepo.softDelete(id, LocalDateTime.now());
	}

	// ========== 私有方法 ==========

	private NoticeVO toVO(SysNotice entity) {
		NoticeVO vo = new NoticeVO();
		vo.setId(entity.getId());
		vo.setTitle(entity.getTitle());
		vo.setContent(entity.getContent());
		vo.setType(entity.getType());
		vo.setStatus(entity.getStatus());
		vo.setPublishTime(entity.getPublishTime());
		vo.setCreateTime(entity.getCreateTime());
		vo.setCreateBy(entity.getCreateBy());
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
