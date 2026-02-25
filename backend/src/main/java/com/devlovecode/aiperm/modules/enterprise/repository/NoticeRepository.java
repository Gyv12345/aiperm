package com.devlovecode.aiperm.modules.enterprise.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.enterprise.entity.SysNotice;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class NoticeRepository extends BaseRepository<SysNotice> {

    public NoticeRepository(JdbcClient db) {
        super(db, "sys_notice", SysNotice.class);
    }

    /**
     * 插入公告
     */
    public void insert(SysNotice entity) {
        String sql = """
            INSERT INTO sys_notice (title, content, type, status, publish_time, deleted, version, create_time, create_by)
            VALUES (:title, :content, :type, :status, :publishTime, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("title", entity.getTitle())
                .param("content", entity.getContent())
                .param("type", entity.getType())
                .param("status", entity.getStatus())
                .param("publishTime", entity.getPublishTime())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 更新公告
     */
    public int update(SysNotice entity) {
        String sql = """
            UPDATE sys_notice
            SET title = :title, content = :content, type = :type, status = :status,
                publish_time = :publishTime, update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("title", entity.getTitle())
                .param("content", entity.getContent())
                .param("type", entity.getType())
                .param("status", entity.getStatus())
                .param("publishTime", entity.getPublishTime())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 发布公告（更新状态和发布时间）
     */
    public int publish(Long id, String updateBy) {
        String sql = """
            UPDATE sys_notice
            SET status = 1, publish_time = :publishTime, update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("publishTime", LocalDateTime.now())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", updateBy)
                .param("id", id)
                .update();
    }

    /**
     * 撤回公告（更新状态为草稿）
     */
    public int withdraw(Long id, String updateBy) {
        String sql = """
            UPDATE sys_notice
            SET status = 0, update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", updateBy)
                .param("id", id)
                .update();
    }

    /**
     * 分页查询
     */
    public PageResult<SysNotice> queryPage(String title, Integer type, Integer status, int pageNum, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(title != null && !title.isBlank(), "title", title)
          .whereIf(type != null, "type = ?", type)
          .whereIf(status != null, "status = ?", status);

        return queryPage(sb.getWhereClause(), sb.getParams(), pageNum, pageSize);
    }

    /**
     * 查询已发布的公告列表
     */
    public List<SysNotice> findPublished(Integer type, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_notice WHERE status = 1 AND deleted = 0");
        if (type != null) {
            sql.append(" AND type = ").append(type);
        }
        sql.append(" ORDER BY publish_time DESC LIMIT ").append(limit);
        return db.sql(sql.toString()).query(SysNotice.class).list();
    }

    /**
     * 根据ID查询
     */
    public Optional<SysNotice> findById(Long id) {
        String sql = "SELECT * FROM sys_notice WHERE id = :id AND deleted = 0";
        return db.sql(sql).param("id", id).query(SysNotice.class).optional();
    }
}
