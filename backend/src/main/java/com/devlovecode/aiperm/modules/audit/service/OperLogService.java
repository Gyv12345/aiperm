package com.devlovecode.aiperm.modules.audit.service;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.common.util.ExcelExportHelper;
import com.devlovecode.aiperm.modules.audit.entity.SysOperLog;
import com.devlovecode.aiperm.modules.audit.export.OperLogExportModel;
import com.devlovecode.aiperm.modules.audit.repository.OperLogRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperLogService {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final OperLogRepository operLogRepo;

	private final ExcelExportHelper excelExportHelper;

	/**
	 * 分页查询
	 */
	public PageResult<SysOperLog> queryPage(String title, Integer status, String operUser, String operIp,
			LocalDate startDate, LocalDate endDate, int page, int pageSize) {
		Specification<SysOperLog> specification = SpecificationUtils.and(SpecificationUtils.like("title", title),
				SpecificationUtils.eq("status", status), SpecificationUtils.like("operUser", operUser),
				SpecificationUtils.like("operIp", operIp),
				SpecificationUtils.ge("createTime", startDate == null ? null : startDate.atStartOfDay()),
				SpecificationUtils.le("createTime", endDate == null ? null : endDate.atTime(23, 59, 59)));
		Page<SysOperLog> jpaPage = operLogRepo.findAll(specification,
				PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime")));
		return PageResult.fromJpaPage(jpaPage);
	}

	public SysOperLog findById(Long id) {
		return operLogRepo.findById(id).orElseThrow(() -> new BusinessException("操作日志不存在"));
	}

	public void export(String title, Integer status, String operUser, String operIp, LocalDate startDate,
			LocalDate endDate, HttpServletResponse response) {
		List<OperLogExportModel> rows = queryPage(title, status, operUser, operIp, startDate, endDate, 1, Integer.MAX_VALUE)
			.getList()
			.stream()
			.map(this::toExportModel)
			.toList();
		excelExportHelper.export(response, "oper-logs", OperLogExportModel.class, rows);
	}

	/**
	 * 删除
	 */
	@Transactional
	public void delete(Long id) {
		operLogRepo.deleteById(id);
	}

	/**
	 * 清空
	 */
	@Transactional
	public void clean() {
		operLogRepo.deleteAll();
	}

	private OperLogExportModel toExportModel(SysOperLog entity) {
		OperLogExportModel model = new OperLogExportModel();
		model.setTitle(entity.getTitle());
		model.setOperType(entity.getOperType());
		model.setOperUser(entity.getOperUser());
		model.setRequestMethod(entity.getRequestMethod());
		model.setOperUrl(entity.getOperUrl());
		model.setOperIp(entity.getOperIp());
		model.setStatusText(entity.getStatus() != null && entity.getStatus() == 0 ? "成功" : "失败");
		model.setErrorMsg(entity.getErrorMsg());
		model.setCostTime(entity.getCostTime());
		model.setCreateTime(entity.getCreateTime() == null ? "" : entity.getCreateTime().format(DATE_TIME_FORMATTER));
		return model;
	}

}
