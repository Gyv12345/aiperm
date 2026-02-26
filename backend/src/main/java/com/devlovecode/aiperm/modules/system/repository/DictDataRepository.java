package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.system.entity.SysDictData;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DictDataRepository extends BaseRepository<SysDictData> {

    public DictDataRepository(JdbcClient db) {
        super(db, "sys_dict_data", SysDictData.class);
    }

    /**
     * 插入字典数据
     */
    public void insert(SysDictData entity) {
        String sql = """
            INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort, status, list_class, remark, deleted, version, create_time, create_by)
            VALUES (:dictType, :dictLabel, :dictValue, :sort, :status, :listClass, :remark, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("dictType", entity.getDictType())
                .param("dictLabel", entity.getDictLabel())
                .param("dictValue", entity.getDictValue())
                .param("sort", entity.getSort())
                .param("status", entity.getStatus())
                .param("listClass", entity.getListClass() != null ? entity.getListClass() : "")
                .param("remark", entity.getRemark())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 更新字典数据
     */
    public int update(SysDictData entity) {
        String sql = """
            UPDATE sys_dict_data
            SET dict_label = :dictLabel, dict_value = :dictValue, sort = :sort, status = :status,
                list_class = :listClass, remark = :remark,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("dictLabel", entity.getDictLabel())
                .param("dictValue", entity.getDictValue())
                .param("sort", entity.getSort())
                .param("status", entity.getStatus())
                .param("listClass", entity.getListClass() != null ? entity.getListClass() : "")
                .param("remark", entity.getRemark())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 根据字典类型查询
     */
    public List<SysDictData> findByDictType(String dictType) {
        String sql = "SELECT * FROM sys_dict_data WHERE dict_type = :dictType AND deleted = 0 AND status = 1 ORDER BY sort ASC";
        return db.sql(sql).param("dictType", dictType).query(SysDictData.class).list();
    }
}
