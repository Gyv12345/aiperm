package com.devlovecode.aiperm.modules.enterprise.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.enterprise.dto.NoticeDTO;
import com.devlovecode.aiperm.modules.enterprise.entity.SysNotice;
import com.devlovecode.aiperm.modules.enterprise.repository.NoticeRepository;
import com.devlovecode.aiperm.modules.enterprise.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        PageResult<SysNotice> result = noticeRepo.queryPage(
                dto.getTitle(), dto.getType(), dto.getStatus(),
                dto.getPage(), dto.getPageSize()
        );
        return result.map(this::toVO);
    }

    /**
     * 查询详情
     */
    public NoticeVO findById(Long id) {
        return noticeRepo.findById(id)
                .map(this::toVO)
                .orElseThrow(() -> new BusinessException("公告不存在"));
    }

    /**
     * 查询已发布公告列表
     */
    public List<NoticeVO> findPublished(Integer type, int limit) {
        return noticeRepo.findPublished(type, limit).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
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

        noticeRepo.insert(entity);

        // 获取自增ID
        return noticeRepo.findById(noticeRepo.findAll().stream()
                .filter(n -> n.getTitle().equals(dto.getTitle()))
                .findFirst()
                .map(SysNotice::getId)
                .orElse(null))
                .map(SysNotice::getId)
                .orElse(null);
    }

    /**
     * 更新
     */
    @Transactional
    public void update(Long id, NoticeDTO dto) {
        SysNotice entity = noticeRepo.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setType(dto.getType());
        entity.setStatus(dto.getStatus());
        entity.setUpdateBy(getCurrentUsername());

        noticeRepo.update(entity);
    }

    /**
     * 发布
     */
    @Transactional
    public void publish(Long id) {
        SysNotice entity = noticeRepo.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (entity.getStatus() == 1) {
            throw new BusinessException("公告已发布");
        }

        noticeRepo.publish(id, getCurrentUsername());
    }

    /**
     * 撤回
     */
    @Transactional
    public void withdraw(Long id) {
        SysNotice entity = noticeRepo.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (entity.getStatus() == 0) {
            throw new BusinessException("公告已是草稿状态");
        }

        noticeRepo.withdraw(id, getCurrentUsername());
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        if (!noticeRepo.existsById(id)) {
            throw new BusinessException("公告不存在");
        }
        noticeRepo.deleteById(id);
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
        } catch (Exception e) {
            return "system";
        }
    }
}
