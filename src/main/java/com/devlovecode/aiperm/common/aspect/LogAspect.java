package com.devlovecode.aiperm.common.aspect;

import cn.hutool.json.JSONUtil;
import com.devlovecode.aiperm.common.annotation.Log;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Around("@annotation(com.devlovecode.aiperm.common.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);

        HttpServletRequest request = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            request = attributes.getRequest();
        }

        Object result = null;
        String errorMsg = null;
        int status = 0;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = 1;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            LogEvent event = new LogEvent(
                logAnnotation.title(),
                logAnnotation.operType().getCode(),
                joinPoint.getTarget().getClass().getName() + "." + method.getName(),
                request != null ? request.getMethod() : "",
                request != null ? request.getRequestURI() : "",
                request != null ? getIp(request) : "",
                logAnnotation.saveRequestParam() ? buildParams(joinPoint.getArgs()) : "",
                logAnnotation.saveResponseResult() && result != null ? JSONUtil.toJsonStr(result) : "",
                status,
                errorMsg,
                costTime
            );
            eventPublisher.publishEvent(event);
        }
    }

    private String buildParams(Object[] args) {
        try {
            return JSONUtil.toJsonStr(args);
        } catch (Exception e) {
            return "";
        }
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
