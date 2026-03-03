package com.devlovecode.aiperm.modules.approval.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalInstance;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ApprovalInstanceRepository extends BaseRepository<SysApprovalInstance> {

    public ApprovalInstanceRepository(JdbcClient db) {
        super(db, "sys_approval_instance", SysApprovalInstance.class);
    }

    public void insert(SysApprovalInstance entity) {
        String sql = """
            INSERT INTO sys_approval_instance (
                scene_code, business_type, business_id, initiator_id, platform, platform_instance_id,
                status, form_data, deleted, version, create_time, create_by
            ) VALUES (
                :sceneCode, :businessType, :businessId, :initiatorId, :platform, :platformInstanceId,
                :status, CAST(:formData AS JSON), 0, 0, :createTime, :createBy
            )
            """;
        db.sql(sql)
                .param("sceneCode", entity.getSceneCode())
                .param("businessType", entity.getBusinessType())
                .param("businessId", entity.getBusinessId())
                .param("initiatorId", entity.getInitiatorId())
                .param("platform", entity.getPlatform())
                .param("platformInstanceId", entity.getPlatformInstanceId())
                .param("status", entity.getStatus())
                .param("formData", entity.getFormData() == null ? "{}" : entity.getFormData())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    public int updateStatus(Long id, String status, LocalDateTime resultTime, String updateBy) {
        String sql = """
            UPDATE sys_approval_instance
            SET status = :status, result_time = :resultTime, update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("status", status)
                .param("resultTime", resultTime)
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", updateBy)
                .param("id", id)
                .update();
    }

    public Optional<SysApprovalInstance> findByPlatformInstanceId(String platformInstanceId) {
        if (platformInstanceId == null || platformInstanceId.isBlank()) return Optional.empty();
        String sql = "SELECT * FROM sys_approval_instance WHERE platform_instance_id = :platformInstanceId AND deleted = 0";
        return db.sql(sql).param("platformInstanceId", platformInstanceId).query(SysApprovalInstance.class).optional();
    }

    public boolean existsPending(String businessType, Long businessId) {
        String sql = """
            SELECT COUNT(*) FROM sys_approval_instance
            WHERE business_type = :businessType AND business_id = :businessId
              AND status = 'PENDING' AND deleted = 0
            """;
        Integer count = db.sql(sql)
                .param("businessType", businessType)
                .param("businessId", businessId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    public PageResult<SysApprovalInstance> queryPage(Long initiatorId, String sceneCode, String status, int page, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.whereIf(initiatorId != null, "initiator_id = ?", initiatorId)
                .whereIf(sceneCode != null && !sceneCode.isBlank(), "scene_code = ?", sceneCode)
                .whereIf(status != null && !status.isBlank(), "status = ?", status);
        return queryPage(sb.getWhereClause(), sb.getParams(), page, pageSize);
    }

    public Optional<SysApprovalInstance> findLatestFinishedByPlatform(String platform) {
        String sql = """
            SELECT * FROM sys_approval_instance
            WHERE platform = :platform
              AND status IN ('APPROVED', 'REJECTED', 'CANCELED')
              AND deleted = 0
            ORDER BY result_time DESC, update_time DESC, id DESC
            LIMIT 1
            """;
        List<SysApprovalInstance> result = db.sql(sql)
                .param("platform", platform)
                .query(SysApprovalInstance.class)
                .list();
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.getFirst());
    }
}
