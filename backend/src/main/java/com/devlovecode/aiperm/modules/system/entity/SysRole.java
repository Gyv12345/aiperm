package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_role")
public class SysRole extends BaseEntity {

    private String roleName;

    private String roleCode;

    private Integer sort;

    private Integer status;

    private String remark;

    /**
     * 数据权限范围
     * 1-全部数据，2-本部门数据，3-本部门及下级部门数据，4-仅本人数据
     */
    private Integer dataScope;
}
