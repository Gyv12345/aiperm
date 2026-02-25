package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.system.entity.SysPost;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PostRepository extends BaseRepository<SysPost> {

    public PostRepository(JdbcClient db) {
        super(db, "sys_post", SysPost.class);
    }

    /**
     * 插入岗位
     */
    public void insert(SysPost entity) {
        String sql = """
            INSERT INTO sys_post (post_name, post_code, sort, status, remark, deleted, version, create_time, create_by)
            VALUES (:postName, :postCode, :sort, :status, :remark, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("postName", entity.getPostName())
                .param("postCode", entity.getPostCode())
                .param("sort", entity.getSort())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 更新岗位
     */
    public int update(SysPost entity) {
        String sql = """
            UPDATE sys_post
            SET post_name = :postName, post_code = :postCode, sort = :sort, status = :status, remark = :remark,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("postName", entity.getPostName())
                .param("postCode", entity.getPostCode())
                .param("sort", entity.getSort())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 检查岗位编码是否存在
     */
    public boolean existsByPostCode(String postCode) {
        String sql = "SELECT COUNT(*) FROM sys_post WHERE post_code = :postCode AND deleted = 0";
        Integer count = db.sql(sql).param("postCode", postCode).query(Integer.class).single();
        return count != null && count > 0;
    }

    /**
     * 检查岗位编码是否存在（排除指定ID）
     */
    public boolean existsByPostCodeExcludeId(String postCode, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM sys_post WHERE post_code = :postCode AND id != :id AND deleted = 0";
        Integer count = db.sql(sql)
                .param("postCode", postCode)
                .param("id", excludeId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    /**
     * 分页查询
     */
    public PageResult<SysPost> queryPage(String postName, String postCode, Integer status, int pageNum, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(postName != null && !postName.isBlank(), "post_name", postName)
          .likeIf(postCode != null && !postCode.isBlank(), "post_code", postCode)
          .whereIf(status != null, "status = ?", status);

        return queryPage(sb.getWhereClause(), sb.getParams(), pageNum, pageSize);
    }
}
