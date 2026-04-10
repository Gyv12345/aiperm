package com.devlovecode.aiperm.common.util;

import java.util.List;
import java.util.regex.Pattern;

/**
 * User-Agent 解析工具
 */
public final class UserAgentUtils {

	private static final String UNKNOWN_VALUE = "未知";

	private static final List<UserAgentRule> BROWSER_RULES = List.of(
			new UserAgentRule(Pattern.compile("edg(?:e|ios|a)?/", Pattern.CASE_INSENSITIVE), "Edge"),
			new UserAgentRule(Pattern.compile("(opr/|opera)", Pattern.CASE_INSENSITIVE), "Opera"),
			new UserAgentRule(Pattern.compile("chrome/", Pattern.CASE_INSENSITIVE), "Chrome"),
			new UserAgentRule(Pattern.compile("firefox/", Pattern.CASE_INSENSITIVE), "Firefox"),
			new UserAgentRule(Pattern.compile("version/.*safari/", Pattern.CASE_INSENSITIVE), "Safari"),
			new UserAgentRule(Pattern.compile("(msie |trident/)", Pattern.CASE_INSENSITIVE), "Internet Explorer"));

	private static final List<UserAgentRule> OS_RULES = List.of(
			new UserAgentRule(Pattern.compile("(iphone|ipad|ipod|cpu iphone os)", Pattern.CASE_INSENSITIVE), "iOS"),
			new UserAgentRule(Pattern.compile("android", Pattern.CASE_INSENSITIVE), "Android"),
			new UserAgentRule(Pattern.compile("windows", Pattern.CASE_INSENSITIVE), "Windows"),
			new UserAgentRule(Pattern.compile("(mac os x|macintosh)", Pattern.CASE_INSENSITIVE), "macOS"),
			new UserAgentRule(Pattern.compile("linux", Pattern.CASE_INSENSITIVE), "Linux"));

	private UserAgentUtils() {
	}

	public static String resolveBrowser(String userAgent) {
		return matchUserAgent(userAgent, BROWSER_RULES);
	}

	public static String resolveOs(String userAgent) {
		return matchUserAgent(userAgent, OS_RULES);
	}

	private static String matchUserAgent(String userAgent, List<UserAgentRule> rules) {
		if (userAgent == null || userAgent.isBlank()) {
			return UNKNOWN_VALUE;
		}

		return rules.stream()
			.filter(rule -> rule.pattern().matcher(userAgent).find())
			.map(UserAgentRule::name)
			.findFirst()
			.orElse(UNKNOWN_VALUE);
	}

	private record UserAgentRule(Pattern pattern, String name) {
	}

}
