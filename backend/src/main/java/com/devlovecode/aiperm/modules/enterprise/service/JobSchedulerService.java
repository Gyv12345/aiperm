package com.devlovecode.aiperm.modules.enterprise.service;

import com.devlovecode.aiperm.modules.enterprise.entity.SysJob;
import com.devlovecode.aiperm.modules.enterprise.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSchedulerService {

	private final ThreadPoolTaskScheduler jobTaskScheduler;

	private final JobRepository jobRepo;

	private final JobInvokeExecutor jobInvokeExecutor;

	private final JobLogService jobLogService;

	private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

	@EventListener(ApplicationReadyEvent.class)
	public void initOnStartup() {
		refreshAllEnabledJobs();
	}

	public void refreshAllEnabledJobs() {
		scheduledTasks.forEach((jobId, future) -> cancelFuture(jobId, future));
		scheduledTasks.clear();

		jobRepo.findAllEnabled().forEach(this::scheduleJobInternal);
		log.info("定时任务初始化完成，已加载 {} 个运行中任务", scheduledTasks.size());
	}

	public void refreshJobAfterCommit(SysJob job) {
		runAfterCommit(() -> refreshJob(job));
	}

	public void removeJobAfterCommit(Long jobId) {
		runAfterCommit(() -> removeJob(jobId));
	}

	public void refreshJob(SysJob job) {
		if (job == null || job.getId() == null) {
			return;
		}

		removeJob(job.getId());
		if (Integer.valueOf(1).equals(job.getStatus()) && Integer.valueOf(0).equals(job.getDeleted())) {
			scheduleJobInternal(job);
		}
	}

	public void removeJob(Long jobId) {
		if (jobId == null) {
			return;
		}
		ScheduledFuture<?> future = scheduledTasks.remove(jobId);
		if (future != null) {
			cancelFuture(jobId, future);
		}
	}

	private void scheduleJobInternal(SysJob job) {
		if (job.getCronExpression() == null || job.getCronExpression().isBlank()) {
			log.warn("任务 {} Cron 为空，跳过调度", job.getId());
			return;
		}

		Trigger trigger;
		try {
			trigger = new CronTrigger(job.getCronExpression().trim());
		}
		catch (Exception e) {
			log.error("任务 {} Cron 非法 [{}]，跳过调度", job.getId(), job.getCronExpression(), e);
			return;
		}

		ScheduledFuture<?> future = jobTaskScheduler.schedule(() -> executeJob(job), trigger);
		if (future == null) {
			log.error("任务 {} 调度失败，schedule 返回 null", job.getId());
			return;
		}
		scheduledTasks.put(job.getId(), future);
		log.info("任务 {} 已调度，cron={}", job.getId(), job.getCronExpression());
	}

	private void executeJob(SysJob job) {
		executeJob(job, "SCHEDULE", "system");
	}

	public void executeNow(SysJob job, String operator) {
		executeJob(job, "MANUAL", operator);
	}

	private void executeJob(SysJob job, String triggerSource, String operator) {
		LocalDateTime startTime = LocalDateTime.now();
		try {
			jobInvokeExecutor.execute(job);
			LocalDateTime endTime = LocalDateTime.now();
			jobLogService.recordSuccess(job, triggerSource, operator, startTime, endTime, "执行成功");
			log.info("任务 {} 执行成功", job.getId());
		}
		catch (Exception e) {
			LocalDateTime endTime = LocalDateTime.now();
			jobLogService.recordFailure(job, triggerSource, operator, startTime, endTime, e);
			log.error("任务 {} 执行失败，target={}", job.getId(), job.getBeanClass(), e);
		}
	}

	private void cancelFuture(Long jobId, ScheduledFuture<?> future) {
		boolean cancelled = future.cancel(false);
		log.info("任务 {} 取消调度: {}", jobId, cancelled);
	}

	private void runAfterCommit(Runnable action) {
		if (!TransactionSynchronizationManager.isActualTransactionActive()) {
			action.run();
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				action.run();
			}
		});
	}

}
