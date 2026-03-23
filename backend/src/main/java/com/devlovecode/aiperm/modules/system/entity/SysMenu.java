package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单实体类
 *
 * @author devlovecode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_menu")
public class SysMenu extends BaseEntity {

    private String menuName;

    private Long parentId;

    private String menuType;

    private Integer sort;

    private String path;

    private String component;

    private String perms;

    private String icon;

    private Integer isExternal;

    private Integer isCache;

    private Integer visible;

    private Integer status;

    private String permission;

    private String remark;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<SysMenu> children = new ArrayList<>();
}
