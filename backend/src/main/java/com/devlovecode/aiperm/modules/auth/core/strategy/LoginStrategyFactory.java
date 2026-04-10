package com.devlovecode.aiperm.modules.auth.core.strategy;

import com.devlovecode.aiperm.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LoginStrategyFactory {

	private final List<LoginStrategy> strategies;

	private Map<String, LoginStrategy> strategyMap;

	@PostConstruct
	public void init() {
		strategyMap = strategies.stream().collect(Collectors.toMap(LoginStrategy::getLoginType, Function.identity()));
	}

	public LoginStrategy getStrategy(String loginType) {
		LoginStrategy strategy = strategyMap.get(loginType.toUpperCase());
		if (strategy == null) {
			throw new BusinessException("不支持的登录方式：" + loginType);
		}
		return strategy;
	}

}
