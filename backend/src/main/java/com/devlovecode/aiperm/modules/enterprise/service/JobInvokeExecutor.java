package com.devlovecode.aiperm.modules.enterprise.service;

import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.enterprise.entity.SysJob;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class JobInvokeExecutor {

    private final ApplicationContext applicationContext;
    private final ConcurrentMap<Class<?>, Optional<Method>> executableMethodCache = new ConcurrentHashMap<>();

    public void execute(SysJob job) {
        Object target = resolveTarget(job.getBeanClass());
        if (target instanceof Runnable runnable) {
            runnable.run();
            return;
        }

        Method method = resolveExecutableMethod(target.getClass())
                .orElseThrow(() -> new BusinessException("任务执行目标缺少无参 execute()/run() 方法: " + job.getBeanClass()));
        ReflectionUtils.invokeMethod(method, target);
    }

    private Object resolveTarget(String beanClass) {
        if (beanClass == null || beanClass.isBlank()) {
            throw new BusinessException("任务执行目标不能为空");
        }

        String target = beanClass.trim();
        if (!target.contains(".")) {
            return applicationContext.getBean(target);
        }

        try {
            Class<?> clazz = Class.forName(target);
            return applicationContext.getBean(clazz);
        } catch (ClassNotFoundException e) {
            // 兼容按 beanName 传参但名字里带点号的场景
            return applicationContext.getBean(target);
        }
    }

    private Optional<Method> resolveExecutableMethod(Class<?> targetClass) {
        return executableMethodCache.computeIfAbsent(targetClass, this::findExecutableMethod);
    }

    private Optional<Method> findExecutableMethod(Class<?> targetClass) {
        Method method = ReflectionUtils.findMethod(targetClass, "execute");
        if (method == null) {
            method = ReflectionUtils.findMethod(targetClass, "run");
        }
        if (method != null) {
            ReflectionUtils.makeAccessible(method);
        }
        return Optional.ofNullable(method);
    }
}
