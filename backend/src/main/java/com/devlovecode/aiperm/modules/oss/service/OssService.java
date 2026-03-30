package com.devlovecode.aiperm.modules.oss.service;

import com.devlovecode.aiperm.modules.oss.domain.OssResult;
import org.springframework.web.multipart.MultipartFile;

public interface OssService {

	/** 上传文件，返回访问信息 */
	OssResult upload(MultipartFile file);

	/** 删除文件（传入 fileName，即 OssResult.fileName） */
	void delete(String fileName);

}
