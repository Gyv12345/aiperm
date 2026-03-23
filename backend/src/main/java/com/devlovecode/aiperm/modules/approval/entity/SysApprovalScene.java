package com.devlovecode.aiperm.modules.approval.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "sys_approval_scene")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysApprovalScene extends BaseEntity {
    private String sceneCode;
    private String sceneName;
    private String platform;
    private String templateId;
    private Integer enabled;
    private String handlerClass;
    private Integer timeoutHours;
    private String timeoutAction;
}
