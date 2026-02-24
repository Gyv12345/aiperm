package com.devlovecode.aiperm.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.system.entity.SysPost;

import java.util.List;

/**
 * 岗位服务接口
 *
 * @author devlovecode
 */
public interface ISysPostService extends IService<SysPost> {

    /**
     * 根据岗位编码查询岗位
     *
     * @param postCode 岗位编码
     * @return 岗位信息
     */
    SysPost getByPostCode(String postCode);

    /**
     * 根据用户ID查询岗位列表
     *
     * @param userId 用户ID
     * @return 岗位列表
     */
    List<SysPost> listByUserId(Long userId);

    /**
     * 分页查询岗位列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param postName 岗位名称（可选）
     * @param postCode 岗位编码（可选）
     * @param status   状态（可选）
     * @return 岗位分页列表
     */
    PageResult<SysPost> page(Long pageNum, Long pageSize, String postName, String postCode, Integer status);

    /**
     * 创建岗位
     *
     * @param post 岗位信息
     * @return 是否成功
     */
    boolean create(SysPost post);

    /**
     * 更新岗位
     *
     * @param post 岗位信息
     * @return 是否成功
     */
    boolean update(SysPost post);

    /**
     * 删除岗位
     *
     * @param postId 岗位ID
     * @return 是否成功
     */
    boolean delete(Long postId);
}
