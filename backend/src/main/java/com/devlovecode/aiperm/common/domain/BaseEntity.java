package com.devlovecode.aiperm.common.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 所有实体类的父类，包含通用字段
 *
 * @author devlovecode
 */
@Data
@MappedSuperclass
@SQLRestriction("deleted = 0")
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    private Integer deleted;

    @Version
    private Integer version;
}
