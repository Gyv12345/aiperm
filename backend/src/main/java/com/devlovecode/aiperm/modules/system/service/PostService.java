package com.devlovecode.aiperm.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.dto.PostDTO;
import com.devlovecode.aiperm.modules.system.entity.SysPost;
import com.devlovecode.aiperm.modules.system.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepo;

    /**
     * 分页查询
     */
    public PageResult<SysPost> queryPage(PostDTO dto) {
        Page<SysPost> jpaPage = postRepo.queryPage(dto.getPostName(), dto.getPostCode(), dto.getStatus(), dto.getPage(), dto.getPageSize());
        return PageResult.fromJpaPage(jpaPage);
    }

    /**
     * 查询所有
     */
    public List<SysPost> listAll() {
        return postRepo.findAll();
    }

    /**
     * 查询详情
     */
    public SysPost findById(Long id) {
        return postRepo.findById(id)
                .orElseThrow(() -> new BusinessException("岗位不存在"));
    }

    /**
     * 创建
     */
    @Transactional
    public void create(PostDTO dto) {
        if (postRepo.existsByPostCode(dto.getPostCode())) {
            throw new BusinessException("岗位编码已存在");
        }

        SysPost entity = new SysPost();
        entity.setPostName(dto.getPostName());
        entity.setPostCode(dto.getPostCode());
        entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        entity.setRemark(dto.getRemark());
        entity.setCreateBy(getCurrentUsername());
        entity.setCreateTime(LocalDateTime.now());

        postRepo.save(entity);
    }

    /**
     * 更新
     */
    @Transactional
    public void update(Long id, PostDTO dto) {
        SysPost entity = postRepo.findById(id)
                .orElseThrow(() -> new BusinessException("岗位不存在"));

        if (postRepo.existsByPostCodeExcludeId(dto.getPostCode(), id)) {
            throw new BusinessException("岗位编码已存在");
        }

        entity.setPostName(dto.getPostName());
        entity.setPostCode(dto.getPostCode());
        entity.setSort(dto.getSort());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(getCurrentUsername());
        entity.setUpdateTime(LocalDateTime.now());

        postRepo.save(entity);
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        if (!postRepo.existsById(id)) {
            throw new BusinessException("岗位不存在");
        }
        postRepo.softDelete(id, LocalDateTime.now());
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
