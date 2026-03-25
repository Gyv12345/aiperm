package com.devlovecode.aiperm.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.devlovecode.aiperm.common.annotation.Idempotent;
import com.devlovecode.aiperm.common.enums.AccessLimitScope;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.util.ClientIpUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 接口幂等切面
 */
@Slf4j
@Aspect
@Component
@Order(11)
@RequiredArgsConstructor
public class IdempotentAspect {

    private static final String KEY_PREFIX = "aiperm:idempotent:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String key = buildIdempotentKey(joinPoint, idempotent);

        Boolean acquired = tryAcquire(key, idempotent.expireSeconds());
        if (Boolean.FALSE.equals(acquired)) {
            throw new BusinessException(ErrorCode.IDEMPOTENT_CONFLICT, idempotent.message());
        }

        boolean success = false;
        try {
            Object result = joinPoint.proceed();
            success = true;
            return result;
        } finally {
            // 异常时释放锁，允许用户修正参数后重试
            if (!success) {
                safeRelease(key);
            }
        }
    }

    private Boolean tryAcquire(String key, long expireSeconds) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(expireSeconds));
        } catch (Exception e) {
            // Redis 出现短暂异常时降级为放行，避免影响主链路可用性
            log.warn("幂等降级放行，key={}", key, e);
            return null;
        }
    }

    private void safeRelease(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("释放幂等锁失败，key={}", key, e);
        }
    }

    private String buildIdempotentKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        HttpServletRequest request = currentRequest();
        String resource = hasText(idempotent.key())
                ? idempotent.key()
                : resolveResource(joinPoint, request);
        String subject = resolveSubject(idempotent.scope(), request);
        String token = resolveToken(joinPoint, request, idempotent);
        return KEY_PREFIX + normalize(resource) + ":" + normalize(subject) + ":" + DigestUtil.sha256Hex(token);
    }

    private String resolveToken(ProceedingJoinPoint joinPoint, HttpServletRequest request, Idempotent idempotent) {
        String headerValue = request == null ? null : request.getHeader(idempotent.header());
        if (hasText(headerValue)) {
            return headerValue;
        }

        if (idempotent.requireHeader()) {
            throw new BusinessException(ErrorCode.PARAM_MISSING, "缺少请求头: " + idempotent.header());
        }

        return buildFallbackToken(joinPoint, request);
    }

    private String buildFallbackToken(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        if (request != null) {
            sb.append(request.getMethod()).append(':').append(request.getRequestURI());
            if (hasText(request.getQueryString())) {
                sb.append('?').append(request.getQueryString());
            }
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        sb.append('#').append(signature.getDeclaringTypeName()).append('.').append(signature.getMethod().getName());
        sb.append(':').append(serializeArgs(joinPoint.getArgs()));
        return sb.toString();
    }

    private String serializeArgs(Object[] args) {
        List<Object> serializableArgs = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                serializableArgs.add(null);
                continue;
            }
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse || arg instanceof BindingResult) {
                continue;
            }
            if (arg instanceof MultipartFile || arg instanceof MultipartFile[]) {
                continue;
            }
            serializableArgs.add(arg);
        }

        try {
            return objectMapper.writeValueAsString(serializableArgs);
        } catch (JsonProcessingException e) {
            return String.valueOf(serializableArgs.hashCode());
        }
    }

    private String resolveResource(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        if (request != null) {
            return request.getMethod() + ":" + request.getRequestURI();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringTypeName() + "#" + signature.getName();
    }

    private String resolveSubject(AccessLimitScope scope, HttpServletRequest request) {
        if (scope == AccessLimitScope.GLOBAL) {
            return "global";
        }

        if (scope == AccessLimitScope.USER && StpUtil.isLogin()) {
            return "user:" + StpUtil.getLoginIdAsString();
        }

        return "ip:" + ClientIpUtils.getClientIp(request);
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String normalize(String value) {
        if (!hasText(value)) {
            return "unknown";
        }
        return value.replaceAll("\\s+", "_").replace(":", "_");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
