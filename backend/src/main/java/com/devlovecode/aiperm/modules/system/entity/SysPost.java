package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPost extends BaseEntity {

    private String postName;

    private String postCode;

    private Integer sort;

    private Integer status;

    private String remark;
}
