package com.devlovecode.aiperm.modules.monitor.interceptor;

import com.devlovecode.aiperm.modules.monitor.service.OnlineUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class OnlineUserActivityInterceptor implements HandlerInterceptor {

	private final OnlineUserService onlineUserService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		onlineUserService.touchCurrentSession();
		return true;
	}

}
