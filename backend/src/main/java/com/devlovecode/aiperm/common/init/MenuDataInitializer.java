package com.devlovecode.aiperm.common.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 菜单数据初始化器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuDataInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        // 检查是否有菜单数据
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_menu WHERE deleted = 0",
                Integer.class);

        if (count != null && count > 0) {
            log.info("菜单数据已存在，跳过初始化");
            return;
        }

        log.info("开始初始化菜单数据...");

        // 仪表板
        insertMenu(jdbc, 1L, "仪表板", 0L, "1", 1, "/dashboard", null, null, "Odometer");
        insertMenu(jdbc, 2L, "首页", 1L, "2", 1, "index", "dashboard/index", null, null);

        // 系统管理
        insertMenu(jdbc, 10L, "系统管理", 0L, "1", 2, "/system", null, null, "Setting");
        insertMenu(jdbc, 11L, "用户管理", 10L, "2", 1, "user", "system/user/index", null, null);
        insertMenu(jdbc, 12L, "角色管理", 10L, "2", 2, "role", "system/role/index", null, null);
        insertMenu(jdbc, 13L, "菜单管理", 10L, "2", 3, "menu", "system/menu/index", null, null);
        insertMenu(jdbc, 14L, "部门管理", 10L, "2", 4, "dept", "system/dept/index", null, null);
        insertMenu(jdbc, 15L, "岗位管理", 10L, "2", 5, "post", "system/post/index", null, null);
        insertMenu(jdbc, 16L, "字典管理", 10L, "2", 6, "dict", "system/dict/index", null, null);
        insertMenu(jdbc, 17L, "权限管理", 10L, "2", 7, "permission", "system/permission/index", null, null);

        // 企业管理
        insertMenu(jdbc, 20L, "企业管理", 0L, "1", 3, "/enterprise", null, null, "OfficeBuilding");
        insertMenu(jdbc, 21L, "公告通知", 20L, "2", 1, "notice", "enterprise/notice/index", null, null);
        insertMenu(jdbc, 22L, "消息中心", 20L, "2", 2, "message", "enterprise/message/index", null, null);
        insertMenu(jdbc, 23L, "定时任务", 20L, "2", 3, "job", "enterprise/job/index", null, null);
        insertMenu(jdbc, 24L, "参数配置", 20L, "2", 4, "config", "enterprise/config/index", null, null);

        // 为超级管理员角色分配所有菜单
        jdbc.update("INSERT IGNORE INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu WHERE deleted = 0");

        log.info("菜单数据初始化完成");
    }

    private void insertMenu(JdbcTemplate jdbc, Long id, String menuName, Long parentId,
            String menuType, int sort, String path, String component, String perms, String icon) {
        String sql = """
            INSERT INTO sys_menu (id, menu_name, parent_id, menu_type, sort, path, component, icon,
                is_frame, is_cache, visible, status, deleted, version, create_time, create_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 1, 1, 0, 0, NOW(), 'system')
            ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name)
            """;
        jdbc.update(sql, id, menuName, parentId, menuType, sort, path, component, icon);
    }
}
