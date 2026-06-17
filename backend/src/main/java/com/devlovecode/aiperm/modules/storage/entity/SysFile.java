package com.devlovecode.aiperm.modules.storage.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件记录实体
 *
 * <p>对应 sys_file 表（V2.0.0 建表）。该表结构较精简（无 update_time/update_by/version），
 * 因此不继承 BaseEntity，独立管理字段。
 *
 * @author DevLoveCode
 */
@Data
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "sys_file")
@SQLRestriction("deleted = 0")
public class SysFile implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 存储文件名（UUID 生成） */
	private String fileName;

	/** 原始文件名 */
	private String originalName;

	/** 存储路径（本地相对路径） */
	private String filePath;

	/** 访问 URL */
	private String fileUrl;

	/** 文件大小（字节） */
	private Long fileSize;

	/** MIME 类型 */
	private String fileType;

	/** 存储类型：local / aliyun */
	private String storageType;

	/** 删除标志：0-未删除 1-已删除 */
	private Integer deleted;

	/** 上传时间 */
	private LocalDateTime createTime;

	/** 上传人（用户ID） */
	private String createBy;

}
