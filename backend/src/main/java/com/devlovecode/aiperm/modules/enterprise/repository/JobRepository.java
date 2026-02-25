package com.devlovecode.aiperm.modules.enterprise.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.enterprise.entity.SysJob;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class JobRepository extends BaseRepository<SysJob> {

    public JobRepository(JdbcClient db) {
        super(db, "sys_job", SysJob.class);
    }

    /**
     * 插入定时任务
     */
    public void insert(SysJob entity) {
        String sql = """
            INSERT INTO sys_job (job_name, job_group, cron_expression, bean_class, status, remark, deleted, version, create_time, create_by)
            VALUES (:jobName, :jobGroup, :cronExpression, :beanClass, :status, :remark, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("jobName", entity.getJobName())
                .param("jobGroup", entity.getJobGroup())
                .param("cronExpression", entity.getCronExpression())
                .param("beanClass", entity.getBeanClass())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 更新定时任务
     */
    public int update(SysJob entity) {
        String sql = """
            UPDATE sys_job
            SET job_name = :jobName, job_group = :jobGroup, cron_expression = :cronExpression,
                bean_class = :beanClass, status = :status, remark = :remark,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("jobName", entity.getJobName())
                .param("jobGroup", entity.getJobGroup())
                .param("cronExpression", entity.getCronExpression())
                .param("beanClass", entity.getBeanClass())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 更新任务状态
     */
    public int updateStatus(Long id, Integer status, String updateBy) {
        String sql = """
            UPDATE sys_job
            SET status = :status, update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("status", status)
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", updateBy)
                .param("id", id)
                .update();
    }

    /**
     * 分页查询
     */
    public PageResult<SysJob> queryPage(String jobName, String jobGroup, Integer status, int pageNum, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(jobName != null && !jobName.isBlank(), "job_name", jobName)
          .likeIf(jobGroup != null && !jobGroup.isBlank(), "job_group", jobGroup)
          .whereIf(status != null, "status = ?", status);

        return queryPage(sb.getWhereClause(), sb.getParams(), pageNum, pageSize);
    }
}
