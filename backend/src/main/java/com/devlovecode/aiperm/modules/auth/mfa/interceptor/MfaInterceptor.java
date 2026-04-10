package com.devlovecode.aiperm.modules.auth.mfa.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.modules.auth.mfa.entity.SysMfaPolicy;
import com.devlovecode.aiperm.modules.auth.mfa.repository.MfaPolicyRepository;
import com.devlovecode.aiperm.modules.auth.mfa.service.MfaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2FA 拦截器 检查敏感操作是否需要2FA验证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MfaInterceptor implements HandlerInterceptor {

	private final MfaPolicyRepository mfaPolicyRepo;

	private final MfaService mfaService;

	private final ObjectMapper objectMapper;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 未登录直接放行（Sa-Token 拦截器会处理）
		if (!StpUtil.isLogin()) {
			return true;
		}

		Long userId = StpUtil.getLoginIdAsLong();
		String requestUri = request.getRequestURI();

		// 检查当前 API 是否在2FA策略中
		List<SysMfaPolicy> policies = mfaPolicyRepo.findByEnabled(1);
		boolean requiresMfa = policies.stream().anyMatch(p -> matchPattern(requestUri, p.getApiPattern()));

		if (!requiresMfa) {
			return true; // 不需要2FA，放行
		}

		// 检查用户是否在有效期内已验证2FA
		if (mfaService.isVerified(userId)) {
			return true; // 已验证，放行
		}

		// 需要2FA验证，返回 423 状态码
		response.setStatus(423);
		response.setContentType("application/json;charset=UTF-8");
		Map<String, Object> result = new HashMap<>();
		result.put("code", 423);
		result.put("message", "需要二次验证，请完成2FA验证后重试");
		result.put("data", null);
		response.getWriter().write(objectMapper.writeValueAsString(result));
		return false;
	}

	/**
	 * 简单通配符匹配（支持 * 作为多字符通配符） 例如：/system/user/* 匹配 /system/user/1 和 /system/user/delete
	 */
	private boolean matchPattern(String uri, String pattern) {
		if (pattern == null || pattern.isBlank())
			return false;
		// 将通配符 * 转换为正则
		String regex = pattern.replace("/", "\\/").replace("*", ".*");
		return uri.matches(regex);
	}

}
