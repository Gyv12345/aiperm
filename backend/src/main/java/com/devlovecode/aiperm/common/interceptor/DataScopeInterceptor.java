package com.devlovecode.aiperm.common.interceptor;

import com.devlovecode.aiperm.common.context.DataScopeContext;
import com.devlovecode.aiperm.common.context.DataScopeHolder;
import com.devlovecode.aiperm.common.service.DataScopeService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 数据权限拦截器：在请求开始时计算数据权限上下文并存入 Holder，供业务查询消费。
 *
 * @author DevLoveCode
 */
@Component
@RequiredArgsConstructor
public class DataScopeInterceptor implements HandlerInterceptor {

	private final DataScopeService dataScopeService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		// 跳过异步分发请求（SSE 等场景），此时 Sa-Token 上下文已不存在；
		// 数据权限上下文已在原始请求中计算
		if (request.getDispatcherType() == DispatcherType.ASYNC) {
			return true;
		}

		DataScopeContext context = dataScopeService.getDataScopeContext();
		DataScopeHolder.set(context);
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		DataScopeHolder.clear();
	}

}

