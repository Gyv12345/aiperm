package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_post")
public class SysPost extends BaseEntity {

    private String postName;

    private String postCode;

    private Integer sort;

    private Integer status;

    private String remark;
}
