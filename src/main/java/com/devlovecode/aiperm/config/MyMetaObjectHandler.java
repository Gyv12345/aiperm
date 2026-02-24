package com.devlovecode.aiperm.config;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus字段自动填充处理器
 *
 * @author DevLoveCode
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 获取当前登录用户ID
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId != null) {
                this.strictInsertFill(metaObject, "createBy", Long.class, Long.valueOf(loginId.toString()));
                this.strictInsertFill(metaObject, "updateBy", Long.class, Long.valueOf(loginId.toString()));
            }
        } catch (Exception e) {
            // 未登录或获取失败时忽略
            log.debug("获取当前登录用户ID失败: {}", e.getMessage());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 获取当前登录用户ID
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId != null) {
                this.strictUpdateFill(metaObject, "updateBy", Long.class, Long.valueOf(loginId.toString()));
            }
        } catch (Exception e) {
            // 未登录或获取失败时忽略
            log.debug("获取当前登录用户ID失败: {}", e.getMessage());
        }
    }
}
