package com.devlovecode.aiperm.modules.notification.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class MessageTemplateRepository extends BaseRepository<SysMessageTemplate> {

    public MessageTemplateRepository(JdbcClient db) {
        super(db, "sys_message_template", SysMessageTemplate.class);
    }

    public Optional<SysMessageTemplate> findByTemplateCode(String templateCode) {
        String sql = "SELECT * FROM sys_message_template WHERE template_code = :templateCode AND deleted = 0";
        return db.sql(sql).param("templateCode", templateCode).query(SysMessageTemplate.class).optional();
    }

    public boolean existsByTemplateCode(String templateCode) {
        String sql = "SELECT COUNT(*) FROM sys_message_template WHERE template_code = :templateCode AND deleted = 0";
        Integer count = db.sql(sql).param("templateCode", templateCode).query(Integer.class).single();
        return count != null && count > 0;
    }

    public boolean existsByTemplateCodeExcludeId(String templateCode, Long id) {
        String sql = "SELECT COUNT(*) FROM sys_message_template WHERE template_code = :templateCode AND id != :id AND deleted = 0";
        Integer count = db.sql(sql).param("templateCode", templateCode).param("id", id).query(Integer.class).single();
        return count != null && count > 0;
    }

    public void insert(SysMessageTemplate entity) {
        String sql = """
            INSERT INTO sys_message_template (
                template_code, template_name, category, platform, title, content,
                deleted, version, create_time, create_by
            ) VALUES (
                :templateCode, :templateName, :category, :platform, :title, :content,
                0, 0, :createTime, :createBy
            )
            """;
        db.sql(sql)
                .param("templateCode", entity.getTemplateCode())
                .param("templateName", entity.getTemplateName())
                .param("category", entity.getCategory())
                .param("platform", entity.getPlatform())
                .param("title", entity.getTitle())
                .param("content", entity.getContent())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    public int update(SysMessageTemplate entity) {
        String sql = """
            UPDATE sys_message_template
            SET template_code = :templateCode, template_name = :templateName, category = :category,
                platform = :platform, title = :title, content = :content,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("templateCode", entity.getTemplateCode())
                .param("templateName", entity.getTemplateName())
                .param("category", entity.getCategory())
                .param("platform", entity.getPlatform())
                .param("title", entity.getTitle())
                .param("content", entity.getContent())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    public PageResult<SysMessageTemplate> queryPage(String templateCode, String category, String platform, int page, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(templateCode != null && !templateCode.isBlank(), "template_code", templateCode)
                .whereIf(category != null && !category.isBlank(), "category = ?", category)
                .whereIf(platform != null && !platform.isBlank(), "platform = ?", platform);
        return queryPage(sb.getWhereClause(), sb.getParams(), page, pageSize);
    }
}
