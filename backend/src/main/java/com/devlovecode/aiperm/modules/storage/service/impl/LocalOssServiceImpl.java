package com.devlovecode.aiperm.modules.storage.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.storage.config.OssProperties;
import com.devlovecode.aiperm.modules.storage.domain.OssResult;
import com.devlovecode.aiperm.modules.storage.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oss.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalOssServiceImpl implements OssService {

	private final OssProperties ossProperties;

	@Override
	public OssResult upload(MultipartFile file) {
		String originalName = file.getOriginalFilename();
		String ext = StrUtil.isNotBlank(originalName) && originalName.contains(".")
				? originalName.substring(originalName.lastIndexOf(".")) : "";
		String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		String fileName = UUID.randomUUID().toString(true) + ext;
		String relativePath = datePath + "/" + fileName;
		String fullPath = ossProperties.getLocal().getPath() + "/" + relativePath;

		try {
			File dest = new File(fullPath);
			FileUtil.mkParentDirs(dest);
			file.transferTo(dest);
		}
		catch (IOException e) {
			log.error("本地文件上传失败", e);
			throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
		}

		String url = ossProperties.getLocal().getAccessUrl() + "/" + relativePath;
		return OssResult.builder()
			.fileName(relativePath)
			.originalName(originalName)
			.url(url)
			.size(file.getSize())
			.contentType(file.getContentType())
			.build();
	}

	@Override
	public void delete(String fileName) {
		String fullPath = ossProperties.getLocal().getPath() + "/" + fileName;
		FileUtil.del(fullPath);
	}

}
