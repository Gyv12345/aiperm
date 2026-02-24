package com.devlovecode.aiperm.modules.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.entity.SysPost;
import com.devlovecode.aiperm.modules.system.mapper.SysPostMapper;
import com.devlovecode.aiperm.modules.system.service.ISysPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 岗位服务实现类
 *
 * @author devlovecode
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {

    private final SysPostMapper sysPostMapper;

    @Override
    public SysPost getByPostCode(String postCode) {
        if (StrUtil.isBlank(postCode)) {
            return null;
        }
        return sysPostMapper.selectByPostCode(postCode);
    }

    @Override
    public List<SysPost> listByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return sysPostMapper.selectByUserId(userId);
    }

    @Override
    public PageResult<SysPost> page(Long pageNum, Long pageSize, String postName, String postCode, Integer status) {
        Page<SysPost> page = new Page<>(pageNum, pageSize);
        Page<SysPost> result = sysPostMapper.selectPostPage(page, postName, postCode, status);
        return PageResult.of(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(SysPost post) {
        // 检查岗位编码是否存在
        SysPost existPost = getByPostCode(post.getPostCode());
        if (existPost != null) {
            throw new BusinessException("岗位编码已存在");
        }

        return save(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysPost post) {
        SysPost existPost = getById(post.getId());
        if (existPost == null) {
            throw new BusinessException("岗位不存在");
        }

        // 检查岗位编码是否被其他岗位占用
        if (!existPost.getPostCode().equals(post.getPostCode())) {
            SysPost sameCodePost = getByPostCode(post.getPostCode());
            if (sameCodePost != null) {
                throw new BusinessException("岗位编码已存在");
            }
        }

        return updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long postId) {
        SysPost post = getById(postId);
        if (post == null) {
            throw new BusinessException("岗位不存在");
        }

        return removeById(postId);
    }
}
