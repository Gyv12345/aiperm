package com.devlovecode.aiperm.modules.log.listener;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.aspect.LogEvent;
import com.devlovecode.aiperm.modules.log.entity.SysOperLog;
import com.devlovecode.aiperm.modules.log.repository.OperLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventListener {

    private final OperLogRepository operLogRepo;

    @Async
    @EventListener
    public void onLogEvent(LogEvent event) {
        try {
            SysOperLog operLog = new SysOperLog();
            operLog.setTitle(event.getTitle());
            operLog.setOperType(event.getOperType());
            operLog.setMethod(event.getMethod());
            operLog.setRequestMethod(event.getRequestMethod());
            operLog.setOperUrl(event.getOperUrl());
            operLog.setOperIp(event.getOperIp());
            operLog.setOperParam(event.getOperParam());
            operLog.setJsonResult(event.getJsonResult());
            operLog.setStatus(event.getStatus());
            operLog.setErrorMsg(event.getErrorMsg());
            operLog.setCostTime(event.getCostTime());
            operLog.setCreateTime(LocalDateTime.now());

            try {
                String loginId = StpUtil.getLoginIdAsString();
                operLog.setOperUser(loginId);
            } catch (Exception ignored) {
                operLog.setOperUser("anonymous");
            }

            operLogRepo.insert(operLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}
