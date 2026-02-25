package com.devlovecode.aiperm.modules.log.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysOperLog {

    private Long id;

    private String title;

    private Integer operType;

    private String method;

    private String requestMethod;

    private String operUrl;

    private String operIp;

    private String operParam;

    private String jsonResult;

    private Integer status;

    private String errorMsg;

    private Long costTime;

    private String operUser;

    private String operName;

    private LocalDateTime createTime;
}
