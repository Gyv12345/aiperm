package com.devlovecode.aiperm.modules.enterprise.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.enterprise.dto.JobDTO;
import com.devlovecode.aiperm.modules.enterprise.entity.SysJob;
import com.devlovecode.aiperm.modules.enterprise.repository.JobRepository;
import com.devlovecode.aiperm.modules.enterprise.vo.JobVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobService {

	private final JobRepository jobRepo;

	private final JobSchedulerService jobSchedulerService;

	/**
	 * 分页查询
	 */
	public PageResult<JobVO> queryPage(JobDTO dto) {
		Specification<SysJob> spec = SpecificationUtils.and(SpecificationUtils.like("jobName", dto.getJobName()),
				SpecificationUtils.like("jobGroup", dto.getJobGroup()),
				SpecificationUtils.eq("status", dto.getStatus()));
		PageRequest pageRequest = PageRequest.of(dto.getPage() - 1, dto.getPageSize());
		Page<SysJob> page = jobRepo.findAll(spec, pageRequest);
		PageResult<SysJob> result = PageResult.fromJpaPage(page);
		return result.map(this::toVO);
	}

	/**
	 * 查询详情
	 */
	public JobVO findById(Long id) {
		return jobRepo.findByIdAndDeleted(id, 0).map(this::toVO).orElseThrow(() -> new BusinessException("定时任务不存在"));
	}

	/**
	 * 创建
	 */
	@Transactional
	public Long create(JobDTO dto) {
		validateCronExpression(dto.getCronExpression());

		SysJob entity = new SysJob();
		entity.setJobName(dto.getJobName());
		entity.setJobGroup(dto.getJobGroup());
		entity.setCronExpression(dto.getCronExpression());
		entity.setBeanClass(dto.getBeanClass());
		entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
		entity.setRemark(dto.getRemark());
		entity.setCreateBy(getCurrentUsername());
		entity.setCreateTime(LocalDateTime.now());

		jobRepo.save(entity);
		jobSchedulerService.refreshJobAfterCommit(entity);

		return entity.getId();
	}

	/**
	 * 更新
	 */
	@Transactional
	public void update(Long id, JobDTO dto) {
		validateCronExpression(dto.getCronExpression());

		SysJob entity = jobRepo.findByIdAndDeleted(id, 0).orElseThrow(() -> new BusinessException("定时任务不存在"));

		entity.setJobName(dto.getJobName());
		entity.setJobGroup(dto.getJobGroup());
		entity.setCronExpression(dto.getCronExpression());
		entity.setBeanClass(dto.getBeanClass());
		entity.setStatus(dto.getStatus());
		entity.setRemark(dto.getRemark());
		entity.setUpdateBy(getCurrentUsername());
		entity.setUpdateTime(LocalDateTime.now());

		jobRepo.save(entity);
		jobSchedulerService.refreshJobAfterCommit(entity);
	}

	/**
	 * 删除
	 */
	@Transactional
	public void delete(Long id) {
		jobRepo.findByIdAndDeleted(id, 0).orElseThrow(() -> new BusinessException("定时任务不存在"));
		jobRepo.softDelete(id, LocalDateTime.now());
		jobSchedulerService.removeJobAfterCommit(id);
	}

	/**
	 * 暂停任务
	 */
	@Transactional
	public void pause(Long id) {
		jobRepo.findByIdAndDeleted(id, 0).orElseThrow(() -> new BusinessException("定时任务不存在"));
		jobRepo.updateStatus(id, 0, getCurrentUsername(), LocalDateTime.now());
		jobSchedulerService.removeJobAfterCommit(id);
	}

	/**
	 * 恢复任务
	 */
	@Transactional
	public void resume(Long id) {
		SysJob entity = jobRepo.findByIdAndDeleted(id, 0).orElseThrow(() -> new BusinessException("定时任务不存在"));
		jobRepo.updateStatus(id, 1, getCurrentUsername(), LocalDateTime.now());
		entity.setStatus(1);
		jobSchedulerService.refreshJobAfterCommit(entity);
	}

	// ========== 私有方法 ==========

	private JobVO toVO(SysJob entity) {
		JobVO vo = new JobVO();
		vo.setId(entity.getId());
		vo.setJobName(entity.getJobName());
		vo.setJobGroup(entity.getJobGroup());
		vo.setCronExpression(entity.getCronExpression());
		vo.setBeanClass(entity.getBeanClass());
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

	private void validateCronExpression(String cronExpression) {
		try {
			CronExpression.parse(cronExpression);
		}
		catch (Exception e) {
			throw new BusinessException("Cron表达式不合法");
		}
	}

}
