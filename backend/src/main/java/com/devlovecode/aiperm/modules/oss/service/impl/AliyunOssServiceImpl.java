package com.devlovecode.aiperm.modules.oss.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.oss.config.OssProperties;
import com.devlovecode.aiperm.modules.oss.domain.OssResult;
import com.devlovecode.aiperm.modules.oss.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oss.storage-type", havingValue = "aliyun")
public class AliyunOssServiceImpl implements OssService {

	private final OssProperties ossProperties;

	@Override
	public OssResult upload(MultipartFile file) {
		OssProperties.Aliyun config = ossProperties.getAliyun();
		String originalName = file.getOriginalFilename();
		String ext = StrUtil.isNotBlank(originalName) && originalName.contains(".")
				? originalName.substring(originalName.lastIndexOf(".")) : "";
		String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		String fileName = datePath + "/" + UUID.randomUUID().toString(true) + ext;

		OSS ossClient = new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKeyId(),
				config.getAccessKeySecret());
		try {
			ossClient.putObject(config.getBucketName(), fileName, file.getInputStream());
		}
		catch (Exception e) {
			log.error("阿里云 OSS 上传失败", e);
			throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
		}
		finally {
			ossClient.shutdown();
		}

		String url = config.getAccessUrl() + "/" + fileName;
		return OssResult.builder()
			.fileName(fileName)
			.originalName(originalName)
			.url(url)
			.size(file.getSize())
			.contentType(file.getContentType())
			.build();
	}

	@Override
	public void delete(String fileName) {
		OssProperties.Aliyun config = ossProperties.getAliyun();
		OSS ossClient = new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKeyId(),
				config.getAccessKeySecret());
		try {
			ossClient.deleteObject(config.getBucketName(), fileName);
		}
		finally {
			ossClient.shutdown();
		}
	}

}
