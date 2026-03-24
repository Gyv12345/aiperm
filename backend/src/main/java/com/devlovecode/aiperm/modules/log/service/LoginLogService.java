package com.devlovecode.aiperm.modules.log.service;

import com.devlovecode.aiperm.modules.log.entity.SysLoginLog;
import com.devlovecode.aiperm.modules.log.repository.LoginLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    /**
     * 记录登录成功日志
     */
    public void recordSuccess(Long userId, String username, String ip) {
        record(userId, username, ip, 0, "登录成功");
    }

    /**
     * 记录登录失败日志
     */
    public void recordFailed(String username, String ip, String message) {
        record(null, username, ip, 1, message);
    }

    private void record(Long userId, String username, String ip, Integer status, String message) {
        try {
            SysLoginLog logRecord = new SysLoginLog();
            logRecord.setUserId(userId);
            logRecord.setUsername((username == null || username.isBlank()) ? "-" : username);
            logRecord.setIp((ip == null || ip.isBlank()) ? "unknown" : ip);
            logRecord.setStatus(status);
            logRecord.setMsg(message);
            logRecord.setLoginTime(LocalDateTime.now());
            logRecord.setDeleted(0);
            logRecord.setCreateTime(LocalDateTime.now());
            loginLogRepository.save(logRecord);
        } catch (Exception e) {
            log.warn("记录登录日志失败: username={}, ip={}", username, ip, e);
        }
    }
}
