package com.devlovecode.aiperm.modules.enterprise.service;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.util.ExcelExportHelper;
import com.devlovecode.aiperm.modules.enterprise.entity.SysJob;
import com.devlovecode.aiperm.modules.enterprise.entity.SysJobLog;
import com.devlovecode.aiperm.modules.enterprise.export.JobLogExportModel;
import com.devlovecode.aiperm.modules.enterprise.repository.JobLogRepository;
import com.devlovecode.aiperm.modules.enterprise.vo.JobLogVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobLogService {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final JobLogRepository jobLogRepo;

	private final ExcelExportHelper excelExportHelper;

	public PageResult<JobLogVO> queryPage(String jobName, Integer status, String triggerSource, LocalDateTime startTime,
			LocalDateTime endTime, Integer page, Integer pageSize) {
		Page<SysJobLog> jpaPage = jobLogRepo.queryPage(jobName, status, triggerSource, startTime, endTime, page, pageSize);
		return PageResult.fromJpaPage(jpaPage).map(this::toVO);
	}

	public List<JobLogVO> listForExport(String jobName, Integer status, String triggerSource, LocalDateTime startTime,
			LocalDateTime endTime) {
		return jobLogRepo.queryPage(jobName, status, triggerSource, startTime, endTime, 1, Integer.MAX_VALUE)
			.getContent()
			.stream()
			.map(this::toVO)
			.toList();
	}

	public void export(String jobName, Integer status, String triggerSource, LocalDateTime startTime,
			LocalDateTime endTime, HttpServletResponse response) {
		List<JobLogExportModel> rows = listForExport(jobName, status, triggerSource, startTime, endTime)
			.stream()
			.map(this::toExportModel)
			.toList();
		excelExportHelper.export(response, "job-logs", JobLogExportModel.class, rows);
	}

	@Transactional
	public void delete(Long id) {
		if (!jobLogRepo.existsById(id)) {
			throw new BusinessException("任务日志不存在");
		}
		jobLogRepo.softDelete(id, LocalDateTime.now());
	}

	@Transactional
	public void clean() {
		jobLogRepo.findAll().forEach(log -> jobLogRepo.softDelete(log.getId(), LocalDateTime.now()));
	}

	public void recordSuccess(SysJob job, String triggerSource, String operator, LocalDateTime startTime,
			LocalDateTime endTime, String message) {
		saveLog(job, triggerSource, operator, startTime, endTime, 1, message, null);
	}

	public void recordFailure(SysJob job, String triggerSource, String operator, LocalDateTime startTime,
			LocalDateTime endTime, Exception exception) {
		saveLog(job, triggerSource, operator, startTime, endTime, 0, exception.getMessage(), toStackTrace(exception));
	}

	private void saveLog(SysJob job, String triggerSource, String operator, LocalDateTime startTime, LocalDateTime endTime,
			Integer status, String message, String exceptionInfo) {
		SysJobLog log = new SysJobLog();
		log.setJobId(job.getId());
		log.setJobName(job.getJobName());
		log.setJobGroup(job.getJobGroup());
		log.setBeanClass(job.getBeanClass());
		log.setTriggerSource(triggerSource);
		log.setStatus(status);
		log.setMessage(message);
		log.setExceptionInfo(exceptionInfo);
		log.setStartTime(startTime);
		log.setEndTime(endTime);
		log.setCostTime(Math.max(0L, java.time.Duration.between(startTime, endTime).toMillis()));
		log.setCreateBy(operator);
		log.setCreateTime(LocalDateTime.now());
		jobLogRepo.save(log);
	}

	private JobLogVO toVO(SysJobLog entity) {
		JobLogVO vo = new JobLogVO();
		vo.setId(entity.getId());
		vo.setJobId(entity.getJobId());
		vo.setJobName(entity.getJobName());
		vo.setJobGroup(entity.getJobGroup());
		vo.setBeanClass(entity.getBeanClass());
		vo.setTriggerSource(entity.getTriggerSource());
		vo.setStatus(entity.getStatus());
		vo.setMessage(entity.getMessage());
		vo.setExceptionInfo(entity.getExceptionInfo());
		vo.setStartTime(entity.getStartTime());
		vo.setEndTime(entity.getEndTime());
		vo.setCostTime(entity.getCostTime());
		return vo;
	}

	private JobLogExportModel toExportModel(JobLogVO vo) {
		JobLogExportModel model = new JobLogExportModel();
		model.setJobId(vo.getJobId());
		model.setJobName(vo.getJobName());
		model.setJobGroup(vo.getJobGroup());
		model.setBeanClass(vo.getBeanClass());
		model.setTriggerSource(vo.getTriggerSource());
		model.setStatusText(vo.getStatus() != null && vo.getStatus() == 1 ? "成功" : "失败");
		model.setMessage(vo.getMessage());
		model.setStartTime(formatDateTime(vo.getStartTime()));
		model.setEndTime(formatDateTime(vo.getEndTime()));
		model.setCostTime(vo.getCostTime());
		return model;
	}

	private String formatDateTime(LocalDateTime value) {
		return value == null ? "" : value.format(DATE_TIME_FORMATTER);
	}

	private String toStackTrace(Exception exception) {
		StringWriter stringWriter = new StringWriter();
		exception.printStackTrace(new PrintWriter(stringWriter));
		String stackTrace = stringWriter.toString();
		return stackTrace.length() > 4000 ? stackTrace.substring(0, 4000) : stackTrace;
	}

}
