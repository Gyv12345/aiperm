package com.devlovecode.aiperm.modules.log.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_oper_log")
@Data
public class SysOperLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
