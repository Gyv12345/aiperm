package com.devlovecode.aiperm.common.interceptor;

import com.devlovecode.aiperm.common.context.DataScopeHolder;
import com.devlovecode.aiperm.common.service.DataScopeService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 数据权限拦截器
 * 在请求开始时计算并存储数据权限 SQL
 *
 * @author DevLoveCode
 */
@Component
@RequiredArgsConstructor
public class DataScopeInterceptor implements HandlerInterceptor {

    private final DataScopeService dataScopeService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 跳过异步分发请求（SSE 等场景），此时 Sa-Token 上下文已不存在
        // 数据权限 SQL 已在原始请求中计算
        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            return true;
        }

        // 计算并存储数据权限 SQL（使用默认别名 d 和 u）
        String sql = dataScopeService.buildDataScopeSql("d", "u");
        DataScopeHolder.set(sql);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        DataScopeHolder.clear();
    }
}
