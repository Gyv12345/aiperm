package com.devlovecode.aiperm.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 客户端 IP 解析工具
 */
public final class ClientIpUtils {

    private static final String UNKNOWN_IP = "unknown";
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };

    private ClientIpUtils() {
    }

    public static String getCurrentRequestIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return getClientIp(servletRequestAttributes.getRequest());
        }
        return UNKNOWN_IP;
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN_IP;
        }

        for (String header : IP_HEADERS) {
            String ip = normalizeIp(request.getHeader(header));
            if (isValidIp(ip)) {
                return ip;
            }
        }

        String remoteAddr = normalizeIp(request.getRemoteAddr());
        return isValidIp(remoteAddr) ? remoteAddr : UNKNOWN_IP;
    }

    private static String normalizeIp(String ip) {
        if (ip == null) {
            return null;
        }
        String normalized = ip;
        if (normalized.contains(",")) {
            normalized = normalized.split(",")[0];
        }
        return normalized.trim();
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
