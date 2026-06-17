package com.devlovecode.aiperm.modules.enterprise.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.enterprise.dto.NoticeDTO;
import com.devlovecode.aiperm.modules.enterprise.entity.SysNotice;
import com.devlovecode.aiperm.modules.enterprise.repository.NoticeRepository;
import com.devlovecode.aiperm.modules.enterprise.vo.NoticeVO;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysUser;
import com.devlovecode.aiperm.modules.system.rbac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

	private final NoticeRepository noticeRepo;
	private final UserRepository userRepo;

	/**
	 * 分页查询
	 */
	public PageResult<NoticeVO> queryPage(NoticeDTO dto) {
		Specification<SysNotice> spec = SpecificationUtils.and(SpecificationUtils.like("title", dto.getTitle()),
				SpecificationUtils.eq("type", dto.getType()), SpecificationUtils.eq("status", dto.getStatus()));
		PageRequest pageRequest = PageRequest.of(dto.getPage() - 1, dto.getPageSize());
		Page<SysNotice> page = noticeRepo.findAll(spec, pageRequest);
		List<NoticeVO> voList = toVOList(page.getContent());
		return PageResult.of(page.getTotalElements(), voList, (long) dto.getPage(), (long) dto.getPageSize());
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
		List<SysNotice> entities = noticeRepo.findPublished(type).stream().limit(limit).collect(Collectors.toList());
		return toVOList(entities);
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

	/**
	 * 批量转换：一次性解析所有 createBy（登录ID）为用户姓名，避免 N+1 查询。
	 */
	private List<NoticeVO> toVOList(List<SysNotice> entities) {
		if (entities.isEmpty()) {
			return List.of();
		}
		Map<Long, SysUser> userMap = buildNameMap(entities);
		return entities.stream().map(e -> toVO(e, userMap)).collect(Collectors.toList());
	}

	private NoticeVO toVO(SysNotice entity) {
		return toVO(entity, buildNameMap(List.of(entity)));
	}

	private NoticeVO toVO(SysNotice entity, Map<Long, SysUser> userMap) {
		NoticeVO vo = new NoticeVO();
		vo.setId(entity.getId());
		vo.setTitle(entity.getTitle());
		vo.setContent(entity.getContent());
		vo.setType(entity.getType());
		vo.setStatus(entity.getStatus());
		vo.setPublishTime(entity.getPublishTime());
		vo.setCreateTime(entity.getCreateTime());
		vo.setCreateBy(entity.getCreateBy());
		vo.setCreateByName(resolveName(entity.getCreateBy(), userMap));
		return vo;
	}

	/**
	 * 收集所有 createBy（登录ID 字符串），批量查出用户，构建 id→用户 映射。
	 * createBy 可能是用户ID、用户名或历史脏数据，统一按ID查询，解析失败则忽略。
	 */
	private Map<Long, SysUser> buildNameMap(List<SysNotice> entities) {
		Set<Long> userIds = entities.stream().map(SysNotice::getCreateBy).filter(Objects::nonNull)
				.map(this::parseUserId).filter(Objects::nonNull).collect(Collectors.toSet());
		if (userIds.isEmpty()) {
			return Map.of();
		}
		return userRepo.findAllById(userIds).stream()
				.collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
	}

	/**
	 * 把 createBy 解析为用户ID。优先按纯数字解析（getCurrentUsername 返回登录ID）。
	 */
	private Long parseUserId(String createBy) {
		try {
			return Long.valueOf(createBy.trim());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 优先返回真实姓名，其次昵称、用户名；都缺失时回退原始 createBy。
	 */
	private String resolveName(String createBy, Map<Long, SysUser> userMap) {
		if (createBy == null || createBy.isBlank()) {
			return null;
		}
		Long userId = parseUserId(createBy);
		if (userId == null) {
			return createBy;
		}
		SysUser user = userMap.get(userId);
		if (user == null) {
			return createBy;
		}
		if (user.getRealName() != null && !user.getRealName().isBlank()) {
			return user.getRealName();
		}
		if (user.getNickname() != null && !user.getNickname().isBlank()) {
			return user.getNickname();
		}
		return user.getUsername();
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
