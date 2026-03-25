package com.devlovecode.aiperm.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.annotation.RateLimit;
import com.devlovecode.aiperm.common.enums.AccessLimitScope;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.util.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 接口限流切面
 */
@Slf4j
@Aspect
@Component
@Order(10)
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final String KEY_PREFIX = "aiperm:rate-limit:";

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildRateLimitKey(joinPoint, rateLimit);

        Long current = incrementCounter(key, rateLimit.windowSeconds());
        if (current != null && current > rateLimit.count()) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, rateLimit.message());
        }

        return joinPoint.proceed();
    }

    private Long incrementCounter(String key, int windowSeconds) {
        try {
            Long current = redisTemplate.opsForValue().increment(key);
            if (current != null && current == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            return current;
        } catch (Exception e) {
            // Redis 出现短暂异常时降级为放行，避免影响主链路可用性
            log.warn("限流降级放行，key={}", key, e);
            return null;
        }
    }

    private String buildRateLimitKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        HttpServletRequest request = currentRequest();
        String resource = hasText(rateLimit.key())
                ? rateLimit.key()
                : resolveResource(joinPoint, request);
        String subject = resolveSubject(rateLimit.scope(), request);
        return KEY_PREFIX + normalize(resource) + ":" + normalize(subject);
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
