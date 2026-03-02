package com.devlovecode.aiperm.modules.agent.config;

import cn.dev33.satoken.context.SaTokenContextForThreadLocalStaff;
import cn.dev33.satoken.context.model.SaTokenContextModelBox;
import com.devlovecode.aiperm.common.context.DataScopeHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Agent 异步执行器配置
 */
@Configuration
public class AgentExecutorConfig {

    @Bean("agentTaskExecutor")
    public Executor agentTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("agent-");
        executor.setTaskDecorator(task -> {
            SaTokenContextModelBox modelBox = SaTokenContextForThreadLocalStaff.getModelBoxOrNull();
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            String dataScopeSql = DataScopeHolder.get();
            boolean dataScopeEnabled = DataScopeHolder.isEnabled();

            return () -> {
                try {
                    if (modelBox != null) {
                        SaTokenContextForThreadLocalStaff.setModelBox(
                                modelBox.getRequest(),
                                modelBox.getResponse(),
                                modelBox.getStorage()
                        );
                    }
                    if (requestAttributes != null) {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                    }
                    if (dataScopeEnabled) {
                        DataScopeHolder.enable();
                        DataScopeHolder.set(dataScopeSql);
                    } else {
                        DataScopeHolder.disable();
                    }
                    task.run();
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                    SaTokenContextForThreadLocalStaff.clearModelBox();
                    DataScopeHolder.clear();
                }
            };
        });
        executor.setRejectedExecutionHandler((r, e) -> {
            throw new RuntimeException("Agent 任务队列已满，请稍后重试");
        });
        executor.initialize();
        return executor;
    }
}
