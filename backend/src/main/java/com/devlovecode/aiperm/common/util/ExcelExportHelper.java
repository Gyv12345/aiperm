package com.devlovecode.aiperm.common.util;

import cn.idev.excel.FastExcel;
import com.devlovecode.aiperm.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel 导出工具类
 *
 * @author DevLoveCode
 */
@Component
public class ExcelExportHelper {

	/**
	 * 导出 Excel
	 * @param response HTTP 响应
	 * @param filename 文件名（不含扩展名）
	 * @param clazz 导出实体类（需标注 @ExcelProperty）
	 * @param data 数据列表
	 * @param <T> 数据类型
	 */
	public <T> void export(HttpServletResponse response, String filename, Class<T> clazz, List<T> data) {
		try {
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setCharacterEncoding("utf-8");
			String encodedFilename = URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8).replace("+", "%20");
			response.setHeader("Content-Disposition", "attachment;filename=" + encodedFilename);

			FastExcel.write(response.getOutputStream(), clazz).sheet(filename).doWrite(data);
		}
		catch (IOException e) {
			throw new BusinessException("导出失败: " + e.getMessage());
		}
	}

}
