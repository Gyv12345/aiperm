package com.devlovecode.aiperm.modules.mcp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * MCP 安全拦截器
 * 对 MCP 端点进行 IP 白名单验证
 *
 * @author DevLoveCode
 */
@Slf4j
@Component
public class McpSecurityInterceptor implements HandlerInterceptor {

    /**
     * 允许访问 MCP 的 IP 白名单
     */
    private static final Set<String> ALLOWED_IPS = Set.of(
            "127.0.0.1",
            "0:0:0:0:0:0:0:1",
            "::1"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // MCP SSE 端点需要验证 IP 白名单
        if (path.startsWith("/mcp/")) {
            String clientIp = getClientIp(request);

            if (!ALLOWED_IPS.contains(clientIp)) {
                log.warn("MCP 请求 IP 不在白名单中: {}, path: {}", clientIp, path);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"IP 不在白名单中\"}");
                return false;
            }

            log.debug("MCP 请求 IP 验证通过: {}, path: {}", clientIp, path);
        }

        return true;
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个 IP（经过代理），取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
