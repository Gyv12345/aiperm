package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.modules.system.entity.SysMenu;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MenuRepository extends BaseRepository<SysMenu> {

    public MenuRepository(JdbcClient db) {
        super(db, "sys_menu", SysMenu.class);
    }

    /**
     * 插入菜单
     */
    public void insert(SysMenu entity) {
        String sql = """
            INSERT INTO sys_menu (menu_name, parent_id, menu_type, sort, path, component, perms, icon,
                is_external, is_cache, visible, status, permission, remark, deleted, version, create_time, create_by)
            VALUES (:menuName, :parentId, :menuType, :sort, :path, :component, :perms, :icon,
                :isExternal, :isCache, :visible, :status, :permission, :remark, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("menuName", entity.getMenuName())
                .param("parentId", entity.getParentId())
                .param("menuType", entity.getMenuType())
                .param("sort", entity.getSort())
                .param("path", entity.getPath())
                .param("component", entity.getComponent())
                .param("perms", entity.getPerms())
                .param("icon", entity.getIcon())
                .param("isExternal", entity.getIsExternal())
                .param("isCache", entity.getIsCache())
                .param("visible", entity.getVisible())
                .param("status", entity.getStatus())
                .param("permission", entity.getPermission())
                .param("remark", entity.getRemark())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 更新菜单
     */
    public int update(SysMenu entity) {
        String sql = """
            UPDATE sys_menu
            SET menu_name = :menuName, parent_id = :parentId, menu_type = :menuType, sort = :sort,
                path = :path, component = :component, perms = :perms, icon = :icon,
                is_external = :isExternal, is_cache = :isCache, visible = :visible, status = :status,
                permission = :permission, remark = :remark,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("menuName", entity.getMenuName())
                .param("parentId", entity.getParentId())
                .param("menuType", entity.getMenuType())
                .param("sort", entity.getSort())
                .param("path", entity.getPath())
                .param("component", entity.getComponent())
                .param("perms", entity.getPerms())
                .param("icon", entity.getIcon())
                .param("isExternal", entity.getIsExternal())
                .param("isCache", entity.getIsCache())
                .param("visible", entity.getVisible())
                .param("status", entity.getStatus())
                .param("permission", entity.getPermission())
                .param("remark", entity.getRemark())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 根据父ID查询子菜单
     */
    public List<SysMenu> findByParentId(Long parentId) {
        String sql = "SELECT * FROM sys_menu WHERE parent_id = :parentId AND deleted = 0 ORDER BY sort ASC";
        return db.sql(sql).param("parentId", parentId).query(SysMenu.class).list();
    }

    /**
     * 查询所有菜单（按排序）
     */
    public List<SysMenu> findAllOrderBySort() {
        String sql = "SELECT * FROM sys_menu WHERE deleted = 0 ORDER BY parent_id ASC, sort ASC";
        return db.sql(sql).query(SysMenu.class).list();
    }

    /**
     * 检查是否有子菜单
     */
    public boolean hasChildren(Long parentId) {
        String sql = "SELECT COUNT(*) FROM sys_menu WHERE parent_id = :parentId AND deleted = 0";
        Integer count = db.sql(sql).param("parentId", parentId).query(Integer.class).single();
        return count != null && count > 0;
    }

    /**
     * 检查菜单名称是否重复（同父级下）
     */
    public boolean existsByMenuNameAndParentId(String menuName, Long parentId) {
        String sql = "SELECT COUNT(*) FROM sys_menu WHERE menu_name = :menuName AND parent_id = :parentId AND deleted = 0";
        Integer count = db.sql(sql)
                .param("menuName", menuName)
                .param("parentId", parentId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    /**
     * 检查菜单名称是否重复（同父级下，排除指定ID）
     */
    public boolean existsByMenuNameAndParentIdExcludeId(String menuName, Long parentId, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM sys_menu WHERE menu_name = :menuName AND parent_id = :parentId AND id != :id AND deleted = 0";
        Integer count = db.sql(sql)
                .param("menuName", menuName)
                .param("parentId", parentId)
                .param("id", excludeId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }
}
