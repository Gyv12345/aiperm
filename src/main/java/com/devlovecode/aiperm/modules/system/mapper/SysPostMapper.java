package com.devlovecode.aiperm.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devlovecode.aiperm.modules.system.entity.SysPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位Mapper接口
 *
 * @author devlovecode
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {

    /**
     * 根据岗位编码查询岗位
     *
     * @param postCode 岗位编码
     * @return 岗位信息
     */
    SysPost selectByPostCode(@Param("postCode") String postCode);

    /**
     * 根据用户ID查询岗位列表
     *
     * @param userId 用户ID
     * @return 岗位列表
     */
    List<SysPost> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据岗位ID列表查询岗位列表
     *
     * @param postIds 岗位ID列表
     * @return 岗位列表
     */
    List<SysPost> selectByIds(@Param("postIds") List<Long> postIds);

    /**
     * 分页查询岗位列表
     *
     * @param page     分页参数
     * @param postName 岗位名称（可选）
     * @param postCode 岗位编码（可选）
     * @param status   状态（可选）
     * @return 岗位分页列表
     */
    IPage<SysPost> selectPostPage(Page<SysPost> page,
                                   @Param("postName") String postName,
                                   @Param("postCode") String postCode,
                                   @Param("status") Integer status);
}
