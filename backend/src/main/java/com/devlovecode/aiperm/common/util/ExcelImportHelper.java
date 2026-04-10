package com.devlovecode.aiperm.common.util;

import cn.idev.excel.FastExcel;
import com.devlovecode.aiperm.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Excel 导入工具
 */
@Component
public class ExcelImportHelper {

	public <T> List<T> read(MultipartFile file, Class<T> clazz) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("导入文件不能为空");
		}
		try (InputStream inputStream = file.getInputStream()) {
			return FastExcel.read(inputStream, clazz, null).sheet().doReadSync();
		}
		catch (IOException e) {
			throw new BusinessException("读取导入文件失败: " + e.getMessage());
		}
		catch (Exception e) {
			throw new BusinessException("解析导入文件失败: " + e.getMessage());
		}
	}

}
