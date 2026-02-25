package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.system.entity.SysDictType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DictTypeRepository extends BaseRepository<SysDictType> {

    public DictTypeRepository(JdbcClient db) {
        super(db, "sys_dict_type", SysDictType.class);
    }

    /**
     * 插入字典类型
     */
    public void insert(SysDictType entity) {
        String sql = """
            INSERT INTO sys_dict_type (dict_name, dict_type, status, remark, deleted, version, create_time, create_by)
            VALUES (:dictName, :dictType, :status, :remark, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("dictName", entity.getDictName())
                .param("dictType", entity.getDictType())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 更新字典类型
     */
    public int update(SysDictType entity) {
        String sql = """
            UPDATE sys_dict_type
            SET dict_name = :dictName, status = :status, remark = :remark,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("dictName", entity.getDictName())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 根据字典类型查询
     */
    public Optional<SysDictType> findByDictType(String dictType) {
        String sql = "SELECT * FROM sys_dict_type WHERE dict_type = :dictType AND deleted = 0";
        return db.sql(sql).param("dictType", dictType).query(SysDictType.class).optional();
    }

    /**
     * 检查字典类型是否存在
     */
    public boolean existsByDictType(String dictType) {
        String sql = "SELECT COUNT(*) FROM sys_dict_type WHERE dict_type = :dictType AND deleted = 0";
        Integer count = db.sql(sql).param("dictType", dictType).query(Integer.class).single();
        return count != null && count > 0;
    }

    /**
     * 检查字典类型是否存在（排除指定ID）
     */
    public boolean existsByDictTypeExcludeId(String dictType, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM sys_dict_type WHERE dict_type = :dictType AND id != :id AND deleted = 0";
        Integer count = db.sql(sql)
                .param("dictType", dictType)
                .param("id", excludeId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    /**
     * 分页查询
     */
    public PageResult<SysDictType> queryPage(String dictName, String dictType, Integer status, int pageNum, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(dictName != null && !dictName.isBlank(), "dict_name", dictName)
          .likeIf(dictType != null && !dictType.isBlank(), "dict_type", dictType)
          .whereIf(status != null, "status = ?", status);

        return queryPage(sb.getWhereClause(), sb.getParams(), pageNum, pageSize);
    }

    /**
     * 查询启用的字典类型列表
     */
    public List<SysDictType> findAllEnabled() {
        String sql = "SELECT * FROM sys_dict_type WHERE status = 1 AND deleted = 0 ORDER BY create_time DESC";
        return db.sql(sql).query(SysDictType.class).list();
    }
}
