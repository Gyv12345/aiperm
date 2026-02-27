package com.devlovecode.aiperm.modules.captcha.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 验证码发送日志（无软删除，不继承 BaseEntity）
 */
@Data
public class SysCaptchaLog {
    private Long id;
    private String type;
    private String target;
    private String code;
    private String scene;
    private Integer status;        // 1成功,0失败
    private String failReason;
    private String ip;
    private LocalDateTime createTime;
}
