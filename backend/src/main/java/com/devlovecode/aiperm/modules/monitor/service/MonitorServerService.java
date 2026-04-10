package com.devlovecode.aiperm.modules.monitor.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.IndicatedHealthDescriptor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.devlovecode.aiperm.modules.monitor.vo.HealthComponentVO;
import com.devlovecode.aiperm.modules.monitor.vo.ServerMonitorVO;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class MonitorServerService {

	private final Environment environment;

	private final MeterRegistry meterRegistry;

	private final HealthEndpoint healthEndpoint;

	public ServerMonitorVO getOverview() {
		ServerMonitorVO vo = new ServerMonitorVO();
		vo.setAppName(environment.getProperty("spring.application.name", "aiperm"));
		vo.setActiveProfiles(Arrays.asList(environment.getActiveProfiles()));
		vo.setJavaVersion(System.getProperty("java.version"));
		vo.setOsName(System.getProperty("os.name"));
		vo.setProcessors(Runtime.getRuntime().availableProcessors());
		vo.setSystemCpuUsage(readGauge("system.cpu.usage"));
		vo.setProcessCpuUsage(readGauge("process.cpu.usage"));
		vo.setUptime(ManagementFactory.getRuntimeMXBean().getUptime());

		fillMemoryInfo(vo);
		fillThreadInfo(vo);
		fillDiskInfo(vo);
		fillHealthInfo(vo);
		return vo;
	}

	private void fillMemoryInfo(ServerMonitorVO vo) {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		vo.setHeapUsed(memoryMXBean.getHeapMemoryUsage().getUsed());
		vo.setHeapMax(memoryMXBean.getHeapMemoryUsage().getMax());
		vo.setNonHeapUsed(memoryMXBean.getNonHeapMemoryUsage().getUsed());
		vo.setNonHeapMax(memoryMXBean.getNonHeapMemoryUsage().getMax());
	}

	private void fillThreadInfo(ServerMonitorVO vo) {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		vo.setLiveThreads(threadMXBean.getThreadCount());
		vo.setDaemonThreads(threadMXBean.getDaemonThreadCount());
		vo.setPeakThreads(threadMXBean.getPeakThreadCount());
	}

	private void fillDiskInfo(ServerMonitorVO vo) {
		try {
			FileStore fileStore = Files.getFileStore(Path.of("."));
			vo.setDiskTotal(fileStore.getTotalSpace());
			vo.setDiskUsable(fileStore.getUsableSpace());
		}
		catch (IOException e) {
			vo.setDiskTotal(0L);
			vo.setDiskUsable(0L);
		}
	}

	private void fillHealthInfo(ServerMonitorVO vo) {
		HealthDescriptor descriptor = healthEndpoint.health();
		vo.setStatus(descriptor.getStatus().getCode());
		appendHealthDescriptor("system", descriptor, vo);
	}

	private void appendHealthDescriptor(String name, HealthDescriptor descriptor, ServerMonitorVO vo) {
		if (descriptor instanceof CompositeHealthDescriptor composite) {
			composite.getComponents().forEach((componentName, child) -> appendHealthDescriptor(componentName, child, vo));
			return;
		}

		if (descriptor instanceof IndicatedHealthDescriptor indicated) {
			HealthComponentVO component = new HealthComponentVO();
			component.setName(name);
			component.setStatus(indicated.getStatus().getCode());
			component.setDetails(String.valueOf(indicated.getDetails()));
			vo.getHealthComponents().add(component);
		}
	}

	private double readGauge(String meterName) {
		Gauge gauge = meterRegistry.find(meterName).gauge();
		return gauge == null ? 0D : gauge.value();
	}

}
