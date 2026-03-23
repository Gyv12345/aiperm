package com.devlovecode.aiperm.modules.approval.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_approval_instance")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysApprovalInstance extends BaseEntity {
    private String sceneCode;
    private String businessType;
    private Long businessId;
    private Long initiatorId;
    private String platform;
    private String platformInstanceId;
    private String status;
    private String formData;
    private LocalDateTime resultTime;
}
