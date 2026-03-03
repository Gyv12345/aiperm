package com.devlovecode.aiperm.modules.approval.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ApprovalSceneRepository extends BaseRepository<SysApprovalScene> {

    public ApprovalSceneRepository(JdbcClient db) {
        super(db, "sys_approval_scene", SysApprovalScene.class);
    }

    public Optional<SysApprovalScene> findBySceneCode(String sceneCode) {
        String sql = "SELECT * FROM sys_approval_scene WHERE scene_code = :sceneCode AND deleted = 0";
        return db.sql(sql).param("sceneCode", sceneCode).query(SysApprovalScene.class).optional();
    }

    public boolean existsBySceneCode(String sceneCode) {
        String sql = "SELECT COUNT(*) FROM sys_approval_scene WHERE scene_code = :sceneCode AND deleted = 0";
        Integer count = db.sql(sql).param("sceneCode", sceneCode).query(Integer.class).single();
        return count != null && count > 0;
    }

    public boolean existsBySceneCodeExcludeId(String sceneCode, Long id) {
        String sql = "SELECT COUNT(*) FROM sys_approval_scene WHERE scene_code = :sceneCode AND id != :id AND deleted = 0";
        Integer count = db.sql(sql).param("sceneCode", sceneCode).param("id", id).query(Integer.class).single();
        return count != null && count > 0;
    }

    public void insert(SysApprovalScene entity) {
        String sql = """
            INSERT INTO sys_approval_scene (
                scene_code, scene_name, platform, template_id, enabled, handler_class,
                timeout_hours, timeout_action, deleted, version, create_time, create_by
            ) VALUES (
                :sceneCode, :sceneName, :platform, :templateId, :enabled, :handlerClass,
                :timeoutHours, :timeoutAction, 0, 0, :createTime, :createBy
            )
            """;
        db.sql(sql)
                .param("sceneCode", entity.getSceneCode())
                .param("sceneName", entity.getSceneName())
                .param("platform", entity.getPlatform())
                .param("templateId", entity.getTemplateId())
                .param("enabled", entity.getEnabled())
                .param("handlerClass", entity.getHandlerClass())
                .param("timeoutHours", entity.getTimeoutHours())
                .param("timeoutAction", entity.getTimeoutAction())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    public int update(SysApprovalScene entity) {
        String sql = """
            UPDATE sys_approval_scene
            SET scene_code = :sceneCode, scene_name = :sceneName, platform = :platform,
                template_id = :templateId, enabled = :enabled, handler_class = :handlerClass,
                timeout_hours = :timeoutHours, timeout_action = :timeoutAction,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("sceneCode", entity.getSceneCode())
                .param("sceneName", entity.getSceneName())
                .param("platform", entity.getPlatform())
                .param("templateId", entity.getTemplateId())
                .param("enabled", entity.getEnabled())
                .param("handlerClass", entity.getHandlerClass())
                .param("timeoutHours", entity.getTimeoutHours())
                .param("timeoutAction", entity.getTimeoutAction())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    public PageResult<SysApprovalScene> queryPage(String sceneCode, String sceneName, String platform, Integer enabled, int page, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(sceneCode != null && !sceneCode.isBlank(), "scene_code", sceneCode)
                .likeIf(sceneName != null && !sceneName.isBlank(), "scene_name", sceneName)
                .whereIf(platform != null && !platform.isBlank(), "platform = ?", platform)
                .whereIf(enabled != null, "enabled = ?", enabled);
        return queryPage(sb.getWhereClause(), sb.getParams(), page, pageSize);
    }

    public int countEnabledByPlatform(String platform) {
        String sql = """
            SELECT COUNT(*) FROM sys_approval_scene
            WHERE platform = :platform AND enabled = 1 AND deleted = 0
            """;
        Integer count = db.sql(sql).param("platform", platform).query(Integer.class).single();
        return count == null ? 0 : count;
    }

    public Optional<String> findOneEnabledSceneCodeByPlatform(String platform) {
        String sql = """
            SELECT scene_code FROM sys_approval_scene
            WHERE platform = :platform AND enabled = 1 AND deleted = 0
            ORDER BY update_time DESC, id DESC
            LIMIT 1
            """;
        List<String> result = db.sql(sql).param("platform", platform).query(String.class).list();
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(result.getFirst());
    }
}
