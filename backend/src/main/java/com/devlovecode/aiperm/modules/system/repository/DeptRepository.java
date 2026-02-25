package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.system.entity.SysDept;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DeptRepository extends BaseRepository<SysDept> {

    public DeptRepository(JdbcClient db) {
        super(db, "sys_dept", SysDept.class);
    }

    /**
     * 插入部门
     */
    public void insert(SysDept entity) {
        String sql = """
            INSERT INTO sys_dept (dept_name, parent_id, sort, leader, phone, email, status, remark, deleted, version, create_time, create_by)
            VALUES (:deptName, :parentId, :sort, :leader, :phone, :email, :status, :remark, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("deptName", entity.getDeptName())
                .param("parentId", entity.getParentId())
                .param("sort", entity.getSort())
                .param("leader", entity.getLeader())
                .param("phone", entity.getPhone())
                .param("email", entity.getEmail())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 更新部门
     */
    public int update(SysDept entity) {
        String sql = """
            UPDATE sys_dept
            SET dept_name = :deptName, parent_id = :parentId, sort = :sort, leader = :leader,
                phone = :phone, email = :email, status = :status, remark = :remark,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("deptName", entity.getDeptName())
                .param("parentId", entity.getParentId())
                .param("sort", entity.getSort())
                .param("leader", entity.getLeader())
                .param("phone", entity.getPhone())
                .param("email", entity.getEmail())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 根据父ID查询子部门
     */
    public List<SysDept> findByParentId(Long parentId) {
        String sql = "SELECT * FROM sys_dept WHERE parent_id = :parentId AND deleted = 0 ORDER BY sort ASC";
        return db.sql(sql).param("parentId", parentId).query(SysDept.class).list();
    }

    /**
     * 查询所有部门（按排序）
     */
    public List<SysDept> findAllOrderBySort() {
        String sql = "SELECT * FROM sys_dept WHERE deleted = 0 ORDER BY parent_id ASC, sort ASC";
        return db.sql(sql).query(SysDept.class).list();
    }

    /**
     * 检查是否有子部门
     */
    public boolean hasChildren(Long parentId) {
        String sql = "SELECT COUNT(*) FROM sys_dept WHERE parent_id = :parentId AND deleted = 0";
        Integer count = db.sql(sql).param("parentId", parentId).query(Integer.class).single();
        return count != null && count > 0;
    }

    /**
     * 检查部门名称是否重复（同父级下）
     */
    public boolean existsByDeptNameAndParentId(String deptName, Long parentId) {
        String sql = "SELECT COUNT(*) FROM sys_dept WHERE dept_name = :deptName AND parent_id = :parentId AND deleted = 0";
        Integer count = db.sql(sql)
                .param("deptName", deptName)
                .param("parentId", parentId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    /**
     * 检查部门名称是否重复（同父级下，排除指定ID）
     */
    public boolean existsByDeptNameAndParentIdExcludeId(String deptName, Long parentId, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM sys_dept WHERE dept_name = :deptName AND parent_id = :parentId AND id != :id AND deleted = 0";
        Integer count = db.sql(sql)
                .param("deptName", deptName)
                .param("parentId", parentId)
                .param("id", excludeId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }
}
