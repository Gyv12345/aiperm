package com.devlovecode.aiperm.modules.enterprise.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.enterprise.dto.JobDTO;
import com.devlovecode.aiperm.modules.enterprise.entity.SysJob;
import com.devlovecode.aiperm.modules.enterprise.repository.JobRepository;
import com.devlovecode.aiperm.modules.enterprise.vo.JobVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepo;

    /**
     * 分页查询
     */
    public PageResult<JobVO> queryPage(JobDTO dto) {
        PageResult<SysJob> result = jobRepo.queryPage(
                dto.getJobName(), dto.getJobGroup(), dto.getStatus(),
                dto.getPage(), dto.getPageSize()
        );
        return result.map(this::toVO);
    }

    /**
     * 查询详情
     */
    public JobVO findById(Long id) {
        return jobRepo.findById(id)
                .map(this::toVO)
                .orElseThrow(() -> new BusinessException("定时任务不存在"));
    }

    /**
     * 创建
     */
    @Transactional
    public Long create(JobDTO dto) {
        SysJob entity = new SysJob();
        entity.setJobName(dto.getJobName());
        entity.setJobGroup(dto.getJobGroup());
        entity.setCronExpression(dto.getCronExpression());
        entity.setBeanClass(dto.getBeanClass());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setRemark(dto.getRemark());
        entity.setCreateBy(getCurrentUsername());

        jobRepo.insert(entity);

        // 获取自增ID
        return jobRepo.findById(jobRepo.count()).map(SysJob::getId).orElse(null);
    }

    /**
     * 更新
     */
    @Transactional
    public void update(Long id, JobDTO dto) {
        SysJob entity = jobRepo.findById(id)
                .orElseThrow(() -> new BusinessException("定时任务不存在"));

        entity.setJobName(dto.getJobName());
        entity.setJobGroup(dto.getJobGroup());
        entity.setCronExpression(dto.getCronExpression());
        entity.setBeanClass(dto.getBeanClass());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(getCurrentUsername());

        jobRepo.update(entity);
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        if (!jobRepo.existsById(id)) {
            throw new BusinessException("定时任务不存在");
        }
        jobRepo.deleteById(id);
    }

    /**
     * 暂停任务
     */
    @Transactional
    public void pause(Long id) {
        if (!jobRepo.existsById(id)) {
            throw new BusinessException("定时任务不存在");
        }
        jobRepo.updateStatus(id, 0, getCurrentUsername());
    }

    /**
     * 恢复任务
     */
    @Transactional
    public void resume(Long id) {
        if (!jobRepo.existsById(id)) {
            throw new BusinessException("定时任务不存在");
        }
        jobRepo.updateStatus(id, 1, getCurrentUsername());
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
        } catch (Exception e) {
            return "system";
        }
    }
}
