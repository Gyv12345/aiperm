package com.devlovecode.aiperm.modules.mcp.tool;

import com.devlovecode.aiperm.modules.enterprise.dto.JobDTO;
import com.devlovecode.aiperm.modules.enterprise.service.JobService;
import com.devlovecode.aiperm.modules.enterprise.vo.JobVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 定时任务 MCP 工具
 *
 * @author DevLoveCode
 */
@Component
@RequiredArgsConstructor
public class JobTool extends BaseMcpTool {

    private final JobService jobService;

    @Tool(description = "根据ID查询定时任务详情")
    public String getJobById(@ToolParam(description = "任务ID") Long jobId) {
        try {
            return toToon(toMap(jobService.findById(jobId)));
        } catch (Exception e) {
            return error("查询失败: " + e.getMessage());
        }
    }

    @Tool(description = "创建新的定时任务")
    public String createJob(
            @ToolParam(description = "任务名称") String jobName,
            @ToolParam(description = "任务分组") String jobGroup,
            @ToolParam(description = "Cron表达式") String cronExpression,
            @ToolParam(description = "执行类") String beanClass,
            @ToolParam(description = "状态：0暂停，1正常") Integer status,
            @ToolParam(description = "备注") String remark) {
        try {
            JobDTO dto = new JobDTO();
            dto.setJobName(jobName);
            dto.setJobGroup(jobGroup);
            dto.setCronExpression(cronExpression);
            dto.setBeanClass(beanClass);
            dto.setStatus(status != null ? status : 1);
            dto.setRemark(remark);

            Long id = jobService.create(dto);
            return "id: " + id + "\njobName: " + jobName;
        } catch (Exception e) {
            return error("创建失败: " + e.getMessage());
        }
    }

    @Tool(description = "更新定时任务")
    public String updateJob(
            @ToolParam(description = "任务ID") Long jobId,
            @ToolParam(description = "任务名称") String jobName,
            @ToolParam(description = "任务分组") String jobGroup,
            @ToolParam(description = "Cron表达式") String cronExpression,
            @ToolParam(description = "执行类") String beanClass,
            @ToolParam(description = "状态：0暂停，1正常") Integer status,
            @ToolParam(description = "备注") String remark) {
        try {
            JobDTO dto = new JobDTO();
            dto.setJobName(jobName);
            dto.setJobGroup(jobGroup);
            dto.setCronExpression(cronExpression);
            dto.setBeanClass(beanClass);
            dto.setStatus(status);
            dto.setRemark(remark);

            jobService.update(jobId, dto);
            return "ok: true";
        } catch (Exception e) {
            return error("更新失败: " + e.getMessage());
        }
    }

    @Tool(description = "删除定时任务")
    public String deleteJob(@ToolParam(description = "任务ID") Long jobId) {
        try {
            jobService.delete(jobId);
            return "ok: true";
        } catch (Exception e) {
            return error("删除失败: " + e.getMessage());
        }
    }

    @Tool(description = "暂停定时任务")
    public String pauseJob(@ToolParam(description = "任务ID") Long jobId) {
        try {
            jobService.pause(jobId);
            return "ok: true";
        } catch (Exception e) {
            return error("暂停失败: " + e.getMessage());
        }
    }

    @Tool(description = "恢复定时任务")
    public String resumeJob(@ToolParam(description = "任务ID") Long jobId) {
        try {
            jobService.resume(jobId);
            return "ok: true";
        } catch (Exception e) {
            return error("恢复失败: " + e.getMessage());
        }
    }

    private Map<String, Object> toMap(JobVO job) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", job.getId());
        map.put("jobName", job.getJobName());
        map.put("jobGroup", job.getJobGroup());
        map.put("cronExpression", job.getCronExpression());
        map.put("beanClass", job.getBeanClass());
        map.put("status", job.getStatus());
        map.put("remark", job.getRemark());
        map.put("createTime", job.getCreateTime());
        return map;
    }
}
