package com.devlovecode.aiperm.modules.storage.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.storage.config.OssProperties;
import com.devlovecode.aiperm.modules.storage.domain.OssResult;
import com.devlovecode.aiperm.modules.storage.dto.FileDTO;
import com.devlovecode.aiperm.modules.storage.entity.SysFile;
import com.devlovecode.aiperm.modules.storage.repository.SysFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 文件管理服务
 *
 * <p>在 {@link OssService}（存储层）之上增加元数据持久化：上传后落库 sys_file，
 * 支持分页查询与软删除。存储类型由 OssProperties 决定（local/aliyun）。
 *
 * @author DevLoveCode
 */
@Service
@RequiredArgsConstructor
public class FileService {

	private final OssService ossService;

	private final SysFileRepository fileRepo;

	private final OssProperties ossProperties;

	/**
	 * 上传文件：先走存储层（OSS/本地），再将元数据落库。
	 */
	@Transactional
	public SysFile upload(MultipartFile file) {
		OssResult result = ossService.upload(file);

		SysFile entity = new SysFile();
		entity.setFileName(result.getFileName());
		entity.setOriginalName(result.getOriginalName());
		entity.setFilePath(result.getFileName());
		entity.setFileUrl(result.getUrl());
		entity.setFileSize(result.getSize());
		entity.setFileType(result.getContentType());
		entity.setStorageType(ossProperties.getStorageType());
		entity.setDeleted(0);
		entity.setCreateTime(LocalDateTime.now());
		entity.setCreateBy(getCurrentUserId());
		return fileRepo.save(entity);
	}

	/**
	 * 分页查询
	 */
	public PageResult<SysFile> queryPage(FileDTO dto) {
		Page<SysFile> page = fileRepo.queryPage(dto.getOriginalName(), dto.getFileType(), dto.getStorageType(),
				dto.getPage(), dto.getPageSize());
		return PageResult.fromJpaPage(page);
	}

	/**
	 * 查询详情
	 */
	public SysFile findById(Long id) {
		return fileRepo.findById(id).orElseThrow(() -> new BusinessException("文件不存在"));
	}

	/**
	 * 删除：先删存储层文件，再软删记录。存储层删除失败不阻断（避免孤立记录无法清理）。
	 */
	@Transactional
	public void delete(Long id) {
		SysFile file = findById(id);
		try {
			ossService.delete(file.getFileName());
		}
		catch (Exception e) {
			// 存储层删除失败（文件可能已被手动删除），仅记录日志，继续软删记录
		}
		fileRepo.softDelete(id);
	}

	private String getCurrentUserId() {
		try {
			return StpUtil.getLoginIdAsString();
		}
		catch (Exception e) {
			return "system";
		}
	}

}
