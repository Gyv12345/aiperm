package com.devlovecode.aiperm.modules.audit.service;

import com.devlovecode.aiperm.modules.audit.entity.SysLoginLog;
import com.devlovecode.aiperm.modules.audit.repository.LoginLogRepository;
import com.devlovecode.aiperm.common.util.ExcelExportHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("登录日志服务测试")
class LoginLogServiceTest {

	@Mock
	private LoginLogRepository loginLogRepository;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private ExcelExportHelper excelExportHelper;

	@Test
	@DisplayName("记录成功登录时应解析本地 IP、浏览器和操作系统")
	void shouldPopulateLocalIpBrowserAndOs() {
		LoginLogService loginLogService = new LoginLogService(loginLogRepository, restTemplate, excelExportHelper);
		MockHttpServletRequest request = new MockHttpServletRequest();
		String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
				+ "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

		loginLogService.recordSuccess(1L, "admin", "127.0.0.1", userAgent, request);

		ArgumentCaptor<SysLoginLog> captor = ArgumentCaptor.forClass(SysLoginLog.class);
		verify(loginLogRepository, timeout(1000)).save(captor.capture());

		SysLoginLog record = captor.getValue();
		assertEquals("内网IP", record.getLocation());
		assertEquals("Chrome", record.getBrowser());
		assertEquals("Windows", record.getOs());
	}

	@Test
	@DisplayName("相同公网 IP 应命中缓存，避免重复请求定位接口")
	void shouldCacheGeoLocationForSamePublicIp() {
		LoginLogService loginLogService = new LoginLogService(loginLogRepository, restTemplate, excelExportHelper);
		MockHttpServletRequest request = new MockHttpServletRequest();
		String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 "
				+ "(KHTML, like Gecko) Version/17.3 Safari/605.1.15";

		when(restTemplate.getForObject(anyString(), eq(Map.class)))
			.thenReturn(Map.of("status", "success", "country", "美国", "regionName", "加利福尼亚"));

		loginLogService.recordSuccess(1L, "admin", "8.8.8.8", userAgent, request);
		verify(loginLogRepository, timeout(1000)).save(any(SysLoginLog.class));

		loginLogService.recordSuccess(2L, "operator", "8.8.8.8", userAgent, request);

		verify(loginLogRepository, timeout(1000).times(2)).save(any(SysLoginLog.class));
		verify(restTemplate, times(1)).getForObject(anyString(), eq(Map.class));
	}

}
